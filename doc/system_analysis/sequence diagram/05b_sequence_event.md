# BIỂU ĐỒ TUẦN TỰ - PHẦN B: QUẢN TRỊ HỆ THỐNG & SỰ KIỆN (UC11 – UC19)

---

## UC11 – Xem danh sách người dùng (Admin)

```mermaid
sequenceDiagram
    actor Admin as Admin / Organizer
    participant UI as Màn hình Quản lý Người dùng
    participant HT as Hệ thống
    participant DB as Cơ sở dữ liệu

    Admin->>UI: 1. Truy cập trang "Quản lý người dùng/Staff"
    UI->>HT: 2. Yêu cầu danh sách người dùng (kèm Token)
    HT->>HT: 3. Kiểm tra quyền (Admin hoặc Organizer)
    alt Không có quyền
        HT-->>UI: 4a. Từ chối truy cập (403)
        UI-->>Admin: 4b. Thông báo không có quyền
    else Có quyền (Organizer)
        HT->>HT: 4c. Ép buộc tham số role = STAFF
    end
    HT->>DB: 4. Truy vấn danh sách người dùng (có phân trang, lọc theo điều kiện)
    DB-->>HT: 5. Trả về danh sách người dùng
    HT-->>UI: 6. Trả về dữ liệu có phân trang
    UI-->>Admin: 7. Hiển thị bảng danh sách

    note over Admin,UI: Admin có thể lọc theo:<br/>Vai trò (ADMIN/ORGANIZER/STAFF/USER)<br/>Trạng thái (ACTIVE/LOCKED)<br/>Từ khóa tìm kiếm
```

---

## UC12 – Khóa / Mở khóa tài khoản (Admin)

```mermaid
sequenceDiagram
    actor Admin as Quản trị viên
    participant UI as Màn hình Quản lý Người dùng
    participant HT as Hệ thống
    participant DB as Cơ sở dữ liệu

    Admin->>UI: 1. Nhấn nút "Khóa" hoặc "Mở khóa" bên cạnh tài khoản
    UI->>HT: 2. Gửi yêu cầu thay đổi trạng thái tài khoản (kèm Token Admin)
    HT->>HT: 3. Kiểm tra tài khoản đích có phải chính Admin đang thao tác không
    alt Admin tự khóa chính mình
        HT-->>UI: 4a. Từ chối - không được tự khóa tài khoản của mình
        UI-->>Admin: 4b. Hiển thị thông báo lỗi
    else Hợp lệ
        HT->>DB: 4. Cập nhật trạng thái tài khoản (ACTIVE ↔ LOCKED)
        DB-->>HT: 5. Lưu thành công
        HT-->>UI: 6. Cập nhật trạng thái thành công
        UI-->>Admin: 7. Cập nhật lại trạng thái trong bảng danh sách
    end
```

---

## UC12b – Xóa tài khoản vĩnh viễn (Admin)

```mermaid
sequenceDiagram
    actor Admin as Quản trị viên (Super Admin)
    participant UI as Màn hình Quản lý Người dùng
    participant HT as Hệ thống
    participant DB as Cơ sở dữ liệu

    Admin->>UI: 1. Chọn "Xóa vĩnh viễn" tài khoản
    UI-->>Admin: 2. Hiển thị cảnh báo mất dữ liệu
    Admin->>UI: 3. Xác nhận xóa
    UI->>HT: 4. Gửi yêu cầu xóa Hard Delete (kèm Token)
    HT->>DB: 5. Thực thi Hard Delete khỏi CSDL
    alt Có ràng buộc khóa ngoại (User đã mua vé/tổ chức SK)
        DB-->>HT: 6a. Lỗi khóa ngoại (Foreign Key Constraint)
        HT-->>UI: 6b. Báo lỗi không thể xóa
    else Thành công
        DB-->>HT: 6. Đã xóa bản ghi
        HT-->>UI: 7. Trả về thành công
        UI-->>Admin: 8. Cập nhật lại danh sách
    end
```

