#!/bin/bash


# WSL2 환경에서는 localhost 사용
ES="http://localhost:9200"

echo "==== Elasticsearch Cluster Health ===="
curl -s "$ES/_cluster/health?pretty"

echo
echo "==== Elasticsearch Node Info ===="
curl -s "$ES/_nodes/stats?pretty" | head -n 50
