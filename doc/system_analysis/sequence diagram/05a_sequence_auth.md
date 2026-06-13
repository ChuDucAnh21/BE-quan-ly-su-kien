# BIỂU ĐỒ TUẦN TỰ - PHẦN A: XÁC THỰC & HỒ SƠ CÁ NHÂN (UC01 – UC10)

> **Quy ước ký hiệu:**
> - **Actor**: Người dùng thực hiện hành động
> - **Màn hình**: Giao diện hiển thị với người dùng
> - **Hệ thống**: Xử lý logic nghiệp vụ phía máy chủ
> - **CSDL**: Cơ sở dữ liệu lưu trữ
> - **Dịch vụ ngoài**: Email, Cloudinary, Google OAuth

---

## UC01 – Đăng ký tài khoản

```mermaid
sequenceDiagram
    actor Guest as Khách (Guest)
    participant UI as Màn hình Đăng ký
    participant HT as Hệ thống
    participant DB as Cơ sở dữ liệu

    Guest->>UI: 1. Điền Họ tên, Email, Mật khẩu, SĐT
    UI->>HT: 2. Gửi yêu cầu đăng ký
    HT->>HT: 3. Kiểm tra định dạng Email (@gmail.com), Mật khẩu ≥ 6 ký tự, SĐT đúng chuẩn VN
    alt Thông tin không hợp lệ
        HT-->>UI: 4a. Thông báo lỗi validate
        UI-->>Guest: 4b. Hiển thị lỗi cụ thể
    else Thông tin hợp lệ
        HT->>DB: 4. Kiểm tra Email đã tồn tại chưa
        alt Email đã tồn tại
            DB-->>HT: 5a. Email đã có trong hệ thống
            HT-->>UI: 5b. Thông báo tài khoản đã tồn tại
            UI-->>Guest: 5c. Hiển thị lỗi
        else Email mới
            DB-->>HT: 5. Email chưa tồn tại
            HT->>HT: 6. Mã hóa mật khẩu (BCrypt)
            HT->>DB: 7. Lưu tài khoản mới (Vai trò: USER, Trạng thái: ACTIVE)
            DB-->>HT: 8. Lưu thành công
            HT-->>UI: 9. Đăng ký thành công
            UI-->>Guest: 10. Thông báo tạo tài khoản thành công
        end
    end
```

---

## UC02 – Đăng nhập hệ thống

```mermaid
sequenceDiagram
    actor Guest as Khách (Guest)
    participant UI as Màn hình Đăng nhập
    participant HT as Hệ thống
    participant DB as Cơ sở dữ liệu

    Guest->>UI: 1. Nhập Email và Mật khẩu
    UI->>HT: 2. Gửi yêu cầu đăng nhập
    HT->>DB: 3. Tìm tài khoản theo Email
    alt Email không tồn tại
        DB-->>HT: 4a. Không tìm thấy
        HT-->>UI: 4b. Thông báo sai thông tin đăng nhập
        UI-->>Guest: 4c. Hiển thị lỗi
    else Email tồn tại
        DB-->>HT: 4. Trả về thông tin tài khoản
        HT->>HT: 5. Đối chiếu mật khẩu với mã hóa đã lưu
        alt Mật khẩu sai
            HT-->>UI: 6a. Thông báo sai thông tin đăng nhập
        else Tài khoản bị khóa
            HT-->>UI: 6b. Thông báo tài khoản đang bị khóa
        else Hợp lệ
            HT->>HT: 6. Tạo Token xác thực (Access + Refresh)
            HT-->>UI: 7. Trả về Token và thông tin tài khoản
            UI-->>Guest: 8. Chuyển vào trang chính
        end
    end
```

---

## UC02a – Đăng nhập bằng Google (OAuth 2.0)

```mermaid
sequenceDiagram
    actor Guest as Khách (Guest)
    participant UI as Màn hình Đăng nhập
    participant HT as Hệ thống
    participant GG as Google OAuth 2.0
    participant DB as Cơ sở dữ liệu

    Guest->>UI: 1. Nhấn "Đăng nhập bằng Google"
    UI->>GG: 2. Chuyển hướng đến trang xác thực Google
    Guest->>GG: 3. Chọn tài khoản Google và đồng ý cấp quyền
    GG-->>UI: 4. Trả về mã xác thực (Authorization Code)
    UI->>HT: 5. Gửi mã xác thực lên hệ thống
    HT->>GG: 6. Dùng mã xác thực để lấy thông tin người dùng Google
    GG-->>HT: 7. Trả về Email, Họ tên, Ảnh đại diện
    HT->>DB: 8. Kiểm tra Email đã tồn tại trong hệ thống chưa
    alt Lần đầu đăng nhập
        DB-->>HT: 9a. Email chưa tồn tại
        HT->>DB: 9b. Tạo tài khoản mới (Vai trò: USER, authProvider: GOOGLE)
    else Đã có tài khoản
        DB-->>HT: 9. Email đã tồn tại
        HT->>HT: 10. Kiểm tra trạng thái tài khoản
        alt Tài khoản bị khóa
            HT-->>UI: 10a. Thông báo tài khoản bị khóa
            UI-->>Guest: 10b. Hiển thị lỗi
        else Hợp lệ
            HT->>DB: 10c. Hợp nhất tài khoản (Cập nhật authProvider=GOOGLE)
        end
    end
    HT->>HT: 11. Tạo Token xác thực
    HT-->>UI: 12. Trả về Token và thông tin tài khoản
    UI-->>Guest: 13. Chuyển vào trang chính
```

