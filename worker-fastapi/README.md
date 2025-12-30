# FastAPI OCR Worker (MVP)

This worker consumes RabbitMQ messages (DOCUMENT_UPLOADED), downloads the document from MinIO (S3),
runs a placeholder OCR/extraction, then calls Spring Boot internal endpoints.

## Env vars (used in compose)
- RABBIT_HOST, RABBIT_PORT, RABBIT_QUEUE
- STORAGE_S3_ENDPOINT, STORAGE_S3_ACCESS_KEY, STORAGE_S3_SECRET_KEY, STORAGE_S3_BUCKET, STORAGE_S3_REGION
- SPRING_INTERNAL_BASE_URL
- INTERNAL_JWT_ISSUER, INTERNAL_JWT_SECRET
