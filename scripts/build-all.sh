#!/usr/bin/env bash
set -e

echo "========================================================"
echo "Building Retail Microservices System (18 Modules)"
echo "========================================================"

cd "$(dirname "$0")/.."
mvn clean install

echo "[SUCCESS] All 18 modules compiled, tested, and packaged successfully!"

