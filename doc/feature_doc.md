# 📌 TỔNG QUAN CHỨC NĂNG HỆ THỐNG QUẢN LÝ SỰ KIỆN & CHECK-IN QR

---

# I. CHỨC NĂNG CHUNG CỦA HỆ THỐNG

Áp dụng cho nhiều role tùy theo quyền truy cập.

## 1. Xác thực tài khoản

- ✅ Đăng nhập
- ✅ Đăng xuất (Triển khai bằng cách xóa HttpOnly Cookie phía Client)
- ✅ Quên mật khẩu (đã triển khai — gửi mã UUID qua email, bảng riêng `password_reset_tokens`, hết hạn 15 phút)
- ✅ Đổi mật khẩu

## 2. Hồ sơ cá nhân

- ✅ Xem thông tin cá nhân (gồm: họ tên, email, SĐT, avatar, role, trạng thái, giới tính, ngày sinh, địa chỉ)
- ✅ Cập nhật thông tin cá nhân (Họ tên, SĐT bắt buộc; **Giới tính, Ngày sinh, Địa chỉ** tuỳ chọn)
- ✅ Upload ảnh đại diện (Cloudinary)
- ✅ Tự khóa / xóa tài khoản (Tự chuyển tài khoản của chính mình thành `LOCKED` để bảo vệ an toàn)

## 3. Tra cứu & hiển thị

- Tìm kiếm, lọc, phân trang danh sách (sự kiện, người dùng,...)
- Xem chi tiết sự kiện

---

# II. CHỨC NĂNG THEO ROLE

---

# A. USER (Người tham gia sự kiện)

## 1. Chức năng chính

- Đăng ký tài khoản (các trường bắt buộc: email, số điện thoại, Họ tên, mật khẩu. Các thông tin khác như giới tính, ngày sinh, địa chỉ sẽ được cập nhật sau trong trang cá nhân)
- Xem danh sách sự kiện public
- Xem chi tiết sự kiện
- Đăng ký tham gia sự kiện
- Thanh toán trực tuyến (PayOS)
- Xem trạng thái thanh toán
- Xem thông tin vé / đơn đăng ký
- Nhận vé điện tử dưới dạng mã QR sau khi thanh toán thành công
- Xem danh sách sự kiện đã đăng ký
- Xem trạng thái đăng ký / thanh toán / vé tham gia sự kiện
- Tìm kiếm / lọc sự kiện theo tên, thời gian, địa điểm
- Khóa tài khoản của chính mình

## 2. Chức năng mở rộng

- Hủy đăng ký tham gia sự kiện

- Xem lịch sử tham gia sự kiện

---

# B. STAFF (Nhân sự check-in)

## 1. Chức năng chính

- Xem danh sách sự kiện được phân công
- Xem chi tiết sự kiện được phân công
- Xem danh sách người tham gia của sự kiện
- Quét mã QR để check-in
- Cập nhật trạng thái check-in
- Tìm kiếm người tham gia theo tên / email / mã đăng ký
- Xem danh sách đã check-in / chưa check-in
- Check-in thủ công khi QR lỗi (mất mạng, lỗi hệ thống,...)

## 2. Chức năng mở rộng

- Xem lịch sử check-in gần nhất của sự kiện

---

# C. ORGANIZER (Người tổ chức sự kiện)

## 1. Quản lý sự kiện

- ✅ Tạo sự kiện
- ✅ Chỉnh sửa sự kiện
- ✅ Xóa sự kiện
- ✅ Xem danh sách sự kiện do mình tạo (thông qua query `organizerId`)
- ✅ Xem danh sách sự kiện public
- ✅ Xem chi tiết sự kiện
- ✅ Thay đổi trạng thái sự kiện:
  - DRAFT
  - PUBLISHED
  - CLOSED
  - CANCELLED
  - COMPLETED
- ✅ Quản lý lịch trình sự kiện (EventSchedules): Thêm, Sửa, Xoá timeline chi tiết.

## 2. Quản lý vé & thanh toán

- ✅ Thiết lập giá vé
- Xem trạng thái thanh toán của các lượt đăng ký
- Xem số lượng vé đã thanh toán thành công
- Xem doanh thu của sự kiện (mở rộng)

## 3. Quản lý người tham gia

- Xem danh sách người tham gia
- Xem chi tiết thông tin đăng ký (Hồ sơ người đăng kí)
- Theo dõi trạng thái:
  - Đăng ký
  - Thanh toán
  - Check-in

## 4. Quản lý Staff

- Xem danh sách staff
- Phân công staff cho sự kiện
- Gỡ staff khỏi sự kiện
- Xem danh sách staff đã được gán

## 5. Thống kê

- Tổng số đăng ký
- Tổng số đã check-in
- Tổng số chưa check-in
- Số lượng chỗ còn lại
- Tổng số vé đã thanh toán thành công
- Tỷ lệ tham gia
- Tổng doanh thu (mở rộng)

---

# D. ADMIN (Quản trị hệ thống)

## 1. Quản lý tài khoản

- ✅ Xem danh sách tài khoản (Tìm kiếm, Phân trang, Lọc)
- ❌ Xem chi tiết tài khoản (Chưa triển khai)
- ❌ Cập nhật thông tin tài khoản (Chưa triển khai)
- ✅ Khóa / mở khóa tài khoản
- ✅ Phân quyền:
  - USER
  - STAFF
  - ORGANIZER
  - ADMIN

## 2. Quản lý Staff & Organizer

- ✅ Tạo / cấp quyền staff
- ✅ Quản lý danh sách staff toàn hệ thống (Thông qua bộ lọc Role của API danh sách user)

## 3. Quản lý sự kiện

- Xem tất cả sự kiện theo trạng thái:
  - DRAFT
  - PUBLISHED
  - CLOSED
  - CANCELLED
  - COMPLETED
- Xem chi tiết sự kiện
- Theo dõi trạng thái sự kiện
- Khóa / hủy sự kiện vi phạm

## 4. Quản lý thanh toán

- Xem danh sách giao dịch
- Xem chi tiết giao dịch
- Theo dõi trạng thái thanh toán toàn hệ thống
- Thống kê doanh thu hệ thống (mở rộng)

## 5. Thống kê hệ thống

- Tổng số người dùng
- Tổng số organizer
- Tổng số staff
- Tổng số sự kiện
- Tổng số lượt đăng ký
- Tổng số lượt check-in

---

# III. GHI CHÚ THIẾT KẾ

- Thanh toán qua cổng thanh toán PayOS. Người dùng quét mã QR để thanh toán.Sau đó database sẽ update trạng thái thanh toán.

- QR Code được sử dụng như vé điện tử để check-in
- Vé chỉ được cấp khi:
  - Đăng ký thành công (event miễn phí)
  - Hoặc thanh toán thành công (event trả phí)
- Phân quyền rõ ràng theo role
- Staff chỉ thao tác trong phạm vi sự kiện được phân công
- Organizer chỉ quản lý sự kiện của mình
- Admin quản lý toàn hệ thống

---

# IV. HƯỚNG MỞ RỘNG
