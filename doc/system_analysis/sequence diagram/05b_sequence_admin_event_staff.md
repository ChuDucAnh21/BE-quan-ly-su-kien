# BIỂU ĐỒ TUẦN TỰ - PHẦN B: QUẢN LÝ SỰ KIỆN (UC09 – UC14)

---

## UC09 – Thêm mới sự kiện

```mermaid
sequenceDiagram
    actor Organizer
    participant Màn hình
    participant Hệ thống
    participant CSDL

    Organizer->>Màn hình: Nhập dữ liệu sự kiện (thời gian, hình ảnh, giá)
    Màn hình->>Hệ thống: Gửi yêu cầu tạo sự kiện
    Hệ thống->>Hệ thống: Kiểm tra mốc thời gian
    alt Lỗi mốc thời gian
        Hệ thống-->>Màn hình: Báo lỗi thời gian không hợp lệ
        Màn hình-->>Organizer: Hiển thị thông báo lỗi
    else Hợp lệ
        Hệ thống->>CSDL: Lưu thông tin sự kiện với trạng thái Bản nháp
        CSDL-->>Hệ thống: Xác nhận tạo thành công
        Hệ thống-->>Màn hình: Trả về kết quả
        Màn hình-->>Organizer: Báo thành công
    end
```

---

## UC10 – Cập nhật thông tin sự kiện

```mermaid
sequenceDiagram
    actor Organizer
    participant Màn hình
    participant Hệ thống
    participant CSDL

    Organizer->>Màn hình: Sửa thông tin và nhấn Lưu
    Màn hình->>Hệ thống: Gửi dữ liệu cập nhật
    Hệ thống->>CSDL: Lấy thông tin sự kiện
    Hệ thống->>Hệ thống: Kiểm tra quyền sở hữu và số lượng sức chứa
    alt Sức chứa nhỏ hơn số vé đã bán
        Hệ thống-->>Màn hình: Báo lỗi sức chứa
        Màn hình-->>Organizer: Hiển thị lỗi
    else Hợp lệ
        Hệ thống->>CSDL: Cập nhật dữ liệu mới
        CSDL-->>Hệ thống: Xác nhận
        Hệ thống-->>Màn hình: Trả về thành công
        Màn hình-->>Organizer: Báo thành công
    end
```

---

## UC11 – Xóa / Hủy sự kiện

```mermaid
sequenceDiagram
    actor Organizer
    participant Màn hình
    participant Hệ thống
    participant CSDL

    Organizer->>Màn hình: Nhấn nút Xóa sự kiện
    Màn hình->>Hệ thống: Gửi yêu cầu xóa
    Hệ thống->>CSDL: Kiểm tra số lượng vé đã bán
    alt Chưa bán vé nào
        Hệ thống->>CSDL: Xóa vĩnh viễn
        CSDL-->>Hệ thống: Xác nhận
    else Đã bán vé
        Hệ thống->>CSDL: Đổi trạng thái thành Đã Hủy
        CSDL-->>Hệ thống: Xác nhận
    end
    Hệ thống-->>Màn hình: Trả về thành công
    Màn hình-->>Organizer: Thông báo thành công
```

---

## UC12 – Thay đổi trạng thái sự kiện

```mermaid
sequenceDiagram
    actor Organizer
    participant Màn hình
    participant Hệ thống
    participant CSDL

    Organizer->>Màn hình: Chọn trạng thái sự kiện mới (Public, Closed, v.v.)
    Màn hình->>Hệ thống: Gửi yêu cầu cập nhật trạng thái
    Hệ thống->>CSDL: Kiểm tra thông tin sự kiện
    alt Đổi sang Public VÀ có thu phí VÀ chưa cấu hình PayOS
        Hệ thống-->>Màn hình: Báo lỗi thiếu cấu hình
        Màn hình-->>Organizer: Cảnh báo "Yêu cầu cài đặt cổng thanh toán trước khi công bố"
    else Hợp lệ
        Hệ thống->>CSDL: Đổi trạng thái sự kiện sang giá trị mới
        CSDL-->>Hệ thống: Xác nhận cập nhật
        Hệ thống-->>Màn hình: Trả về thành công
        Màn hình-->>Organizer: Thông báo cập nhật trạng thái thành công
    end
```

---

## UC13 – Tra cứu / Tìm kiếm sự kiện

```mermaid
sequenceDiagram
    actor Guest
    participant Màn hình
    participant Hệ thống
    participant CSDL

    Guest->>Màn hình: Mở trang tìm kiếm và nhập từ khóa/bộ lọc
    Màn hình->>Hệ thống: Gửi tham số tìm kiếm (keyword, category)
    Hệ thống->>CSDL: Truy vấn các sự kiện Đã công bố
    CSDL-->>Hệ thống: Trả về danh sách kết quả
    Hệ thống-->>Màn hình: Trả về dữ liệu
    Màn hình-->>Guest: Hiển thị danh sách kết quả
```

---

## UC14 – Xem chi tiết sự kiện

```mermaid
sequenceDiagram
    actor Guest
    participant Màn hình
    participant Hệ thống
    participant CSDL

    Guest->>Màn hình: Nhấn vào 1 sự kiện
    Màn hình->>Hệ thống: Gửi yêu cầu lấy chi tiết
    Hệ thống->>CSDL: Truy xuất dữ liệu
    alt Sự kiện không tồn tại hoặc bị ẩn
        CSDL-->>Hệ thống: Trả về dữ liệu rỗng
        Hệ thống-->>Màn hình: Báo lỗi không tìm thấy
        Màn hình-->>Guest: Thông báo "Sự kiện không tồn tại"
    else Tồn tại
        CSDL-->>Hệ thống: Trả về chi tiết (lịch trình, giá vé)
        Hệ thống-->>Màn hình: Trả về dữ liệu
        Màn hình-->>Guest: Vẽ giao diện chi tiết sự kiện
    end
```
