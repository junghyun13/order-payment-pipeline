#!/bin/bash
# run_portfolio.sh
set -e

echo "🚀 Deploy 시작 (Docker Desktop 사용)"

# BuildKit 활성화
export DOCKER_BUILDKIT=1

# Docker 연결 테스트
docker run --rm hello-world >/dev/null 2>&1 || { echo "[ERROR] Docker 연결 실패"; exit 1; }
echo "✅ Docker 연결 정상"

# docker-compose 파일 존재 확인
COMPOSE_FILE="./docker-compose.yml"
if [ ! -f "$COMPOSE_FILE" ]; then
    echo "[ERROR] docker-compose.yml 파일 없음"
    exit 1
fi

# compose 명령 선택
COMPOSE_CMD=$(command -v docker-compose >/dev/null 2>&1 && echo "docker-compose" || echo "docker compose")

echo "1️⃣ Docker Compose 서비스 시작"
$COMPOSE_CMD -f "$COMPOSE_FILE" up -d --build
$COMPOSE_CMD -f "$COMPOSE_FILE" ps

# ---------------------------
# 🔥 WSL2 환경용 TailScale Funnel
# ---------------------------
if command -v tailscale >/dev/null 2>&1; then
    echo "🌐 TailScale Funnel 준비"

    # tailscaled 데몬 백그라운드 실행 (systemd 없이)
    if ! pgrep -x tailscaled >/dev/null; then
        echo "🟢 tailscaled 백그라운드 실행"
        sudo tailscaled --state=/var/lib/tailscale/tailscaled.state &

        # 데몬 안정화 시간
        sleep 3
    fi

    # tailscale 로그인 체크
    if ! tailscale status >/dev/null 2>&1; then
        echo "🔑 TailScale 로그인 필요. 인증 URL 확인 후 완료하세요."
        sudo tailscale up
    fi

    # Funnel 실행
    sudo tailscale funnel stop 8080 >/dev/null 2>&1 || true
    sudo tailscale funnel 8080 >/tmp/funnel.log 2>&1 &
    echo "✨ Funnel 실행 중"
    tailscale funnel status || echo "[INFO] Funnel 준비 중..."
else
    echo "[INFO] TailScale 미설치 → Funnel 생략"
fi

echo "✅ 서비스 실행 완료!"
echo "외부에서 접속 가능한 URL:"
echo "https://laptop-8vevpj3e.tail433c38.ts.net/"

echo "💡 Docker 로그 확인: docker-compose logs -f"
