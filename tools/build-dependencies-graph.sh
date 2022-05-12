#!/usr/bin/env bash

: "${TARGET:=${BASH_SOURCE%/*/*}/birdview/target/birdview-dist/lib/birdview-1.0.0.jar}"
OUTPUT="./deps"
OUTPUT_RESUT="${OUTPUT}/cleaned.dot"
echo "Building dependency graph for ${TARGET}"
mkdir -p "${OUTPUT}"
jdeps -verbose \
  -e "org\.birdview.*" \
  --dot-output "${OUTPUT}" \
  --ignore-missing-deps \
  "${TARGET}"

cat "${OUTPUT}/${TARGET##*/}.dot" \
  | perl -pe 's|[.\w]*\.||g' \
  | perl -pe 's| \(.*\)||g' \
  | perl -nle 'print if ! /.*\$.*/' > "${OUTPUT_RESUT}"

echo "Graph is written to:"
echo $(realpath "${OUTPUT_RESUT}")