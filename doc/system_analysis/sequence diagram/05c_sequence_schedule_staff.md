# BIỂU ĐỒ TUẦN TỰ - PHẦN C: PHÂN CÔNG NHÂN SỰ (UC15)

---

## UC15 – Phân công Nhân viên (Staff) cho sự kiện

```mermaid
sequenceDiagram
    actor Organizer
    participant Màn hình
    participant Hệ thống
    participant CSDL

    Organizer->>Màn hình: Tích chọn Staff mới hoặc bỏ tích Staff cũ
    Màn hình->>Hệ thống: Gửi danh sách ID Staff cần phân công
    Hệ thống->>CSDL: Truy vấn thông tin sự kiện
    Hệ thống->>Hệ thống: Xác minh quyền sở hữu sự kiện
    alt Không phải chủ sự kiện
        Hệ thống-->>Màn hình: Báo lỗi thao tác trái phép
        Màn hình-->>Organizer: Hiển thị lỗi từ chối truy cập
    else Hợp lệ
        Hệ thống->>CSDL: Cập nhật danh sách Staff vào bảng phân công
        CSDL-->>Hệ thống: Xác nhận
        Hệ thống-->>Màn hình: Trả về thành công
        Màn hình-->>Organizer: Thông báo đã phân công nhân sự
    end
```
