# ĐẶC TẢ USE CASE - PHẦN C: PHÂN CÔNG NHÂN SỰ (UC15)

---

## UC15 – Phân công Nhân viên (Staff) cho sự kiện

| Trường | Nội dung |
| --- | --- |
| **Tên Use Case** | Phân công Nhân viên trực sự kiện |
| **Tác nhân** | Organizer, Admin |
| **Mức độ ưu tiên** | Trung bình |
| **Điều kiện bắt buộc** | Là chủ sở hữu sự kiện và hệ thống đang có tài khoản Staff. |
| **Luồng sự kiện chính** | 1. Organizer mở danh sách cấu hình nhân sự của sự kiện.<br>2. Hệ thống hiển thị danh sách Staff, đánh dấu những người đang được phân công.<br>3. Organizer tích chọn Staff mới hoặc bỏ tích Staff cũ.<br>4. Organizer nhấn Lưu phân công.<br>5. Hệ thống xác minh quyền sở hữu sự kiện.<br>6. Hệ thống cập nhật danh sách vào cơ sở dữ liệu và báo thành công. |
| **Luồng ngoại lệ** | - Cố tình thao tác trên sự kiện không sở hữu: Hệ thống từ chối và báo lỗi truy cập trái phép. |
| **Yêu cầu phi chức năng** | - Hệ thống phải xác thực Backend chặt chẽ trước khi lưu. |
