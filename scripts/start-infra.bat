@echo off
echo ========================================================
echo Starting Backing Infrastructure (MySQL, Kafka, Zipkin, ELK, Prometheus, Grafana)
echo ========================================================
cd ..\docker
docker compose -f docker-compose-infra.yml up -d
echo [SUCCESS] Infrastructure containers launched!
pause

