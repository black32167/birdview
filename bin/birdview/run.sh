#!/bin/bash

SERVICE="${1}"
set -euo pipefail

: ${SERVICE:=}
SCRIPT_DIR="${BASH_SOURCE%/*}"
BV_HOME="${HOME}/.birdview"
mkdir -p "${BV_HOME}/postgres"

if [[ "${SERVICE}" == "" ]]; then
  ${SCRIPT_DIR}/build.sh maven
fi

docker-compose -f "${SCRIPT_DIR}/docker-compose.yml" up "${SERVICE}"
