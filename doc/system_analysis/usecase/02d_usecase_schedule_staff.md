# ĐẶC TẢ USE CASE - PHẦN D: LỊCH TRÌNH & PHÂN CÔNG STAFF (UC20 – UC28)

---

## UC20 – Xem lịch trình sự kiện

| Trường | Nội dung |
|--------|----------|
| **Tên Use Case** | Xem lịch trình con của sự kiện |
| **Tác nhân** | Guest, User, Staff, Organizer, Admin |
| **Mục đích** | Cho phép xem chi tiết timeline các hoạt động trong sự kiện |
| **Mức độ ưu tiên** | Trung bình |
| **Điều kiện kích hoạt** | Người dùng xem trang chi tiết sự kiện hoặc gọi API lịch trình riêng |
| **Điều kiện tiên quyết** | Sự kiện tồn tại trong CSDL |
| **Điều kiện thất bại** | Hệ thống gặp lỗi truy vấn CSDL |
| **Điều kiện thành công** | Danh sách lịch trình con được trả về, sắp xếp tăng dần theo startTime |
| **Luồng sự kiện chính** | 1. Người dùng truy cập trang chi tiết sự kiện. 2. Frontend gọi GET /events/{eventId}/schedules. 3. Hệ thống truy vấn bảng event_schedules theo eventId. 4. Hệ thống sắp xếp theo startTime tăng dần. 5. Hệ thống trả về danh sách (id, eventId, title, startTime, endTime, description) |
| **Luồng sự kiện thay thế** | Không có |
| **Luồng sự kiện ngoại lệ** | Không có |

---

## UC21 – Tạo lịch trình con (Batch)

| Trường | Nội dung |
|--------|----------|
| **Tên Use Case** | Tạo mới nhiều lịch trình con cùng lúc (Batch Create) |
| **Tác nhân** | Organizer |
| **Mục đích** | Cho phép Organizer thêm nhiều hoạt động (timeline) vào sự kiện trong 1 lần gọi API |
| **Mức độ ưu tiên** | Trung bình |
| **Điều kiện kích hoạt** | Organizer nhấn "Thêm lịch trình" và nhập thông tin nhiều hoạt động |
| **Điều kiện tiên quyết** | Đã đăng nhập. Organizer là chủ sự kiện |
| **Điều kiện thất bại** | Lịch trình không được tạo. Hệ thống báo lỗi |
| **Điều kiện thành công** | Các bản ghi EventSchedule được tạo trong CSDL |
| **Luồng sự kiện chính** | 1. Organizer nhập thông tin nhiều hoạt động (title, startTime, endTime, description). 2. Organizer nhấn "Lưu". 3. Frontend gửi POST /events/{eventId}/schedules với body là Array JSON. 4. Hệ thống kiểm tra quyền sở hữu sự kiện. 5. Hệ thống kiểm tra mỗi schedule có startTime và endTime nằm trong khung thời gian của sự kiện cha. 6. Hệ thống lưu các bản ghi vào CSDL. 7. Hệ thống trả về danh sách lịch trình đã tạo |
| **Luồng sự kiện thay thế** | Không có |
| **Luồng sự kiện ngoại lệ** | 4a. Không phải chủ sự kiện → ACCESS_DENIED (403). 5a. Thời gian vượt khung sự kiện → TIME_OUT_OF_EVENT_BOUNDS (400) |

---

## UC22 – Cập nhật lịch trình con (Batch)

| Trường | Nội dung |
|--------|----------|
| **Tên Use Case** | Cập nhật lịch trình con theo ID (Batch Update) |
| **Tác nhân** | Organizer |
| **Mục đích** | Cho phép Organizer sửa đổi thông tin nhiều hoạt động trong sự kiện |
| **Mức độ ưu tiên** | Trung bình |
| **Điều kiện kích hoạt** | Organizer chỉnh sửa lịch trình trên giao diện và nhấn "Cập nhật" |
| **Điều kiện tiên quyết** | Đã đăng nhập. Organizer là chủ sự kiện. Các schedule có id tồn tại |
| **Điều kiện thất bại** | Lịch trình không được cập nhật |
| **Điều kiện thành công** | Thông tin các schedule được cập nhật trong CSDL |
| **Luồng sự kiện chính** | 1. Organizer chỉnh sửa thông tin lịch trình. 2. Organizer nhấn "Cập nhật". 3. Frontend gửi PUT /events/{eventId}/schedules với body là Array JSON (mỗi phần tử phải có trường id). 4. Hệ thống kiểm tra quyền. 5. Hệ thống tìm schedule theo id. 6. Hệ thống kiểm tra thời gian nằm trong khung sự kiện. 7. Hệ thống cập nhật title, startTime, endTime, description. 8. Hệ thống trả về danh sách đã cập nhật |
| **Luồng sự kiện thay thế** | Không có |
| **Luồng sự kiện ngoại lệ** | 5a. ID không tồn tại → SCHEDULE_NOT_FOUND (400). 6a. Thời gian vượt khung → TIME_OUT_OF_EVENT_BOUNDS (400) |

