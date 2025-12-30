FROM docker.io/python:3.11-slim

WORKDIR /app

# (optionnel) dépendances système utiles (ex: pour OCR plus tard)
RUN apt-get update && apt-get install -y --no-install-recommends \
    curl \
  && rm -rf /var/lib/apt/lists/*

# Dépendances Python
COPY requirements.txt /app/requirements.txt
RUN pip install --no-cache-dir -r /app/requirements.txt

# Code
COPY . /app

# Worker (pika consumer)
CMD ["python", "-u", "worker.py"]
