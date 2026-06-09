# 🗺️ LỘ TRÌNH ĐỌC HIỂU DỰ ÁN (CHI TIẾT TỪ A - Z)

Đây là bản đồ lộ trình giúp bạn đọc hiểu toàn bộ cấu trúc code, logic nghiệp vụ và các công nghệ cốt lõi của dự án (Cập nhật đến tiến độ hiện tại, bao gồm Phase 11: Google OAuth2, Tối ưu Code và Tích hợp Chatbot AI).

---

## 🏗️ PHẦN 1: CẤU TRÚC NỀN TẢNG (FOUNDATION)

Trước khi đọc code chức năng, hãy hiểu cách dữ liệu vận hành qua 4 lớp:
**Entity (Bảng DB) ➡️ Repository (Truy vấn) ➡️ Service (Xử lý Logic) ➡️ Controller (Tạo API)**

### 1. Quản lý Lỗi & Phản hồi Chuẩn hóa
- **`exception/ErrorCode.java`**: Nơi định nghĩa mọi loại lỗi trong hệ thống (mã lỗi, lời nhắn tiếng Việt, HTTP Status).
- **`exception/GlobalExceptionHandler.java`**: "Lưới lọc" lỗi. Nó chặn đứng các lỗi hệ thống khô khan và biến chúng thành định dạng JSON dễ hiểu cho Frontend.
- **`dto/response/ApiResponse.java`**: Khuôn mẫu trả về thống nhất cho mọi API (luôn có status, message, data).

### 2. Cấu trúc Database & Auditing
- **`entity/BaseEntity.java`**: Lớp cha chứa `createdAt` và `updatedAt`. Nhờ lớp này, chúng ta biết chính xác mọi bản ghi (User, Ticket, Payment) được tạo ra lúc nào để vẽ biểu đồ theo thời gian.

---

## 🔐 PHẦN 2: BẢO MẬT & NGƯỜI DÙNG (AUTH & USER)

### 3. Cơ chế Đăng nhập, Cookie & Bảo mật JWT
- **`security/JwtAuthenticationFilter.java`**: "Vọng gác" cổng. Kiểm tra Token từ Cookie/Header, giải mã để biết ai đang gọi API và họ có quyền gì.
- **`controller/AuthController.java`**: Sử dụng **HttpOnly Cookie** để lưu Token, giúp chống lại các cuộc tấn công đánh cắp Token từ JavaScript (XSS). Cookie Max-Age được tính toán động (Dynamic Expiration) lấy trực tiếp từ cấu hình Bean (`JwtTokenProvider`) đảm bảo cookie hoạt động chính xác.
- **`service/impl/AuthServiceImpl.java`**: Logic mã hóa mật khẩu (BCrypt), sinh mã Token và làm mới Token (Refresh Token).

### 4. Đăng nhập bằng Google (Phase 11)
- **`AuthController.googleLogin()`** và **`AuthServiceImpl.googleLogin()`**: Xử lý giải mã và xác thực `IdToken` từ Google thông qua thư viện `google-api-client`.
- Tự động tạo tài khoản mới nếu email chưa tồn tại, hoặc hợp nhất (merge) nếu đã có tài khoản, sau đó trả về JWT Token và gài vào Cookie.

### 5. Quản lý Tài khoản & Phân quyền
- **`service/impl/UserServiceImpl.java`**: Xử lý logic xóa mềm (LOCKED) thay vì xóa cứng để giữ lại dữ liệu thống kê.
- **`controller/AdminController.java`**: Được bảo vệ bởi `@PreAuthorize("hasRole('ADMIN')")`.

---

## 🎟️ PHẦN 3: LÕI NGHIỆP VỤ (EVENTS & TICKETS)

### 6. Quản lý Sự kiện & Lịch trình (Phase 5)
- **`entity/Event.java`**: Thực thể quan trọng nhất. Chứa thông tin về Sức chứa (`capacity`), Giá vé (`ticketPrice`) và Số vé đã bán (`soldTickets`).
- **`service/impl/EventServiceImpl.java`**: Logic kiểm soát sức chứa, không cho phép Organizer giảm sức chứa thấp hơn số lượng vé đã bán thực tế.

