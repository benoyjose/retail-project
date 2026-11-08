@echo off
echo ========================================================
echo Backing Up All 10 Retail Microservice Databases
echo ========================================================
set TIMESTAMP=%date:~10,4%%date:~4,2%%date:~7,2%_%time:~0,2%%time:~3,2%%time:~6,2%
set TIMESTAMP=%TIMESTAMP: =0%
set BACKUP_DIR=..\backups
if not exist %BACKUP_DIR% mkdir %BACKUP_DIR%

echo Backing up MySQL database-per-service schemas to %BACKUP_DIR%\retail_backup_%TIMESTAMP%.sql...

docker exec -t retail-mysql mysqldump -u root -proot --databases customer_db product_db cart_db order_db payment_db inventory_db review_db campaign_db support_db wishlist_db > %BACKUP_DIR%\retail_backup_%TIMESTAMP%.sql

if %ERRORLEVEL% EQU 0 (
    echo [SUCCESS] Backup completed: %BACKUP_DIR%\retail_backup_%TIMESTAMP%.sql
) else (
    echo [ERROR] Backup failed.
)
pause