---

## UC13 – Phân quyền người dùng (Admin)

```mermaid
sequenceDiagram
    actor Admin as Quản trị viên
    participant UI as Màn hình Quản lý Người dùng
    participant HT as Hệ thống
    participant DB as Cơ sở dữ liệu

    Admin->>UI: 1. Chọn tài khoản cần phân quyền, chọn vai trò mới
    UI->>HT: 2. Gửi yêu cầu thay đổi vai trò (kèm Token Admin)
    HT->>HT: 3. Kiểm tra tài khoản đích có phải chính Admin đang thao tác không
    alt Admin tự đổi vai trò của mình
        HT-->>UI: 4a. Từ chối - không được tự thay đổi vai trò của mình
        UI-->>Admin: 4b. Hiển thị thông báo lỗi
    else Hợp lệ
        HT->>DB: 4. Cập nhật vai trò mới (USER / STAFF / ORGANIZER / ADMIN)
        DB-->>HT: 5. Lưu thành công
        HT-->>UI: 6. Phân quyền thành công
        UI-->>Admin: 7. Cập nhật lại vai trò trong bảng danh sách
    end
```

---

## UC13a – Xem Dashboard thống kê tổng quan (Admin)

```mermaid
sequenceDiagram
    actor Admin as Quản trị viên
    participant UI as Màn hình Dashboard
    participant HT as Hệ thống
    participant DB as Cơ sở dữ liệu

    Admin->>UI: 1. Truy cập trang Dashboard
    UI->>HT: 2. Yêu cầu dữ liệu thống kê tổng quan (kèm Token Admin)
    HT->>DB: 3. Đếm tổng số người dùng theo từng vai trò
    HT->>DB: 4. Đếm tổng số sự kiện theo từng trạng thái
    HT->>DB: 5. Tính tổng doanh thu toàn hệ thống (các giao dịch thành công)
    HT->>DB: 6. Đếm tổng số vé đã bán và số lượt check-in
    HT->>HT: 7. Tính tỷ lệ check-in = (Vé đã dùng / Tổng vé) × 100%
    DB-->>HT: 8. Trả về các số liệu
    HT-->>UI: 9. Trả về dữ liệu Dashboard
    UI-->>Admin: 10. Hiển thị các biểu đồ và chỉ số thống kê
```

---

## UC13b – Xem thống kê chi tiết sự kiện (Admin / Organizer)

```mermaid
sequenceDiagram
    actor User as Admin / Người tổ chức
    participant UI as Màn hình Thống kê Sự kiện
    participant HT as Hệ thống
    participant DB as Cơ sở dữ liệu

    User->>UI: 1. Truy cập trang thống kê của sự kiện
    UI->>HT: 2. Yêu cầu dữ liệu thống kê sự kiện (kèm Token)
    HT->>HT: 3. Kiểm tra quyền (Admin hoặc Người tổ chức là chủ sự kiện)
    alt Không có quyền
        HT-->>UI: 4a. Từ chối truy cập
        UI-->>User: 4b. Thông báo không có quyền
    else Có quyền
        HT->>DB: 4. Đếm tổng đăng ký, tổng vé đã bán
        HT->>DB: 5. Tính doanh thu sự kiện (tổng giao dịch thành công)
        HT->>DB: 6. Đếm số đã check-in, số chưa check-in
        HT->>HT: 7. Tính tỷ lệ tham gia, số chỗ còn lại
        DB-->>HT: 8. Trả về số liệu
        HT-->>UI: 9. Trả về báo cáo chi tiết
        UI-->>User: 10. Hiển thị báo cáo sự kiện
    end
```

---

## UC14 – Xem danh sách sự kiện (Public)