---

## UC23 – Xóa toàn bộ lịch trình

| Trường | Nội dung |
|--------|----------|
| **Tên Use Case** | Xóa toàn bộ lịch trình con của sự kiện |
| **Tác nhân** | Organizer |
| **Mục đích** | Cho phép Organizer xóa sạch tất cả hoạt động trong timeline sự kiện |
| **Mức độ ưu tiên** | Thấp |
| **Điều kiện kích hoạt** | Organizer nhấn "Xóa tất cả lịch trình" |
| **Điều kiện tiên quyết** | Đã đăng nhập. Organizer là chủ sự kiện |
| **Điều kiện thất bại** | Hệ thống gặp lỗi CSDL |
| **Điều kiện thành công** | Tất cả bản ghi EventSchedule thuộc sự kiện bị xóa khỏi CSDL |
| **Luồng sự kiện chính** | 1. Organizer nhấn "Xóa tất cả lịch trình". 2. Hệ thống hiển thị xác nhận. 3. Organizer xác nhận. 4. Frontend gửi DELETE /events/{eventId}/schedules. 5. Hệ thống kiểm tra quyền. 6. Hệ thống xóa tất cả schedule của sự kiện. 7. Hệ thống trả về thành công |
| **Luồng sự kiện thay thế** | 3a. Organizer nhấn "Hủy" → Không xóa |
| **Luồng sự kiện ngoại lệ** | 5a. Không phải chủ sự kiện → ACCESS_DENIED (403) |

---

## UC24 – Xóa một lịch trình con

| Trường | Nội dung |
|--------|----------|
| **Tên Use Case** | Xóa một lịch trình con theo ID |
| **Tác nhân** | Organizer |
| **Mục đích** | Cho phép Organizer xóa một hoạt động cụ thể trong timeline |
| **Mức độ ưu tiên** | Thấp |
| **Điều kiện kích hoạt** | Organizer nhấn nút "Xóa" bên cạnh một lịch trình con |
| **Điều kiện tiên quyết** | Đã đăng nhập. Organizer là chủ sự kiện. Schedule tồn tại |
| **Điều kiện thất bại** | Schedule không bị xóa |
| **Điều kiện thành công** | Bản ghi EventSchedule bị xóa khỏi CSDL |
| **Luồng sự kiện chính** | 1. Organizer nhấn "Xóa" bên cạnh schedule. 2. Hệ thống xác nhận. 3. Frontend gửi DELETE /events/{eventId}/schedules/{scheduleId}. 4. Hệ thống kiểm tra quyền. 5. Hệ thống xóa schedule. 6. Hệ thống trả về "Xóa lịch trình con thành công" |
| **Luồng sự kiện thay thế** | 2a. Organizer nhấn "Hủy" → Không xóa |
| **Luồng sự kiện ngoại lệ** | 4a. Không phải chủ sự kiện → ACCESS_DENIED (403) |

---

## UC25 – Xem danh sách Staff hệ thống

| Trường | Nội dung |
|--------|----------|
| **Tên Use Case** | Lấy danh sách Staff toàn hệ thống |
| **Tác nhân** | Admin, Organizer |
| **Mục đích** | Cho phép Admin/Organizer xem danh sách nhân viên có role STAFF đang ACTIVE để phân công |
| **Mức độ ưu tiên** | Trung bình |
| **Điều kiện kích hoạt** | Admin/Organizer truy cập trang phân công Staff hoặc quản lý nhân viên |
| **Điều kiện tiên quyết** | Đã đăng nhập với Role = ADMIN hoặc ORGANIZER |
| **Điều kiện thất bại** | Hệ thống gặp lỗi truy vấn CSDL |
| **Điều kiện thành công** | Danh sách nhân viên role=STAFF, status=ACTIVE được trả về |
| **Luồng sự kiện chính** | 1. Người dùng truy cập trang quản lý Staff. 2. Frontend gọi GET /users/staffs. 3. Hệ thống truy vấn User có role=STAFF và status=ACTIVE. 4. Hệ thống trả về danh sách (id, fullName, email, phone, avatarUrl) |
| **Luồng sự kiện thay thế** | Không có |
| **Luồng sự kiện ngoại lệ** | 2a. Role không phải ADMIN/ORGANIZER → 403 Forbidden |

---

## UC26 – Xem Staff được phân công vào sự kiện

