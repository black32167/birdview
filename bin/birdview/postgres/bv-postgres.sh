#!/bin/bash

set -euo pipefail

SCRIPT_DIR="$(dirname $0)"
: ${PG_CONTAINER_NAME:=birdview_postgres_1}

. "${SCRIPT_DIR}/../include.sh"

mkdir -p generated
PG_SQL_FILE_HOST="${SCRIPT_DIR}/generated/pg_init.sql"
PG_SQL_FILE_CONTAINER="/tmp/pg_init.sql"

generate_pg_init() {
  cat > "${PG_SQL_FILE_HOST}" <<-EOSQL
CREATE USER $PG_BV_USER WITH PASSWORD '$PG_BV_PASSWD';
CREATE DATABASE $PG_BV_DATABASE;
GRANT ALL PRIVILEGES ON DATABASE $PG_BV_DATABASE TO $PG_BV_USER;
EOSQL
  echo "Generated ${PG_SQL_FILE_HOST}"
}

run_pg_init() {
  docker cp "${PG_SQL_FILE_HOST}" "${PG_CONTAINER_NAME}:${PG_SQL_FILE_CONTAINER}"
  docker exec "${PG_CONTAINER_NAME}" psql -U postgres -v ON_ERROR_STOP=1 -f ${PG_SQL_FILE_CONTAINER}
}

pg_shell() {
  set +x
  docker exec -it "${PG_CONTAINER_NAME}" psql -U postgres birdview
}

case "${1}" in
  generate)
    generate_pg_init
  ;;
  init)
    generate_pg_init
    run_pg_init
  ;;
  shell)
    pg_shell
  ;;
esac
