# BIỂU ĐỒ TUẦN TỰ - PHẦN C: LỊCH TRÌNH & PHÂN CÔNG NHÂN VIÊN (UC20 – UC28)

---

## UC20 – Xem lịch trình sự kiện

```mermaid
sequenceDiagram
    actor Anyone as Bất kỳ ai
    participant UI as Màn hình Chi tiết Sự kiện
    participant HT as Hệ thống
    participant DB as Cơ sở dữ liệu

    Anyone->>UI: 1. Xem trang chi tiết sự kiện (tab Lịch trình)
    UI->>HT: 2. Yêu cầu danh sách lịch trình của sự kiện
    HT->>DB: 3. Truy vấn tất cả lịch trình con thuộc sự kiện (sắp xếp theo thời gian tăng dần)
    DB-->>HT: 4. Trả về danh sách lịch trình (Tên hoạt động, Thời gian bắt đầu, Kết thúc, Mô tả)
    HT-->>UI: 5. Trả về dữ liệu
    UI-->>Anyone: 6. Hiển thị timeline lịch trình sự kiện
```

---

## UC21 – Tạo lịch trình con hàng loạt (Organizer)

```mermaid
sequenceDiagram
    actor Org as Người tổ chức
    participant UI as Màn hình Quản lý Lịch trình
    participant HT as Hệ thống
    participant DB as Cơ sở dữ liệu

    Org->>UI: 1. Nhấn "Thêm lịch trình", nhập thông tin nhiều hoạt động
    Org->>UI: 2. Nhấn "Lưu tất cả"
    UI->>HT: 3. Gửi danh sách lịch trình mới (kèm Token Organizer)
    HT->>HT: 4. Kiểm tra người gửi có phải chủ sự kiện không
    alt Không phải chủ sự kiện
        HT-->>UI: 5a. Từ chối - không có quyền
        UI-->>Org: 5b. Thông báo không có quyền
    else Kiểm tra thời gian từng lịch trình
        HT->>HT: 5. Kiểm tra thời gian bắt đầu/kết thúc mỗi hoạt động nằm trong khung giờ của sự kiện cha
        alt Có lịch trình vượt khung thời gian
            HT-->>UI: 6a. Thông báo thời gian vượt quá khung sự kiện
            UI-->>Org: 6b. Hiển thị lỗi cụ thể
        else Tất cả hợp lệ
            HT->>DB: 6. Lưu tất cả lịch trình vào cơ sở dữ liệu
            DB-->>HT: 7. Lưu thành công
            HT-->>UI: 8. Trả về danh sách lịch trình vừa tạo
            UI-->>Org: 9. Hiển thị timeline đã cập nhật
        end
    end
```

---

## UC22 – Cập nhật lịch trình con hàng loạt (Organizer)

```mermaid
sequenceDiagram
    actor Org as Người tổ chức
    participant UI as Màn hình Quản lý Lịch trình
    participant HT as Hệ thống
    participant DB as Cơ sở dữ liệu

    Org->>UI: 1. Chỉnh sửa thông tin các hoạt động trong timeline
    Org->>UI: 2. Nhấn "Cập nhật"
    UI->>HT: 3. Gửi danh sách lịch trình đã chỉnh sửa (kèm mã ID từng lịch trình)
    HT->>HT: 4. Kiểm tra quyền chủ sự kiện
    alt Không phải chủ sự kiện
        HT-->>UI: 5a. Từ chối truy cập
    else Kiểm tra từng lịch trình
        loop Mỗi lịch trình trong danh sách
            HT->>DB: 5. Tìm lịch trình theo mã ID
            alt ID không tồn tại
                DB-->>HT: 5a. Không tìm thấy
                HT->>HT: Ghi nhận lỗi
            else Thời gian vượt khung sự kiện
                HT-->>UI: 5b. Thông báo thời gian không hợp lệ
            else Hợp lệ
                HT->>DB: 6. Cập nhật thông tin lịch trình
            end
        end
        DB-->>HT: 7. Lưu thành công
        HT-->>UI: 8. Trả về danh sách đã cập nhật
        UI-->>Org: 9. Hiển thị timeline mới
    end
```

