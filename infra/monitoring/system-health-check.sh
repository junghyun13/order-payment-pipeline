#!/bin/bash
echo "===== [System Health Check] ====="

echo "[1] CPU"
top -bn1 | grep "Cpu(s)"

echo "[2] Memory"
free -h

echo "[3] Disk"
df -h

echo "[4] Docker Containers"
docker ps --format "table {{.Names}}\t{{.Status}}\t{{.Ports}}"

echo "[5] Elasticsearch"
curl -s http://es-nori:9200/_cluster/health?pretty || echo "ES unreachable"

echo "[6] Redis"
docker exec redis redis-cli ping || echo "Redis unreachable"

echo "[7] Redpanda"
curl -s http://redpanda:9644/v1/status/ready || echo "RP unreachable"
