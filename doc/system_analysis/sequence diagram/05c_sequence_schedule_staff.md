# BIỂU ĐỒ TUẦN TỰ - PHẦN D: LỊCH TRÌNH & PHÂN CÔNG NHÂN VIÊN (UC21 – UC24)

---

> **[LƯU Ý QUAN TRỌNG VỀ KIẾN TRÚC MỚI]**
> Kể từ phiên bản V2, các chức năng Tạo, Sửa, Xóa, Xem lịch trình con độc lập **đã bị loại bỏ**.
> Các thao tác này đã được tích hợp đồng bộ thành một mảng `schedules` và được xử lý chung trong luồng Tạo/Cập nhật/Xem Sự kiện (Xem phần B và C).

---

## UC21 – Xem danh sách Nhân viên toàn hệ thống (Admin / Organizer)

```mermaid
sequenceDiagram
    actor User as Admin / Người tổ chức
    participant UI as Màn hình Quản lý Nhân viên
    participant HT as Hệ thống
    participant DB as Cơ sở dữ liệu

    User->>UI: 1. Truy cập trang danh sách nhân viên
    UI->>HT: 2. Gọi API GET /admin/get-all-users?role=STAFF
    HT->>HT: 3. Kiểm tra quyền (Admin hoặc Organizer)
    alt Không có quyền
        HT-->>UI: 4a. Từ chối truy cập
        UI-->>User: 4b. Thông báo không có quyền
    else Có quyền (Organizer)
        HT->>HT: 4c. Ép buộc lọc theo role = STAFF
    end
    HT->>DB: 5. Truy vấn danh sách người dùng phân trang
    DB-->>HT: 6. Trả về danh sách nhân viên
    HT-->>UI: 7. Trả về danh sách
    UI-->>User: 8. Hiển thị danh sách nhân viên để phân công
```

---

## UC22 – Xem danh sách staff được phân công vào sự kiện

```mermaid
sequenceDiagram
    actor User as Admin / Người tổ chức / Nhân viên
    participant UI as Màn hình Chi tiết Sự kiện - Tab Nhân viên
    participant HT as Hệ thống
    participant DB as Cơ sở dữ liệu

    User->>UI: 1. Mở tab "Nhân viên" của sự kiện
    UI->>HT: 2. Yêu cầu danh sách nhân viên phụ trách sự kiện (kèm Token)
    HT->>HT: 3. Kiểm tra quyền (Admin, Organizer chủ sự kiện, hoặc Nhân viên được phân công)
    alt Không có quyền (ví dụ: User thường)
        HT-->>UI: 4a. Từ chối truy cập
        UI-->>User: 4b. Thông báo không có quyền
    else Có quyền
        HT->>DB: 4. Truy vấn bảng phân công nhân viên của sự kiện
        DB-->>HT: 5. Trả về danh sách nhân viên đang phụ trách
        HT-->>UI: 6. Trả về danh sách
        UI-->>User: 7. Hiển thị danh sách nhân viên đang trực
    end
```

---

## UC23 – Đồng bộ phân công Nhân viên (Organizer)

```mermaid
sequenceDiagram
    actor Org as Người tổ chức
    participant UI as Màn hình Phân công Nhân viên
    participant HT as Hệ thống
    participant DB as Cơ sở dữ liệu

    Org->>UI: 1. Tích chọn / bỏ chọn nhân viên trong danh sách
    Org->>UI: 2. Nhấn "Lưu phân công"
    UI->>HT: 3. Gửi danh sách mã nhân viên muốn phân công (kèm Token)
    HT->>HT: 4. Kiểm tra người gửi có phải chủ sự kiện không
    alt Không phải chủ sự kiện
        HT-->>UI: 5a. Từ chối truy cập
        UI-->>Org: 5b. Thông báo không có quyền
    else Kiểm tra tính hợp lệ
        HT->>DB: 5. Xác minh tất cả ID phải là nhân viên (STAFF) đang ACTIVE
        alt Có ID không hợp lệ
            DB-->>HT: 6a. Có tài khoản không hợp lệ
            HT-->>UI: 6b. Thông báo lỗi danh sách nhân viên không hợp lệ
        else Tất cả hợp lệ
            HT->>DB: 6. Lấy danh sách nhân viên đang phụ trách hiện tại
            HT->>HT: 7. So sánh: Thêm mới / Giữ nguyên / Xóa bỏ
            HT->>DB: 8. Cập nhật bảng phân công (xóa dư thừa, thêm mới)
            DB-->>HT: 9. Lưu thành công
            HT-->>UI: 10. Trả về danh sách nhân viên sau đồng bộ
            UI-->>Org: 11. Hiển thị kết quả phân công mới
        end
    end

    note over Org,UI: Gửi danh sách rỗng [] sẽ xóa toàn bộ nhân viên khỏi sự kiện
```

---

## UC24 – Xóa một Nhân viên khỏi sự kiện (Organizer)

```mermaid
sequenceDiagram
    actor Org as Người tổ chức
    participant UI as Màn hình Phân công Nhân viên
    participant HT as Hệ thống
    participant DB as Cơ sở dữ liệu

    Org->>UI: 1. Nhấn nút "Xóa" bên cạnh tên nhân viên
    UI-->>Org: 2. Hộp thoại xác nhận gỡ nhân viên
    alt Nhấn "Hủy"
        Org->>UI: 3a. Nhấn "Hủy"
        UI-->>Org: 3b. Giữ nguyên danh sách
    else Xác nhận
        Org->>UI: 3. Xác nhận
        UI->>HT: 4. Gửi yêu cầu gỡ nhân viên khỏi sự kiện (kèm Token)
        HT->>HT: 5. Kiểm tra quyền chủ sự kiện
        alt Không có quyền
            HT-->>UI: 6a. Từ chối
        else Có quyền
            HT->>DB: 6. Xóa bản ghi phân công của nhân viên khỏi sự kiện
            DB-->>HT: 7. Xóa thành công
            HT-->>UI: 8. Thông báo đã gỡ nhân viên thành công
            UI-->>Org: 9. Cập nhật lại danh sách phân công
        end
    end
```
