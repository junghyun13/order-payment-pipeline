#!/bin/bash
#run_portfolio.sh 코드
set -e

echo "🚀 Deploy 시작 (Docker Desktop 사용)"

# Docker Desktop 데몬 자동 사용
echo "✅ Docker Desktop Docker daemon 자동 실행 중"

# BuildKit 활성화
export DOCKER_BUILDKIT=1
echo "✅ BuildKit 활성화 완료"

# Docker 연결 테스트
docker run --rm hello-world >/dev/null 2>&1 || {
    echo "[ERROR] Docker Desktop 연결 실패"
    exit 1
}
echo "✅ Docker 연결 정상"

# docker-compose 파일 존재 확인
COMPOSE_FILE="./docker-compose.yml"
if [ ! -f "$COMPOSE_FILE" ]; then
    echo "[ERROR] docker-compose.yml 파일 없음"
    exit 1
fi

# compose 명령 선택
if command -v docker-compose >/dev/null 2>&1; then
    COMPOSE_CMD="docker-compose"
else
    COMPOSE_CMD="docker compose"
fi

echo "1️⃣ Docker Compose 서비스 시작"
$COMPOSE_CMD -f "$COMPOSE_FILE" up -d --build
$COMPOSE_CMD -f "$COMPOSE_FILE" ps

# ---------------------------
# 🔥 TAILSCALE FUNNEL 추가됨
# ---------------------------
if command -v tailscale >/dev/null 2>&1; then
    echo "🌐 Tailscale Funnel 실행 (포트 8080 공개)"

    # Tailscale 상태 확인
    if ! tailscale status >/dev/null 2>&1; then
        echo "🔑 Tailscale 로그인 필요. 로그인 창을 확인하세요."
        sudo tailscale up
    fi

    # 기존 Funnel 중지 (중복 방지)
    sudo tailscale funnel stop 8080 >/dev/null 2>&1 || true

    # Funnel 실행
    sudo tailscale funnel 8080 > /tmp/funnel.log 2>&1 &
    echo "✨ Funnel 실행됨! 공개 URL:"
    tailscale funnel status
else
    echo "[INFO] Tailscale 미설치 → Funnel 생략"
fi

# ---------------------------

echo "2️⃣ GitHub Actions Runner 유지 실행"
RUNNER_DIR="$HOME/actions-runner"
if [ -d "$RUNNER_DIR" ]; then
    cd "$RUNNER_DIR"
    nohup ./run.sh > runner.log 2>&1 &
    echo "✅ Actions Runner 실행됨"
fi

echo "✅ 서비스 실행 완료!"
echo "외부에서 접속 가능한 URL:"
echo "https://laptop-8vevpj3e.tail433c38.ts.net/"

echo "💡 Docker 로그 확인: docker-compose logs -f"
