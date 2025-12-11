#!/bin/bash
echo "====== Docker Container Health Check ======"

containers=$(docker ps --format "{{.Names}}")

for container in $containers; do
    status=$(docker inspect --format='{{.State.Health.Status}}' $container 2>/dev/null)

    if [[ "$status" == "healthy" ]]; then
        echo "🟢 $container : HEALTHY"
    elif [[ "$status" == "unhealthy" ]]; then
        echo "🔴 $container : UNHEALTHY — 재시작"
        docker restart $container
    else
        echo "🟡 $container : No Healthcheck"
    fi
done
