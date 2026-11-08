#!/usr/bin/env bash
set -e

echo "========================================================"
echo "Starting Backing Infrastructure (MySQL, Kafka, Zipkin, ELK, Prometheus, Grafana)"
echo "========================================================"

cd "$(dirname "$0")/../docker"
docker compose -f docker-compose-infra.yml up -d

echo "[SUCCESS] Infrastructure containers launched!"

