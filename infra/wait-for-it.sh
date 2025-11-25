#!/usr/bin/env bash
set -e

TIMEOUT=${WAIT_FOR_IT_TIMEOUT:-30}
HOST_PORT=$1
shift
HOST=$(echo $HOST_PORT | cut -d':' -f1)
PORT=$(echo $HOST_PORT | cut -d':' -f2)

echo "Waiting for $HOST:$PORT for $TIMEOUT seconds..."
for i in $(seq $TIMEOUT); do
  if curl -s "http://$HOST:$PORT/_cluster/health" >/dev/null 2>&1; then
    echo "$HOST:$PORT is ready!"
    if [ "$#" -gt 0 ]; then
      exec "$@"
    fi
    exit 0
  fi
  sleep 1
done

echo "Timeout waiting for $HOST:$PORT"
exit 1
