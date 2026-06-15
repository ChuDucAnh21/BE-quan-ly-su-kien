# ĐẶC TẢ USE CASE - PHẦN E: TÍNH NĂNG NÂNG CAO (UC23)

---

## UC23 – Tương tác với Trợ lý ảo AI

| Trường                    | Nội dung                                                                                                                                                                                                                                                                                                                     |
| ------------------------- | ---------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| **Tên Use Case**          | Trò chuyện với Trợ lý ảo AI                                                                                                                                                                                                                                                                                                  |
| **Tác nhân**              | Guest, User, Staff, Organizer, Admin                                                                                                                                                                                                                                                                                         |
| **Mức độ ưu tiên**        | Trung bình                                                                                                                                                                                                                                                                                                                   |
| **Điều kiện bắt buộc**    | Hệ thống đang kết nối ổn định với dịch vụ AI.                                                                                                                                                                                                                                                                                |
| **Luồng sự kiện chính**   | 1. Tác nhân mở khung Chatbot, nhập câu hỏi và nhấn Gửi.<br>2. Hệ thống tiếp nhận và bổ sung ngữ cảnh (Prompt) hướng dẫn của hệ thống.<br>3. Hệ thống gửi yêu cầu sang dịch vụ AI để xử lý.<br>4. Dịch vụ AI sinh ra câu trả lời tư vấn tự nhiên.<br>5. Hệ thống nhận kết quả và hiển thị lên giao diện Chatbot của Tác nhân. |
| **Luồng ngoại lệ**        | - Dịch vụ AI quá tải: Hệ thống tự báo lỗi "Trợ lý ảo đang bận, vui lòng thử lại sau".                                                                                                                                                                                                                                        |
| **Yêu cầu phi chức năng** | - Phải áp dụng kỹ thuật chống Prompt Injection để ngăn người dùng moi móc dữ liệu nội bộ.                                                                                                                                                                                                                                    |

#test commit
