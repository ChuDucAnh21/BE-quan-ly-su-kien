# ĐẶC TẢ USE CASE - PHẦN B: QUẢN TRỊ HỆ THỐNG (UC11 – UC13)

---

## UC11 – Xem danh sách người dùng

| Trường | Nội dung |
|--------|----------|
| **Tên Use Case** | Xem danh sách người dùng (Phân trang, Tìm kiếm, Lọc) |
| **Tác nhân** | Admin |
| **Mục đích** | Cho phép Admin xem và quản lý toàn bộ tài khoản trong hệ thống |
| **Mức độ ưu tiên** | Cao |
| **Điều kiện kích hoạt** | Admin truy cập trang "Quản lý người dùng" |
| **Điều kiện tiên quyết** | Đã đăng nhập với Role = ADMIN |
| **Điều kiện thất bại** | Hệ thống gặp lỗi truy vấn CSDL |
| **Điều kiện thành công** | Danh sách người dùng được trả về có phân trang, kèm thông tin: currentPage, totalPages, totalElements |
| **Luồng sự kiện chính** | 1. Admin truy cập trang quản lý người dùng. 2. Frontend gọi GET /admin/users với tham số tùy chọn (page, size, search, role, status, sort). 3. Hệ thống truy vấn CSDL có phân trang và lọc theo điều kiện. 4. Hệ thống trả về danh sách User (id, email, fullName, phone, avatarUrl, role, status, createdAt) |
| **Luồng sự kiện thay thế** | 2a. Admin nhập từ khóa tìm kiếm (search) → Hệ thống lọc theo email, họ tên, SĐT. 2b. Admin chọn lọc theo role (ADMIN/ORGANIZER/STAFF/USER) → Hệ thống chỉ trả về user có role tương ứng. 2c. Admin chọn lọc theo status (ACTIVE/LOCKED) → Hệ thống lọc theo trạng thái |
| **Luồng sự kiện ngoại lệ** | 2d. Role không phải ADMIN → Hệ thống báo lỗi 403 Forbidden |

---

## UC12 – Khóa / Mở khóa tài khoản

| Trường | Nội dung |
|--------|----------|
| **Tên Use Case** | Thay đổi trạng thái tài khoản (Khóa / Mở khóa) |
| **Tác nhân** | Admin |
| **Mục đích** | Cho phép Admin khóa tài khoản vi phạm hoặc mở khóa tài khoản đã bị khóa |
| **Mức độ ưu tiên** | Cao |
| **Điều kiện kích hoạt** | Admin nhấn nút "Khóa" hoặc "Mở khóa" bên cạnh tài khoản trong danh sách |
| **Điều kiện tiên quyết** | Đã đăng nhập với Role = ADMIN. Tài khoản đích không phải là chính Admin đang thao tác |
| **Điều kiện thất bại** | Trạng thái không được thay đổi. Hệ thống hiển thị lỗi |
| **Điều kiện thành công** | Trạng thái tài khoản đích được cập nhật (ACTIVE ↔ LOCKED) |
| **Luồng sự kiện chính** | 1. Admin xem danh sách người dùng (UC11). 2. Admin nhấn nút "Khóa" hoặc "Mở khóa" bên cạnh tài khoản cần thay đổi. 3. Frontend gửi PUT /admin/users/{id}/status với body {status: "LOCKED" hoặc "ACTIVE"}. 4. Hệ thống kiểm tra tài khoản đích không phải chính Admin đang thao tác. 5. Hệ thống cập nhật trạng thái trong CSDL. 6. Hệ thống trả về thông báo "Cập nhật trạng thái người dùng thành công" |
| **Luồng sự kiện thay thế** | Không có |
| **Luồng sự kiện ngoại lệ** | 4a. Admin cố tự khóa chính mình → Hệ thống báo lỗi CANNOT_MODIFY_YOURSELF (400) |

---

## UC13 – Phân quyền người dùng

