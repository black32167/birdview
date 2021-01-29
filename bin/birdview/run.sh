#!/bin/bash

set -euxo pipefail

SCRIPT_DIR="${BASH_SOURCE%/*}"

docker-compose -f "${SCRIPT_DIR}/docker-compose.yml" up
