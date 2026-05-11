# 🗺️ LỘ TRÌNH ĐỌC HIỂU DỰ ÁN (CHI TIẾT TỪ A - Z)

Đây là bản đồ lộ trình giúp bạn đọc hiểu toàn bộ cấu trúc code, logic nghiệp vụ và các công nghệ cốt lõi của dự án (Cập nhật đến hết Phase 10).

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

### 3. Cơ chế Đăng nhập & Bảo mật JWT
- **`security/JwtAuthenticationFilter.java`**: "Vọng gác" cổng. Kiểm tra Token từ Cookie/Header, giải mã để biết ai đang gọi API và họ có quyền gì.
- **`controller/AuthController.java`**: Sử dụng **HttpOnly Cookie** để lưu Token, giúp chống lại các cuộc tấn công đánh cắp Token từ JavaScript (XSS).
- **`service/impl/AuthServiceImpl.java`**: Logic mã hóa mật khẩu (BCrypt) và sinh mã Token.

### 4. Quản lý Tài khoản & Phân quyền
- **`service/impl/UserServiceImpl.java`**: Xử lý logic xóa mềm (LOCKED) thay vì xóa cứng để giữ lại dữ liệu thống kê.
- **`controller/AdminController.java`**: Được bảo vệ bởi `@PreAuthorize("hasRole('ADMIN')")`, nơi Admin quản lý danh sách người dùng toàn hệ thống.

---

## 🎟️ PHẦN 3: LÕI NGHIỆP VỤ (EVENTS & TICKETS)

### 5. Quản lý Sự kiện & Lịch trình (Phase 5)
- **`entity/Event.java`**: Thực thể quan trọng nhất. Chứa thông tin về Sức chứa (`capacity`), Giá vé (`ticketPrice`) và Số vé đã bán (`soldTickets`).
- **`service/impl/EventServiceImpl.java`**: Logic kiểm soát sức chứa, không cho phép Organizer giảm sức chứa thấp hơn số lượng vé đã bán thực tế.

### 6. Đăng ký vé & Thanh toán PayOS (Phase 7 - 8)
- **`service/impl/RegistrationServiceImpl.java`**: Logic "Giữ chỗ tạm thời" (Race Condition). Khi khách bấm mua, hệ thống nhích `soldTickets` lên ngay lập tức để tránh bán quá số lượng (Overbooking).
- **`service/impl/WebhookServiceImpl.java`**: Xử lý tín hiệu từ Ngân hàng (PayOS). Sử dụng **Khóa Bi quan (`@Lock(PESSIMISTIC_WRITE)`)** để đảm bảo nếu ngân hàng gửi 2 thông báo cùng lúc, hệ thống cũng chỉ sinh duy nhất 1 vé.

### 7. Cổng Check-in QrCode (Phase 9)
- **`service/impl/TicketServiceImpl.java`**: Quản lý rào chắn vào cửa. Đảm bảo 1 vé chỉ được quét 1 lần (`USED`).

---

## 📊 PHẦN 4: HỆ THỐNG BÁO CÁO & DASHBOARD (PHASE 10)

Đây là phần quan trọng nhất để hiểu cách biến dữ liệu thô thành thông tin hữu ích.

### 8. Thống kê & Tổng hợp dữ liệu (Aggregations)
- **`repository/` (Tất cả)**: Hãy xem các hàm có `@Query`. Chúng sử dụng SQL Aggregate (`COUNT`, `SUM`, `GROUP BY`) để tính toán trực tiếp trên DB.
  - `UserRepository`: Đếm User theo Role.
  - `PaymentRepository`: Tính tổng doanh thu thành công.
  - `TicketRepository`: Đếm tần suất check-in theo khung giờ (dùng `HOUR(checkedInAt)`).

### 9. Kiến trúc Dashboard Module (API Độc lập)
- **`controller/DashboardController.java`**: Thay vì tạo 1 API khổng lồ, chúng ta chia nhỏ thành 14 Endpoints độc lập (8 cho Admin, 6 cho Organizer).
- **Lý do:** Giúp Frontend có thể load từng biểu đồ riêng lẻ, mỗi biểu đồ có bộ lọc thời gian (`from`/`to`) riêng mà không làm chậm cả trang web.
- **`service/impl/DashboardServiceImpl.java`**: Trung tâm xử lý logic mapping từ kết quả SQL thô sang định dạng JSON mà thư viện biểu đồ (Ant Design Charts) yêu cầu.

---

## 📚 PHẦN 5: CÔNG CỤ HỖ TRỢ PHÁT TRIỂN

### 10. Swagger UI - Tài liệu API Tự động
- **Cấu hình:** `config/OpenApiConfig.java`.
- **Cách dùng:** Truy cập `http://localhost:8080/swagger-ui.html`. Bạn có thể thử nghiệm (Try it out) trực tiếp mọi API của dự án tại đây mà không cần dùng Postman.

