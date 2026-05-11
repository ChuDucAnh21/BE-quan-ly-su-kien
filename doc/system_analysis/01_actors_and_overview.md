# PHÂN TÍCH THIẾT KẾ HỆ THỐNG - PHẦN 1
# TÁC NHÂN VÀ TỔNG QUAN HỆ THỐNG

**Dự án:** Hệ thống Quản lý Sự kiện & Check-in QR (Event Management & QR Check-in System)
**Công nghệ Backend:** Java 21, Spring Boot 3.x, MySQL, PayOS SDK v2

---

## I. MÔ TẢ BÀI TOÁN

Xây dựng hệ thống quản lý sự kiện trực tuyến cho phép:
- Người tổ chức (Organizer) tạo và quản lý sự kiện, thiết lập giá vé, theo dõi doanh thu.
- Người dùng (User) duyệt, đăng ký và thanh toán vé tham gia sự kiện trực tuyến qua cổng PayOS.
- Nhân viên (Staff) được phân công soát vé tại cổng bằng mã QR hoặc thủ công, hỗ trợ chế độ Offline.
- Quản trị viên (Admin) quản lý toàn bộ người dùng, phân quyền và giám sát hệ thống.

---

## II. CÁC TÁC NHÂN (ACTORS)

| STT | Tác nhân | Vai trò | Mô tả |
|-----|----------|---------|-------|
| 1 | **Khách (Guest)** | Người dùng chưa đăng nhập | Xem danh sách sự kiện công khai, đăng ký tài khoản, đăng nhập, quên mật khẩu |
| 2 | **Người dùng (User)** | Người tham gia sự kiện | Đăng ký tham gia sự kiện, thanh toán vé, xem vé cá nhân, quản lý hồ sơ |
| 3 | **Nhân viên (Staff)** | Nhân viên soát vé | Xem danh sách vé sự kiện được phân công, quét mã QR check-in, check-in thủ công, đồng bộ check-in offline (Batch Sync) |
| 4 | **Người tổ chức (Organizer)** | Người tạo & quản lý sự kiện | Tạo/sửa/xóa sự kiện, quản lý lịch trình, phân công Staff, cấu hình thanh toán PayOS, xem danh sách vé |
| 5 | **Quản trị viên (Admin)** | Quản trị toàn hệ thống | Quản lý tài khoản, phân quyền, khóa/mở khóa, giám sát toàn bộ sự kiện, xem báo cáo thống kê |


---

## III. DANH SÁCH CHỨC NĂNG TỔNG QUÁT

### Nhóm 1: Xác thực & Bảo mật
| Mã | Chức năng | Tác nhân |
|----|-----------|----------|
| UC01 | Đăng ký tài khoản | Guest |
| UC02 | Đăng nhập | Guest |
| UC02a | Đăng ký / Đăng nhập bằng Google | Guest |
| UC03 | Đăng xuất | User, Staff, Organizer, Admin |
| UC04 | Quên mật khẩu (Gửi email) | Guest |
| UC05 | Đặt lại mật khẩu | Guest |
| UC06 | Đổi mật khẩu | User, Staff, Organizer, Admin |

### Nhóm 2: Quản lý Hồ sơ cá nhân
| Mã | Chức năng | Tác nhân |
|----|-----------|----------|
| UC07 | Xem thông tin cá nhân | User, Staff, Organizer, Admin |
| UC08 | Cập nhật thông tin cá nhân | User, Staff, Organizer, Admin |
| UC09 | Cập nhật ảnh đại diện | User, Staff, Organizer, Admin |
| UC10 | Tự khóa tài khoản | User, Staff, Organizer, Admin |

### Nhóm 3: Quản trị hệ thống (Admin)
| Mã | Chức năng | Tác nhân |
|----|-----------|----------|
| UC11 | Xem danh sách người dùng | Admin |
| UC12 | Khóa / Mở khóa tài khoản | Admin |
| UC13 | Phân quyền người dùng | Admin |
| UC13a | Xem báo cáo thống kê tổng quan (Dashboard) | Admin |
| UC13b | Xem báo cáo thống kê sự kiện | Admin, Organizer |

### Nhóm 4: Quản lý Sự kiện
| Mã | Chức năng | Tác nhân |
|----|-----------|----------|
| UC14 | Xem danh sách sự kiện | Guest, User, Staff, Organizer, Admin |
| UC15 | Xem chi tiết sự kiện | Guest, User, Staff, Organizer, Admin |
| UC16 | Tạo sự kiện mới | Organizer |
| UC17 | Cập nhật sự kiện | Organizer |
| UC18 | Đổi trạng thái sự kiện | Organizer, Admin |
| UC19 | Xóa sự kiện (Smart Delete) | Organizer, Admin |

### Nhóm 5: Quản lý Lịch trình sự kiện
| Mã | Chức năng | Tác nhân |
|----|-----------|----------|
| UC20 | Xem lịch trình sự kiện | Guest, User, Staff, Organizer, Admin |
| UC21 | Tạo lịch trình con (Batch) | Organizer |
| UC22 | Cập nhật lịch trình con (Batch) | Organizer |
| UC23 | Xóa toàn bộ lịch trình | Organizer |
| UC24 | Xóa một lịch trình con | Organizer |

### Nhóm 6: Phân công Staff
| Mã | Chức năng | Tác nhân |
|----|-----------|----------|
| UC25 | Xem danh sách Staff hệ thống | Admin, Organizer |
| UC26 | Xem Staff của sự kiện | Admin, Organizer, Staff (được phân công) |
| UC27 | Đồng bộ phân công Staff | Organizer |
| UC28 | Xóa lẻ Staff khỏi sự kiện | Organizer |

### Nhóm 7: Đăng ký & Thanh toán
| Mã | Chức năng | Tác nhân |
|----|-----------|----------|
| UC29 | Cấu hình PayOS | Organizer |
| UC30 | Xem cấu hình PayOS | Organizer |
| UC31 | Đăng ký tham gia sự kiện | User, Organizer, Admin |
| UC32 | Hủy vé (chỉ vé Free) | User |
| UC33 | Xử lý Webhook PayOS | (Hệ thống tự động) |
| UC34 | Đồng bộ trạng thái đơn hàng | User |
| UC35 | Xem đơn hàng chờ thanh toán | User, Organizer, Admin |

### Nhóm 8: Kiểm soát Vé & Check-in
| Mã | Chức năng | Tác nhân |
|----|-----------|----------|
| UC36 | Xem vé cá nhân (My Tickets) | User, Organizer, Admin |
| UC37 | Xem danh sách vé của sự kiện | Admin, Organizer, Staff (được phân công) |
| UC38 | Quét QR Check-in đơn lẻ | Admin, Organizer, Staff (được phân công) |
| UC39 | Đồng bộ Check-in hàng loạt (Batch Sync) | Admin, Organizer, Staff (được phân công) |

### Nhóm 9: Tự động hóa
| Mã | Chức năng | Tác nhân |
|----|-----------|----------|
| UC40 | Tự động hủy đơn hàng quá hạn | (Hệ thống tự động) |
