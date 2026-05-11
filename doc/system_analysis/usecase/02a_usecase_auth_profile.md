# ĐẶC TẢ USE CASE - PHẦN A: XÁC THỰC & HỒ SƠ CÁ NHÂN (UC01 – UC10)

---

## UC01 – Đăng ký tài khoản

| Trường | Nội dung |
|--------|----------|
| **Tên Use Case** | Đăng ký tài khoản |
| **Tác nhân** | Guest (Khách chưa đăng nhập) |
| **Mục đích** | Cho phép người dùng mới tạo tài khoản để sử dụng các chức năng của hệ thống |
| **Mức độ ưu tiên** | Cao |
| **Điều kiện kích hoạt** | Guest nhấn nút "Đăng ký" trên giao diện và điền đầy đủ thông tin |
| **Điều kiện tiên quyết** | Hệ thống đang hoạt động. Email đăng ký chưa tồn tại trong hệ thống |
| **Điều kiện thất bại** | Tài khoản không được tạo. Hệ thống hiển thị thông báo lỗi cụ thể cho người dùng |
| **Điều kiện thành công** | Tài khoản mới được tạo trong CSDL với Role = USER, Status = ACTIVE. Mật khẩu được mã hóa BCrypt |
| **Luồng sự kiện chính** | 1. Guest truy cập trang đăng ký. 2. Guest nhập: fullName, email, password, phone. 3. Guest nhấn nút "Đăng ký". 4. Hệ thống kiểm tra email phải có đuôi @gmail.com. 5. Hệ thống kiểm tra password tối thiểu 6 ký tự. 6. Hệ thống kiểm tra phone bắt đầu bằng 0 hoặc +84, đủ 10 số. 7. Hệ thống kiểm tra email chưa tồn tại. 8. Hệ thống mã hóa mật khẩu bằng BCrypt. 9. Hệ thống lưu User mới vào CSDL (role=USER, status=ACTIVE). 10. Hệ thống trả về thông báo "User registered successfully" |
| **Luồng sự kiện thay thế** | Không có |
| **Luồng sự kiện ngoại lệ** | 4a. Email không có đuôi @gmail.com → Hệ thống báo lỗi VALIDATION_ERROR. 5a. Password dưới 6 ký tự → Hệ thống báo lỗi VALIDATION_ERROR. 6a. Phone không đúng định dạng → Hệ thống báo lỗi VALIDATION_ERROR. 7a. Email đã tồn tại → Hệ thống báo lỗi USER_EXISTED (400) |

---

## UC02 – Đăng nhập hệ thống

| Trường | Nội dung |
|--------|----------|
| **Tên Use Case** | Đăng nhập hệ thống |
| **Tác nhân** | Guest (Khách chưa đăng nhập) |
| **Mục đích** | Cho phép người dùng xác thực danh tính để truy cập các chức năng yêu cầu quyền hạn |
| **Mức độ ưu tiên** | Cao |
| **Điều kiện kích hoạt** | Guest nhấn nút "Đăng nhập" và nhập email + mật khẩu |
| **Điều kiện tiên quyết** | Tài khoản đã được đăng ký (UC01) và đang ở trạng thái ACTIVE |
| **Điều kiện thất bại** | Hệ thống không cấp Token. Hiển thị thông báo lỗi |
| **Điều kiện thành công** | Hệ thống trả về accessToken, refreshToken kèm toàn bộ thông tin User (id, email, fullName, phone, avatarUrl, role, status, gender, dateOfBirth, address, createdAt) |
| **Luồng sự kiện chính** | 1. Guest truy cập trang đăng nhập. 2. Guest nhập email và password. 3. Guest nhấn nút "Đăng nhập". 4. Hệ thống tìm User theo email trong CSDL. 5. Hệ thống so sánh password với hash BCrypt đã lưu. 6. Hệ thống kiểm tra trạng thái tài khoản là ACTIVE. 7. Hệ thống sinh JWT accessToken và refreshToken. 8. Hệ thống thiết lập HTTP-Only Cookie chứa Token. 9. Hệ thống trả về Token kèm thông tin User |
| **Luồng sự kiện thay thế** | Không có |
| **Luồng sự kiện ngoại lệ** | 4a. Email không tồn tại → Hệ thống báo lỗi INVALID_CREDENTIALS (401). 5a. Mật khẩu không khớp → Hệ thống báo lỗi INVALID_CREDENTIALS (401). 6a. Tài khoản đang bị LOCKED → Hệ thống báo lỗi USER_LOCKED |

