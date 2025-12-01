#!/bin/bash
set -e

echo "🚀 Deploy 시작 (WSL2 Docker Engine 환경)"

# ---------------------------
# 0️⃣ 환경 준비
# ---------------------------
export DOCKER_BUILDKIT=1
COMPOSE_FILE="./docker-compose.yml"
COMPOSE_CMD=$(command -v docker-compose >/dev/null 2>&1 && echo "docker-compose" || echo "docker compose")

# ---------------------------
# 0.5️⃣ Docker 데몬 확인
# ---------------------------
# Docker 데몬 확인 및 실행
if ! docker info >/dev/null 2>&1; then
    echo "⚠️ Docker 데몬 미실행. 시작 시도..."
    # 이미 dockerd 실행 중이면 재실행 안함
    if ! pgrep -x dockerd >/dev/null; then
        nohup ~/bin/start-docker.sh >/tmp/dockerd.log 2>&1 &
    fi

    echo "⏳ Docker 데몬 준비 중..."
    while ! docker info >/dev/null 2>&1; do
        sleep 1
    done
fi
echo "✅ Docker 데몬 준비 완료"


# Docker 연결 테스트
docker run --rm hello-world >/dev/null 2>&1 || { echo "[ERROR] Docker 연결 실패"; exit 1; }
echo "✅ Docker 연결 정상"

# docker-compose 파일 확인
if [ ! -f "$COMPOSE_FILE" ]; then
    echo "[ERROR] docker-compose.yml 파일 없음"
    exit 1
fi

# ---------------------------
# 1️⃣ Docker Compose 실행
# ---------------------------
echo "1️⃣ Docker Compose 서비스 시작"
$COMPOSE_CMD -f "$COMPOSE_FILE" up -d --build
$COMPOSE_CMD -f "$COMPOSE_FILE" ps

# ---------------------------
# 2️⃣ TailScale Funnel (선택)
# ---------------------------
TAILSCALE_URL=""
if command -v tailscale >/dev/null 2>&1; then
    echo "🌐 TailScale Funnel 준비"

    if ! pgrep -x tailscaled >/dev/null; then
        echo "🟢 tailscaled 백그라운드 실행"
        sudo tailscaled --state=/var/lib/tailscale/tailscaled.state &
        sleep 3
    fi

    if ! tailscale status >/dev/null 2>&1; then
        echo "🔑 TailScale 로그인 필요. 인증 URL 확인 후 완료하세요."
        sudo tailscale up
    fi

    sudo tailscale funnel stop 8080 >/dev/null 2>&1 || true
    sudo tailscale funnel 8080 >/tmp/funnel.log 2>&1 &
    echo "✨ Funnel 실행 중"

    TAILSCALE_IP=$(tailscale ip -4)
    if [ -n "$TAILSCALE_IP" ]; then
        TAILSCALE_URL="https://$TAILSCALE_IP:8080"
    fi

    tailscale funnel status || echo "[INFO] Funnel 준비 중..."
else
    echo "[INFO] TailScale 미설치 → Funnel 생략"
fi

# ---------------------------
# 3️⃣ 서비스 모니터링
# ---------------------------
echo "📊 서비스 모니터링 시작"
$COMPOSE_CMD -f "$COMPOSE_FILE" ps
echo "💡 실시간 로그 확인 가능: $COMPOSE_CMD logs -f"

# ---------------------------
# 4️⃣ 완료 메시지
# ---------------------------
echo "✅ 서비스 실행 완료!"
if [ -n "$TAILSCALE_URL" ]; then
    echo "🌐 외부 접속 가능한 TailScale URL: $TAILSCALE_URL"
else
    echo "🌐 TailScale URL 미사용 또는 미설치"
fi
