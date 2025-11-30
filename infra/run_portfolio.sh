#!/bin/bash
# run_portfolio.sh
set -e

echo "🚀 1️⃣ Docker daemon 확인/실행"

# Docker daemon 확인 및 실행
if ! docker info > /dev/null 2>&1; then
    echo "[INFO] Docker daemon not running. Starting dockerd..."
    sudo rm -f /var/run/docker.pid /var/run/docker.sock
    sudo nohup dockerd --host=unix:///var/run/docker.sock \
                       --containerd=/run/containerd/containerd.sock \
                       > /tmp/dockerd.log 2>&1 &
    echo "[INFO] Docker daemon 로그: /tmp/dockerd.log"

    # Docker 완전 기동 대기
    until docker info > /dev/null 2>&1; do
        echo "[INFO] Docker daemon starting..."
        sleep 2
    done
fi
echo "✅ Docker daemon 실행 중"

# Docker 그룹 권한 확인
if ! groups $USER | grep -q '\bdocker\b'; then
    echo "[WARN] $USER is not in docker group. Adding..."
    sudo usermod -aG docker $USER
    echo "[INFO] 재로그인 필요. 다음 명령으로 세션 재시작 가능:"
    echo "      exec su -l $USER"
    exit 1
fi

# BuildKit / Buildx 활성화
export DOCKER_BUILDKIT=1
export DOCKER_CLI_PLUGIN_DIR=$HOME/.docker/cli-plugins
echo "✅ BuildKit 및 Buildx 활성화 완료"

# Docker 연결 테스트
echo "🔧 Docker 연결 테스트..."
docker run --rm hello-world
echo "✅ Docker 연결 정상"

# Docker Compose 파일 경로 확인
COMPOSE_FILE="./docker-compose.yml"
if [ ! -f "$COMPOSE_FILE" ]; then
    echo "[ERROR] docker-compose.yml 파일이 infra 디렉토리에 없습니다."
    exit 1
fi

# Docker Compose 서비스 실행 (V1)
echo "1️⃣ Docker Compose 서비스 시작"
docker-compose -f "$COMPOSE_FILE" up -d --build

# 서비스 상태 출력
echo "🔍 Docker Compose 서비스 상태:"
docker-compose -f "$COMPOSE_FILE" ps

# Tailscale Funnel 실행
echo "2️⃣ Tailscale Funnel 실행 (포트 8080)"
sudo tailscale funnel 8080 &

# GitHub Actions Runner 자동 실행
RUNNER_DIR="$HOME/actions-runner"
if [ -d "$RUNNER_DIR" ]; then
    echo "3️⃣ GitHub Actions Runner 실행"
    cd "$RUNNER_DIR"
    nohup ./run.sh > runner.log 2>&1 &
    echo "[INFO] GitHub Actions Runner 로그: $RUNNER_DIR/runner.log"
    echo "✅ GitHub Actions Runner 시작 완료"
fi




echo "✅ 서비스 실행 완료!"
echo "외부에서 접속 가능한 URL:"
echo "https://laptop-8vevpj3e.tail433c38.ts.net/"

echo "💡 Docker 로그 확인: docker-compose logs -f"
