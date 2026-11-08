#!/usr/bin/env bash
set -e

TIMESTAMP=$(date +"%Y%m%d_%H%M%S")
BACKUP_DIR="$(dirname "$0")/../backups"
mkdir -p "$BACKUP_DIR"

echo "========================================================"
echo "Backing Up All 10 Retail Microservice Databases"
echo "========================================================"

BACKUP_FILE="$BACKUP_DIR/retail_backup_$TIMESTAMP.sql"

docker exec -t retail-mysql mysqldump -u root -proot --databases \
  customer_db product_db cart_db order_db payment_db \
  inventory_db review_db campaign_db support_db wishlist_db > "$BACKUP_FILE"

echo "[SUCCESS] Backup completed: $BACKUP_FILE"

