#!/bin/bash
echo "===== Mount Status Check ====="

mount | grep -E "/mnt|/docker|/data"

# WSL2 감지
if grep -qEi "(Microsoft|WSL)" /proc/version &> /dev/null; then
    echo "[INFO] WSL2 환경 감지 → fstab 체크 생략"
else
    echo "[fstab 설정]"
    grep -v "^#" /etc/fstab || echo "[WARN] fstab 내용 읽기 실패"
fi
