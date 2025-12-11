#!/bin/bash
APP_LOG_DIR="/opt/app/logs"
BACKUP_DIR="/opt/backup/app-logs"
TIMESTAMP=$(date +"%Y%m%d-%H%M")

mkdir -p $BACKUP_DIR

tar -czf $BACKUP_DIR/app-logs-$TIMESTAMP.tar.gz $APP_LOG_DIR

echo "Spring Boot 로그 백업 완료 → $BACKUP_DIR/app-logs-$TIMESTAMP.tar.gz"
