# Documentation — Handle Multiple Service Gateway

Hệ thống tài liệu cho agent `senior-backend` và team dev.

## Cấu trúc

| Thư mục | Mục đích | Agent được sửa? |
|---------|----------|-----------------|
| [tasks/](./tasks/) | Task index, sprint, file task chi tiết | ✅ Có |
| [srs/](./srs/) | Software Requirements Specification | ❌ Read-only |
| [design/screens/](./design/screens/) | API response / screen specs | ❌ Read-only |
| [prd/](./prd/) | Product requirements (link PRD gốc) | ❌ Read-only |
| [postman/](./postman/) | Postman collections E2E | ✅ Có (task G3-T15) |
| [AGENT_WORK_LOG.md](./AGENT_WORK_LOG.md) | Nhật ký công việc agent | ✅ Có |

## Bắt đầu nhanh

1. Mở [tasks/TASK-INDEX.md](./tasks/TASK-INDEX.md)
2. Chọn task `⬜ TODO` trong sprint hiện tại
3. Đọc file task chi tiết trong `tasks/sprints/SPRINT-XX/`
4. Invoke agent: `.cursor/agents/senior-backend.md`

## Tài liệu gốc (repo root)

- [ARCHITECTURE.md](../ARCHITECTURE.md)
- [DEV_TASK_ASSIGNMENT.md](../DEV_TASK_ASSIGNMENT.md)
- [PROJECT_PLAN.md](../PROJECT_PLAN.md)
- [BUSINESS_ANALYSIS.md](../BUSINESS_ANALYSIS.md)
