# Screen Specs — API Response & Flow (Read-only)

> MVP không có Web UI. Thư mục này mô tả **dữ liệu trả về API** mà client (Postman, SoapUI) cần nhận — giúp backend agent hiểu expected behavior.

## Danh sách screen spec

| File | Mô tả | API liên quan |
|------|-------|---------------|
| [SCR-GUI-HO-SO.md](./SCR-GUI-HO-SO.md) | Nộp hồ sơ qua NSW | `POST /nsw/thu-tuc-1/gui-ho-so` |
| [SCR-TRA-CUU-HO-SO.md](./SCR-TRA-CUU-HO-SO.md) | Tra cứu trạng thái hồ sơ | `GET /nsw/thu-tuc-1/ho-so/{maSoHoSo}` |
| [SCR-BCT-DANH-SACH.md](./SCR-BCT-DANH-SACH.md) | Danh sách hồ sơ chờ xử lý BCT | `GET /bct/thu-tuc-1/ho-so` |
| [SCR-BCT-DUYET-TU-CHOI.md](./SCR-BCT-DUYET-TU-CHOI.md) | Cán bộ duyệt / từ chối | `POST .../duyet`, `POST .../tu-choi` |
