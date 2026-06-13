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

> **[LƯU Ý QUAN TRỌNG VỀ KIẾN TRÚC MỚI]**
> Kể từ phiên bản V2, các chức năng Tạo (Create), Sửa (Update), Xóa (Delete) lịch trình con độc lập **đã bị loại bỏ**.
> Thay vào đó, Lịch trình con được **tích hợp trực tiếp** thành một mảng dữ liệu (Array `schedules`) bên trong form Tạo/Sửa Sự kiện (UC16 và UC17). 
> Khi Organizer Lưu sự kiện, hệ thống Backend sẽ tự động quét mảng này để Thêm mới, Cập nhật, hoặc Xoá bỏ các lịch trình cũ nhằm tối ưu hóa UX (chỉ cần nhấn Lưu 1 lần duy nhất).
>
> UC20 (Xem lịch trình riêng lẻ) vẫn được giữ lại làm dự phòng.

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
| **Luồng sự kiện chính** | 1. Người dùng truy cập trang quản lý Staff. 2. Frontend gọi GET /admin/get-all-users?role=STAFF. 3. Hệ thống kiểm tra quyền, nếu là Organizer thì ép buộc `role=STAFF`. 4. Hệ thống truy vấn danh sách (id, fullName, email, phone, avatarUrl). 5. Hệ thống trả về danh sách phân trang |
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
