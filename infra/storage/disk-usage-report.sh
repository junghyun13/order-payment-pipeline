#!/bin/bash
echo "===== Storage Usage Report ====="
df -h | grep -E "Filesystem|/docker|/var|/home" || true