---

## UC02a – Đăng ký / Đăng nhập bằng Google

| Trường | Nội dung |
|--------|----------|
| **Tên Use Case** | Đăng ký / Đăng nhập bằng tài khoản Google (OAuth 2.0) |
| **Tác nhân** | Guest, Google OAuth 2.0 (External) |
| **Mục đích** | Cho phép người dùng đăng ký hoặc đăng nhập nhanh bằng tài khoản Google mà không cần nhớ mật khẩu riêng |
| **Mức độ ưu tiên** | Cao |
| **Điều kiện kích hoạt** | Guest nhấn nút "Đăng nhập bằng Google" trên trang đăng nhập hoặc đăng ký |
| **Điều kiện tiên quyết** | Hệ thống đã cấu hình Google OAuth Client ID/Secret. Dịch vụ Google đang hoạt động |
| **Điều kiện thất bại** | Google từ chối xác thực hoặc hệ thống gặp lỗi kết nối Google |
| **Điều kiện thành công** | Người dùng được xác thực, nhận JWT Token. Nếu là lần đầu → tự động tạo tài khoản mới |
| **Luồng sự kiện chính** | 1. Guest nhấn "Đăng nhập bằng Google". 2. Frontend redirect sang trang xác thực Google. 3. Guest chọn tài khoản Google và cấp quyền. 4. Google trả về Authorization Code cho Frontend. 5. Frontend gửi Authorization Code tới Backend (POST /auth/google). 6. Backend gọi Google API để đổi Code lấy thông tin người dùng (email, fullName, avatarUrl). 7. Backend kiểm tra email trong CSDL |
| **Luồng sự kiện thay thế** | 7a. Email chưa tồn tại (Lần đầu): Backend tạo User mới (role=USER, status=ACTIVE, password=null hoặc random, avatar lấy từ Google). Sinh JWT Token. Trả về Token + thông tin User. 7b. Email đã tồn tại (Đăng nhập lại): Backend kiểm tra trạng thái ACTIVE. Sinh JWT Token. Trả về Token + thông tin User |
| **Luồng sự kiện ngoại lệ** | 4a. Guest từ chối cấp quyền → Google trả về lỗi, Frontend hiển thị thông báo. 6a. Authorization Code không hợp lệ hoặc hết hạn → Backend báo lỗi INVALID_GOOGLE_TOKEN (401). 7b1. Tài khoản đang bị LOCKED → Backend báo lỗi USER_LOCKED |

---

## UC03 – Đăng xuất

| Trường | Nội dung |
|--------|----------|
| **Tên Use Case** | Đăng xuất khỏi hệ thống |
| **Tác nhân** | User, Staff, Organizer, Admin |
| **Mục đích** | Cho phép người dùng kết thúc phiên làm việc an toàn |
| **Mức độ ưu tiên** | Cao |
| **Điều kiện kích hoạt** | Người dùng nhấn nút "Đăng xuất" |
| **Điều kiện tiên quyết** | Người dùng đã đăng nhập (có Token hợp lệ) |
| **Điều kiện thất bại** | Cookie không bị xóa do lỗi hệ thống |
| **Điều kiện thành công** | HTTP-Only Cookie chứa accessToken và refreshToken bị xóa phía Client. Phiên đăng nhập kết thúc |
| **Luồng sự kiện chính** | 1. Người dùng nhấn nút "Đăng xuất". 2. Frontend gọi POST /auth/logout. 3. Hệ thống xóa HTTP-Only Cookie. 4. Hệ thống trả về thông báo "Đăng xuất thành công". 5. Frontend chuyển hướng về trang đăng nhập |
| **Luồng sự kiện thay thế** | Không có |
| **Luồng sự kiện ngoại lệ** | Không có |

