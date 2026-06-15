# ĐẶC TẢ USE CASE - PHẦN A: NỀN TẢNG HỆ THỐNG (UC01 – UC08)

---

## UC01 – Đăng ký tài khoản

| Trường | Nội dung |
| --- | --- |
| **Tên Use Case** | Đăng ký tài khoản |
| **Tác nhân** | Guest |
| **Mức độ ưu tiên** | Cao |
| **Điều kiện bắt buộc** | Email chưa từng tồn tại trên hệ thống. |
| **Luồng sự kiện chính** | 1. Guest mở trang Đăng ký và điền thông tin (Họ tên, Email, SĐT, Mật khẩu).<br>2. Guest nhấn nút Đăng ký.<br>3. Hệ thống kiểm tra định dạng dữ liệu.<br>4. Hệ thống xác minh Email không bị trùng lặp.<br>5. Hệ thống mã hóa mật khẩu và lưu tài khoản mới.<br>6. Hệ thống hiển thị thông báo thành công và chuyển sang Đăng nhập. |
| **Luồng ngoại lệ** | - Sai định dạng hoặc Email trùng lặp: Hệ thống báo lỗi và yêu cầu nhập lại. |
| **Yêu cầu phi chức năng** | - Mật khẩu phải được mã hóa trước khi lưu trữ. |

---

## UC02 – Đăng nhập hệ thống (Email & Google)

| Trường | Nội dung |
| --- | --- |
| **Tên Use Case** | Đăng nhập hệ thống |
| **Tác nhân** | Guest |
| **Mức độ ưu tiên** | Cao |
| **Điều kiện bắt buộc** | Có tài khoản hợp lệ. |
| **Luồng sự kiện chính** | **[Bằng Email]**<br>1. Guest nhập Email, Mật khẩu và nhấn Đăng nhập.<br>2. Hệ thống kiểm tra mật khẩu và trạng thái hoạt động của tài khoản.<br>3. Hệ thống khởi tạo phiên làm việc và chuyển vào Trang chủ.<br><br>**[Bằng Google]**<br>1. Guest nhấn Đăng nhập bằng Google.<br>2. Hệ thống nhận thông tin từ Google.<br>3. Hệ thống tự động tạo tài khoản nếu Email chưa tồn tại.<br>4. Hệ thống cấp phiên làm việc và chuyển vào Trang chủ. |
| **Luồng ngoại lệ** | - Sai thông tin hoặc tài khoản bị khóa: Hệ thống báo lỗi từ chối đăng nhập. |
| **Yêu cầu phi chức năng** | - Phiên làm việc lưu trong Cookie an toàn (HttpOnly). |

---

## UC03 – Đăng xuất

| Trường | Nội dung |
| --- | --- |
| **Tên Use Case** | Đăng xuất |
| **Tác nhân** | User, Staff, Organizer, Admin |
| **Mức độ ưu tiên** | Trung bình |
| **Điều kiện bắt buộc** | Đang có phiên làm việc hợp lệ. |
| **Luồng sự kiện chính** | 1. Tác nhân nhấn nút Đăng xuất.<br>2. Hệ thống xóa phiên làm việc hiện tại.<br>3. Hệ thống xóa Cookie nhận diện.<br>4. Hệ thống điều hướng Tác nhân về trang Đăng nhập. |
| **Luồng ngoại lệ** | - Không có. |
| **Yêu cầu phi chức năng** | - Thao tác xử lý ngay lập tức. |

---

## UC04 – Quản lý mật khẩu (Quên mật khẩu)

| Trường | Nội dung |
| --- | --- |
| **Tên Use Case** | Quên mật khẩu |
| **Tác nhân** | Guest |
| **Mức độ ưu tiên** | Cao |
| **Điều kiện bắt buộc** | Tài khoản sử dụng phương thức Mật khẩu truyền thống. |
| **Luồng sự kiện chính** | 1. Guest nhập Email và chọn Quên mật khẩu.<br>2. Hệ thống tạo mã OTP và gửi qua Email.<br>3. Guest nhập mã OTP và mật khẩu mới.<br>4. Hệ thống xác minh mã OTP hợp lệ.<br>5. Hệ thống mã hóa và cập nhật mật khẩu mới. |
| **Luồng ngoại lệ** | - Mã OTP sai hoặc hết hạn: Báo lỗi và từ chối. |
| **Yêu cầu phi chức năng** | - Mã OTP có thời hạn sử dụng rất ngắn (VD: 5 phút). |

