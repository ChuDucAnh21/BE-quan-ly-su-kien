# KẾ HOẠCH TRIỂN KHAI VÀ DANH SÁCH CÔNG VIỆC (TASKS)

**Dự án:** Hệ thống quản lý sự kiện, thanh toán PayOS và check-in QR
**Công nghệ:** Java Spring Boot, Hibernate (JPA), MySQL, PayOS
**Dịch vụ ngoài:** Cloudinary (Lưu trữ ảnh), JavaMailSender (Gửi email)

---

## I. MỤC TIÊU VÀ YÊU CẦU KỸ THUẬT

- **Backend:** Spring Boot (RESTful API).
- **Database:** MySQL.
- **Bảo mật:** Spring Security + JWT Token.
- **Tiêu chuẩn Code:** Sạch, phân lớp rõ ràng (Layered Architecture), xử lý lỗi quy chuẩn (Global Exception).
- **Tài liệu API:** Tích hợp Swagger / OpenAPI để Frontend dễ kết nối. Khoá ngoại (CORS) tuỳ chỉnh.

---

## II. KẾ HOẠCH TRIỂN KHAI CHI TIẾT

(Kế hoạch bám sát 100% theo đối chiếu `entities_event.md` và `feature_doc.md`)

### 1. Khởi tạo & Cấu hình (Core Setup)

- Dependency cơ bản và cấu hình `application.yml` (MySQL, HikariCP, JPA auto-ddl).
- Kiến trúc thư mục: `config`, `controller`, `dto`, `entity`, `exception`, `repository`, `security`, `service`, `utils`.
- Các class gốc: `BaseEntity` (Auditing), `ApiResponse<T>`, `GlobalExceptionHandler` (`@RestControllerAdvice`).
- Tích hợp **Cloudinary** (hoặc AWS S3) để xử lý việc upload `avatar_url` (User) và `banner_url` (Event).

### 2. Tầng Database (Entities & Enums)

- Khởi tạo 9 Entity bám sát DB: `Users`, `Events`, `EventSchedules`, `Registrations`, `Payments`, `PaymentConfigs`, `Tickets`, `EventStaffAssignments`, `CheckInLogs`.
- Các Enum Ràng buộc: `Role(ADMIN, ORGANIZER, STAFF, USER)`, `UserStatus(ACTIVE, LOCKED)`, `EventStatus(DRAFT, PUBLISHED, CLOSED, CANCELLED, COMPLETED)`, `RegistrationStatus`, `PaymentStatus`, `TicketStatus`, `CheckInMethod`.
- Khóa ngoại logic: `ManyToOne`, Ràng buộc UNIQUE `(event_id, user_id)` tránh 1 người mua 2 vé.

### 3. User & Auth Module (Quản lý User)

- **Cơ bản:** Login, Register, Xem/Cập nhật thông tin (Tên, SĐT, Upload Avatar).
- **Bảo mật JWT (HTTP-Only Cookie):** API Login sẽ được lập trình để trả về `access_token` và `refresh_token` ẩn dưới dạng **HTTP-Only Cookie** tích hợp thẳng vào trình duyệt để chống tấn công XSS. Frontend không cần xử lý lưu trữ hay gắn Token thủ công.
- **Quản lý Mật khẩu:** Thay đổi mật khẩu. Cấu hình `JavaMailSender` gửi OTP/Link cho tính năng mở rộng "Quên mật khẩu".
- **Hủy tài khoản:** Xóa tài khoản (Có thể thiết kế Soft Delete bằng cách set status thành LOCKED/DELETED).
- **Đăng nhập Google (OAuth2 - Tương lai):** Sẽ được phát triển ở Phase cuối, mang lại trải nghiệm 1-Click Authentication thông minh cho khách hàng tham dự Sự kiện.

### 4. Admin Management (Quản trị Hệ thống)

- **User/Staff/Organizer:** Phân quyền, Khóa/Mở tài khoản, Cấp quyền STAFF/ORGANIZER cho người dùng. Thiết lập Staff cho toàn hệ thống.
- **Quản lý Event:** Xem tất cả trạng thái Event toàn hệ thống, Ép Hủy/Khóa sự kiện vi phạm (Đổi status).
- **Quản lý Giao dịch:** Xem danh sách giao dịch tổng, chi tiết đơn, Thống kê Doanh thu/User/Event hệ thống.

