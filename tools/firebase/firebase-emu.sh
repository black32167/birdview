#!/bin/bash

set -e

case $1 in
build)
  docker build -t firebase .
  ;;
run)
  docker run -it \
    -p 8080:8080 \
    -p 4000:4000 \
    firebase firebase emulators:start \
    --only firestore \
    --project birdview-286612 \
    --config /fire.json
  ;;
*)
  echo "Usage:"
  echo " $0 {build|run}"
esac

