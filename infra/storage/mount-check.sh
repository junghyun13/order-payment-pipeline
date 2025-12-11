#!/bin/bash
echo "===== Mount Status Check ====="

mount | grep -E "/mnt|/docker|/data"

echo "[fstab 설정]"
grep -v "^#" /etc/fstab
