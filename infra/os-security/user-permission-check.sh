#!/bin/bash
echo "===== User & Permission Audit ====="

echo "[1] Sudo 그룹 사용자"
grep '^sudo:.*' /etc/group

echo "[2] 홈 디렉토리 권한 체크"
ls -ld /home/*

echo "[3] 비밀번호 만료 정책"
chage -l $USER
