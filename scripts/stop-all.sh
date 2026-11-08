#!/usr/bin/env bash
set -e

echo "========================================================"
echo "Stopping All Containers"
echo "========================================================"

cd "$(dirname "$0")/../docker"
docker compose -f docker-compose-services.yml down
docker compose -f docker-compose-infra.yml down

echo "[SUCCESS] All containers gracefully stopped."