### 7. Đăng ký vé & Thanh toán PayOS (Phase 7 - 8)
- **`service/impl/RegistrationServiceImpl.java`**: Logic "Giữ chỗ tạm thời". Khi khách bấm mua, hệ thống sử dụng Khóa Bi Quan để nhích `soldTickets` lên ngay lập tức nhằm tránh bán quá số lượng (Overbooking - Race Condition).
- **`service/impl/WebhookServiceImpl.java`**: Xử lý tín hiệu từ Ngân hàng (PayOS). Sử dụng **Khóa Bi quan (`@Lock(PESSIMISTIC_WRITE)`)** thông qua `PaymentRepository` để đảm bảo hệ thống chặn đứng việc webhook gọi lặp lại (Idempotency).

### 8. Cổng Check-in QrCode (Phase 9)
- **`service/impl/TicketServiceImpl.java`**: Quản lý rào chắn vào cửa. Khóa vé lại khi quét để đảm bảo 1 mã QR không bị quét 2 lần (`USED`).

---

## 🤖 PHẦN 4: HỆ THỐNG AI CHATBOT TƯ VẤN (MỚI)

Sử dụng Spring AI kết nối với model AI qua OpenRouter để đóng vai trò trợ lý ảo.

### 9. Cấu hình & Tools (Function Calling)
- **`config/ChatbotConfig.java`**: Trái tim của Chatbot. Định nghĩa 5 "Công cụ" (Tools/Functions) giúp AI tự động chọc vào Database để trả lời câu hỏi: Tìm sự kiện, lấy chi tiết sự kiện, lịch trình, sự kiện sắp tới, và thông tin ban tổ chức.
- **`controller/CustomerChatbotController.java`**: Cung cấp API dạng luồng dữ liệu (Stream) dạng văn bản để hiển thị hiệu ứng gõ chữ (typing effect) cho Frontend.

---

## 📊 PHẦN 5: HỆ THỐNG BÁO CÁO & DASHBOARD (PHASE 10)

Đây là phần quan trọng nhất để hiểu cách biến dữ liệu thô thành thông tin hữu ích.

### 10. Kiến trúc Dashboard Module
- **`controller/DashboardController.java`**: Chia nhỏ thành 14 Endpoints độc lập (8 cho Admin, 6 cho Organizer). Class này đã được "dọn dẹp" (Clean Code) loại bỏ hoàn toàn các Annotation của Swagger để code cực kỳ gọn gàng, thuần khiết.
- **`service/impl/DashboardServiceImpl.java`**: Mapping kết quả từ SQL thô sang chuẩn JSON mà thư viện Ant Design Charts yêu cầu.

### 11. Thống kê Database Tối ưu (Aggregations)
- **`repository/` (Tất cả)**: Sử dụng SQL Aggregate (`COUNT`, `SUM`, `GROUP BY`) để tính toán trực tiếp trên DB thay vì lấy lên RAM.
  - Ví dụ: `sumRevenueGroupByDay` trong `PaymentRepository`.

---

## 📚 PHẦN 6: CÔNG CỤ HỖ TRỢ PHÁT TRIỂN

### 12. Swagger UI - Tài liệu API
- **Cấu hình:** `config/OpenApiConfig.java`. Dùng cấu hình tập trung để các Controller luôn được giữ sạch sẽ 100%.
- **Cách dùng:** Truy cập `http://localhost:8080/swagger-ui.html` để kiểm thử (Try it out) trực tiếp mọi API của dự án.

### 13. Các Tài liệu Quan trọng (Thư mục `doc/`)
1. **`api_endpoints.md`**: Chi tiết danh sách API cho Frontend.
2. **`plan_dashboard.md`**: Cấu trúc dữ liệu JSON chi tiết cho từng loại biểu đồ.
3. **`technical_guide.md`**: Giải thích sâu về các kỹ thuật khó như Khóa Bi Quan, Race Condition, Webhook.

---

