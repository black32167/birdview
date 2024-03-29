#!/bin/bash

set -euo pipefail

SCRIPT_DIR="${BASH_SOURCE%/*}"
. "${SCRIPT_DIR}/include.sh"

MODUE_DIR="${SCRIPT_DIR%/*/*}/birdview"

mbuild() {
  FIRESTORE_EMULATOR_HOST=localhost:8080 \
  GOOGLE_APPLICATION_CREDENTIALS=${HOME}/gcloud/birdview-creds.json \
    mvn clean install -pl "${MODUE_DIR}"
}

dbuild() {
	local CTX_DIR="$(cd ${MODUE_DIR} && pwd)/target/birdview-dist"
	local DOCKER_FILE="${MODUE_DIR}/docker/Dockerfile"

  mbuild
	echo "==== Building docker image ${IMAGE_NAME} ===="

	docker build -t ${IMAGE_NAME} -f "${DOCKER_FILE}" "${CTX_DIR}"
}

case "${1}" in
docker)
  dbuild
  ;;
maven)
  mbuild
  ;;
*)
  echo "Usage:"
  echo "${BASH_SOURCE} {docker|maven}"
  ;;
esac