```mermaid
sequenceDiagram
    actor Anyone as Bất kỳ ai
    participant UI as Màn hình Danh sách Sự kiện
    participant HT as Hệ thống
    participant DB as Cơ sở dữ liệu

    Anyone->>UI: 1. Truy cập trang sự kiện (có thể lọc theo từ khóa, danh mục, trạng thái)
    UI->>HT: 2. Yêu cầu danh sách sự kiện
    HT->>HT: 3. Nếu là Khách chưa đăng nhập, tự động chỉ lấy sự kiện đã công bố (PUBLISHED)
    HT->>DB: 4. Truy vấn danh sách sự kiện theo điều kiện lọc (có phân trang)
    DB-->>HT: 5. Trả về danh sách sự kiện
    HT-->>UI: 6. Trả về dữ liệu
    UI-->>Anyone: 7. Hiển thị danh sách sự kiện
```

---

## UC15 – Xem chi tiết sự kiện

```mermaid
sequenceDiagram
    actor Anyone as Bất kỳ ai
    participant UI as Màn hình Chi tiết Sự kiện
    participant HT as Hệ thống
    participant DB as Cơ sở dữ liệu

    Anyone->>UI: 1. Nhấn vào một sự kiện trong danh sách
    UI->>HT: 2. Yêu cầu chi tiết sự kiện
    HT->>DB: 3. Tìm sự kiện và lịch trình con theo mã sự kiện
    alt Sự kiện không tồn tại
        DB-->>HT: 4a. Không tìm thấy
        HT-->>UI: 4b. Thông báo sự kiện không tồn tại
        UI-->>Anyone: 4c. Hiển thị trang lỗi 404
    else Tìm thấy
        DB-->>HT: 4. Trả về thông tin sự kiện và danh sách lịch trình (sắp xếp theo thời gian)
        HT-->>UI: 5. Trả về đầy đủ thông tin
        UI-->>Anyone: 6. Hiển thị chi tiết sự kiện kèm lịch trình
    end
```

---

## UC16 – Tạo sự kiện mới (Organizer)

```mermaid
sequenceDiagram
    actor Org as Người tổ chức
    participant UI as Màn hình Tạo Sự kiện
    participant HT as Hệ thống
    participant Cloud as Cloudinary (Lưu ảnh)
    participant DB as Cơ sở dữ liệu

    Org->>UI: 1. Điền thông tin sự kiện VÀ danh sách lịch trình con (schedules)
    Org->>UI: 2. Chọn ảnh banner (tùy chọn)
    Org->>UI: 3. Nhấn "Tạo sự kiện"
    UI->>HT: 4. Gửi payload sự kiện (kèm mảng lịch trình và Token)
    HT->>HT: 5. Kiểm tra dữ liệu validate (Sức chứa, Giá vé, Thời gian)
    alt Thông tin không hợp lệ
        HT-->>UI: 6a. Thông báo lỗi validate
        UI-->>Org: 6b. Hiển thị lỗi
    else Có ảnh banner
        HT->>Cloud: 6. Tải ảnh lên Cloudinary
        Cloud-->>HT: 7. Trả về đường dẫn ảnh
    end
    HT->>DB: 8. Lưu Event (DRAFT, soldTickets=0) và đồng loạt lưu mảng EventSchedule
    DB-->>HT: 9. Lưu thành công
    HT-->>UI: 10. Trả về thông tin sự kiện và lịch trình vừa tạo
    UI-->>Org: 11. Chuyển đến trang sự kiện
```

---

## UC17 – Cập nhật sự kiện (Organizer)