| Trường | Nội dung |
|--------|----------|
| **Tên Use Case** | Phân quyền người dùng (Thăng cấp / Giáng cấp) |
| **Tác nhân** | Admin |
| **Mục đích** | Cho phép Admin thay đổi vai trò (Role) của người dùng để cấp hoặc thu hồi quyền hạn |
| **Mức độ ưu tiên** | Cao |
| **Điều kiện kích hoạt** | Admin chọn người dùng và nhấn "Phân quyền" |
| **Điều kiện tiên quyết** | Đã đăng nhập với Role = ADMIN. Tài khoản đích không phải chính Admin đang thao tác |
| **Điều kiện thất bại** | Role không được thay đổi. Hệ thống hiển thị lỗi |
| **Điều kiện thành công** | Role của tài khoản đích được cập nhật thành giá trị mới (USER/STAFF/ORGANIZER/ADMIN) |
| **Luồng sự kiện chính** | 1. Admin xem danh sách người dùng (UC11). 2. Admin chọn tài khoản cần phân quyền. 3. Admin chọn Role mới (USER, STAFF, ORGANIZER, ADMIN). 4. Frontend gửi PUT /admin/users/{id}/role với body {role: "STAFF"}. 5. Hệ thống kiểm tra tài khoản đích không phải chính Admin. 6. Hệ thống cập nhật role trong CSDL. 7. Hệ thống trả về "Cập nhật quyền người dùng thành công" |
| **Luồng sự kiện thay thế** | Không có |
| **Luồng sự kiện ngoại lệ** | 5a. Admin cố đổi quyền chính mình → Hệ thống báo lỗi CANNOT_MODIFY_YOURSELF (400) |

---

## UC13a – Xem báo cáo thống kê tổng quan (Dashboard Admin)

| Trường | Nội dung |
|--------|----------|
| **Tên Use Case** | Xem báo cáo thống kê tổng quan hệ thống (Dashboard) |
| **Tác nhân** | Admin |
| **Mục đích** | Cho phép Admin xem tổng quan các chỉ số vận hành hệ thống: số người dùng, số sự kiện, số vé bán ra, tổng doanh thu, tỷ lệ check-in trên Dashboard |
| **Mức độ ưu tiên** | Cao |
| **Điều kiện kích hoạt** | Admin truy cập trang Dashboard (Trang chủ Admin) |
| **Điều kiện tiên quyết** | Đã đăng nhập với Role = ADMIN |
| **Điều kiện thất bại** | Hệ thống gặp lỗi truy vấn CSDL |
| **Điều kiện thành công** | Hệ thống trả về các chỉ số thống kê tổng quan: Tổng số người dùng (theo role), Tổng số sự kiện (theo trạng thái), Tổng số vé đã bán, Tổng doanh thu toàn hệ thống, Tổng số lượt check-in, Tỷ lệ check-in (số USED / tổng vé ACTIVE+USED) |
| **Luồng sự kiện chính** | 1. Admin truy cập Dashboard. 2. Frontend gọi GET /admin/dashboard. 3. Hệ thống truy vấn CSDL tổng hợp: COUNT(Users) theo role, COUNT(Events) theo status, SUM(Payments.amount) WHERE status=SUCCESS, COUNT(Tickets) theo status. 4. Hệ thống tính tỷ lệ check-in = (Tickets USED / Tickets ACTIVE+USED) × 100%. 5. Hệ thống trả về JSON thống kê |
| **Luồng sự kiện thay thế** | Không có |
| **Luồng sự kiện ngoại lệ** | 2a. Role không phải ADMIN → 403 Forbidden |

---

## UC13b – Xem báo cáo thống kê sự kiện

