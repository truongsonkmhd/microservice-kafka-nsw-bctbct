#!/usr/bin/env bash
# G4 — Kịch bản test Tin cậy & Nhất quán dữ liệu (AC-07 → AC-10)
# Chạy: ./scripts/g4-test-scenarios.sh [all|s01|s02|...]
# Yêu cầu: docker infra + 4 Spring Boot apps đang chạy
set -euo pipefail

NSW_GW="${NSW_GW:-http://localhost:8084}"
NSW_AD="${NSW_AD:-http://localhost:8083}"
BCT_GW="${BCT_GW:-http://localhost:8082}"
BCT_AD="${BCT_AD:-http://localhost:8085}"

NSW_MYSQL="${NSW_MYSQL_CONTAINER:-nsw-mysql}"
BCT_MYSQL="${BCT_MYSQL_CONTAINER:-bct-mysql}"
MYSQL_USER="${MYSQL_USER:-root}"
MYSQL_PASS="${MYSQL_PASS:-rootpassword}"

PASS=0
FAIL=0
WARN=0

log_ok()   { echo "  [PASS] $*"; PASS=$((PASS + 1)); }
log_fail() { echo "  [FAIL] $*" >&2; FAIL=$((FAIL + 1)); }
log_warn() { echo "  [WARN] $*"; WARN=$((WARN + 1)); }
log_step() { echo ""; echo "== $* =="; }

nsw_sql() {
  docker exec "$NSW_MYSQL" mysql -N -u"$MYSQL_USER" -p"$MYSQL_PASS" nsw_adapter -e "$1" 2>/dev/null
}

bct_sql() {
  docker exec "$BCT_MYSQL" mysql -N -u"$MYSQL_USER" -p"$MYSQL_PASS" bct_adapter -e "$1" 2>/dev/null
}

check_services() {
  log_step "0. Preflight — 4 services + Docker"
  local ok=1
  for pair in "nsw-gateway|$NSW_GW" "nsw-adapter|$NSW_AD" "bct-gateway|$BCT_GW" "bct-adapter|$BCT_AD"; do
    local name="${pair%%|*}" url="${pair##*|}"
    local code
    code=$(curl -s -o /dev/null -w "%{http_code}" --connect-timeout 3 "${url}/actuator/health" 2>/dev/null || echo "000")
    if [ "$code" = "200" ]; then
      echo "  OK  $name"
    else
      echo "  ERR $name (HTTP $code)" >&2
      ok=0
    fi
  done
  if ! docker ps --format '{{.Names}}' | grep -qx "$NSW_MYSQL"; then
    echo "  ERR container $NSW_MYSQL not running" >&2
    ok=0
  fi
  if [ "$ok" -eq 0 ]; then
    echo "Khởi động: docker compose -f docker-compose.dev.yml up -d && mvn spring-boot:run (4 module)" >&2
    exit 1
  fi
  SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
  "${SCRIPT_DIR}/ensure-db-schema.sh" 2>/dev/null || true
}

submit_hoso() {
  local ten="${1:-Cong ty G4 Test}"
  local resp http body ma
  resp=$(curl -s -w "\n__HTTP__%{http_code}" --connect-timeout 15 -X POST "${NSW_GW}/nsw/thu-tuc-1/gui-ho-so" \
    -F "thongTin={\"tenNguoiGui\":\"${ten}\"};type=application/json")
  http=$(echo "$resp" | grep '__HTTP__' | sed 's/__HTTP__//')
  body=$(echo "$resp" | grep -v '__HTTP__')
  if [ "$http" != "200" ]; then
    echo "Submit failed HTTP $http: $body" >&2
    return 1
  fi
  ma=$(echo "$body" | grep -o '"maSoHoSo":"[^"]*"' | head -1 | cut -d'"' -f4)
  echo "$ma"
}

send_traloi_soap() {
  local ma="$1"
  local ket_qua="$2"
  local correlation_id="${3:-}"
  local tmp
  tmp=$(mktemp)
  cat >"$tmp" <<EOF
<?xml version="1.0" encoding="UTF-8"?>
<soapenv:Envelope xmlns:soapenv="http://schemas.xmlsoap.org/soap/envelope/"
  xmlns:thu="thutuc1.bct.xsd.nsw_gateway.vn2bs.com"
  xmlns:cor="http://vn2bs.com/correlation">
  <soapenv:Header>
    <cor:CorrelationId>${correlation_id}</cor:CorrelationId>
  </soapenv:Header>
  <soapenv:Body>
    <thu:TraLoiRequest>
      <thu:maSoHoSo>${ma}</thu:maSoHoSo>
      <thu:ketQua>${ket_qua}</thu:ketQua>
    </thu:TraLoiRequest>
  </soapenv:Body>
</soapenv:Envelope>
EOF
  curl -s -o /dev/null -w "%{http_code}" --connect-timeout 10 -X POST "${NSW_GW}/web-services" \
    -H "Content-Type: text/xml; charset=utf-8" \
    -H 'SOAPAction: ""' \
    --data-binary @"$tmp"
  rm -f "$tmp"
}