### 5. Events Module cho Organizer & User

- **Organizer:**
  - CRUD sự kiện (Có upload file `banner_url`). **Ràng buộc cứng:** Tuyệt đối không được phép sửa giảm `capacity` (số ghế) thấp hơn tổng số vé đã bán thành công để tránh lỗi overbooking.
  - Chuyển tiếp trạng thái (`DRAFT` -> `PUBLISHED` -> `CLOSED` -> `COMPLETED` / `CANCELLED`). **Chốt chặn Public:** Mã nguồn sẽ cố tình báo lỗi, cấm Organizer đổi status sang `PUBLISHED` đối với sự kiện Trả phí nếu phát hiện họ chưa cấu hình Key PayOS trong bảng `PaymentConfigs`.
  - Thiết lập giá vé. Quản lý `EventSchedules` (Ràng buộc: Lịch trình con bắt buộc phải diễn ra trong khuôn khổ `start_time` và `end_time` của Event cha).
  - Phân công / Gỡ Staff từ danh sách cho Event của mình (Bảng `EventStaffAssignments`).
  - Quản lý list người tham dự: Tra cứu trạng thái (Đăng ký, Thanh toán, Check-in), Xem chi tiết hồ sơ đăng ký của khách.
- **User:**
  - Tra cứu sự kiện `PUBLISHED` -> Bộ lọc Tìm kiếm (Tên, Lịch trình, Địa điểm kết hợp Paging).

### 6. Registrations, Payments & PayOS

- **User Logic:** Đăng ký sự kiện (Lọc chặn lỗi khi hết `capacity` hoặc trùng user). **Ràng buộc Thời gian (Deadline):** Chặn đứng luồng đăng ký của khách nếu Sự kiện đã bắt đầu (Tức là `current_time` > `start_time` của Event). Xem lịch sử sự kiện.
- **Hủy Đăng ký (Ràng buộc nghiệp vụ):** Người dùng CHỈ được quyền "Hủy đăng ký" đối với các sự kiện **Miễn phí** (Free Events). Tuyệt đối **cấm Hủy đăng ký** với các vé đã thanh toán `SUCCESS` để khóa luồng thất thoát doanh thu.
- **Xử lý Sự kiện Free:** Các sự kiện Free sẽ có `ticket_price = 0`. Backend tự động Bypass PayOS, sinh một bản ghi **"Thanh toán ảo" (Dummy Payment)** vào bảng `Payments` với `provider = 'SYSTEM'`, `amount = 0` (Bảo toàn nguyên vẹn CSDL và luồng thống kê), sau đó tự động cấp vé QR lập tức.
- **Organizer Logic:** Cấu hình riêng biệt API Key của cổng PayOS gắn vào Database `PaymentConfigs`.
- **Thanh toán:** Backend giao tiếp PayOS API để tạo `checkout_url`. Tra trạng thái cho User. **Bảo vệ rổ hàng (Payment Timeout):** Backend tích hợp **Cron Job (Spring `@Scheduled`)** chạy ngầm 5 phút/lần chống spam dìm chỗ. Đơn hàng `PENDING_PAYMENT` nào quá 5 phút sẽ tự động bị xóa sổ thành `CANCELLED` để giải phóng `capacity` cho người sau nhảy vào mua.
- **Webhook PayOS:** Xây dựng API Webhook an toàn (check `checksum_key` của Organizer) xác nhận `SUCCESS` / `FAILED`. **Xếp hàng Idempotency:** Ngăn chặn lỗi cấp 2 vé nếu cổng thanh toán bắn Webhook 2 lần. **Xử lý Race-Condition:** Dùng Pessimistic/Optimistic Locking trong JPA để khóa giao dịch, chặn triệt để tình trạng nghẽn cổ chai khiến khách mua vượt quá `capacity`.

### 7. Tickets & Check-in QR