## 💡 CÁC ANNOTATION "THẦN THÁNH" TRONG DỰ ÁN
- `@Transactional`: Đảm bảo tính toàn vẹn (Hoặc thành công hết, hoặc hủy bỏ hết nếu có lỗi).
- `@PreAuthorize`: Rào chắn quyền truy cập (Admin/Organizer/Staff).
- `@Lock(LockModeType.PESSIMISTIC_WRITE)`: Khóa dòng dữ liệu trong DB, chống tranh chấp dữ liệu khi nhiều người cùng thao tác 1 lúc.
- `@Scheduled`: Tự động chạy ngầm (ví dụ: Tự động hủy đơn hàng).

---

## 🌀 PHẦN 7: CÁC LUỒNG NGHIỆP VỤ TRỌNG YẾU (TRACING GUIDE)

Nếu bạn muốn biết "một yêu cầu từ Frontend sẽ đi qua những đâu", hãy đọc theo thứ tự các hàm sau:

### 1. Luồng Đăng nhập Google (OAuth2)
1.  **`AuthController.googleLogin()`**: Nhận `IdToken` từ Frontend.
2.  **`AuthServiceImpl.googleLogin()`**: Dùng `GoogleIdTokenVerifier` giải mã token. Nếu hợp lệ, lấy Email và Tên. Cấp JWT và nhét vào HttpOnly Cookie một cách an toàn.

### 2. Luồng Đăng ký & Mua vé (Phức tạp nhất)
Hệ thống xử lý từ lúc khách bấm mua đến lúc nhận được vé:
1.  **`RegistrationController.registerToEvent()`**: Tiếp nhận yêu cầu.
2.  **`RegistrationServiceImpl.registerToEvent()`**: Mở `@Lock`, tăng `soldTickets + 1` để giữ chỗ. Tạo Payment. Gọi `payOS.createPaymentLink()`.
3.  **`WebhookController.handlePayosWebhook()`**: Đón tín hiệu khi chuyển khoản xong.
4.  **`WebhookServiceImpl.processWebhook()`**: Mở `@Lock` cho Payment, kiểm tra chữ ký số, gọi `ticketService.createTicket()` để sinh mã QR gửi Email.

### 3. Luồng Hỏi đáp Chatbot AI
1.  **`CustomerChatbotController.streamChat()`**: Nhận prompt từ khách hàng.
2.  **Spring AI (ChatClient)**: Phân tích câu hỏi. Nếu cần, AI sẽ tự gọi hàm (`@Tool`) trong `ChatbotConfig`.
3.  **`EventRepository.findTop5ByStatusAndStartTimeAfterOrderByStartTimeAsc()`**: Hàm tối ưu lấy dữ liệu trả cho AI mà không gây tràn bộ nhớ.
4.  **Spring AI**: Nhận dữ liệu DB, sinh câu trả lời tự nhiên và Stream trả về.

---

## 🛠️ PHẦN 8: DANH SÁCH FILE "PHẢI ĐỌC" THEO THỨ TỰ (TOP 10)

Để nắm bắt dự án một cách nhanh chóng, hãy mở file theo đúng trật tự này:
1.  **`entity/BaseEntity.java` & `entity/Event.java`**: Hiểu về nền tảng dữ liệu.
2.  **`exception/ErrorCode.java`**: Nắm bắt các quy tắc nghiệp vụ (Business Rules).
3.  **`config/SecurityConfig.java`**: Hiểu quyền hạn, phân luồng API và CORS.
4.  **`controller/AuthController.java`**: Đọc cách Controller tính toán vòng đời (Dynamic Expiration) để cấu hình Cookie JWT an toàn.
5.  **`service/impl/RegistrationServiceImpl.java`**: Đọc để hiểu logic bán vé, khóa bi quan (Race Condition).
6.  **`service/impl/WebhookServiceImpl.java`**: Đọc để hiểu bảo mật Webhook PayOS (Idempotency).
7.  **`service/impl/TicketServiceImpl.java`**: Đọc để hiểu logic Check-in bằng QR Code.
8.  **`config/ChatbotConfig.java`**: Đọc để mở mang kiến thức về AI Function Calling.
9.  **`service/impl/DashboardServiceImpl.java`**: Đọc để hiểu nghệ thuật biến hóa số liệu thành JSON chuẩn cho biểu đồ.
10. **`controller/DashboardController.java`**: Đọc để tận hưởng một class API cực kỳ "sạch sẽ", gọn gàng sau khi loại bỏ hoàn toàn Swagger Polishing.
