#!/bin/bash
echo "🚨 Detected Service Failure - Attempting Auto Recovery"

cd "$(dirname "$0")/../"
docker compose down
docker compose up -d --force-recreate

echo "🔄 Restart Completed"