- **Sinh Vé:** Ticket (kèm mã QR unique) và Issued Date chỉ sinh ra duy nhất ở bước Payment báo `SUCCESS` (hoặc ngay lúc Đăng ký với Event Free).
- **Check-in (Staff Apps):**
  - Xem danh sách Event mình phụ trách, Xem list Participant.
  - Tìm kiếm Participant (Tên, Email, Mã đăng ký).
  - Check-in **CẦM TAY QR** (API Online). Cập nhật vé thành `USED`. Log `CheckInLogs` method=`QR`. **Chống gian lận (QR Duplicate):** Áp dụng Database Locking (`SELECT FOR UPDATE`) ở bảng Tickets để chặn tình trạng 1 mã QR bị đi copy lại và 3 người cùng đem quét ở 3 cửa trúng một thời điểm.
  - Check-in **THỦ CÔNG MANUAL** (API nhập tay mã khi lỗi QR). Log method=`MANUAL`.
  - Fetch DS Người tham gia Offline để Staff Check-in Offline lúc rớt mạng, sau đó đồng bộ lên bằng Batch API. Xem danh sách đã/chưa check-in. Khôi phục/Tra cứu lịch sử.

### 8. Analytics & Dashboard (Thống kê)

- **Organizer:** Dashboard báo cáo số lượng Đăng ký (Ticket), Số vé thu tiền `SUCCESS`, Chỗ khả dụng, Số lượt QR Checked-in, Tỷ lệ tham dự, Tổng Doanh Thu dự kiện.
- **Admin:** Dashboard thống kê tổng Lượng User, Tống Organizer, Tổng Staff, Tổng luồng đăng ký trên toàn ứng dụng.

---

## III. DANH SÁCH CÔNG VIỆC THỰC THI (TASK CHECKLIST CHI TIẾT)

- [x] **Phase 1: Project Setup & Core Architecture**
  - [x] Khởi tạo Spring Boot 3.3.4 (Java 21). Chèn Dependencies: JPA, MySQL, Security, JWT, Cloudinary, Mailer.
  - [x] Thiết lập `application.yml` (Kết nối DB, JWT Secret, Cloudinary Keys, Mail SMTP).
  - [x] Tạo cấu trúc thư mục MVC chuẩn (Controller, Service, Entity, Repository...).
  - [x] Xây dựng Core Framework: `ApiResponse<T>` (Nested Status Format), `GlobalExceptionHandler`, `BaseEntity` (hỗ trợ Auditing).

- [x] **Phase 2: Database Initialization (Tầng Data)**
  - [x] Khởi tạo 8 file Enum (Role, UserStatus, EventStatus, RegistrationStatus, PaymentStatus, PaymentProvider, TicketStatus, CheckInMethod).
  - [x] Code 9 file Entity, gắn toàn bộ Annotation khóa ngoại (`@ManyToOne`, `@OneToMany`) và `UniqueConstraint(event_id, user_id)` tránh mua lặp vé.
  - [x] Kiểm tra Hibernate tự động Generate ra DB thực tế.

- [x] **Phase 3: Security & Auth Module**
  - [x] Cấu hình Spring Security: `SecurityConfig`, `JwtAuthenticationFilter`, `JwtTokenProvider`, `JwtAuthenticationEntryPoint`.
  - [x] Viết API Sinh Token (Access, Refresh) và lưu vào **HTTP-Only Cookie** chặn đứng nạn XSS.
  - [x] Viết API Đăng ký, Đăng nhập (kèm trả về User Info), và Đăng xuất (xóa Cookie).
  - [x] Viết API Quên mật khẩu + Đặt lại mật khẩu (bảng riêng `PasswordResetToken`, gửi qua `JavaMailSender`).
  - [x] Chuẩn hóa toàn bộ Response Format: `{ status: { code, message, label, requestId }, data }`.
  - [x] Áp dụng Regex Validation: Email bắt buộc `@gmail.com`, SĐT đúng chuẩn Việt Nam, Password tối thiểu 6 ký tự.
  - [x] API Hồ sơ: Xem/Cập nhật thông tin, Upload `avatar_url` qua Cloudinary, Đổi mật khẩu, Soft-delete Account.

- [x] **Phase 4: Cấp quyền & Quản trị (Admin System)**
  - [x] API (Admin): Xem danh sách toàn bộ User (Hỗ trợ phân trang, tìm kiếm, lọc theo Role và Status).
  - [x] API (Admin): Khóa/Mở Khóa User.
  - [x] API (Admin): Cấp Role `STAFF` hoặc `ORGANIZER` cho 1 user bất kỳ.

