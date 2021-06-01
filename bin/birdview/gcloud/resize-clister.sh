#!/usr/bin/env bash

if (( $# == 0 )); then
  echo "Usage:"
  echo "  $0 <nodes>"
  exit 1
fi

: "${CLUSTER_NAME:=birdview}"
NUM_NODES="$1"
gcloud container clusters resize "${CLUSTER_NAME}" --num-nodes "${NUM_NODES}"