# BIỂU ĐỒ TUẦN TỰ - PHẦN A: XÁC THỰC & HỒ SƠ CÁ NHÂN (UC01 – UC09)

> **Quy ước ký hiệu:**
>
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

## UC03 – Đăng nhập bằng Google (OAuth 2.0)

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

## UC04 – Đăng xuất

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

## UC05 – Quên mật khẩu & Đặt lại mật khẩu

```mermaid
sequenceDiagram
    actor Guest as Khách (Guest)
    participant UI as Màn hình Khôi phục
    participant HT as Hệ thống
    participant DB as Cơ sở dữ liệu
    participant Mail as Dịch vụ Email

    Guest->>UI: 1. Nhập Email để nhận mã khôi phục
    UI->>HT: 2. Gửi yêu cầu lấy mã khôi phục
    HT->>DB: 3. Xóa mã cũ, sinh mã UUID mới, lưu kèm thời hạn 15 phút
    note over HT: Dù Email có tồn tại hay không,<br/>hệ thống luôn báo thành công để bảo mật
    HT->>Mail: 4. Gửi mã khôi phục qua email
    Mail-->>Guest: 5. Nhận email chứa mã khôi phục
    Guest->>UI: 6. Nhập mã khôi phục và mật khẩu mới
    UI->>HT: 7. Gửi yêu cầu đặt lại mật khẩu
    HT->>DB: 8. Kiểm tra mã hợp lệ và chưa hết hạn trong CSDL
    alt Mã không hợp lệ hoặc hết hạn
        DB-->>HT: 9a. Mã lỗi / hết hạn
        HT-->>UI: 9b. Báo lỗi và yêu cầu gửi lại mã
    else Hợp lệ
        HT->>HT: 9. Mã hóa mật khẩu mới (BCrypt)
        HT->>DB: 10. Cập nhật mật khẩu và xóa mã khôi phục
        DB-->>HT: 11. Cập nhật thành công
        HT-->>UI: 12. Báo đổi mật khẩu thành công
        UI-->>Guest: 13. Chuyển về màn hình đăng nhập
    end
```

---

## UC06 – Đổi mật khẩu

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

## UC07 – Xem thông tin cá nhân & Hồ sơ người dùng khác

```mermaid
sequenceDiagram
    actor User as Người dùng
    participant UI as Giao diện Hồ sơ
    participant HT as Hệ thống
    participant DB as Cơ sở dữ liệu

    User->>UI: 1. Truy cập trang cá nhân hoặc nhấn vào hồ sơ người khác
    UI->>HT: 2. Gọi API GET /users/profile (cá nhân) hoặc /users/info-user/{id} (người khác)
    HT->>DB: 3. Truy vấn tài khoản từ CSDL
    alt Không tìm thấy tài khoản
        DB-->>HT: 4a. Tài khoản không tồn tại
        HT-->>UI: 4b. Báo lỗi 404 USER_NOT_FOUND
    else Hồ sơ cá nhân
        HT->>HT: 4. Xác thực Token hợp lệ
        DB-->>HT: 5. Trả về toàn bộ thông tin chi tiết (Email, SĐT, Ngày sinh, v.v.)
    else Hồ sơ người khác
        DB-->>HT: 4. Trả về thực thể User
        HT->>HT: 5. Lọc bỏ thông tin nhạy cảm (ẩn email, sđt, password...)
        HT->>HT: 6. Chỉ giữ lại thông tin công khai (avatar, fullName...)
    end
    HT-->>UI: 7. Trả về dữ liệu JSON hồ sơ
    UI-->>User: 8. Hiển thị thông tin hồ sơ
```

---

## UC08 – Cập nhật thông tin cá nhân & Ảnh đại diện

```mermaid
sequenceDiagram
    actor User as Người dùng
    participant UI as Màn hình Hồ sơ
    participant HT as Hệ thống
    participant Cloud as Cloudinary
    participant DB as Cơ sở dữ liệu

    User->>UI: 1. Chỉnh sửa thông tin text hoặc tải lên ảnh đại diện mới
    UI->>HT: 2. Gọi API PUT thông tin text hoặc ảnh (kèm Token)
    
    alt Nếu cập nhật ảnh đại diện
        HT->>Cloud: 3a. Tải tệp ảnh lên Cloudinary
        alt Lỗi kết nối
            Cloud-->>HT: 3b. Lỗi tải ảnh
            HT-->>UI: 3c. Báo lỗi, giữ nguyên ảnh cũ
        else Tải thành công
            Cloud-->>HT: 3b. Trả về đường dẫn ảnh mới (URL)
        end
    else Nếu cập nhật thông tin text
        HT->>HT: 3. Kiểm tra tính hợp lệ dữ liệu (Họ tên không rỗng, SĐT chuẩn)
        alt Không hợp lệ
            HT-->>UI: 3d. Thông báo lỗi nhập liệu
        end
    end
    
    HT->>DB: 4. Cập nhật thông tin hoặc đường dẫn ảnh vào tài khoản
    DB-->>HT: 5. Lưu thành công
    HT-->>UI: 6. Trả về thông tin User đã cập nhật
    UI-->>User: 7. Giao diện hiển thị thay đổi mới
```

---

## UC09 – Tự khóa / xóa tài khoản (Soft Delete)

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
