#!/bin/bash
echo "===== Fail2ban Setup ====="

apt-get install -y fail2ban

cat <<EOF >/etc/fail2ban/jail.local
[sshd]
enabled = true
port = ssh
filter = sshd
logpath = /var/log/auth.log
maxretry = 4
bantime = 3600
EOF

systemctl restart fail2ban
systemctl enable fail2ban

echo "Fail2ban 설정 완료"