```mermaid
sequenceDiagram
    actor Org as Người tổ chức (chủ sự kiện)
    participant UI as Màn hình Chỉnh sửa Sự kiện
    participant HT as Hệ thống
    participant DB as Cơ sở dữ liệu

    Org->>UI: 1. Chỉnh sửa thông tin sự kiện VÀ danh sách lịch trình con
    Org->>UI: 2. Nhấn "Lưu thay đổi"
    UI->>HT: 3. Gửi payload sự kiện (kèm mảng lịch trình và Token)
    HT->>DB: 4. Tìm sự kiện theo mã
    HT->>HT: 5. Kiểm tra người gửi có phải chủ sự kiện không
    alt Không phải chủ sự kiện
        HT-->>UI: 6a. Từ chối truy cập
        UI-->>Org: 6b. Thông báo lỗi
    else Sức chứa mới nhỏ hơn số vé đã bán
        HT-->>UI: 6c. Báo lỗi sức chứa
    else Hợp lệ
        HT->>DB: 6. Cập nhật Event và đồng bộ mảng EventSchedule (Xóa cũ, Thêm mới, Sửa)
        DB-->>HT: 7. Lưu thành công
        HT-->>UI: 8. Trả về thông tin đã cập nhật
        UI-->>Org: 9. Hiển thị thông tin sự kiện mới
    end
```

---

## UC18 – Đổi trạng thái sự kiện (Organizer / Admin)

```mermaid
sequenceDiagram
    actor User as Người tổ chức / Admin
    participant UI as Màn hình Quản lý Sự kiện
    participant HT as Hệ thống
    participant DB as Cơ sở dữ liệu

    User->>UI: 1. Chọn trạng thái mới (DRAFT → PUBLISHED / CLOSED / CANCELLED / COMPLETED)
    UI->>HT: 2. Gửi yêu cầu đổi trạng thái (kèm Token)
    HT->>DB: 3. Tìm sự kiện và thông tin người tổ chức
    HT->>HT: 4. Kiểm tra quyền (Organizer là chủ sự kiện, hoặc Admin)
    alt Chuyển sang PUBLISHED và sự kiện có phí
        HT->>DB: 5. Kiểm tra cấu hình cổng thanh toán PayOS
        alt Chưa cấu hình PayOS
            DB-->>HT: 5a. Chưa có cấu hình
            HT-->>UI: 5b. Yêu cầu thiết lập PayOS trước khi công bố
            UI-->>User: 5c. Chuyển đến trang cài đặt thanh toán
        end
    end
    HT->>DB: 6. Cập nhật trạng thái sự kiện
    DB-->>HT: 7. Lưu thành công
    HT-->>UI: 8. Thông báo cập nhật trạng thái thành công
    UI-->>User: 9. Hiển thị trạng thái mới
```

---

## UC19 – Xóa sự kiện (Organizer / Admin)

```mermaid
sequenceDiagram
    actor User as Người tổ chức / Admin
    participant UI as Màn hình Quản lý Sự kiện
    participant HT as Hệ thống
    participant DB as Cơ sở dữ liệu

    User->>UI: 1. Nhấn "Xóa sự kiện"
    UI-->>User: 2. Hộp thoại xác nhận "Bạn có chắc muốn xóa?"
    alt Nhấn "Hủy"
        User->>UI: 3a. Nhấn "Hủy"
        UI-->>User: 3b. Đóng hộp thoại
    else Xác nhận xóa
        User->>UI: 3. Xác nhận xóa
        UI->>HT: 4. Gửi yêu cầu xóa sự kiện (kèm Token)
        HT->>DB: 5. Kiểm tra số vé đã bán của sự kiện
        alt Chưa có vé nào được bán (soldTickets = 0)
            DB-->>HT: 6a. soldTickets = 0
            HT->>DB: 6b. Xóa hoàn toàn sự kiện khỏi hệ thống
            HT-->>UI: 6c. Thông báo xóa thành công
        else Đã có vé được bán
            DB-->>HT: 6. soldTickets > 0
            HT->>DB: 7. Chuyển trạng thái sự kiện sang CANCELLED (giữ lại dữ liệu)
            HT-->>UI: 8. Thông báo sự kiện đã bị hủy
        end
        UI-->>User: 9. Cập nhật danh sách sự kiện
    end
```