---

## UC04 – Quên mật khẩu (Gửi email khôi phục)

| Trường | Nội dung |
|--------|----------|
| **Tên Use Case** | Quên mật khẩu |
| **Tác nhân** | Guest |
| **Mục đích** | Cho phép người dùng khôi phục quyền truy cập khi quên mật khẩu bằng cách gửi mã xác minh qua email |
| **Mức độ ưu tiên** | Cao |
| **Điều kiện kích hoạt** | Guest nhấn liên kết "Quên mật khẩu?" trên trang đăng nhập và nhập email |
| **Điều kiện tiên quyết** | Hệ thống email SMTP đang hoạt động |
| **Điều kiện thất bại** | Hệ thống gặp sự cố gửi email (SMTP lỗi) |
| **Điều kiện thành công** | Mã UUID được tạo, lưu vào bảng password_reset_tokens (hạn 15 phút), và gửi tới hộp thư người dùng |
| **Luồng sự kiện chính** | 1. Guest nhấn "Quên mật khẩu". 2. Guest nhập email đã đăng ký. 3. Guest nhấn nút "Gửi mã khôi phục". 4. Hệ thống tìm User theo email. 5. Hệ thống xóa mọi token cũ của User (nếu có). 6. Hệ thống sinh mã UUID ngẫu nhiên. 7. Hệ thống lưu token vào bảng password_reset_tokens kèm expiry_date = now + 15 phút. 8. Hệ thống gửi email chứa mã UUID tới hộp thư người dùng. 9. Hệ thống trả về thông báo "Mã khôi phục đã được gửi đến email của bạn" |
| **Luồng sự kiện thay thế** | 4a. Email không tồn tại trong hệ thống → Hệ thống vẫn trả về thông báo thành công (200 OK) để không tiết lộ email nào đang đăng ký (bảo mật chống dò quét) |
| **Luồng sự kiện ngoại lệ** | 8a. Hệ thống SMTP gặp lỗi → Hệ thống báo lỗi 500 UNCATEGORIZED_EXCEPTION |

---

## UC05 – Đặt lại mật khẩu

| Trường | Nội dung |
|--------|----------|
| **Tên Use Case** | Đặt lại mật khẩu bằng token |
| **Tác nhân** | Guest |
| **Mục đích** | Cho phép người dùng thiết lập mật khẩu mới sau khi nhận mã xác minh từ email |
| **Mức độ ưu tiên** | Cao |
| **Điều kiện kích hoạt** | Guest nhập mã token nhận từ email và mật khẩu mới |
| **Điều kiện tiên quyết** | Đã thực hiện UC04 thành công. Token chưa hết hạn (< 15 phút) |
| **Điều kiện thất bại** | Mật khẩu không được thay đổi. Hệ thống hiển thị lỗi |
| **Điều kiện thành công** | Mật khẩu mới được mã hóa BCrypt và cập nhật vào CSDL. Token bị xóa (chỉ dùng 1 lần) |
| **Luồng sự kiện chính** | 1. Guest nhập token nhận từ email. 2. Guest nhập mật khẩu mới (tối thiểu 6 ký tự). 3. Guest nhấn nút "Đặt lại mật khẩu". 4. Hệ thống tìm token trong bảng password_reset_tokens. 5. Hệ thống kiểm tra token chưa hết hạn. 6. Hệ thống mã hóa mật khẩu mới bằng BCrypt. 7. Hệ thống cập nhật password của User. 8. Hệ thống xóa token khỏi CSDL. 9. Hệ thống trả về "Mật khẩu đã được đặt lại thành công. Vui lòng đăng nhập lại" |
| **Luồng sự kiện thay thế** | Không có |
| **Luồng sự kiện ngoại lệ** | 4a. Token không tồn tại → Hệ thống báo lỗi 400. 5a. Token đã hết hạn (quá 15 phút) → Hệ thống báo lỗi 400 |

---

## UC06 – Đổi mật khẩu

