#!/bin/bash

LOG_DIR="/opt/app/logs"

find $LOG_DIR -type f -name "*.log" -mtime +7 -print -delete

echo "7일 지난 로그 자동 삭제 완료"
