# ĐẶC TẢ USE CASE - PHẦN D: BÁN VÉ, THANH TOÁN & CHECK-IN (UC16 – UC22)

---

## UC16 – Cấu hình cổng thanh toán (PayOS)

| Trường | Nội dung |
| --- | --- |
| **Tên Use Case** | Cấu hình cổng thanh toán |
| **Tác nhân** | Organizer |
| **Mức độ ưu tiên** | Cao |
| **Điều kiện bắt buộc** | Tổ chức sự kiện có thu phí tiền vé. |
| **Luồng sự kiện chính** | 1. Organizer truy cập trang Cài đặt Thanh toán.<br>2. Organizer điền các thông số API của ngân hàng (PayOS) và nhấn Lưu.<br>3. Hệ thống mã hóa thông số nhạy cảm.<br>4. Hệ thống lưu cấu hình vào cơ sở dữ liệu và báo thành công. |
| **Luồng ngoại lệ** | - Bỏ trống thông số quan trọng: Hệ thống báo lỗi. |
| **Yêu cầu phi chức năng** | - Mã hóa an toàn thông tin API thanh toán. |

---

## UC17 – Đặt mua vé sự kiện

| Trường | Nội dung |
| --- | --- |
| **Tên Use Case** | Đặt mua vé sự kiện |
| **Tác nhân** | User |
| **Mức độ ưu tiên** | Rất cao |
| **Điều kiện bắt buộc** | Sự kiện đang mở bán và còn vé. |
| **Luồng sự kiện chính** | 1. User chọn sự kiện, loại vé, số lượng và nhấn Mua vé.<br>2. Hệ thống kiểm tra sức chứa khả dụng của sự kiện.<br>3. Hệ thống tạm giữ số lượng vé.<br>4. Nếu là vé miễn phí: Hệ thống cấp mã vé điện tử ngay.<br>5. Nếu là vé có phí: Hệ thống tạo hóa đơn chờ và chuyển User sang trang thanh toán. |
| **Luồng ngoại lệ** | - Sự kiện vừa hết vé: Hệ thống báo lỗi từ chối bán. |
| **Yêu cầu phi chức năng** | - Hệ thống phải có cơ chế khóa (Locking) để chống Over-booking. |

---

## UC18 – Thanh toán vé sự kiện

| Trường | Nội dung |
| --- | --- |
| **Tên Use Case** | Thanh toán vé sự kiện |
| **Tác nhân** | User, Hệ thống PayOS |
| **Mức độ ưu tiên** | Rất cao |
| **Điều kiện bắt buộc** | Có hóa đơn ở trạng thái chờ thanh toán. |
| **Luồng sự kiện chính** | 1. User quét mã QR thanh toán qua ứng dụng ngân hàng.<br>2. PayOS gửi tín hiệu thanh toán thành công (Webhook) về hệ thống.<br>3. Hệ thống xác minh tín hiệu Webhook.<br>4. Hệ thống đổi trạng thái hóa đơn thành "Đã Thanh toán".<br>5. Hệ thống kích hoạt vé điện tử.<br>6. Giao diện tự động chuyển User sang trang Đặt vé thành công. |
| **Luồng ngoại lệ** | - User nhấn Hủy thanh toán: Hệ thống xóa hóa đơn và nhả lại chỗ trống vé. |
| **Yêu cầu phi chức năng** | - Tín hiệu Webhook phải được xác thực chữ ký để chống làm giả giao dịch. |

---

## UC19 – Xem danh sách vé cá nhân

| Trường | Nội dung |
| --- | --- |
| **Tên Use Case** | Xem danh sách vé cá nhân |
| **Tác nhân** | User |
| **Mức độ ưu tiên** | Cao |
| **Điều kiện bắt buộc** | Đã đăng nhập. |
| **Luồng sự kiện chính** | 1. User truy cập trang "Vé của tôi".<br>2. Hệ thống truy vấn các vé hợp lệ của User.<br>3. Hệ thống hiển thị danh sách vé kèm mã QR to, rõ ràng. |
| **Luồng ngoại lệ** | - Sự kiện đã kết thúc: Mã QR bị làm mờ. |
| **Yêu cầu phi chức năng** | - Mã QR tải nhanh để tránh gây ùn tắc ở cổng. |

