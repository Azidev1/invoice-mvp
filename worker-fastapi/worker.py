import json
import os
import time
from datetime import datetime, timedelta, timezone

import boto3
import jwt
import pika
import requests


# -------------------------
# Env
# -------------------------
RABBIT_HOST = os.getenv("RABBIT_HOST", "localhost")
RABBIT_PORT = int(os.getenv("RABBIT_PORT", "5672"))
RABBIT_QUEUE = os.getenv("RABBIT_QUEUE", "documents.ocr.queue")

S3_ENDPOINT = os.getenv("STORAGE_S3_ENDPOINT", "http://localhost:9000")
S3_ACCESS = os.getenv("STORAGE_S3_ACCESS_KEY", "minio")
S3_SECRET = os.getenv("STORAGE_S3_SECRET_KEY", "minio123456")
S3_BUCKET_DEFAULT = os.getenv("STORAGE_S3_BUCKET", "invoices")
S3_REGION = os.getenv("STORAGE_S3_REGION", "us-east-1")

SPRING_BASE = os.getenv("SPRING_INTERNAL_BASE_URL", "http://localhost:8080")

INTERNAL_JWT_ISSUER = os.getenv("INTERNAL_JWT_ISSUER", "invoices-app")
INTERNAL_JWT_SECRET = os.getenv("INTERNAL_JWT_SECRET", "")

WORKER_SUBJECT = os.getenv("WORKER_SUBJECT", "fastapi-worker")
HTTP_TIMEOUT = float(os.getenv("HTTP_TIMEOUT", "15"))


# -------------------------
# Helpers: JWT for internal calls
# -------------------------
def make_internal_jwt() -> str:
    if not INTERNAL_JWT_SECRET or len(INTERNAL_JWT_SECRET) < 32:
        raise RuntimeError("INTERNAL_JWT_SECRET must be set (>=32 chars).")

    now = datetime.now(timezone.utc)
    payload = {
        "iss": INTERNAL_JWT_ISSUER,
        "sub": WORKER_SUBJECT,
        "iat": int(now.timestamp()),
        "exp": int((now + timedelta(minutes=10)).timestamp()),
        "scope": "internal",
    }
    return jwt.encode(payload, INTERNAL_JWT_SECRET, algorithm="HS256")


def spring_headers() -> dict:
    return {
        "Authorization": f"Bearer {make_internal_jwt()}",
        "Content-Type": "application/json",
    }


# -------------------------
# MinIO / S3 client
# -------------------------
s3 = boto3.client(
    "s3",
    endpoint_url=S3_ENDPOINT,
    aws_access_key_id=S3_ACCESS,
    aws_secret_access_key=S3_SECRET,
    region_name=S3_REGION,
)


# -------------------------
# Spring internal callbacks
# -------------------------
def set_status(document_id: str, status: str, error_message: str | None = None) -> None:
    payload = {"status": status, "errorMessage": error_message}
    url = f"{SPRING_BASE}/internal/documents/{document_id}/status"
    r = requests.post(url, headers=spring_headers(), data=json.dumps(payload), timeout=HTTP_TIMEOUT)
    r.raise_for_status()


def post_results(
    document_id: str,
    tenant_id: str,
    ocr_text: str,
    fields: dict,
    confidence: dict,
    pages: int,
    engine: str,
) -> None:
    payload = {
        "tenantId": tenant_id,
        "ocrText": ocr_text,
        "fields": json.dumps(fields),
        "confidence": json.dumps(confidence),
        "engine": engine,
        "pages": pages,
    }
    url = f"{SPRING_BASE}/internal/documents/{document_id}/results"
    r = requests.post(url, headers=spring_headers(), data=json.dumps(payload), timeout=HTTP_TIMEOUT)
    r.raise_for_status()


# -------------------------
# OCR placeholder
# -------------------------
def placeholder_ocr_and_extract(file_bytes: bytes, content_type: str) -> tuple[str, dict, dict, int, str]:
    # MVP: retourne une extraction factice pour valider le pipeline
    ocr_text = "INVOICE SAMPLE OCR TEXT\nTOTAL TTC: 123.45 EUR\nDATE: 2025-12-29\nVENDOR: ACME"
    fields = {
        "vendor": "ACME",
        "invoice_date": "2025-12-29",
        "total_ttc": 123.45,
        "currency": "EUR",
    }
    confidence = {
        "vendor": 0.70,
        "invoice_date": 0.80,
        "total_ttc": 0.60,
        "currency": 0.50,
    }
    pages = 1
    engine = "placeholder"
    return ocr_text, fields, confidence, pages, engine


# -------------------------
# RabbitMQ consumer
# -------------------------
def on_message(ch, method, properties, body: bytes) -> None:
    try:
        msg = json.loads(body.decode("utf-8"))

        event_type = msg.get("eventType")
        if event_type != "DOCUMENT_UPLOADED":
            # Ignore messages we don't understand
            ch.basic_ack(delivery_tag=method.delivery_tag)
            return

        document_id = msg["documentId"]
        tenant_id = msg["tenantId"]
        bucket = msg.get("bucket", S3_BUCKET_DEFAULT)
        object_key = msg["objectKey"]
        content_type = msg.get("contentType", "application/octet-stream")

        # 1) status PROCESSING
        set_status(document_id, "PROCESSING")

        # 2) download from MinIO
        obj = s3.get_object(Bucket=bucket, Key=object_key)
        data = obj["Body"].read()

        # 3) OCR + extraction
        ocr_text, fields, conf, pages, engine = placeholder_ocr_and_extract(data, content_type)

        # 4) callback Spring results -> REVIEW
        post_results(document_id, tenant_id, ocr_text, fields, conf, pages, engine)

        # ACK message (done)
        ch.basic_ack(delivery_tag=method.delivery_tag)

    except Exception as e:
        # Mark FAILED and send to DLQ (we set requeue=False)
        # Note: set_status itself may fail; wrap safely.
        try:
            # if we can decode doc id, try to set FAILED
            try:
                msg2 = json.loads(body.decode("utf-8"))
                doc_id = msg2.get("documentId", "unknown")
            except Exception:
                doc_id = "unknown"

            if doc_id != "unknown":
                set_status(doc_id, "FAILED", str(e))
        except Exception:
            pass

        ch.basic_nack(delivery_tag=method.delivery_tag, requeue=False)


def main() -> None:
    # Wait a bit so RabbitMQ + Spring are up
    time.sleep(5)

    params = pika.ConnectionParameters(
        host=RABBIT_HOST,
        port=RABBIT_PORT,
        heartbeat=60,
        blocked_connection_timeout=30,
    )

    conn = pika.BlockingConnection(params)
    channel = conn.channel()

    # Make sure queue exists (if created by Spring, it's fine; this is harmless)
    channel.queue_declare(queue=RABBIT_QUEUE, durable=True)

    # Process one message at a time
    channel.basic_qos(prefetch_count=1)
    channel.basic_consume(queue=RABBIT_QUEUE, on_message_callback=on_message)

    print(f"[worker] consuming queue={RABBIT_QUEUE} rabbit={RABBIT_HOST}:{RABBIT_PORT}")
    channel.start_consuming()


if __name__ == "__main__":
    main()
