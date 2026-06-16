#!/usr/bin/env bash
# G5-T04: Performance smoke — ~100 req/min to NSW Gateway GuiHoSo endpoint
set -euo pipefail

NSW_GW="${NSW_GW:-http://localhost:8084}"
TARGET_RPM="${TARGET_RPM:-100}"
DURATION_SEC="${DURATION_SEC:-60}"
TEN_NGUOI_GUI="${TEN_NGUOI_GUI:-G5 Perf Smoke}"

echo "== G5 Performance Smoke =="
echo "Target: ${TARGET_RPM} req/min for ${DURATION_SEC}s at ${NSW_GW}"
echo ""

code=$(curl -s -o /dev/null -w "%{http_code}" --connect-timeout 3 "${NSW_GW}/actuator/health" 2>/dev/null || echo "000")
if [ "$code" != "200" ]; then
  echo "ERROR: nsw-gateway not healthy (HTTP ${code})" >&2
  exit 1
fi

interval_ms=$((60000 / TARGET_RPM))
if [ "$interval_ms" -lt 1 ]; then interval_ms=1; fi

end=$((SECONDS + DURATION_SEC))
ok=0
fail=0
total=0

while [ "$SECONDS" -lt "$end" ]; do
  start_ms=$(date +%s%3N)
  http=$(curl -s -o /dev/null -w "%{http_code}" --connect-timeout 5 -X POST \
    "${NSW_GW}/nsw/thu-tuc-1/gui-ho-so" \
    -F "thongTin={\"tenNguoiGui\":\"${TEN_NGUOI_GUI}-${total}\"};type=application/json" \
    2>/dev/null || echo "000")
  total=$((total + 1))
  if [ "$http" = "200" ]; then
    ok=$((ok + 1))
  else
    fail=$((fail + 1))
    echo "WARN: request ${total} failed HTTP ${http}" >&2
  fi
  elapsed_ms=$(($(date +%s%3N) - start_ms))
  sleep_ms=$((interval_ms - elapsed_ms))
  if [ "$sleep_ms" -gt 0 ]; then
    sleep "$(awk "BEGIN {printf \"%.3f\", ${sleep_ms}/1000}")"
  fi
done

actual_rpm=$(awk "BEGIN {printf \"%.1f\", ${total} * 60 / ${DURATION_SEC}}")
success_rate=$(awk "BEGIN {printf \"%.1f\", ${ok} * 100 / ${total}}")

echo ""
echo "Results: total=${total} ok=${ok} fail=${fail}"
echo "Actual rate: ~${actual_rpm} req/min | Success rate: ${success_rate}%"

if [ "$fail" -gt 0 ]; then
  echo "FAIL: ${fail} requests failed" >&2
  exit 1
fi

echo "PASS: smoke test completed"
