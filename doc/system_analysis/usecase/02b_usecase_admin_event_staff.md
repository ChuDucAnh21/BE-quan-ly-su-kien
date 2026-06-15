# ĐẶC TẢ USE CASE - PHẦN B: QUẢN LÝ SỰ KIỆN (UC09 – UC14)

---

## UC09 – Thêm mới sự kiện

| Trường | Nội dung |
| --- | --- |
| **Tên Use Case** | Thêm mới sự kiện |
| **Tác nhân** | Organizer, Admin |
| **Mức độ ưu tiên** | Cao |
| **Điều kiện bắt buộc** | Có quyền tạo sự kiện. |
| **Luồng sự kiện chính** | 1. Organizer nhập thông tin sự kiện (thời gian, hình ảnh, giá vé, lịch trình).<br>2. Organizer nhấn Lưu sự kiện.<br>3. Hệ thống kiểm tra các mốc thời gian hợp lệ.<br>4. Hệ thống tạo sự kiện mới và lưu ở trạng thái "Bản nháp" (Draft).<br>5. Hệ thống báo thành công. |
| **Luồng ngoại lệ** | - Thời gian kết thúc trước thời gian bắt đầu: Hệ thống báo lỗi từ chối lưu. |
| **Yêu cầu phi chức năng** | - Không có. |

---

## UC10 – Cập nhật thông tin sự kiện

| Trường | Nội dung |
| --- | --- |
| **Tên Use Case** | Cập nhật thông tin sự kiện |
| **Tác nhân** | Organizer, Admin |
| **Mức độ ưu tiên** | Cao |
| **Điều kiện bắt buộc** | Là chủ sở hữu của sự kiện đó. |
| **Luồng sự kiện chính** | 1. Organizer chọn sự kiện cần sửa và cập nhật lại thông tin.<br>2. Organizer nhấn Lưu thay đổi.<br>3. Hệ thống xác minh quyền sở hữu.<br>4. Hệ thống kiểm tra sức chứa mới không được nhỏ hơn số vé đã bán.<br>5. Hệ thống lưu dữ liệu cập nhật và báo thành công. |
| **Luồng ngoại lệ** | - Can thiệp sự kiện của người khác: Hệ thống chặn và báo lỗi truy cập trái phép. |
| **Yêu cầu phi chức năng** | - Luôn xác thực backend chặt chẽ. |

---

## UC11 – Xóa / Hủy sự kiện

| Trường | Nội dung |
| --- | --- |
| **Tên Use Case** | Xóa hoặc Hủy sự kiện |
| **Tác nhân** | Organizer, Admin |
| **Mức độ ưu tiên** | Cao |
| **Điều kiện bắt buộc** | Là chủ sở hữu sự kiện. |
| **Luồng sự kiện chính** | 1. Organizer nhấn nút Xóa sự kiện.<br>2. Hệ thống kiểm tra số lượng vé đã bán của sự kiện.<br>3. Nếu chưa bán vé nào, hệ thống xóa vĩnh viễn sự kiện khỏi cơ sở dữ liệu.<br>4. Nếu đã bán vé, hệ thống đổi trạng thái sự kiện sang "Đã Hủy" (Cancelled).<br>5. Hệ thống hiển thị thông báo thành công. |
| **Luồng ngoại lệ** | - Không có. |
| **Yêu cầu phi chức năng** | - Sự kiện chuyển sang "Đã Hủy" phải tự động khóa tính năng mua vé. |

---

## UC12 – Thay đổi trạng thái sự kiện

| Trường | Nội dung |
| --- | --- |
| **Tên Use Case** | Thay đổi trạng thái sự kiện |
| **Tác nhân** | Organizer, Admin |
| **Mức độ ưu tiên** | Cao |
| **Điều kiện bắt buộc** | Là chủ sở hữu sự kiện hoặc Admin. |
| **Luồng sự kiện chính** | 1. Organizer chọn sự kiện và nhấn chọn trạng thái mới (Ví dụ: Công bố, Đóng, Dừng bán vé).<br>2. Nếu trạng thái mới là "Công bố" (Public) và sự kiện có thu phí: Hệ thống kiểm tra cấu hình cổng thanh toán PayOS.<br>3. Hệ thống đổi trạng thái sự kiện sang trạng thái mới được yêu cầu.<br>4. Hệ thống hiển thị thông báo cập nhật thành công. |
| **Luồng ngoại lệ** | - Chuyển sang "Công bố" nhưng chưa cấu hình cổng thanh toán PayOS: Hệ thống từ chối thao tác và yêu cầu cài đặt. |
| **Yêu cầu phi chức năng** | - Trạng thái sự kiện phải được đồng bộ để ẩn/hiện hợp lý với người dùng đang truy cập hệ thống. |

---

## UC13 – Tra cứu / Tìm kiếm sự kiện

| Trường | Nội dung |
| --- | --- |
| **Tên Use Case** | Tìm kiếm sự kiện |
| **Tác nhân** | Guest, User |
| **Mức độ ưu tiên** | Cao |
| **Điều kiện bắt buộc** | Không có. |
| **Luồng sự kiện chính** | 1. Tác nhân mở trang Khám phá sự kiện.<br>2. Tác nhân nhập từ khóa hoặc chọn các bộ lọc (danh mục, mức giá).<br>3. Hệ thống truy vấn các sự kiện đang ở trạng thái "Đã công bố" phù hợp với điều kiện.<br>4. Hệ thống trả về danh sách kết quả. |
| **Luồng ngoại lệ** | - Không tìm thấy sự kiện nào: Hiển thị thông báo "Không có kết quả phù hợp". |
| **Yêu cầu phi chức năng** | - Giao diện sử dụng hiệu ứng tải khung (Skeleton) để tăng trải nghiệm mượt mà. |

---

## UC14 – Xem chi tiết sự kiện

| Trường | Nội dung |
| --- | --- |
| **Tên Use Case** | Xem chi tiết sự kiện |
| **Tác nhân** | Guest, User |
| **Mức độ ưu tiên** | Cao |
| **Điều kiện bắt buộc** | Sự kiện đang tồn tại trên hệ thống. |
| **Luồng sự kiện chính** | 1. Tác nhân nhấn vào một sự kiện trong danh sách.<br>2. Hệ thống truy xuất dữ liệu chi tiết của sự kiện.<br>3. Hệ thống hiển thị toàn bộ thông tin: Mô tả, Giá vé, Lịch trình, Tình trạng còn vé. |
| **Luồng ngoại lệ** | - Sự kiện vừa bị Organizer xóa/ẩn: Hệ thống báo lỗi "Sự kiện không tồn tại". |
| **Yêu cầu phi chức năng** | - Không có. |