---

## UC23 – Xóa toàn bộ lịch trình (Organizer)

```mermaid
sequenceDiagram
    actor Org as Người tổ chức
    participant UI as Màn hình Quản lý Lịch trình
    participant HT as Hệ thống
    participant DB as Cơ sở dữ liệu

    Org->>UI: 1. Nhấn "Xóa tất cả lịch trình"
    UI-->>Org: 2. Hộp thoại xác nhận
    alt Nhấn "Hủy"
        Org->>UI: 3a. Nhấn "Hủy"
        UI-->>Org: 3b. Đóng hộp thoại
    else Xác nhận xóa
        Org->>UI: 3. Xác nhận xóa tất cả
        UI->>HT: 4. Gửi yêu cầu xóa toàn bộ lịch trình (kèm Token)
        HT->>HT: 5. Kiểm tra quyền chủ sự kiện
        alt Không có quyền
            HT-->>UI: 6a. Từ chối truy cập
            UI-->>Org: 6b. Thông báo không có quyền
        else Có quyền
            HT->>DB: 6. Xóa tất cả lịch trình thuộc sự kiện
            DB-->>HT: 7. Xóa thành công
            HT-->>UI: 8. Thông báo thành công
            UI-->>Org: 9. Timeline trống rỗng
        end
    end
```

---

## UC24 – Xóa một lịch trình con (Organizer)

```mermaid
sequenceDiagram
    actor Org as Người tổ chức
    participant UI as Màn hình Quản lý Lịch trình
    participant HT as Hệ thống
    participant DB as Cơ sở dữ liệu

    Org->>UI: 1. Nhấn nút "Xóa" bên cạnh một hoạt động
    UI-->>Org: 2. Hộp thoại xác nhận
    alt Nhấn "Hủy"
        Org->>UI: 3a. Nhấn "Hủy"
        UI-->>Org: 3b. Giữ nguyên
    else Xác nhận xóa
        Org->>UI: 3. Xác nhận xóa
        UI->>HT: 4. Gửi yêu cầu xóa lịch trình cụ thể (kèm Token)
        HT->>HT: 5. Kiểm tra quyền chủ sự kiện
        alt Không có quyền
            HT-->>UI: 6a. Từ chối truy cập
        else Có quyền
            HT->>DB: 6. Xóa lịch trình theo mã
            DB-->>HT: 7. Xóa thành công
            HT-->>UI: 8. Thông báo xóa thành công
            UI-->>Org: 9. Cập nhật lại timeline
        end
    end
```

---

## UC25 – Xem danh sách Nhân viên toàn hệ thống (Admin / Organizer)

```mermaid
sequenceDiagram
    actor User as Admin / Người tổ chức
    participant UI as Màn hình Quản lý Nhân viên
    participant HT as Hệ thống
    participant DB as Cơ sở dữ liệu

    User->>UI: 1. Truy cập trang danh sách nhân viên
    UI->>HT: 2. Yêu cầu danh sách nhân viên (kèm Token)
    HT->>HT: 3. Kiểm tra quyền (Admin hoặc Organizer)
    alt Không có quyền
        HT-->>UI: 4a. Từ chối truy cập
        UI-->>User: 4b. Thông báo không có quyền
    else Có quyền
        HT->>DB: 4. Truy vấn tất cả tài khoản có vai trò STAFF và đang ACTIVE
        DB-->>HT: 5. Trả về danh sách nhân viên (Họ tên, Email, SĐT, Ảnh đại diện)
        HT-->>UI: 6. Trả về danh sách
        UI-->>User: 7. Hiển thị danh sách nhân viên để phân công
    end
```

---

## UC26 – Xem Nhân viên được phân công vào sự kiện

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

## UC27 – Đồng bộ phân công Nhân viên (Organizer)

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

## UC28 – Xóa một Nhân viên khỏi sự kiện (Organizer)

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
