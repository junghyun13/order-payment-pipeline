#!/bin/bash
echo "===== VM Metrics Collector ====="

echo "[CPU]"
mpstat 1 2

echo "[Memory]"
free -h

echo "[Disk IO]"
iostat -xz 1 2

echo "[Network]"
ifstat 1 2