### 11. Các Tài liệu Quan trọng (Thư mục `doc/`)
1. **`api_endpoints.md`**: Chi tiết danh sách API cho Frontend.
2. **`plan_dashboard.md`**: Cấu trúc dữ liệu JSON chi tiết cho từng loại biểu đồ.
3. **`technical_guide.md`**: Giải thích sâu về các kỹ thuật khó như Khóa Bi Quan, Race Condition, Webhook.

---

## 💡 CÁC ANNOTATION "THẦN THÁNH" TRONG DỰ ÁN
- `@Transactional`: Đảm bảo tính toàn vẹn (Hoặc thành công hết, hoặc hủy bỏ hết nếu có lỗi).
- `@PreAuthorize`: Rào chắn quyền truy cập (Admin/Organizer/Staff).
- `@Lock(LockModeType.PESSIMISTIC_WRITE)`: Khóa dòng dữ liệu trong DB, chống tranh chấp dữ liệu khi nhiều người cùng thao tác 1 lúc.
- `@Scheduled`: Tự động chạy ngầm (ví dụ: Tự động hủy đơn hàng nếu khách ngâm thanh toán quá 5 phút).

---

## 🌀 PHẦN 6: CÁC LUỒNG NGHIỆP VỤ TRỌNG YẾU (TRACING GUIDE)

Nếu bạn muốn biết "một yêu cầu từ Frontend sẽ đi qua những đâu", hãy đọc theo thứ tự các hàm sau:

### 1. Luồng Đăng ký & Mua vé (Phức tạp nhất)
Hệ thống xử lý từ lúc khách bấm mua đến lúc nhận được vé:
1.  **`RegistrationController.registerToEvent()`**: Tiếp nhận yêu cầu.
2.  **`RegistrationServiceImpl.registerToEvent()`**: 
    - Gọi `soldTickets + 1` để giữ chỗ.
    - Gọi `payOS.createPaymentLink()` để lấy link thanh toán.
3.  **`WebhookController.handlePayosWebhook()`**: Đón tín hiệu khi khách chuyển khoản xong.
4.  **`WebhookServiceImpl.processWebhook()`**: 
    - Xác thực chữ ký số an toàn.
    - Gọi `ticketService.createTicket()` để sinh QR Code và gửi về Email.

### 2. Luồng Dashboard & Biểu đồ (Mới nhất - Phase 10)
Cách dữ liệu từ hàng vạn bản ghi biến thành biểu đồ Ant Design:
1.  **`DashboardController.getRevenueChart()`**: Tiếp nhận tham số `from`/`to`.
2.  **`DashboardServiceImpl.getRevenueChart()`**: 
    - Gọi `paymentRepository.sumRevenueGroupByDay()`.
3.  **`PaymentRepository.sumRevenueGroupByDay()`**: Thực thi SQL: `SELECT DATE(createdAt), SUM(amount) ... GROUP BY DATE`.
4.  **`DashboardServiceImpl`**: Map kết quả từ mảng `Object[]` sang `RevenuePointDTO` để trả về JSON chuẩn.

### 3. Luồng Check-in tại cổng (Staff)
1.  **`TicketController.checkIn()`**: Nhân viên quét mã QR.
2.  **`TicketServiceImpl.checkInTicket()`**: 
    - Sử dụng `@Lock` để khóa vé, tránh 1 vé quét 2 lần cùng lúc.
    - Kiểm tra trạng thái vé (`ACTIVE`) và thời gian sự kiện.
    - Chuyển trạng thái sang `USED` và lưu thời điểm check-in.

---

## 🛠️ PHẦN 7: DANH SÁCH FILE "PHẢI ĐỌC" THEO THỨ TỰ (TOP 10)

Để nắm bắt dự án trong 60 phút, hãy mở file theo đúng trật tự này:
1.  **`entity/BaseEntity.java`**: Hiểu về dấu mốc thời gian.
2.  **`exception/ErrorCode.java`**: Hiểu về các quy tắc nghiệp vụ (Business Rules).
3.  **`entity/Event.java`**: Hiểu về cấu trúc dữ liệu cốt lõi.
4.  **`security/JwtAuthenticationFilter.java`**: Hiểu về cách bảo vệ hệ thống.
5.  **`service/impl/RegistrationServiceImpl.java`**: Hiểu về logic bán vé và thanh toán.
6.  **`service/impl/WebhookServiceImpl.java`**: Hiểu về cách kết nối với bên thứ 3 (PayOS).
7.  **`service/impl/TicketServiceImpl.java`**: Hiểu về logic Check-in và bảo mật vé.
8.  **`repository/TicketRepository.java`**: Xem các câu lệnh SQL nâng cao.
9.  **`service/impl/DashboardServiceImpl.java`**: Xem cách xử lý dữ liệu cho biểu đồ.
10. **`controller/DashboardController.java`**: Xem cách thiết kế API chuyên nghiệp với Swagger.

---
**Lời khuyên:** Đừng cố đọc hết code trong một file. Hãy nhìn vào **Tên hàm** và **Comment** tôi đã viết cực kỳ chi tiết bằng tiếng Việt để hiểu "Ý đồ thiết kế" trước khi đi sâu vào từng dòng logic.