| Trường | Nội dung |
|--------|----------|
| **Tên Use Case** | Xem báo cáo thống kê chi tiết của một sự kiện |
| **Tác nhân** | Admin, Organizer (chủ sự kiện) |
| **Mục đích** | Cho phép Admin hoặc Organizer xem các chỉ số chi tiết cho một sự kiện cụ thể: số đăng ký, số vé đã bán, doanh thu, số check-in, tỷ lệ tham gia |
| **Mức độ ưu tiên** | Trung bình |
| **Điều kiện kích hoạt** | Admin/Organizer truy cập trang thống kê của sự kiện |
| **Điều kiện tiên quyết** | Đã đăng nhập. Admin hoặc Organizer là chủ sự kiện. Sự kiện tồn tại |
| **Điều kiện thất bại** | Hệ thống gặp lỗi truy vấn CSDL |
| **Điều kiện thành công** | Hệ thống trả về thống kê chi tiết: Tổng đăng ký, Tổng vé đã bán (soldTickets), Doanh thu sự kiện (SUM Payments SUCCESS), Số đã check-in (Tickets USED), Số chưa check-in (Tickets ACTIVE), Tỷ lệ tham gia (USED / tổng vé × 100%), Số chỗ còn lại (capacity - soldTickets) |
| **Luồng sự kiện chính** | 1. Admin/Organizer truy cập trang thống kê sự kiện. 2. Frontend gọi GET /events/{eventId}/statistics. 3. Hệ thống kiểm tra quyền (Admin hoặc chủ sự kiện). 4. Hệ thống truy vấn: COUNT(Registrations) theo status, COUNT(Tickets) theo status, SUM(Payments.amount) WHERE status=SUCCESS AND eventId. 5. Hệ thống tính toán tỷ lệ. 6. Hệ thống trả về JSON thống kê |
| **Luồng sự kiện thay thế** | Không có |
| **Luồng sự kiện ngoại lệ** | 3a. Không có quyền → ACCESS_DENIED (403). 3b. eventId không tồn tại → EVENT_NOT_FOUND (404) |

---

# ĐẶC TẢ USE CASE - PHẦN C: QUẢN LÝ SỰ KIỆN (UC14 – UC19)

---

## UC14 – Xem danh sách sự kiện

| Trường | Nội dung |
|--------|----------|
| **Tên Use Case** | Xem danh sách sự kiện (Public / Admin / Organizer) |
| **Tác nhân** | Guest, User, Staff, Organizer, Admin |
| **Mục đích** | Cho phép mọi người duyệt danh sách các sự kiện trong hệ thống |
| **Mức độ ưu tiên** | Cao |
| **Điều kiện kích hoạt** | Người dùng truy cập trang chủ hoặc trang danh sách sự kiện |
| **Điều kiện tiên quyết** | Hệ thống đang hoạt động (API Public, không cần đăng nhập) |
| **Điều kiện thất bại** | Hệ thống gặp lỗi truy vấn CSDL |
| **Điều kiện thành công** | Danh sách sự kiện được trả về có phân trang |
| **Luồng sự kiện chính** | 1. Người dùng truy cập trang danh sách sự kiện. 2. Frontend gọi GET /events với tham số tùy chọn (page, size, keyword, category, status, organizerId). 3. Hệ thống truy vấn CSDL có phân trang. 4. Hệ thống trả về danh sách sự kiện (id, title, category, bannerUrl, startTime, endTime, location, capacity, ticketPrice, status, ticketsSold) |
| **Luồng sự kiện thay thế** | 2a. Người dùng nhập keyword → Lọc theo tên hoặc địa điểm. 2b. Organizer truyền organizerId → Chỉ lấy sự kiện của mình |
| **Luồng sự kiện ngoại lệ** | 2c. Guest cố truyền status=DRAFT → Hệ thống tự động ép thành status=PUBLISHED để bảo vệ sự kiện nháp |

---

## UC15 – Xem chi tiết sự kiện