- [x] **Phase 5: Event CRUD & Logic Ràng buộc (Organizer & User, Admin)**
  - [x] API (Admin): Xem toàn cảnh hệ thống Event, ép buộc Khóa/Hủy các Event lừa đảo.
  - [x] Tích hợp Upload `banner_url` của Event lên Cloudinary.
  - [x] Code bộ API tạo/sửa/xóa Event cho Organizer. **Ràng buộc cứng:** Không được giảm `capacity` thấp hơn tổng vé bán thành công.
  - [x] API đổi trạng thái Event (DRAFT -> PUBLISHED). **Ràng buộc cứng:** Cấm đổi sang PUBLISHED đối với Event bán vé nếu Organizer chưa nhập Key PayOS.
  - [x] Code API Quản lý `EventSchedules`. **Thiết kế nâng cao:** `POST` Batch Create nhận mảng. `PUT` cập nhật đúng theo `id` từng lịch trình (không xóa trắng). `DELETE` xóa toàn bộ hoặc xóa lẻ theo `scheduleId`. **Ràng buộc logic:** Lịch trình con phải nằm lọt trong khoảng giờ Event cha.
  - [x] API chi tiết sự kiện `GET /events/{id}` trả về _kèm danh sách lịch trình con_ của sự kiện đó (đã sắp xếp theo thời gian). API danh sách `GET /events` không bị ảnh hưởng.
  - [x] Code API hiển thị danh sách Event (Paging, Filter Data) cho User vãn lai tìm kiếm.

- [x] **Phase 6: Roles Delegation (Organizer assign Staff)**
  - [x] API Organizer lấy danh sách Staff toàn hệ thống để phân công vào Event của mình (`GET /api/v1/users/staffs`).
  - [x] Lấy nhân viên của 1 sự kiện (`GET /api/v1/events/{eventId}/staffs`).
  - [x] Cập nhật danh sách đồng bộ (`PUT /api/v1/events/{eventId}/staffs`) tự động kiểm tra và Thêm/Xóa logic.
  - [x] Xoá nhanh 1 Staff khỏi sự kiện (`DELETE`).

- [x] **Phase 7: Registration, Limit Checks & Dummy Payment (Bảo vệ rổ hàng)**
  - [x] API Đăng ký Event. **Ràng buộc Deadline:** Chặn đứng thao tác Đăng ký nếu `current_time` > `start_time` của Event. Ràng buộc mã Unique tránh mua đúp vé.
  - [x] Code kịch bản phân nhánh Thanh toán (Free vs Paid):
    - **Vé Paid:** Backend sinh URL Checkout của PayOS trả về Client.
    - **Vé Free (`ticket_price = 0`):** Backend Bypass luồng trên, cắm ngay 1 bản ghi `Payments` giả gọi là "Thanh toán Ảo" (`amount=0`, `provider=SYSTEM`), đẩy Status lên `SUCCESS` tự động.
  - [x] API thiết lập Key PayOS riêng của tầng Organizer lưu vào bảng `PaymentConfigs`.
  - [x] Viết **Cron Job Time-out**: Cứ 5 phút dọn rác 1 lần. Đơn `PENDING_PAYMENT` nào ngâm quá 5 phút sẽ tự động bị ép Hủy (`CANCELLED`) để nhả trả `capacity`.

- [x] **Phase 8: Webhook Idempotency & Sync Ticketing (Thanh toán & Sinh vé)**
  - [x] API Public đón Webhook PayOS (Dùng thư viện checksum để đối soát băm mã).
  - [x] **Xử lý Race-Condition & Idempotency:** Áp dụng kĩ thuật JPA Locking xử lý mua vé song song để 1 đơn không bị cấp 2 vé, không làm âm `capacity` lúc tắc nghẽn.
  - [x] Nối Logic Sinh Vé: Bất cứ khi nào status Payment = `SUCCESS`, Backend tự động khắc một bản Ticket mới chứa mã UUID ngẫu nhiên làm QR cho vào kho.
  - [x] Phép hủy vé: Phát triển API cho người dùng tự hủy vé `Free` (Cập nhật vé -> `INVALID`). Backend cố tình ném lỗi chặn đứng việc hủy đối với vé `Paid`.