| Trường | Nội dung |
|--------|----------|
| **Tên Use Case** | Đổi mật khẩu (khi đã đăng nhập) |
| **Tác nhân** | User, Staff, Organizer, Admin |
| **Mục đích** | Cho phép người dùng thay đổi mật khẩu hiện tại để tăng cường bảo mật |
| **Mức độ ưu tiên** | Trung bình |
| **Điều kiện kích hoạt** | Người dùng vào trang cài đặt và nhấn "Đổi mật khẩu" |
| **Điều kiện tiên quyết** | Người dùng đã đăng nhập (có Token hợp lệ) |
| **Điều kiện thất bại** | Mật khẩu không được thay đổi. Hệ thống hiển thị lỗi |
| **Điều kiện thành công** | Mật khẩu mới được mã hóa BCrypt và cập nhật vào CSDL |
| **Luồng sự kiện chính** | 1. Người dùng nhập mật khẩu cũ (oldPassword). 2. Người dùng nhập mật khẩu mới (newPassword, tối thiểu 6 ký tự). 3. Người dùng nhấn "Xác nhận". 4. Hệ thống xác thực oldPassword với hash BCrypt trong CSDL. 5. Hệ thống mã hóa newPassword. 6. Hệ thống cập nhật password trong CSDL. 7. Hệ thống trả về "Password changed successfully" |
| **Luồng sự kiện thay thế** | Không có |
| **Luồng sự kiện ngoại lệ** | 4a. Mật khẩu cũ không đúng → Hệ thống báo lỗi WRONG_PASSWORD (400) |

---

## UC07 – Xem thông tin cá nhân

| Trường | Nội dung |
|--------|----------|
| **Tên Use Case** | Xem hồ sơ cá nhân |
| **Tác nhân** | User, Staff, Organizer, Admin |
| **Mục đích** | Cho phép người dùng xem toàn bộ thông tin tài khoản của mình |
| **Mức độ ưu tiên** | Trung bình |
| **Điều kiện kích hoạt** | Người dùng nhấn vào mục "Hồ sơ cá nhân" hoặc "Tài khoản" |
| **Điều kiện tiên quyết** | Người dùng đã đăng nhập (có Token hợp lệ) |
| **Điều kiện thất bại** | Hệ thống gặp lỗi truy vấn CSDL |
| **Điều kiện thành công** | Hệ thống trả về đầy đủ thông tin: id, email, fullName, phone, avatarUrl, role, status, gender, dateOfBirth, address, createdAt |
| **Luồng sự kiện chính** | 1. Người dùng truy cập trang hồ sơ cá nhân. 2. Frontend gọi GET /users/me kèm JWT Token. 3. Hệ thống trích xuất email từ JWT. 4. Hệ thống truy vấn User từ CSDL theo email. 5. Hệ thống trả về JSON chứa thông tin cá nhân |
| **Luồng sự kiện thay thế** | Không có |
| **Luồng sự kiện ngoại lệ** | 3a. Token hết hạn hoặc không hợp lệ → Hệ thống báo lỗi 401 Unauthorized |

---

## UC08 – Cập nhật thông tin cá nhân

| Trường | Nội dung |
|--------|----------|
| **Tên Use Case** | Cập nhật hồ sơ cá nhân |
| **Tác nhân** | User, Staff, Organizer, Admin |
| **Mục đích** | Cho phép người dùng chỉnh sửa các thông tin cá nhân (trừ email và role) |
| **Mức độ ưu tiên** | Trung bình |
| **Điều kiện kích hoạt** | Người dùng chỉnh sửa thông tin trên form hồ sơ và nhấn "Lưu" |
| **Điều kiện tiên quyết** | Người dùng đã đăng nhập |
| **Điều kiện thất bại** | Thông tin không được cập nhật. Hệ thống hiển thị lỗi validation |
| **Điều kiện thành công** | Thông tin cá nhân được cập nhật trong CSDL và trả về cho Frontend |
| **Luồng sự kiện chính** | 1. Người dùng chỉnh sửa: fullName, phone (bắt buộc), gender, dateOfBirth, address (tùy chọn). 2. Người dùng nhấn "Lưu". 3. Hệ thống validate fullName không trống. 4. Hệ thống validate phone đúng chuẩn VN (0 hoặc +84 + 9 số). 5. Hệ thống validate gender phải là MALE, FEMALE hoặc OTHER (nếu có). 6. Hệ thống cập nhật thông tin trong CSDL. 7. Hệ thống trả về thông tin đã cập nhật |
| **Luồng sự kiện thay thế** | 1a. Người dùng không điền gender, dateOfBirth, address → Hệ thống giữ nguyên giá trị cũ trong CSDL |
| **Luồng sự kiện ngoại lệ** | 3a. fullName trống → Báo lỗi VALIDATION_ERROR. 4a. phone sai định dạng → Báo lỗi VALIDATION_ERROR |