---

## UC03 – Đăng xuất

```mermaid
sequenceDiagram
    actor User as Người dùng
    participant UI as Giao diện
    participant HT as Hệ thống

    User->>UI: 1. Nhấn nút "Đăng xuất"
    UI->>HT: 2. Gửi yêu cầu đăng xuất
    HT->>HT: 3. Xóa phiên đăng nhập (xóa Cookie chứa Token)
    HT-->>UI: 4. Xác nhận đăng xuất thành công
    UI-->>User: 5. Chuyển về màn hình đăng nhập
```

---

## UC04 – Quên mật khẩu

```mermaid
sequenceDiagram
    actor Guest as Khách (Guest)
    participant UI as Màn hình Quên mật khẩu
    participant HT as Hệ thống
    participant DB as Cơ sở dữ liệu
    participant Mail as Dịch vụ Email

    Guest->>UI: 1. Nhấn "Quên mật khẩu", nhập Email
    UI->>HT: 2. Gửi yêu cầu khôi phục mật khẩu
    HT->>DB: 3. Tìm tài khoản theo Email
    note over HT: Dù Email có tồn tại hay không,<br/>hệ thống luôn trả về thành công<br/>(bảo mật, tránh dò quét Email)
    HT->>DB: 4. Xóa mã khôi phục cũ (nếu có)
    HT->>HT: 5. Tạo mã khôi phục ngẫu nhiên (UUID)
    HT->>DB: 6. Lưu mã khôi phục (hết hạn sau 15 phút)
    HT->>Mail: 7. Gửi Email chứa mã khôi phục
    Mail-->>HT: 8. Gửi Email thành công
    HT-->>UI: 9. Thông báo "Kiểm tra hộp thư Email"
    UI-->>Guest: 10. Hiển thị hướng dẫn
```

---

## UC05 – Đặt lại mật khẩu

```mermaid
sequenceDiagram
    actor Guest as Khách (Guest)
    participant UI as Màn hình Đặt lại mật khẩu
    participant HT as Hệ thống
    participant DB as Cơ sở dữ liệu

    Guest->>UI: 1. Nhập mã khôi phục (từ Email) và mật khẩu mới
    UI->>HT: 2. Gửi yêu cầu đặt lại mật khẩu
    HT->>DB: 3. Tìm mã khôi phục trong hệ thống
    alt Mã không tồn tại
        DB-->>HT: 4a. Không tìm thấy
        HT-->>UI: 4b. Thông báo mã không hợp lệ
        UI-->>Guest: 4c. Hiển thị lỗi
    else Mã đã hết hạn (quá 15 phút)
        DB-->>HT: 4. Tìm thấy nhưng đã hết hạn
        HT-->>UI: 5a. Thông báo mã đã hết hạn
        UI-->>Guest: 5b. Yêu cầu gửi lại mã
    else Mã hợp lệ
        DB-->>HT: 4. Mã hợp lệ, trả về thông tin tài khoản
        HT->>HT: 5. Mã hóa mật khẩu mới
        HT->>DB: 6. Cập nhật mật khẩu mới vào tài khoản
        HT->>DB: 7. Xóa mã khôi phục (chỉ dùng 1 lần)
        HT-->>UI: 8. Đặt lại mật khẩu thành công
        UI-->>Guest: 9. Chuyển về màn hình đăng nhập
    end
```

---

## UC06 – Đổi mật khẩu (khi đã đăng nhập)

```mermaid
sequenceDiagram
    actor User as Người dùng
    participant UI as Màn hình Cài đặt
    participant HT as Hệ thống
    participant DB as Cơ sở dữ liệu

    User->>UI: 1. Nhập mật khẩu cũ và mật khẩu mới
    UI->>HT: 2. Gửi yêu cầu đổi mật khẩu (kèm Token)
    HT->>DB: 3. Lấy thông tin mật khẩu hiện tại của tài khoản
    HT->>HT: 4. Đối chiếu mật khẩu cũ với mã hóa đã lưu
    alt Mật khẩu cũ không đúng
        HT-->>UI: 5a. Thông báo mật khẩu cũ không chính xác
        UI-->>User: 5b. Hiển thị lỗi
    else Hợp lệ
        HT->>HT: 5. Mã hóa mật khẩu mới
        HT->>DB: 6. Cập nhật mật khẩu mới
        DB-->>HT: 7. Lưu thành công
        HT-->>UI: 8. Đổi mật khẩu thành công
        UI-->>User: 9. Thông báo thành công
    end
```

---

## UC07 – Xem hồ sơ cá nhân

