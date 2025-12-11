#!/bin/bash

ES="http://es-nori:9200"

echo "==== Elasticsearch Cluster Health ===="
curl -s "$ES/_cluster/health?pretty"

echo
echo "==== Elasticsearch Node Info ===="
curl -s "$ES/_nodes/stats?pretty" | head -n 50
