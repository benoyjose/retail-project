#!/usr/bin/env bash
set -e

if [ -z "$1" ]; then
    echo "Usage: ./restore-databases.sh <path_to_backup_file.sql>"
    exit 1
fi

echo "========================================================"
echo "Restoring Databases from: $1"
echo "========================================================"

docker exec -i retail-mysql mysql -u root -proot < "$1"

echo "[SUCCESS] Databases successfully restored!"