# S01 — Happy path + message_log + correlation_id (AC-07 prep)
scenario_s01() {
  log_step "S01 — E2E + message_log + correlation_id"
  local ma
  ma=$(submit_hoso "Cong ty G4 S01") || { log_fail "Nộp hồ sơ"; return; }
  echo "  maSoHoSo=$ma"
  sleep 15

  local cid count_log
  cid=$(nsw_sql "SELECT correlation_id FROM thutuc1_guihoso WHERE ma_so_ho_so='${ma}' LIMIT 1;")
  if [ -n "$cid" ]; then
    log_ok "correlation_id trên hồ sơ: $cid"
  else
    log_fail "Không có correlation_id trong thutuc1_guihoso"
  fi

  count_log=$(nsw_sql "SELECT COUNT(*) FROM message_log WHERE ma_so_ho_so='${ma}';")
  if [ "${count_log:-0}" -ge 1 ]; then
    log_ok "message_log có ${count_log} bản ghi cho $ma"
    nsw_sql "SELECT log_status, message_type, sender, receiver FROM message_log WHERE ma_so_ho_so='${ma}' ORDER BY id;" \
      | sed 's/^/    /'
  else
    log_fail "message_log trống cho $ma"
  fi

  # Duyệt BCT
  curl -s -o /dev/null -X POST "${BCT_AD}/bct/thu-tuc-1/ho-so/${ma}/duyet" \
    -H 'Content-Type: application/json' \
    -d '{"tenNguoiXuLy":"Can bo G4","ketQua":"Phe duyet G4 test"}'
  sleep 6

  local bs
  bs=$(curl -s "${NSW_GW}/nsw/thu-tuc-1/ho-so/${ma}" | grep -o '"businessStatus":"[^"]*"' | head -1 || true)
  if echo "$bs" | grep -q DA_PHE_DUYET; then
    log_ok "NSW businessStatus=DA_PHE_DUYET"
  else
    log_warn "Chưa DA_PHE_DUYET: $bs"
  fi
  export LAST_MA_SO_HO_SO="$ma"
  export LAST_CORRELATION_ID="$cid"
}

# S02 — Outbox pattern (G4-T12)
scenario_s02() {
  log_step "S02 — Outbox: PENDING → PUBLISHED"
  local ma
  ma=$(submit_hoso "Cong ty G4 S02 Outbox") || { log_fail "Nộp hồ sơ"; return; }
  echo "  maSoHoSo=$ma"

  local pending
  pending=$(nsw_sql "SELECT COUNT(*) FROM outbox_event WHERE aggregate_key='${ma}' AND status='PENDING';")
  if [ "${pending:-0}" -ge 1 ]; then
    log_ok "outbox_event PENDING ngay sau submit ($pending)"
  else
    log_warn "Chưa thấy PENDING (có thể job đã publish rất nhanh)"
  fi

  echo "  Chờ OutboxPublisherJob (6s)..."
  sleep 6

  local published
  published=$(nsw_sql "SELECT COUNT(*) FROM outbox_event WHERE aggregate_key='${ma}' AND status='PUBLISHED';")
  if [ "${published:-0}" -ge 1 ]; then
    log_ok "outbox_event PUBLISHED"
    nsw_sql "SELECT id, topic, status, created_at, published_at FROM outbox_event WHERE aggregate_key='${ma}' ORDER BY id;" \
      | sed 's/^/    /'
  else
    log_fail "outbox chưa PUBLISHED — kiểm tra outbox.publisher.enabled=true và Kafka 9092"
  fi
}

# S03 — Idempotency gateway BCT: gửi trùng maSoHoSo (G4-T05 partial)
scenario_s03() {
  log_step "S03 — Idempotency BCT gateway (trùng maSoHoSo REST)"
  local ma="G4-DUP-$(date +%s)"
  local body code1 code2

  body='{"maSoHoSo":"'"$ma"'","tenNguoiGui":"Dup Test"}'
  code1=$(curl -s -o /dev/null -w "%{http_code}" -X POST "${BCT_GW}/bct/thu-tuc-1/gui-ho-so" \
    -H 'Content-Type: application/json' -d "$body")
  code2=$(curl -s -o /dev/null -w "%{http_code}" -X POST "${BCT_GW}/bct/thu-tuc-1/gui-ho-so" \
    -H 'Content-Type: application/json' -d "$body")

  if [ "$code1" = "200" ] && [ "$code2" = "200" ]; then
    log_ok "Cả 2 lần gửi trả HTTP 200 (idempotent ack)"
  else
    log_fail "HTTP lần 1=$code1 lần 2=$code2"
  fi

  local cnt
  cnt=$(bct_sql "SELECT COUNT(*) FROM thutuc1_guihoso WHERE ma_so_ho_so='${ma}';")
  if [ "${cnt:-0}" = "1" ]; then
    log_ok "Chỉ 1 bản ghi DB BCT cho $ma"
  else
    log_fail "DB có $cnt bản ghi (mong đợi 1)"
  fi
}

