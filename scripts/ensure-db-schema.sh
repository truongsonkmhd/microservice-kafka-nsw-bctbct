#!/usr/bin/env bash
# Áp dụng enum business_status đầy đủ (Liquibase mặc định tắt trong dev).
set -euo pipefail

ENUM_SQL="ALTER TABLE thutuc1_guihoso MODIFY COLUMN business_status ENUM(
  'KHOI_TAO','CHO_XU_LY','DA_XU_LY','CHO_PHE_DUYET','DA_PHE_DUYET',
  'DA_GUI_KET_QUA','TU_CHOI','DA_HUY'
) DEFAULT 'KHOI_TAO' NOT NULL;"

apply() {
  local container="$1"
  local db="$2"
  if docker ps --format '{{.Names}}' | grep -qx "$container"; then
    docker exec "$container" mysql -uroot -prootpassword "$db" -e "$ENUM_SQL" 2>/dev/null \
      && echo "OK  ${db} business_status enum"
  fi
}

apply nsw-mysql nsw_adapter
apply bct-mysql bct_adapter