---

## UC09 – Cập nhật ảnh đại diện

| Trường | Nội dung |
|--------|----------|
| **Tên Use Case** | Upload ảnh đại diện |
| **Tác nhân** | User, Staff, Organizer, Admin |
| **Mục đích** | Cho phép người dùng thay đổi ảnh đại diện cá nhân |
| **Mức độ ưu tiên** | Thấp |
| **Điều kiện kích hoạt** | Người dùng nhấn nút "Đổi ảnh đại diện" và chọn file ảnh từ máy |
| **Điều kiện tiên quyết** | Người dùng đã đăng nhập. Dịch vụ Cloudinary đang hoạt động |
| **Điều kiện thất bại** | Ảnh không được upload. Hệ thống giữ nguyên avatar cũ |
| **Điều kiện thành công** | Ảnh được upload lên Cloudinary, URL mới được lưu vào trường avatarUrl trong CSDL |
| **Luồng sự kiện chính** | 1. Người dùng nhấn "Đổi ảnh đại diện". 2. Người dùng chọn file ảnh (JPG/PNG). 3. Frontend gửi PUT /users/me/avatar (multipart/form-data). 4. Hệ thống upload file lên Cloudinary. 5. Cloudinary trả về URL ảnh. 6. Hệ thống cập nhật avatarUrl trong CSDL. 7. Hệ thống trả về thông tin User kèm URL mới |
| **Luồng sự kiện thay thế** | Không có |
| **Luồng sự kiện ngoại lệ** | 4a. Cloudinary gặp lỗi kết nối → Hệ thống báo lỗi 500 UNCATEGORIZED_EXCEPTION |

---

## UC10 – Tự khóa tài khoản

| Trường | Nội dung |
|--------|----------|
| **Tên Use Case** | Tự khóa / xóa tài khoản (Soft Delete) |
| **Tác nhân** | User, Staff, Organizer, Admin |
| **Mục đích** | Cho phép người dùng tự vô hiệu hóa tài khoản của mình để bảo vệ dữ liệu |
| **Mức độ ưu tiên** | Thấp |
| **Điều kiện kích hoạt** | Người dùng nhấn "Xóa tài khoản" hoặc "Khóa tài khoản" trong phần cài đặt |
| **Điều kiện tiên quyết** | Người dùng đã đăng nhập |
| **Điều kiện thất bại** | Hệ thống gặp lỗi cập nhật CSDL |
| **Điều kiện thành công** | Trạng thái tài khoản chuyển thành LOCKED. Người dùng không thể đăng nhập lại cho đến khi Admin mở khóa |
| **Luồng sự kiện chính** | 1. Người dùng nhấn "Xóa tài khoản". 2. Hệ thống hiển thị xác nhận "Bạn có chắc chắn?". 3. Người dùng xác nhận. 4. Frontend gọi DELETE /users/me. 5. Hệ thống đổi status = LOCKED trong CSDL (không xóa dữ liệu). 6. Hệ thống trả về "Account deleted/locked successfully". 7. Frontend đăng xuất và chuyển về trang đăng nhập |
| **Luồng sự kiện thay thế** | 3a. Người dùng nhấn "Hủy" → Hệ thống không thực hiện thay đổi, quay lại trang trước |
| **Luồng sự kiện ngoại lệ** | Không có |
