#!/bin/bash

set -euo pipefail

SCRIPT_DIR="${BASH_SOURCE%/*}"
. "${SCRIPT_DIR}/include.sh"

dbuild() {
	${SCRIPT_DIR}/build.sh docker
}

dpublish() {
	docker push "${IMAGE_NAME}"
}

gpush() {
  git push
}

case "${1}" in
docker)
  dbuild
  gpush
  dpublish
  ;;
*)
  echo "Usage:"
  echo "${BASH_SOURCE} docker"
  ;;
esac
