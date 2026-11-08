@echo off
echo ========================================================
echo Stopping All Containers
echo ========================================================
cd ..\docker
docker compose -f docker-compose-services.yml down
docker compose -f docker-compose-infra.yml down
echo [SUCCESS] All containers gracefully stopped.
pause

