#!/bin/bash
set -e

echo "🚀 Docker daemon 확인/실행"

# Docker daemon 확인 및 실행
if ! docker info > /dev/null 2>&1; then
    echo "[INFO] Docker daemon not running. 시작 시도..."
    if sudo systemctl start docker; then
        echo "[INFO] systemd를 통한 Docker 기동 완료"
    else
        echo "[WARN] systemd start 실패, nohup dockerd 백그라운드 기동"
        sudo nohup dockerd > /tmp/dockerd.log 2>&1 &
    fi

    # Docker 완전 기동 대기
    until docker info > /dev/null 2>&1; do
        echo "[INFO] Docker daemon 시작 대기..."
        sleep 2
    done
fi
echo "✅ Docker daemon 실행 중"

# Docker 그룹 권한 확인
if ! groups $USER | grep -q '\bdocker\b'; then
    echo "[WARN] $USER is docker 그룹 미가입. 추가 중..."
    sudo usermod -aG docker $USER
    echo "[INFO] 재로그인 필요: exec su -l $USER"
    exit 1
fi

# BuildKit 활성화
export DOCKER_BUILDKIT=1
export DOCKER_CLI_PLUGIN_DIR=$HOME/.docker/cli-plugins
echo "✅ BuildKit 활성화 완료"

# Docker 연결 테스트
docker run --rm hello-world
echo "✅ Docker 연결 정상"

# Docker Compose 파일 경로 확인
COMPOSE_FILE="./docker-compose.yml"
if [ ! -f "$COMPOSE_FILE" ]; then
    echo "[ERROR] docker-compose.yml 파일이 infra 디렉토리에 없습니다."
    exit 1
fi

# docker-compose v1 / v2 대응
if command -v docker-compose >/dev/null 2>&1; then
    COMPOSE_CMD="docker-compose"
else
    COMPOSE_CMD="docker compose"
fi

echo "1️⃣ Docker Compose 서비스 시작"
$COMPOSE_CMD -f "$COMPOSE_FILE" up -d --build
$COMPOSE_CMD -f "$COMPOSE_FILE" ps

# 선택: Tailscale Funnel 실행
if command -v tailscale >/dev/null 2>&1; then
    echo "🌐 Tailscale Funnel 실행 (포트 8080)"
    sudo tailscale funnel 8080 &
else
    echo "[INFO] Tailscale 설치 안 됨. Funnel 실행 생략."
fi

# GitHub Actions Runner 자동 실행
RUNNER_DIR="$HOME/actions-runner"
if [ -d "$RUNNER_DIR" ]; then
    echo "2️⃣ GitHub Actions Runner 실행"
    cd "$RUNNER_DIR"
    nohup ./run.sh > runner.log 2>&1 &
    echo "✅ GitHub Actions Runner 시작 완료: $RUNNER_DIR/runner.log"
fi

echo "✅ 서비스 실행 완료!"
echo "외부에서 접속 가능한 URL:"
echo "https://laptop-8vevpj3e.tail433c38.ts.net/"

echo "💡 Docker 로그 확인: docker-compose logs -f"
