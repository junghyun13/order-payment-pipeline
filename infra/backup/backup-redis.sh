#!/bin/bash

TIMESTAMP=$(date +"%Y%m%d-%H%M")
BACKUP_DIR="./redis"
mkdir -p $BACKUP_DIR

docker exec redis redis-cli save
docker cp redis:/data/dump.rdb "$BACKUP_DIR/dump-$TIMESTAMP.rdb"

echo "Redis Backup Completed: $BACKUP_DIR/dump-$TIMESTAMP.rdb"
