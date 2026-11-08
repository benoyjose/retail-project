@echo off
echo ========================================================
echo Restoring Databases from Backup
echo ========================================================
if "%~1"=="" (
    echo Usage: restore-databases.bat ^<path_to_backup_file.sql^>
    echo Example: restore-databases.bat ..\backups\retail_backup_latest.sql
    pause
    exit /b 1
)

echo Restoring databases from %1...
docker exec -i retail-mysql mysql -u root -proot < "%~1"
if %ERRORLEVEL% EQU 0 (
    echo [SUCCESS] Databases successfully restored!
) else (
    echo [ERROR] Database restore failed.
)
pause

