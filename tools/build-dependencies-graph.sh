#!/usr/bin/env bash

: "${TARGET:=${BASH_SOURCE%/*/*}/birdview/target/birdview-dist/lib/birdview-1.0.0.jar}"
OUTPUT="./deps"
OUTPUT_RESUT="${OUTPUT}/cleaned.dot"
OUTPUT_STYLED_RESUT="${OUTPUT}/styled.dot"
echo "Building dependency graph for ${TARGET}"
mkdir -p "${OUTPUT}"
#jdeps -verbose \
#  -e "org\.birdview.*" \
#  --dot-output "${OUTPUT}" \
#  --ignore-missing-deps \
#  "${TARGET}"

#cat "${OUTPUT}/${TARGET##*/}.dot" \
#  | perl -pe 's|[.\w]*\.||g' \
#  | perl -pe 's| \(.*\)||g' \
#  | perl -nle 'print if ! /.*\$.*/' > "${OUTPUT_RESUT}1"

cat "${OUTPUT_RESUT}1" \
  | grep '^ *"' \
  | perl -pe 's|^ *("[\w]+").*|  $1|' | sort -u > "${OUTPUT_RESUT}2"



echo 'digraph "birdview" {' > "${OUTPUT_RESUT}"
echo '  node [shape="rect" margin=0.1  fontsize=10 style="filled"]; edge [arrowhead="vee"];' >> "${OUTPUT_RESUT}"
cat "${OUTPUT_RESUT}2" \
  | perl -pe 's|(".*Web.*")|$1 \[fillcolor="red"\]|' \
  | perl -pe 's|(".*Storage.*")|$1 \[fillcolor="blue"\]|' \
  | perl -pe 's|(".*TaskService")|$1 \[fillcolor="green"\]|' \
  | perl -pe 's|(".*Client")|$1 \[fillcolor="cyan"\]|' \
  >> "${OUTPUT_RESUT}"
echo '' >> "${OUTPUT_RESUT}"
cat "${OUTPUT_RESUT}1" | grep '^ *"' >> "${OUTPUT_RESUT}"
echo '}' >> "${OUTPUT_RESUT}"

echo "Graph is written to:"
echo "$(realpath "${OUTPUT_RESUT}")"