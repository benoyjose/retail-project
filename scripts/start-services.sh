#!/usr/bin/env bash
set -e

echo "========================================================"
echo "Starting Retail Microservices (Config, Eureka, Gateway, and 10 Domain Services)"
echo "========================================================"

cd "$(dirname "$0")/../docker"
docker compose -f docker-compose-services.yml up -d

echo "[SUCCESS] All 13 microservices launched!"
echo "Access Web Storefront: http://localhost:8080/"
echo "Access Eureka Registry: http://localhost:8761/"