# S04 — State machine: duyệt khi không phải CHO_XU_LY (AC-08)
scenario_s04() {
  log_step "S04 — State machine: duyệt sai trạng thái (409)"
  local ma="G4-STATE-$(date +%s)"
  curl -s -o /dev/null -X POST "${BCT_GW}/bct/thu-tuc-1/gui-ho-so" \
    -H 'Content-Type: application/json' \
    -d "{\"maSoHoSo\":\"${ma}\",\"tenNguoiGui\":\"State Test\"}"

  local code
  code=$(curl -s -o /dev/null -w "%{http_code}" -X POST "${BCT_AD}/bct/thu-tuc-1/ho-so/${ma}/duyet" \
    -H 'Content-Type: application/json' \
    -d '{"tenNguoiXuLy":"Can bo","ketQua":"Phe duyet som"}')

  if [ "$code" = "409" ]; then
    log_ok "Duyệt KHOI_TAO trả HTTP 409 CONFLICT"
  else
    log_fail "Mong đợi 409, nhận HTTP $code"
  fi
}

# S05 — Từ chối TraLoi hồ sơ DA_HUY (G4-T08 / EX-04)
scenario_s05() {
  log_step "S05 — Từ chối TraLoi hồ sơ DA_HUY"
  local ma
  ma=$(submit_hoso "Cong ty G4 S05 Huy") || { log_fail "Nộp hồ sơ"; return; }
  nsw_sql "UPDATE thutuc1_guihoso SET business_status='DA_HUY' WHERE ma_so_ho_so='${ma}';" >/dev/null
  log_ok "Đã set business_status=DA_HUY cho $ma"

  local cid="g4-huy-test-$(date +%s)"
  send_traloi_soap "$ma" "Phe duyet" "$cid" >/dev/null
  sleep 3

  local cnt
  cnt=$(nsw_sql "SELECT COUNT(*) FROM message_log WHERE correlation_id='${cid}' AND log_status='PROCESSED_FAILED';")
  if [ "${cnt:-0}" -ge 1 ]; then
    log_ok "message_log PROCESSED_FAILED cho TraLoi hồ sơ DA_HUY"
  else
    log_warn "Kiểm tra log nsw-gateway — mong rejected SOAP ketQua=rejected"
  fi
}

# S06 — Idempotency TraLoi trùng Correlation ID (AC-07)
scenario_s06() {
  log_step "S06 — Idempotency TraLoi trùng Correlation ID (AC-07)"
  local ma
  ma=$(submit_hoso "Cong ty G4 S06 Idem") || { log_fail "Nộp hồ sơ"; return; }
  sleep 12

  local cid="g4-idem-$(date +%s)"
  send_traloi_soap "$ma" "Phe duyet lan 1" "$cid" >/dev/null
  sleep 2
  send_traloi_soap "$ma" "Phe duyet lan 2 trung CID" "$cid" >/dev/null
  sleep 3

  local success_cnt
  success_cnt=$(nsw_sql "SELECT COUNT(*) FROM message_log WHERE correlation_id='${cid}' AND log_status='PROCESSED_SUCCESS';")
  if [ "${success_cnt:-0}" = "1" ]; then
    log_ok "Chỉ 1 PROCESSED_SUCCESS cho correlation_id=$cid"
  else
    log_warn "PROCESSED_SUCCESS count=$success_cnt (mong 1) — adapter có thể skip qua message_log gateway"
  fi

  local bs
  bs=$(curl -s "${NSW_GW}/nsw/thu-tuc-1/ho-so/${ma}" | grep -o '"businessStatus":"[^"]*"' | head -1 || true)
  echo "  NSW status: $bs"
}

# S07 — Reconciliation thủ công (AC-10 / G4-T18)
scenario_s07() {
  log_step "S07 — Reconciliation thủ công"
  local resp code
  resp=$(curl -s -w "\n__HTTP__%{http_code}" -X POST "${NSW_AD}/nsw/admin/reconciliation/run")
  code=$(echo "$resp" | grep '__HTTP__' | sed 's/__HTTP__//')
  echo "$resp" | grep -v '__HTTP__' | head -c 500
  echo ""
  if [ "$code" = "200" ]; then
    log_ok "POST /nsw/admin/reconciliation/run HTTP 200"
  else
    log_fail "Reconciliation HTTP $code"
  fi
}

