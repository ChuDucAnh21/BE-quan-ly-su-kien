# BIỂU ĐỒ TUẦN TỰ - PHẦN A: NỀN TẢNG HỆ THỐNG (UC01 – UC08)

---

## UC01 – Đăng ký tài khoản

```mermaid
sequenceDiagram
    actor Guest
    participant Màn hình
    participant Hệ thống
    participant CSDL

    Guest->>Màn hình: Nhập thông tin & Nhấn Đăng ký
    Màn hình->>Màn hình: Kiểm tra định dạng dữ liệu
    Màn hình->>Hệ thống: Gửi yêu cầu đăng ký
    Hệ thống->>CSDL: Kiểm tra Email tồn tại
    alt Email đã tồn tại
        CSDL-->>Hệ thống: Báo trùng lặp
        Hệ thống-->>Màn hình: Trả về lỗi "Email đã được sử dụng"
        Màn hình-->>Guest: Hiển thị thông báo lỗi
    else Email chưa tồn tại
        Hệ thống->>Hệ thống: Mã hóa mật khẩu
        Hệ thống->>CSDL: Lưu thông tin tài khoản mới
        CSDL-->>Hệ thống: Trả về kết quả thành công
        Hệ thống-->>Màn hình: Trả về thành công
        Màn hình-->>Guest: Báo thành công và chuyển Đăng nhập
    end
```

---

## UC02 – Đăng nhập hệ thống

```mermaid
sequenceDiagram
    actor Guest
    participant Màn hình
    participant Google
    participant Hệ thống
    participant CSDL

    Guest->>Màn hình: Chọn phương thức đăng nhập
    alt Đăng nhập bằng Email
        Guest->>Màn hình: Nhập Email, Mật khẩu
        Màn hình->>Hệ thống: Gửi thông tin đăng nhập
        Hệ thống->>CSDL: Kiểm tra tài khoản
        alt Sai thông tin
            Hệ thống-->>Màn hình: Báo lỗi sai tài khoản
            Màn hình-->>Guest: Hiển thị lỗi "Sai tài khoản/mật khẩu"
        else Đúng thông tin
            Hệ thống->>Hệ thống: Xác nhận hợp lệ
        end
    else Đăng nhập bằng Google
        Guest->>Màn hình: Nhấn "Đăng nhập với Google"
        Màn hình->>Google: Yêu cầu xác thực Google
        Google-->>Màn hình: Trả về thông tin (Email, Tên, Avatar)
        Màn hình->>Hệ thống: Gửi dữ liệu tài khoản Google
        Hệ thống->>CSDL: Kiểm tra Email đã tồn tại chưa
        alt Email chưa tồn tại
            Hệ thống->>CSDL: Tự động đăng ký tài khoản mới
        end
    end

    Hệ thống->>Hệ thống: Kiểm tra trạng thái tài khoản
    alt Tài khoản bị khóa
        Hệ thống-->>Màn hình: Báo lỗi tài khoản bị khóa
        Màn hình-->>Guest: Hiển thị lỗi "Tài khoản đang bị khóa"
    else Đang hoạt động
        Hệ thống->>Hệ thống: Khởi tạo phiên làm việc (Session/Token)
        Hệ thống-->>Màn hình: Trả về trạng thái thành công
        Màn hình-->>Guest: Chuyển hướng vào Trang chủ
    end
```

---

## UC03 – Đăng xuất

```mermaid
sequenceDiagram
    actor Actor
    participant Màn hình
    participant Hệ thống

    Actor->>Màn hình: Nhấn nút Đăng xuất
    Màn hình->>Hệ thống: Yêu cầu xóa phiên làm việc
    Hệ thống->>Hệ thống: Hủy Session/Token hiện tại
    Hệ thống-->>Màn hình: Xác nhận thành công
    Màn hình->>Màn hình: Xóa Cookie trình duyệt
    Màn hình-->>Actor: Chuyển hướng về trang Đăng nhập
```

---

## UC04 – Quên mật khẩu

