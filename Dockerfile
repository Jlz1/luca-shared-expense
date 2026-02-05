FROM python:3.9-slim

# Install minimal system dependencies
RUN apt-get update && apt-get install -y \
    libgomp1 \
    && rm -rf /var/lib/apt/lists/*

WORKDIR /app

# Copy requirements and install
COPY requirements.txt .
RUN pip install --no-cache-dir -r requirements.txt

# Copy application
COPY . .

# Expose port 7860 (Hugging Face standard)
EXPOSE 7860

# Run with gunicorn
CMD exec gunicorn --bind :7860 --workers 1 --threads 8 --timeout 0 main:app