| Trường | Nội dung |
|--------|----------|
| **Tên Use Case** | Xem chi tiết một sự kiện |
| **Tác nhân** | Guest, User, Staff, Organizer, Admin |
| **Mục đích** | Cho phép người dùng xem đầy đủ thông tin của sự kiện, bao gồm lịch trình |
| **Mức độ ưu tiên** | Cao |
| **Điều kiện kích hoạt** | Người dùng nhấn vào một sự kiện trong danh sách |
| **Điều kiện tiên quyết** | Sự kiện tồn tại trong CSDL |
| **Điều kiện thất bại** | Sự kiện không tồn tại |
| **Điều kiện thành công** | Hệ thống trả về toàn bộ thông tin sự kiện kèm mảng schedules (lịch trình con) sắp xếp tăng dần theo startTime |
| **Luồng sự kiện chính** | 1. Người dùng nhấn vào sự kiện. 2. Frontend gọi GET /events/{id}. 3. Hệ thống truy vấn Event + EventSchedules từ CSDL. 4. Hệ thống trả về JSON chi tiết gồm: mọi thông tin sự kiện + mảng schedules |
| **Luồng sự kiện thay thế** | Không có |
| **Luồng sự kiện ngoại lệ** | 3a. ID không tồn tại → Hệ thống báo lỗi EVENT_NOT_FOUND (404) |

---

## UC16 – Tạo sự kiện mới

| Trường | Nội dung |
|--------|----------|
| **Tên Use Case** | Tạo sự kiện mới |
| **Tác nhân** | Organizer |
| **Mục đích** | Cho phép Organizer tạo một sự kiện mới ở trạng thái bản nháp (DRAFT) |
| **Mức độ ưu tiên** | Cao |
| **Điều kiện kích hoạt** | Organizer nhấn nút "Tạo sự kiện mới" và điền đầy đủ thông tin |
| **Điều kiện tiên quyết** | Đã đăng nhập với Role = ORGANIZER |
| **Điều kiện thất bại** | Sự kiện không được tạo. Hệ thống hiển thị lỗi validation |
| **Điều kiện thành công** | Sự kiện mới được lưu vào CSDL với status = DRAFT. Ảnh banner (nếu có) được upload lên Cloudinary |
| **Luồng sự kiện chính** | 1. Organizer nhấn "Tạo sự kiện mới". 2. Organizer nhập: title, category, shortDescription, description, capacity (≥ 1), ticketPrice (≥ 0), startTime, endTime, location. 3. Organizer chọn file ảnh banner (tùy chọn). 4. Organizer nhấn "Tạo". 5. Frontend gửi POST /events (multipart/form-data). 6. Hệ thống validate dữ liệu. 7. Hệ thống upload banner lên Cloudinary (nếu có). 8. Hệ thống lưu Event với status=DRAFT, soldTickets=0. 9. Hệ thống trả về thông tin sự kiện vừa tạo |
| **Luồng sự kiện thay thế** | 3a. Organizer không chọn ảnh → Sự kiện được tạo không có bannerUrl |
| **Luồng sự kiện ngoại lệ** | 6a. Thiếu trường bắt buộc → Hệ thống báo lỗi VALIDATION_ERROR. 6b. endTime ≤ startTime → Hệ thống báo lỗi INVALID_TIME_RANGE |

---

## UC17 – Cập nhật sự kiện

| Trường | Nội dung |
|--------|----------|
| **Tên Use Case** | Cập nhật nội dung sự kiện |
| **Tác nhân** | Organizer |
| **Mục đích** | Cho phép Organizer chỉnh sửa thông tin sự kiện do mình tạo |
| **Mức độ ưu tiên** | Cao |
| **Điều kiện kích hoạt** | Organizer nhấn "Chỉnh sửa" trên trang chi tiết sự kiện của mình |
| **Điều kiện tiên quyết** | Đã đăng nhập với Role = ORGANIZER. Phải là người tạo ra sự kiện đó |
| **Điều kiện thất bại** | Thông tin không được cập nhật |
| **Điều kiện thành công** | Thông tin sự kiện được cập nhật trong CSDL |
| **Luồng sự kiện chính** | 1. Organizer chỉnh sửa thông tin sự kiện trên form. 2. Organizer nhấn "Lưu". 3. Frontend gửi PUT /events/{id} (multipart/form-data). 4. Hệ thống kiểm tra Organizer hiện tại là chủ sự kiện. 5. Hệ thống kiểm tra capacity mới ≥ soldTickets. 6. Hệ thống cập nhật CSDL. 7. Hệ thống trả về thông tin đã cập nhật |
| **Luồng sự kiện thay thế** | Không có |
| **Luồng sự kiện ngoại lệ** | 4a. Không phải chủ sự kiện → Hệ thống báo lỗi ACCESS_DENIED (403). 5a. capacity < soldTickets → Hệ thống báo lỗi CAPACITY_CANNOT_BE_LESS_THAN_SOLD (400) |