---

## UC05 – Đổi mật khẩu

| Trường | Nội dung |
| --- | --- |
| **Tên Use Case** | Đổi mật khẩu |
| **Tác nhân** | User, Staff, Organizer, Admin |
| **Mức độ ưu tiên** | Trung bình |
| **Điều kiện bắt buộc** | Đã đăng nhập. |
| **Luồng sự kiện chính** | 1. Tác nhân truy cập Cài đặt, nhập mật khẩu cũ và mật khẩu mới.<br>2. Hệ thống xác minh mật khẩu cũ chính xác.<br>3. Hệ thống cập nhật và lưu mật khẩu mới. |
| **Luồng ngoại lệ** | - Sai mật khẩu cũ: Báo lỗi từ chối cập nhật. |
| **Yêu cầu phi chức năng** | - Không có. |

---

## UC06 – Cập nhật hồ sơ cá nhân

| Trường | Nội dung |
| --- | --- |
| **Tên Use Case** | Cập nhật hồ sơ cá nhân |
| **Tác nhân** | User, Staff, Organizer, Admin |
| **Mức độ ưu tiên** | Cao |
| **Điều kiện bắt buộc** | Đã đăng nhập. |
| **Luồng sự kiện chính** | 1. Tác nhân truy cập Hồ sơ cá nhân.<br>2. Tác nhân thay đổi thông tin (Họ tên, SĐT) hoặc chọn ảnh đại diện mới.<br>3. Tác nhân nhấn Lưu.<br>4. Hệ thống đẩy ảnh lên máy chủ lưu trữ (nếu có).<br>5. Hệ thống lưu dữ liệu vào cơ sở dữ liệu và báo thành công. |
| **Luồng ngoại lệ** | - File ảnh quá dung lượng: Hệ thống báo lỗi. |
| **Yêu cầu phi chức năng** | - Tối ưu kích thước ảnh tải lên. |

---

## UC07 – Quản lý tài khoản người dùng

| Trường | Nội dung |
| --- | --- |
| **Tên Use Case** | Quản lý tài khoản người dùng |
| **Tác nhân** | Admin |
| **Mức độ ưu tiên** | Cao |
| **Điều kiện bắt buộc** | Đăng nhập bằng quyền Admin. |
| **Luồng sự kiện chính** | 1. Admin truy cập danh sách người dùng.<br>2. Admin chọn một tài khoản và chỉ định thao tác (Khóa, Mở khóa, Cấp quyền).<br>3. Hệ thống kiểm tra quyền hợp lệ (không cho phép tự khóa bản thân).<br>4. Hệ thống lưu trạng thái/quyền mới vào hệ thống.<br>5. Hệ thống hiển thị thông báo thành công. |
| **Luồng ngoại lệ** | - Cố tình thao tác lên tài khoản của chính mình: Hệ thống báo lỗi và từ chối. |
| **Yêu cầu phi chức năng** | - Chỉ Admin cấp cao nhất mới có quyền truy cập chức năng này. |

---

## UC08 – Xem báo cáo thống kê tổng quan (Dashboard)

| Trường | Nội dung |
| --- | --- |
| **Tên Use Case** | Xem báo cáo thống kê tổng quan |
| **Tác nhân** | Admin, Organizer |
| **Mức độ ưu tiên** | Trung bình |
| **Điều kiện bắt buộc** | Thuộc Role Admin hoặc Organizer. |
| **Luồng sự kiện chính** | 1. Tác nhân truy cập trang Dashboard.<br>2. Hệ thống kiểm tra phân quyền.<br>3. Hệ thống tổng hợp dữ liệu toàn nền tảng (với Admin) hoặc dữ liệu cá nhân (với Organizer).<br>4. Hệ thống hiển thị số liệu dưới dạng biểu đồ trực quan. |
| **Luồng ngoại lệ** | - Chưa có dữ liệu: Các biểu đồ hiển thị giá trị bằng 0. |
| **Yêu cầu phi chức năng** | - Áp dụng kỹ thuật Cache để tải dữ liệu thống kê nhanh chóng. |
