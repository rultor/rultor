#!/usr/bin/env bash
set -e
set -x

if [ ! -e pid ]; then
  exit 1
fi

pid=$(cat pid)
if ps -p "${pid}" >/dev/null; then
  exit 0
fi

if [ ! -e cid ]; then
  exit 0
fi
cid=$(cat cid)
if docker ps -qa --no-trunc | grep --quiet "${cid}"; then
  echo "container ${cid} is alive"
else
  echo "container ${cid} is dead"
  exit 1
fi