```mermaid
sequenceDiagram
    actor User as Người dùng
    participant UI as Màn hình Hồ sơ
    participant HT as Hệ thống
    participant DB as Cơ sở dữ liệu

    User->>UI: 1. Truy cập trang "Hồ sơ cá nhân"
    UI->>HT: 2. Yêu cầu lấy thông tin (kèm Token)
    HT->>HT: 3. Đọc danh tính từ Token
    HT->>DB: 4. Truy vấn thông tin tài khoản
    DB-->>HT: 5. Trả về thông tin (Họ tên, Email, SĐT, Ảnh, Vai trò, v.v.)
    HT-->>UI: 6. Trả về dữ liệu hồ sơ
    UI-->>User: 7. Hiển thị thông tin cá nhân
```

---

## UC08 – Cập nhật hồ sơ cá nhân

```mermaid
sequenceDiagram
    actor User as Người dùng
    participant UI as Màn hình Hồ sơ
    participant HT as Hệ thống
    participant DB as Cơ sở dữ liệu

    User->>UI: 1. Chỉnh sửa Họ tên, SĐT, Giới tính, Ngày sinh, Địa chỉ
    UI->>HT: 2. Gửi thông tin đã chỉnh sửa (kèm Token)
    HT->>HT: 3. Kiểm tra Họ tên không trống, SĐT đúng chuẩn VN
    alt Thông tin không hợp lệ
        HT-->>UI: 4a. Thông báo lỗi cụ thể
        UI-->>User: 4b. Hiển thị lỗi
    else Hợp lệ
        HT->>DB: 4. Cập nhật thông tin vào tài khoản
        DB-->>HT: 5. Lưu thành công
        HT-->>UI: 6. Trả về thông tin đã cập nhật
        UI-->>User: 7. Hiển thị thông tin mới
    end
```

---

## UC09 – Upload ảnh đại diện

```mermaid
sequenceDiagram
    actor User as Người dùng
    participant UI as Màn hình Hồ sơ
    participant HT as Hệ thống
    participant Cloud as Cloudinary (Lưu trữ ảnh)
    participant DB as Cơ sở dữ liệu

    User->>UI: 1. Nhấn "Đổi ảnh đại diện", chọn file ảnh
    UI->>HT: 2. Gửi file ảnh lên hệ thống (kèm Token)
    HT->>Cloud: 3. Tải ảnh lên dịch vụ lưu trữ đám mây
    alt Lỗi kết nối Cloudinary
        Cloud-->>HT: 4a. Lỗi tải ảnh
        HT-->>UI: 4b. Thông báo lỗi, giữ nguyên ảnh cũ
    else Tải thành công
        Cloud-->>HT: 4. Trả về đường dẫn ảnh mới
        HT->>DB: 5. Cập nhật đường dẫn ảnh đại diện
        DB-->>HT: 6. Lưu thành công
        HT-->>UI: 7. Trả về thông tin tài khoản với ảnh mới
        UI-->>User: 8. Hiển thị ảnh đại diện mới
    end
```

---

## UC10 – Tự khóa tài khoản

```mermaid
sequenceDiagram
    actor User as Người dùng
    participant UI as Màn hình Cài đặt
    participant HT as Hệ thống
    participant DB as Cơ sở dữ liệu

    User->>UI: 1. Nhấn "Xóa/Khóa tài khoản"
    UI-->>User: 2. Hiển thị hộp thoại xác nhận "Bạn có chắc chắn?"
    alt Người dùng nhấn "Hủy"
        User->>UI: 3a. Nhấn "Hủy"
        UI-->>User: 3b. Quay lại trang cài đặt
    else Người dùng xác nhận
        User->>UI: 3. Xác nhận khóa tài khoản
        UI->>HT: 4. Gửi yêu cầu khóa tài khoản (kèm Token)
        HT->>DB: 5. Chuyển trạng thái tài khoản thành LOCKED (không xóa dữ liệu)
        DB-->>HT: 6. Cập nhật thành công
        HT->>HT: 7. Xóa phiên đăng nhập hiện tại
        HT-->>UI: 8. Thông báo tài khoản đã bị khóa
        UI-->>User: 9. Chuyển về màn hình đăng nhập
    end
```

---

## UC10b – Xem hồ sơ công khai của người dùng khác

```mermaid
sequenceDiagram
    actor Guest as Người dùng/Khách
    participant UI as Giao diện
    participant HT as Hệ thống
    participant DB as Cơ sở dữ liệu

    Guest->>UI: 1. Nhấn vào tên/avatar của người khác
    UI->>HT: 2. Gọi API GET /users/info-user/{id}
    HT->>DB: 3. Truy vấn User theo ID
    alt Không tìm thấy User
        DB-->>HT: 4a. Không tìm thấy (null)
        HT-->>UI: 4b. Lỗi USER_NOT_FOUND
        UI-->>Guest: 4c. Hiển thị lỗi không tồn tại
    else Tìm thấy
        DB-->>HT: 4. Trả về thực thể User
        HT->>HT: 5. Lọc dữ liệu công khai (ẩn password, phone...)
        HT-->>UI: 6. Trả về thông tin công khai (avatarUrl, fullName...)
        UI-->>Guest: 7. Hiển thị hồ sơ công khai
    end
```
