#!/bin/bash

SCRIPT_DIR="${BASH_SOURCE%/*}"
. "${SCRIPT_DIR}/include.sh"

: "${HOST_CONFIG_FOLDER:=${HOME}/codes}"

docker run --rm -v "${HOST_CONFIG_FOLDER}":/config "${IMAGE_NAME}" "${@}"
