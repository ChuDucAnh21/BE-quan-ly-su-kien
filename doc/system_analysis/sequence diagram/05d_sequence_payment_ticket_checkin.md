# BIỂU ĐỒ TUẦN TỰ - PHẦN D: BÁN VÉ, THANH TOÁN & CHECK-IN (UC16 – UC22)

---

## UC16 – Cấu hình cổng thanh toán (PayOS)

```mermaid
sequenceDiagram
    actor Organizer
    participant Màn hình
    participant Hệ thống
    participant CSDL

    Organizer->>Màn hình: Nhập API Key, Client ID của PayOS và nhấn Lưu
    Màn hình->>Hệ thống: Gửi dữ liệu cấu hình thanh toán
    Hệ thống->>Hệ thống: Kiểm tra tính toàn vẹn và mã hóa bảo mật
    Hệ thống->>CSDL: Lưu chuỗi cấu hình đã mã hóa
    CSDL-->>Hệ thống: Xác nhận
    Hệ thống-->>Màn hình: Trả về thành công
    Màn hình-->>Organizer: Báo cấu hình cổng thanh toán thành công
```

---

## UC17 – Đặt mua vé sự kiện

```mermaid
sequenceDiagram
    actor User
    participant Màn hình
    participant Hệ thống
    participant CSDL

    User->>Màn hình: Chọn số lượng vé và nhấn Mua vé
    Màn hình->>Hệ thống: Gửi yêu cầu đặt giữ chỗ
    Hệ thống->>CSDL: Kiểm tra sức chứa hiện tại (Transaction Lock)
    alt Sức chứa đã đầy
        CSDL-->>Hệ thống: Báo hết vé
        Hệ thống-->>Màn hình: Lỗi "Sự kiện đã bán hết vé"
        Màn hình-->>User: Hiển thị lỗi từ chối
    else Còn đủ vé
        Hệ thống->>CSDL: Tạm trừ sức chứa và tạo hóa đơn chờ
        CSDL-->>Hệ thống: Trả về thông tin hóa đơn
        alt Vé miễn phí
            Hệ thống->>CSDL: Sinh mã QR vé và đổi trạng thái Đã Thanh Toán
            Hệ thống-->>Màn hình: Chuyển hướng trang Thành công
            Màn hình-->>User: Hiển thị vé điện tử
        else Vé có phí
            Hệ thống-->>Màn hình: Trả về link thanh toán PayOS
            Màn hình-->>User: Chuyển hướng sang cổng thanh toán ngân hàng
        end
    end
```

---

## UC18 – Thanh toán vé sự kiện

```mermaid
sequenceDiagram
    actor User
    participant PayOS
    participant Hệ thống
    participant CSDL

    User->>PayOS: Quét mã QR chuyển khoản ngân hàng
    PayOS->>PayOS: Xử lý giao dịch
    PayOS-->>Hệ thống: Gửi tín hiệu Webhook báo có tiền
    Hệ thống->>Hệ thống: Xác minh chữ ký bảo mật của Webhook
    alt Chữ ký sai (Giao dịch giả)
        Hệ thống-->>PayOS: Báo lỗi và hủy bỏ
    else Chữ ký đúng
        Hệ thống->>CSDL: Cập nhật hóa đơn thành Đã Thanh Toán
        Hệ thống->>CSDL: Sinh mã QR vé điện tử
        CSDL-->>Hệ thống: Xác nhận
        Hệ thống-->>PayOS: Phản hồi tiếp nhận thành công
        Hệ thống-->>User: Socket/Giao diện tự chuyển sang trang "Đặt vé thành công"
    end
```

---

## UC19 – Xem danh sách vé cá nhân

```mermaid
sequenceDiagram
    actor User
    participant Màn hình
    participant Hệ thống
    participant CSDL

    User->>Màn hình: Truy cập trang "Vé của tôi"
    Màn hình->>Hệ thống: Yêu cầu lấy danh sách vé
    Hệ thống->>CSDL: Lấy các vé thuộc về User ID
    CSDL-->>Hệ thống: Trả về dữ liệu vé
    Hệ thống-->>Màn hình: Gửi dữ liệu hiển thị
    Màn hình-->>User: Hiển thị danh sách vé kèm mã QR để soát vé
```

---

## UC20 – Hủy vé đã đặt

```mermaid
sequenceDiagram
    actor User
    participant Màn hình
    participant Hệ thống
    participant CSDL

    User->>Màn hình: Nhấn Hủy vé
    Màn hình->>Hệ thống: Gửi yêu cầu hủy
    Hệ thống->>CSDL: Kiểm tra trạng thái thanh toán của vé
    alt Vé đã được trả tiền
        Hệ thống-->>Màn hình: Báo lỗi không cho tự động hủy
        Màn hình-->>User: Hiển thị "Vui lòng liên hệ BTC để hoàn tiền"
    else Vé miễn phí hoặc chưa thanh toán
        Hệ thống->>CSDL: Đổi trạng thái vé sang Đã Hủy
        Hệ thống->>CSDL: Nhả lại sức chứa (Cộng lại vé)
        CSDL-->>Hệ thống: Xác nhận
        Hệ thống-->>Màn hình: Báo thành công
        Màn hình-->>User: Thông báo vé đã được hủy thành công
    end
```

---

## UC21 – Quét mã QR soát vé (Check-in)

```mermaid
sequenceDiagram
    actor Staff
    participant Màn hình
    participant Hệ thống
    participant CSDL

    Staff->>Màn hình: Dùng thiết bị quét mã QR của khách
    Màn hình->>Hệ thống: Gửi mã QR cần kiểm tra
    Hệ thống->>CSDL: Truy vấn thông tin vé
    alt Vé sai / Không có / Sai sự kiện
        CSDL-->>Hệ thống: Không tìm thấy vé
        Hệ thống-->>Màn hình: Báo lỗi vé không hợp lệ
        Màn hình-->>Staff: Hiển thị màn hình đỏ (Từ chối)
    else Vé hợp lệ
        Hệ thống->>Hệ thống: Kiểm tra trạng thái vé
        alt Vé đã Check-in trước đó
            Hệ thống-->>Màn hình: Báo lỗi vé đã sử dụng
            Màn hình-->>Staff: Hiển thị màn hình đỏ "Vé đã sử dụng"
        else Vé hợp lệ và chưa dùng
            Hệ thống->>CSDL: Đổi trạng thái vé sang "Đã sử dụng"
            Hệ thống->>CSDL: Ghi log thời gian soát vé
            CSDL-->>Hệ thống: Xác nhận
            Hệ thống-->>Màn hình: Báo thành công
            Màn hình-->>Staff: Hiển thị màn hình xanh (Cho qua)
        end
    end
```

---

## UC22 – Xem lịch sử Check-in

```mermaid
sequenceDiagram
    actor Organizer
    participant Màn hình
    participant Hệ thống
    participant CSDL

    Organizer->>Màn hình: Mở trang Nhật ký Check-in
    Màn hình->>Hệ thống: Yêu cầu danh sách khách đã vào cổng
    Hệ thống->>CSDL: Truy xuất log soát vé của sự kiện
    CSDL-->>Hệ thống: Trả về danh sách (Tên khách, Thời gian, Người quét)
    Hệ thống-->>Màn hình: Gửi dữ liệu hiển thị
    Màn hình-->>Organizer: Hiển thị danh sách khách
```
