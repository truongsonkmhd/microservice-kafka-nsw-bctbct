# Postman Collections

Thư mục chứa collection E2E cho demo NSW ↔ BCT.

| File | Task | Trạng thái |
|------|------|------------|
| `NSW-BCT-E2E.postman_collection.json` | G3-T15 | ✅ Done |

## Import vào Postman

1. **Import** → chọn file `NSW-BCT-E2E.postman_collection.json`
2. Đảm bảo 4 service + Docker infra đang chạy (xem [README §6](../../README.md#6-demo-e2e-mvp-g3))
3. Chạy folder **0 → 1 → 2 → 3 → 4** theo thứ tự (hoặc **Collection Runner**)

## Biến collection

| Biến | Mặc định | Mô tả |
|------|----------|-------|
| `nsw_gw` | `http://localhost:8084` | nsw-gateway |
| `nsw_ad` | `http://localhost:8083` | nsw-adapter |
| `bct_gw` | `http://localhost:8082` | bct-gateway |
| `bct_ad` | `http://localhost:8085` | bct-adapter |
| `maSoHoSo` | *(tự set)* | Lấy từ response bước 1 |
| `tenNguoiGui` | `Cong ty ABC Demo` | Tên người gửi |

Tham chiếu: [DEV_TASK_ASSIGNMENT.md](../../DEV_TASK_ASSIGNMENT.md) §7