---

## UC20 – Hủy vé đã đặt

| Trường | Nội dung |
| --- | --- |
| **Tên Use Case** | Hủy vé sự kiện |
| **Tác nhân** | User |
| **Mức độ ưu tiên** | Trung bình |
| **Điều kiện bắt buộc** | User đang sở hữu vé. |
| **Luồng sự kiện chính** | 1. User mở danh sách vé và nhấn Hủy vé.<br>2. Hệ thống kiểm tra tình trạng vé.<br>3. Nếu là vé miễn phí/chưa thanh toán: Hệ thống hủy vé và nhả lại chỗ trống cho sự kiện.<br>4. Hệ thống báo thành công. |
| **Luồng ngoại lệ** | - Vé đã trả tiền: Hệ thống từ chối hủy tự động và yêu cầu User liên hệ Organizer. |
| **Yêu cầu phi chức năng** | - Không có. |

---

## UC21 – Quét mã QR soát vé (Check-in)

| Trường | Nội dung |
| --- | --- |
| **Tên Use Case** | Kiểm soát vé vào cổng (Check-in) |
| **Tác nhân** | Staff |
| **Mức độ ưu tiên** | Rất cao |
| **Điều kiện bắt buộc** | Staff được Organizer phân công soát vé sự kiện. |
| **Luồng sự kiện chính** | 1. Staff quét mã QR trên vé.<br>2. Ứng dụng kiểm tra trạng thái kết nối mạng.<br>3. **[Trường hợp Online]**: Hệ thống xác minh vé tồn tại, kiểm tra quyền soát vé của Staff và tình trạng vé.<br>4. Nếu vé hợp lệ, hệ thống cập nhật thành "Đã sử dụng", ghi nhận lịch sử và báo màn hình xanh hiển thị thông tin vé.<br>5. **[Trường hợp Offline]**: Ứng dụng tự động mã hóa mã QR quét được để so sánh với mã vé trong danh sách người tham gia được lưu trên máy.<br>6. Nếu hợp lệ, thông tin mã vé sẽ được lưu lại trên máy, tới khi có mạng trở lại toàn bộ danh sách vé sẽ được đẩy lên hệ thống. |
| **Luồng ngoại lệ** | - Mã vé sai, trái tuyến, hoặc đã quét trước đó: Hệ thống (hoặc ứng dụng Offline) báo lỗi màn hình đỏ.<br>- Dữ liệu Offline sẽ được tự động đồng bộ lên máy chủ khi thiết bị có mạng trở lại. |
| **Yêu cầu phi chức năng** | - Tốc độ soát vé phải dưới 1 giây ngay cả khi Offline. |

---

## UC22 – Xem lịch sử Check-in

| Trường | Nội dung |
| --- | --- |
| **Tên Use Case** | Xem nhật ký Check-in |
| **Tác nhân** | Organizer, Admin |
| **Mức độ ưu tiên** | Trung bình |
| **Điều kiện bắt buộc** | Là chủ sở hữu sự kiện hoặc Admin. |
| **Luồng sự kiện chính** | 1. Tác nhân truy cập mục Nhật ký Check-in của sự kiện.<br>2. Hệ thống truy vấn dữ liệu.<br>3. Hệ thống hiển thị danh sách khách hàng đã qua cổng kèm thời gian thực và người soát vé. |
| **Luồng ngoại lệ** | - Chưa có ai vào cổng: Danh sách hiển thị trống. |
| **Yêu cầu phi chức năng** | - Cho phép xuất dữ liệu ra file Excel. |