- [x] **Phase 9: Multi-mode QR Check-in (Lock DB) & Finalize Payment Link**
  - [x] Gọi SDK PayOS thay thế Mock URL để sinh thực tế Payment Link.
  - [x] API xuất data danh sách những khách mua vé Event cho màn hình App của Organizer và Staff kiểm soát.
  - [x] API Quét Mã QR Online đổi Ticket sang `USED`. **Chống gian lận Code Duplicate:** Áp mã khóa DB `SELECT FOR UPDATE PESSIMISTIC_WRITE` ngăn chặn tình trạng 3 người cầm chung 1 mã đi quét 3 cổng trúng một mili-giây.
  - [x] Xây dựng Endpoint tên `Batch Sync` đón tệp data list Check-in Offline lúc mạng Staff rớt gửi lên để đồng bộ `CheckInLogs` một cách trơn tru (Tùy chọn mở rộng cho tương lai).

- [x] **Phase 10: Dashboards, Analytics & Swagger Polishing**
  - [x] Tích hợp Swagger UI (`springdoc-openapi-starter-webmvc-ui`), mở public cho `/swagger-ui.html`.
  - [x] API Tổng quan: `GET /dashboards/admin` và `GET /dashboards/organizer` (Statistic Cards).
  - [x] **[Admin] 8 API Biểu đồ độc lập** (mỗi cái có bộ lọc `from`/`to` riêng):
    - `GET /dashboards/admin/role-distribution` — Cơ cấu tài khoản (Donut Chart)
    - `GET /dashboards/admin/user-growth` — Tăng trưởng user (Line Chart)
    - `GET /dashboards/admin/top-organizers` — Top 5 Organizer (Bar Chart)
    - `GET /dashboards/admin/monthly-events` — Sự kiện mới theo tháng (Column Chart)
    - `GET /dashboards/admin/event-status` — Trạng thái sự kiện (Pie Chart)
    - `GET /dashboards/admin/event-category` — Phân loại danh mục (Horizontal Bar)
    - `GET /dashboards/admin/revenue-stats` — Doanh thu PayOS theo ngày (Bar Chart)
    - `GET /dashboards/admin/ticket-type-stats` — Vé Free/Paid theo tháng (Stacked Column)
  - [x] **[Organizer] 5 API Biểu đồ độc lập** (mỗi cái có bộ lọc `from`/`to` riêng):
    - `GET /dashboards/organizer/occupancy` — Tỉ lệ lấp đầy (Pie Chart)
    - `GET /dashboards/organizer/sales-trend` — Biến động bán vé (Line Chart)
    - `GET /dashboards/organizer/attendee-structure` — Cơ cấu khách (Pie Chart)
    - `GET /dashboards/organizer/checkin-status` — Trạng thái check-in (Pie Chart)
    - `GET /dashboards/organizer/checkin-frequency` — Tần suất check-in theo giờ (Column Chart)
  - [x] Tài liệu `doc/plan_dashboard.md` đồng bộ với tên API và JSON Format chuẩn.
  - [x] Tài liệu `doc/api_endpoints.md` bổ sung Mục 9 và 10 đặc tả đầy đủ.

- [ ] **Phase 11: Nâng cấp Trải Nghiệm (Bonus OAuth2 Google)**
  - [ ] Đăng ký Google Cloud Console để trích xuất `Client ID` và cấu hình miền được phép gọi API (CORS Origin).
  - [ ] Phát triển API cổng tiếp nhận: `POST /api/v1/auth/google`. Mở khóa bảo mật SecurityConfig cho đường dẫn này.
  - [ ] Tích hợp thư viện `google-api-client` của Google. Giải mã và Verify (xác thực) `IdToken` từ Frontend văng lên.
  - [ ] Bóc xuất siêu dữ liệu: Trích lấy Email, Họ tên, Avatar URL từ Google Payload.
  - [ ] Nhánh Logic 1: Nếu Email tồn tại trong DB, lấy User đó ra sinh JWT và cấp phát thẳng Access Token.
  - [ ] Nhánh Logic 2: Nếu Email chưa tồn tại, Backend tự động đóng gói đối tượng `User` kèm Role `USER` mặc định, lưu xuống DataBase, sau đó sinh JWT và Login trong nháy mắt.



