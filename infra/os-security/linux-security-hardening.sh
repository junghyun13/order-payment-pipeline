#!/bin/bash
echo "=== Linux 보안 설정 ==="

echo "[1] SSH Root 로그인 차단"
sed -i 's/^PermitRootLogin.*/PermitRootLogin no/' /etc/ssh/sshd_config

echo "[2] 패스워드 최소 길이 5 설정"
sed -i 's/^PASS_MIN_LEN.*/PASS_MIN_LEN 5/' /etc/login.defs

echo "[3] 방화벽(UFW) 활성화"
ufw allow 22

ufw enable

echo "[4] Fail2ban 설치"
apt install -y fail2ban
systemctl enable fail2ban

systemctl restart sshd
echo "보안 강화 완료!"
