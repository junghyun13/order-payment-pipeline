#!/bin/bash
set -e

echo "🚀 1️⃣ Docker daemon 확인/실행"

# Docker daemon 확인
if ! docker info > /dev/null 2>&1; then
    echo "[INFO] Docker daemon not running. Starting dockerd..."
    sudo rm -f /var/run/docker.pid
    sudo nohup dockerd > /tmp/dockerd.log 2>&1 &
    sleep 5
fi

echo "✅ Docker daemon 실행 중"

echo "1️⃣ Docker Compose 서비스 시작"
docker-compose up -d --build

echo "2️⃣ Tailscale Funnel 실행"
# 이미 Tailscale 로그인 & up 되어 있어야 함
sudo tailscale funnel 8080 &

echo "✅ 서비스 실행 완료!"
echo "외부에서 접속 가능한 URL:"
echo "https://laptop-8vevpj3e.tail433c38.ts.net/"

echo "💡 Docker 로그 확인: docker-compose logs -f"