---

## UC18 – Đổi trạng thái sự kiện

| Trường | Nội dung |
|--------|----------|
| **Tên Use Case** | Đổi trạng thái sự kiện (Công bố / Đóng / Hủy) |
| **Tác nhân** | Organizer, Admin |
| **Mục đích** | Cho phép thay đổi vòng đời sự kiện: DRAFT → PUBLISHED → CLOSED → COMPLETED hoặc CANCELLED |
| **Mức độ ưu tiên** | Cao |
| **Điều kiện kích hoạt** | Organizer/Admin nhấn nút thay đổi trạng thái (VD: "Công bố", "Đóng", "Hủy") |
| **Điều kiện tiên quyết** | Đã đăng nhập. Organizer phải là chủ sự kiện, hoặc là Admin |
| **Điều kiện thất bại** | Trạng thái không được thay đổi |
| **Điều kiện thành công** | Trạng thái sự kiện được cập nhật |
| **Luồng sự kiện chính** | 1. Organizer/Admin chọn trạng thái mới. 2. Frontend gửi PUT /events/{id}/status?newStatus=PUBLISHED. 3. Hệ thống kiểm tra quyền. 4. Nếu chuyển từ DRAFT → PUBLISHED và ticketPrice > 0: kiểm tra Organizer đã cấu hình PayOS (UC29). 5. Hệ thống cập nhật trạng thái trong CSDL. 6. Hệ thống trả về "Cập nhật trạng thái sự kiện thành: PUBLISHED" |
| **Luồng sự kiện thay thế** | Không có |
| **Luồng sự kiện ngoại lệ** | 4a. Sự kiện có phí nhưng chưa cấu hình PayOS → Hệ thống báo lỗi MUST_SETUP_PAYMENT_FIRST (400) |

---

## UC19 – Xóa sự kiện (Smart Delete)

| Trường | Nội dung |
|--------|----------|
| **Tên Use Case** | Xóa sự kiện (Hard Delete hoặc Soft Delete thông minh) |
| **Tác nhân** | Organizer, Admin |
| **Mục đích** | Cho phép loại bỏ sự kiện khỏi hệ thống với cơ chế bảo vệ dữ liệu đơn hàng |
| **Mức độ ưu tiên** | Trung bình |
| **Điều kiện kích hoạt** | Organizer/Admin nhấn "Xóa sự kiện" |
| **Điều kiện tiên quyết** | Đã đăng nhập. Organizer phải là chủ sự kiện, hoặc là Admin |
| **Điều kiện thất bại** | Sự kiện không được xóa do lỗi hệ thống |
| **Điều kiện thành công** | Sự kiện bị xóa vĩnh viễn (nếu chưa bán vé) hoặc chuyển sang CANCELLED (nếu đã bán vé) |
| **Luồng sự kiện chính** | 1. Organizer/Admin nhấn "Xóa sự kiện". 2. Hệ thống hiển thị xác nhận. 3. Người dùng xác nhận. 4. Frontend gửi DELETE /events/{id}. 5. Hệ thống kiểm tra soldTickets. 6a. Nếu soldTickets = 0 → Hard Delete (xóa hoàn toàn khỏi CSDL). 6b. Nếu soldTickets > 0 → Soft Delete (đổi status = CANCELLED, giữ lại dữ liệu). 7. Hệ thống trả về "Thao tác loại bỏ sự kiện thành công" |
| **Luồng sự kiện thay thế** | 3a. Người dùng nhấn "Hủy" → Không thực hiện xóa |
| **Luồng sự kiện ngoại lệ** | Không có |