| Trường | Nội dung |
|--------|----------|
| **Tên Use Case** | Xem danh sách Staff của một sự kiện |
| **Tác nhân** | Admin, Organizer, Staff (được phân công) |
| **Mục đích** | Cho phép xem những nhân viên nào đang được phân công trực tại sự kiện |
| **Mức độ ưu tiên** | Trung bình |
| **Điều kiện kích hoạt** | Người dùng truy cập trang chi tiết sự kiện, tab "Staff" |
| **Điều kiện tiên quyết** | Đã đăng nhập. Phải là Admin, Organizer của sự kiện, hoặc Staff được phân công |
| **Điều kiện thất bại** | Không có quyền truy cập |
| **Điều kiện thành công** | Danh sách Staff được phân công vào sự kiện được trả về |
| **Luồng sự kiện chính** | 1. Người dùng truy cập tab "Staff" của sự kiện. 2. Frontend gọi GET /events/{eventId}/staffs. 3. Hệ thống kiểm tra quyền. 4. Hệ thống truy vấn bảng event_staff_assignments. 5. Hệ thống trả về danh sách Staff |
| **Luồng sự kiện thay thế** | Không có |
| **Luồng sự kiện ngoại lệ** | 3a. User thường cố gọi → 403 Access Denied |

---

## UC27 – Đồng bộ phân công Staff

| Trường | Nội dung |
|--------|----------|
| **Tên Use Case** | Đồng bộ danh sách Staff cho sự kiện |
| **Tác nhân** | Organizer |
| **Mục đích** | Cho phép Organizer phân công/gỡ bỏ Staff bằng cách gửi danh sách ID mới. Hệ thống tự đối chiếu |
| **Mức độ ưu tiên** | Trung bình |
| **Điều kiện kích hoạt** | Organizer chọn các Staff trong danh sách và nhấn "Lưu phân công" |
| **Điều kiện tiên quyết** | Đã đăng nhập. Organizer là chủ sự kiện. Các ID gửi lên phải là User có role=STAFF và status=ACTIVE |
| **Điều kiện thất bại** | Danh sách Staff không được cập nhật |
| **Điều kiện thành công** | Bảng event_staff_assignments được đồng bộ: thêm mới Staff chưa có, xóa Staff không còn trong danh sách, giữ nguyên Staff đã có |
| **Luồng sự kiện chính** | 1. Organizer tick chọn các Staff cần phân công. 2. Organizer nhấn "Lưu". 3. Frontend gửi PUT /events/{eventId}/staffs với body [15, 18, 22]. 4. Hệ thống kiểm tra quyền sở hữu sự kiện. 5. Hệ thống lấy danh sách Staff hiện tại từ CSDL. 6. Hệ thống đối chiếu: ID có trong DB nhưng thiếu trong request → XÓA. ID chưa có trong DB → THÊM MỚI. ID đã có → GIỮ NGUYÊN. 7. Hệ thống lưu thay đổi vào CSDL. 8. Hệ thống trả về danh sách Staff sau đồng bộ |
| **Luồng sự kiện thay thế** | 1a. Organizer gửi mảng rỗng [] → Hệ thống xóa toàn bộ Staff khỏi sự kiện |
| **Luồng sự kiện ngoại lệ** | 4a. Không phải chủ sự kiện → ACCESS_DENIED (403). 6a. Có ID không phải role STAFF hoặc đang LOCKED → INVALID_STAFF_ROLE (400) |

---

## UC28 – Xóa lẻ Staff khỏi sự kiện

| Trường | Nội dung |
|--------|----------|
| **Tên Use Case** | Xóa một Staff khỏi sự kiện |
| **Tác nhân** | Organizer |
| **Mục đích** | Cho phép Organizer gỡ nhanh một nhân viên khỏi sự kiện mà không cần gửi lại toàn bộ danh sách |
| **Mức độ ưu tiên** | Thấp |
| **Điều kiện kích hoạt** | Organizer nhấn nút "Xóa" bên cạnh tên Staff trong danh sách phân công |
| **Điều kiện tiên quyết** | Đã đăng nhập. Organizer là chủ sự kiện. Staff đó đang được phân công |
| **Điều kiện thất bại** | Bản ghi không bị xóa |
| **Điều kiện thành công** | Bản ghi EventStaffAssignment bị xóa khỏi CSDL |
| **Luồng sự kiện chính** | 1. Organizer nhấn "Xóa" bên cạnh Staff. 2. Hệ thống xác nhận. 3. Frontend gửi DELETE /events/{eventId}/staffs/{staffId}. 4. Hệ thống kiểm tra quyền. 5. Hệ thống xóa bản ghi EventStaffAssignment. 6. Hệ thống trả về thành công |
| **Luồng sự kiện thay thế** | 2a. Organizer nhấn "Hủy" → Không xóa |
| **Luồng sự kiện ngoại lệ** | 4a. Không phải chủ sự kiện → ACCESS_DENIED (403) |
