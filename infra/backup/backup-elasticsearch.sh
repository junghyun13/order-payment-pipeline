#!/bin/bash

ES="http://es-nori:9200"

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