```mermaid
sequenceDiagram
    actor Guest
    participant Màn hình
    participant Hệ thống
    participant CSDL

    Guest->>Màn hình: Nhập Email yêu cầu khôi phục
    Màn hình->>Hệ thống: Gửi yêu cầu Quên mật khẩu
    Hệ thống->>Hệ thống: Sinh mã OTP
    Hệ thống->>Guest: Gửi mã OTP qua Email
    Guest->>Màn hình: Nhập OTP và Mật khẩu mới
    Màn hình->>Hệ thống: Gửi OTP và Mật khẩu mới
    Hệ thống->>Hệ thống: Kiểm tra OTP hợp lệ và còn hạn
    alt OTP sai hoặc hết hạn
        Hệ thống-->>Màn hình: Báo lỗi OTP
        Màn hình-->>Guest: Hiển thị lỗi
    else OTP hợp lệ
        Hệ thống->>Hệ thống: Mã hóa Mật khẩu mới
        Hệ thống->>CSDL: Cập nhật Mật khẩu
        Hệ thống-->>Màn hình: Xác nhận thành công
        Màn hình-->>Guest: Báo thành công
    end
```

---

## UC05 – Đổi mật khẩu

```mermaid
sequenceDiagram
    actor Actor
    participant Màn hình
    participant Hệ thống
    participant CSDL

    Actor->>Màn hình: Nhập mật khẩu cũ & mới
    Màn hình->>Hệ thống: Yêu cầu đổi mật khẩu
    Hệ thống->>CSDL: Lấy mật khẩu cũ
    Hệ thống->>Hệ thống: Đối chiếu mật khẩu
    alt Mật khẩu cũ sai
        Hệ thống-->>Màn hình: Trả về lỗi sai mật khẩu
        Màn hình-->>Actor: Hiển thị lỗi
    else Mật khẩu cũ đúng
        Hệ thống->>Hệ thống: Mã hóa mật khẩu mới
        Hệ thống->>CSDL: Cập nhật mật khẩu mới
        Hệ thống-->>Màn hình: Trả về thành công
        Màn hình-->>Actor: Thông báo thành công
    end
```

---

## UC06 – Cập nhật hồ sơ cá nhân

```mermaid
sequenceDiagram
    actor Actor
    participant Màn hình
    participant Hệ thống
    participant Server_Ảnh
    participant CSDL

    Actor->>Màn hình: Nhập thông tin & Ảnh mới (Nếu có)
    Màn hình->>Hệ thống: Gửi yêu cầu cập nhật hồ sơ
    alt Có tải ảnh lên
        Hệ thống->>Server_Ảnh: Đẩy file ảnh lên máy chủ
        Server_Ảnh-->>Hệ thống: Trả về URL ảnh
    end
    Hệ thống->>CSDL: Lưu thông tin & URL ảnh mới
    CSDL-->>Hệ thống: Xác nhận lưu
    Hệ thống-->>Màn hình: Trả về dữ liệu mới
    Màn hình-->>Actor: Báo thành công và cập nhật giao diện
```

---

## UC07 – Quản lý tài khoản người dùng

```mermaid
sequenceDiagram
    actor Admin
    participant Màn hình
    participant Hệ thống
    participant CSDL

    Admin->>Màn hình: Chọn tài khoản và thao tác (Khóa/Cấp quyền)
    Màn hình->>Hệ thống: Gửi yêu cầu thay đổi trạng thái
    Hệ thống->>Hệ thống: Xác minh quyền thao tác (Không thao tác lên chính mình)
    alt Không hợp lệ
        Hệ thống-->>Màn hình: Báo lỗi thao tác trái phép
        Màn hình-->>Admin: Hiển thị lỗi
    else Hợp lệ
        Hệ thống->>CSDL: Cập nhật trạng thái/Role
        CSDL-->>Hệ thống: Xác nhận cập nhật
        Hệ thống-->>Màn hình: Trả về thành công
        Màn hình-->>Admin: Báo thành công và tải lại danh sách
    end
```

---

## UC08 – Xem báo cáo thống kê tổng quan (Dashboard)

```mermaid
sequenceDiagram
    actor Actor
    participant Màn hình
    participant Hệ thống
    participant CSDL

    Actor->>Màn hình: Truy cập trang Dashboard
    Màn hình->>Hệ thống: Yêu cầu lấy dữ liệu thống kê
    Hệ thống->>Hệ thống: Xác minh Role (Admin hay Organizer)
    Hệ thống->>CSDL: Truy vấn số liệu nền tảng hoặc cá nhân
    CSDL-->>Hệ thống: Trả về các con số (User, Event, Doanh thu)
    Hệ thống-->>Màn hình: Gửi dữ liệu thống kê
    Màn hình-->>Actor: Vẽ biểu đồ trực quan
```