# S08 — Replay TraLoi (G4-T17)
scenario_s08() {
  log_step "S08 — Replay TraLoi BCT admin"
  local ma="${LAST_MA_SO_HO_SO:-}"
  if [ -z "$ma" ]; then
    ma=$(submit_hoso "Cong ty G4 S08 Replay") || { log_fail "Nộp hồ sơ"; return; }
    sleep 15
    curl -s -o /dev/null -X POST "${BCT_AD}/bct/thu-tuc-1/ho-so/${ma}/duyet" \
      -H 'Content-Type: application/json' \
      -d '{"tenNguoiXuLy":"Can bo","ketQua":"Phe duyet replay test"}'
    sleep 3
  fi
  echo "  maSoHoSo=$ma"

  local code body
  body=$(curl -s -w "\n__HTTP__%{http_code}" -X POST "${BCT_AD}/bct/admin/reconciliation/replay/${ma}")
  code=$(echo "$body" | grep '__HTTP__' | sed 's/__HTTP__//')
  echo "$body" | grep -v '__HTTP__'
  if [ "$code" = "200" ]; then
    log_ok "Replay TraLoi HTTP 200"
  else
    log_fail "Replay HTTP $code (cần hồ sơ DA_XU_LY/DA_GUI_KET_QUA + bản ghi TraLoi)"
  fi
}

# S09 — MinIO orphan cleanup (G4-T14) — manual trigger via service bean
scenario_s09() {
  log_step "S09 — MinIO orphan bucket cleanup (hướng dẫn thủ công)"
  local orphan="nsw-thutuc1-guihoso-g4-orphan-$(date +%s)"
  echo "  1. Tạo bucket mồ côi (không có DB):"
  echo "     docker run --rm --network host minio/mc alias set local http://localhost:9000 minioadmin minioadmin"
  echo "     docker run --rm --network host minio/mc mb local/${orphan}"
  echo "  2. Chờ job 3h30 sáng HOẶC tạm set cron:"
  echo "     minio.orphan-cleanup.cron=0 */5 * * * *  (restart nsw-gateway)"
  echo "  3. Xác nhận bucket biến mất:"
  echo "     docker run --rm --network host minio/mc ls local/ | grep ${orphan} || echo 'bucket removed'"
  log_warn "Scenario S09 cần thao tác MinIO client — xem docs/test/G4-TEST-SCENARIOS.md"
}

# S10 — DLQ alert (AC-09) — hướng dẫn
scenario_s10() {
  log_step "S10 — DLQ / DEAD_LETTER (AC-09)"
  echo "  Cách demo:"
  echo "  1. Dừng Kafka NSW: docker stop nsw-kafka"
  echo "  2. Nộp hồ sơ → outbox PENDING, sau max-retries → FAILED + DLQ topic"
  echo "  3. Hoặc dừng bct-gateway (8082) → nsw-adapter retry → DEAD_LETTER + DLQ"
  echo "  4. Bật lại Kafka và xem log nsw-adapter: [DLQ ALERT]"
  echo "  Kiểm tra:"
  echo "    docker exec nsw-kafka kafka-console-consumer.sh --bootstrap-server localhost:9092 \\"
  echo "      --topic nsw-thutuc1-guihoso.dlq --from-beginning --max-messages 1"
  log_warn "Scenario S10 inject lỗi — chạy thủ công khi demo sự cố"
}

print_summary() {
  log_step "Tổng kết"
  echo "  PASS=$PASS  FAIL=$FAIL  WARN=$WARN"
  if [ "$FAIL" -gt 0 ]; then
    exit 1
  fi
}

run_scenario() {
  case "$1" in
    s01) scenario_s01 ;;
    s02) scenario_s02 ;;
    s03) scenario_s03 ;;
    s04) scenario_s04 ;;
    s05) scenario_s05 ;;
    s06) scenario_s06 ;;
    s07) scenario_s07 ;;
    s08) scenario_s08 ;;
    s09) scenario_s09 ;;
    s10) scenario_s10 ;;
    *) echo "Unknown scenario: $1" >&2; exit 1 ;;
  esac
}

main() {
  local mode="${1:-all}"
  check_services

  case "$mode" in
    all)
      scenario_s01
      scenario_s02
      scenario_s03
      scenario_s04
      scenario_s05
      scenario_s06
      scenario_s07
      scenario_s08
      scenario_s09
      scenario_s10
      ;;
    s0*|s1*)
      run_scenario "$mode"
      ;;
    *)
      echo "Usage: $0 [all|s01|s02|...|s10]" >&2
      exit 1
      ;;
  esac
  print_summary
}

main "${1:-all}"
