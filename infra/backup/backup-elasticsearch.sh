#!/bin/bash


# WSL2 환경에서는 localhost 사용
ES="http://localhost:9200"

echo "Register snapshot repository"
curl -XPUT "$ES/_snapshot/es_backup" -H 'Content-Type: application/json' -d'
{
  "type": "fs",
  "settings": {
    "location": "/usr/share/elasticsearch/backup",
    "compress": true
  }
}'

echo "Create snapshot"
curl -XPUT "$ES/_snapshot/es_backup/snapshot_$(date +"%Y%m%d")?wait_for_completion=true"
