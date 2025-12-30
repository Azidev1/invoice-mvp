# Invoice MVP

Stack: Angular (later) / Spring Boot / FastAPI worker / Postgres / RabbitMQ / MinIO

## Run (Podman)
podman compose -f podman-compose.yml up -d --build

## UIs
- MinIO: http://localhost:9001 (minio / minio123456)
- RabbitMQ: http://localhost:15672 (guest / guest)
- API: http://localhost:8080
