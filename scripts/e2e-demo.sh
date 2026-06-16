#!/usr/bin/env bash
# G3-T14: E2E demo — nộp hồ sơ NSW → duyệt BCT → tra cứu kết quả NSW
set -euo pipefail

NSW_GW="${NSW_GW:-http://localhost:8084}"
NSW_AD="${NSW_AD:-http://localhost:8083}"
BCT_GW="${BCT_GW:-http://localhost:8082}"
BCT_AD="${BCT_AD:-http://localhost:8085}"
TEN_NGUOI_GUI="${TEN_NGUOI_GUI:-Cong ty ABC Demo}"

check_service() {
  local name="$1"
  local url="$2"
  local code
  code=$(curl -s -o /dev/null -w "%{http_code}" --connect-timeout 3 "${url}/actuator/health" 2>/dev/null || echo "000")
  if [ "$code" != "200" ]; then
    echo "ERROR: ${name} không phản hồi tại ${url} (HTTP ${code})" >&2
    echo "       Hãy khởi động service trước khi chạy E2E demo." >&2
    return 1
  fi
  echo "OK  ${name} (${url})"
}

echo "== 0. Kiểm tra services =="
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
"${SCRIPT_DIR}/ensure-db-schema.sh" 2>/dev/null || true
check_service "nsw-gateway"  "$NSW_GW"
check_service "nsw-adapter"  "$NSW_AD"
check_service "bct-gateway"  "$BCT_GW"
check_service "bct-adapter"  "$BCT_AD"
echo ""

echo "== 1. Nộp hồ sơ qua NSW Gateway =="
HTTP_CODE=0
RESP=$(curl -s -w "\n__HTTP__%{http_code}" --connect-timeout 10 -X POST "${NSW_GW}/nsw/thu-tuc-1/gui-ho-so" \
  -F "thongTin={\"tenNguoiGui\":\"${TEN_NGUOI_GUI}\"};type=application/json") || true
HTTP_CODE=$(echo "$RESP" | grep '__HTTP__' | sed 's/__HTTP__//')
BODY=$(echo "$RESP" | grep -v '__HTTP__')

if [ "$HTTP_CODE" != "200" ]; then
  echo "ERROR: Nộp hồ sơ thất bại (HTTP ${HTTP_CODE:-000})" >&2
  echo "Response: ${BODY:-<empty>}" >&2
  echo "Gợi ý: kiểm tra nsw-gateway log, MySQL (3316), MinIO (9000), Kafka (9092)" >&2
  exit 1
fi

echo "$BODY"
MA=$(echo "$BODY" | grep -o '"maSoHoSo":"[^"]*"' | head -1 | cut -d'"' -f4)
if [ -z "$MA" ]; then
  echo "ERROR: Không lấy được maSoHoSo từ response" >&2
  exit 1
fi
echo "maSoHoSo=$MA"

echo ""
echo "== 2. Chờ NSW→BCT và BCT adapter consume (15s) =="
sleep 15

echo ""
echo "== 3. Kiểm tra hồ sơ CHO_XU_LY tại BCT =="
LIST=$(curl -s --connect-timeout 5 "${BCT_AD}/bct/thu-tuc-1/ho-so?status=CHO_XU_LY" || echo "")
echo "$LIST"
echo "$LIST" | grep -q "$MA" || {
  echo "WARN: Chưa thấy $MA trong list CHO_XU_LY — kiểm tra nsw-adapter + bct-gateway + bct-adapter" >&2
}

echo ""
echo "== 4. Cán bộ BCT duyệt hồ sơ =="
DUYET=$(curl -s -w "\n__HTTP__%{http_code}" --connect-timeout 10 -X POST "${BCT_AD}/bct/thu-tuc-1/ho-so/${MA}/duyet" \
  -H 'Content-Type: application/json' \
  -d '{"tenNguoiXuLy":"Can bo A","ketQua":"Phe duyet ho so thanh cong. Du dieu kien thong quan."}')
echo "$DUYET" | grep -v '__HTTP__'
DUYET_CODE=$(echo "$DUYET" | grep '__HTTP__' | sed 's/__HTTP__//')
if [ "$DUYET_CODE" != "200" ]; then
  echo "ERROR: Duyệt thất bại (HTTP ${DUYET_CODE})" >&2
  exit 1
fi

echo ""
echo "== 5. Chờ TraLoi NSW adapter xử lý (5s) =="
sleep 5

echo ""
echo "== 6. DN tra cứu kết quả tại NSW =="
TRA_CUU=$(curl -s --connect-timeout 5 "${NSW_GW}/nsw/thu-tuc-1/ho-so/${MA}" || echo "")
echo "$TRA_CUU"

echo "$TRA_CUU" | grep -q 'DA_PHE_DUYET' && echo "OK: businessStatus=DA_PHE_DUYET" || {
  echo "WARN: Chưa thấy DA_PHE_DUYET — kiểm tra nsw-gateway + nsw-adapter" >&2
  exit 1
}

echo ""
echo "E2E demo completed successfully for $MA"
