# 📌 TỔNG HỢP ENTITY BACKEND

Hệ thống quản lý sự kiện, thanh toán PayOS và check-in QR

Tài liệu mô tả chi tiết cấu trúc database và luồng nghiệp vụ để triển khai backend (Spring Boot / JPA)

---

# I. PHẠM VI HỆ THỐNG

- 4 role: ADMIN, ORGANIZER, STAFF, USER
- Thanh toán qua PayOS
- Mỗi sự kiện có 1 loại vé
- Mỗi user chỉ đăng ký 1 lần / event
- Chỉ khi thanh toán thành công mới sinh QR

---

# II. DANH SÁCH ENTITY

Hệ thống sử dụng các bảng chính:

- Users: quản lý tài khoản và phân quyền.
- Events: quản lý thông tin sự kiện do Organizer tạo.
- EventSchedules: Lịch chi tiết sự kiện.
- Registrations: đơn đăng ký tham gia sự kiện của User.
- Payments: giao dịch thanh toán PayOS gắn với Registration.
- Tickets: vé điện tử sinh ra sau khi thanh toán thành công.
- EventStaffAssignments: bảng phân công Staff cho từng Event.
- payment_configs: Lưu thông tin cấu hình PayOS của Organizer.
- CheckInLog: log thao tác check-in để audit và thống kê.
- password_reset_tokens: Lưu mã UUID dùng để đặt lại mật khẩu (tính năng Quên mật khẩu). Mỗi mã có thời hạn 15 phút và chỉ được dùng 1 lần.

---

# III. CHI TIẾT TỪNG BẢNG

---

# 1. USERS – Quản lý tài khoản

## 🎯 Mục đích

Lưu thông tin toàn bộ người dùng và phân quyền trong hệ thống.

## 📌 Các field

| Field         | Type           | Giải thích                                           |
| ------------- | -------------- | ---------------------------------------------------- |
| id            | BIGINT         | Khóa chính (tự tăng)                                 |
| full_name     | VARCHAR        | Họ tên người dùng                                    |
| email         | VARCHAR        | Email đăng nhập (unique, chỉ chấp nhận `@gmail.com`) |
| phone         | VARCHAR        | Số điện thoại (chuẩn VN: 0/+84 + 9 số)               |
| password      | VARCHAR        | Mật khẩu đã mã hóa Bcrypt                            |
| role          | VARCHAR (Enum) | Phân quyền: ADMIN / ORGANIZER / STAFF / USER         |
| status        | VARCHAR (Enum) | Trạng thái tài khoản: ACTIVE / LOCKED                |
| avatar_url    | VARCHAR        | Ảnh đại diện (URL Cloudinary)                        |
| gender        | VARCHAR (Enum) | Giới tính: MALE / FEMALE / OTHER — **nullable**      |
| date_of_birth | DATE           | Ngày sinh `yyyy-MM-dd` — **nullable**                |
| address       | VARCHAR        | Địa chỉ — **nullable**                               |
| created_at    | DATETIME       | Thời điểm tạo (JPA Auditing tự gán)                  |
| updated_at    | DATETIME       | Thời điểm cập nhật gần nhất (JPA Auditing tự gán)    |

---

# 2. EVENTS – Quản lý sự kiện

## 🎯 Mục đích

Lưu thông tin sự kiện do Organizer tạo.

## 📌 Các field

| Field             | Type     | Giải thích                |
| ----------------- | -------- | ------------------------- |
| id                | BIGINT   | Khóa chính                |
| organizer_id      | BIGINT   | Khóa ngoại đến bảng Users |
| title             | VARCHAR  | Tên sự kiện               |
| category          | VARCHAR  | Loại sự kiện              |
| short_description       | VARCHAR  | Mô tả ngắn                |
| description             | TEXT     | Nội dung chi tiết         |
| banner_url              | VARCHAR  | Ảnh bìa                   |
| registration_start_time | DATETIME | Thời gian bắt đầu mở bán  |
| registration_end_time   | DATETIME | Thời gian kết thúc bán    |
| start_time              | DATETIME | Thời gian bắt đầu sự kiện |
| end_time                | DATETIME | Thời gian kết thúc sự kiện|
| location                | VARCHAR  | Địa điểm                  |
| capacity          | INT      | Số lượng tối đa           |
| ticket_price      | DECIMAL  | Giá vé                    |
| status            | VARCHAR  | Trạng thái sự kiện        |
| created_at        | DATETIME | Ngày tạo                  |
| updated_at        | DATETIME | Ngày cập nhật             |

---

# 3. EVENT_SCHEDULES – Lịch chi tiết

## 🎯 Mục đích

Mô tả timeline các hoạt động trong sự kiện.

## 📌 Các field

| Field       | Type     | Giải thích                 |
| ----------- | -------- | -------------------------- |
| id          | BIGINT   | Khóa chính                 |
| event_id    | BIGINT   | khóa ngoại đến bảng Events |
| title       | VARCHAR  | Tên hoạt động              |
| start_time  | DATETIME | Bắt đầu                    |
| end_time    | DATETIME | Kết thúc                   |
| description | TEXT     | Nội dung chi tiết          |

---

# 4. REGISTRATIONS – Đơn đăng ký sự kiện

## 🎯 Mục đích

Lưu thông tin user đăng ký tham gia sự kiện.

## 📌 Các field

| Field             | Type     | Giải thích                                                             |
| ----------------- | -------- | ---------------------------------------------------------------------- |
| id                | BIGINT   | Khóa chính                                                             |
| event_id          | BIGINT   | Khóa ngoại đến bảng Events                                             |
| user_id           | BIGINT   | Khóa ngoại đến bảng Users                                              |
| registration_code | VARCHAR  | Mã đăng ký (unique)                                                    |
| checked_in_at     | DATETIME | Thời điểm check-in                                                     |
| status            | VARCHAR  | Trạng thái đăng ký (PENDING_PAYMENT, CONFIRMED, CHECKED_IN, CANCELLED) |
| created_at        | DATETIME | Ngày tạo                                                               |
| updated_at        | DATETIME | Ngày cập nhật                                                          |

## 📌 Lưu ý

- Cần unique (event_id, user_id) để đảm bảo mỗi user chỉ được 1 vé cho mỗi sự kiện.

## 📌 Ý nghĩa status

- PENDING_PAYMENT: chưa thanh toán
- CONFIRMED: đã thanh toán
- CHECKED_IN: đã vào cổng
- CANCELLED: đã hủy

---

# 5. PAYMENTS – Lưu thông tin Thanh toán

## 🎯 Mục đích

Lưu thông tin giao dịch PayOS.

## 📌 Các field

| Field           | Type     | Giải thích                                                  |
| --------------- | -------- | ----------------------------------------------------------- |
| id              | BIGINT   | Khóa chính                                                  |
| registration_id | BIGINT   | Khóa ngoại đến bảng Registrations                           |
| order_code      | VARCHAR  | Mã đơn PayOS (unique)                                       |
| provider        | VARCHAR  | Cổng thanh toán (PAYOS)                                     |
| amount          | DECIMAL  | Số tiền                                                     |
| status          | VARCHAR  | Trạng thái thanh toán (PENDING, SUCCESS, FAILED, CANCELLED) |
| checkout_url    | VARCHAR  | Link thanh toán trả về                                      |
| payment_link_id | VARCHAR  | ID link PayOS (Nullable)                                    |
| paid_at         | DATETIME | Thời điểm thanh toán (Nullable)                             |
| raw_response    | TEXT     | Dữ liệu webhook (Nullable)                                  |
| created_at      | DATETIME | Ngày tạo                                                    |
| updated_at      | DATETIME | Ngày cập nhật                                               |

## Lưu ý

- User Có thể có nhiều lần thành toán nhưng chỉ 1 payment SUCCESS

## 📌 Ý nghĩa status

- PENDING : Chưa thanh toán
- SUCCESS : Đã thanh toán
- FAILED : Thất bại
- CANCELLED : Đã hủy

---

# 6. TICKETS – Vé điện tử

## 🎯 Mục đích

Đại diện vé hợp lệ để vào sự kiện.

## 📌 Các field

| Field           | Type     | Giải thích                            |
| --------------- | -------- | ------------------------------------- |
| id              | BIGINT   | Khóa chính                            |
| registration_id | BIGINT   | Khóa ngoại đến bảng Registrations     |
| qr_code         | VARCHAR  | Mã QR gốc (Chỉ hiển thị cho Khách)    |
| hashed_qr_code  | VARCHAR  | Mã QR băm SHA-256 (Giao tiếp với App) |
| issued_at       | DATETIME | Thời điểm cấp vé                      |
| checked_in_at   | DATETIME | Thời điểm check-in (Nullable)         |
| status          | VARCHAR  | Trạng thái vé (ACTIVE, USED, INVALID) |
| created_at      | DATETIME | Ngày tạo                              |
| updated_at      | DATETIME | Ngày cập nhật                         |

## Lưu ý

- Chỉ sinh Ticket khi payment thành công. Khi check-in thành công thì ticket chuyển sang USED

## 📌 Ý nghĩa status

- ACTIVE: còn hiệu lực
- USED: đã sử dụng
- INVALID: không hợp lệ

---

# 7. EVENT_STAFF_ASSIGNMENTS – Phân công staff

## 🎯 Mục đích

Gán staff vào sự kiện để check-in.

## 📌 Các field

| Field       | Type     | Giải thích                                     |
| ----------- | -------- | ---------------------------------------------- |
| id          | BIGINT   | Khóa chính                                     |
| event_id    | BIGINT   | Khóa ngoại đến bảng Events                     |
| staff_id    | BIGINT   | Khóa ngoại đến bảng Users                      |
| assigned_by | BIGINT   | Id người phân công (Khóa ngoại đến bảng Users) |
| created_at  | DATETIME | Thời gian                                      |

---

# 8. PAYMENT_CONFIGS – Cấu hình PayOS

## 🎯 Mục đích

Lưu thông tin API PayOS của Organizer.

## 📌 Các field

| Field        | Type    | Giải thích                            |
| ------------ | ------- | ------------------------------------- |
| id           | BIGINT  | Khóa chính                            |
| user_id      | BIGINT  | Khóa ngoại đến bảng Users (Organizer) |
| client_id    | VARCHAR | API client từ PayOS                   |
| api_key      | VARCHAR | API key từ PayOS                      |
| checksum_key | VARCHAR | Checksum key từ PayOS                 |
| is_active    | BOOLEAN | Trạng thái                            |

---

# 9. CHECKIN_LOGS – Log check-in

## 🎯 Mục đích

Ghi lại lịch sử check-in để audit.

## 📌 Các field

| Field         | Type     | Giải thích                        |
| ------------- | -------- | --------------------------------- |
| id            | BIGINT   | Khóa chính                        |
| ticket_id     | BIGINT   | Khóa ngoại đến bảng Tickets       |
| checked_in_by | BIGINT   | Khóa ngoại đến bảng Users (Staff) |
| method        | VARCHAR  | QR / MANUAL                       |
| created_at    | DATETIME | Thời điểm                         |

---

---

# 10. PASSWORD_RESET_TOKENS – Mã đặt lại mật khẩu

## 🎯 Mục đích

Lưu trữ mã UUID tạm thời để xác minh danh tính khi người dùng thực hiện chức năng "Quên mật khẩu". Bảng này tách biệt khỏi bảng `users` để giữ cho dữ liệu người dùng sạch sẽ.

## 📌 Các field

| Field       | Type     | Giải thích                                                  |
| ----------- | -------- | ----------------------------------------------------------- |
| id          | BIGINT   | Khóa chính tự tăng                                          |
| token       | VARCHAR  | Mã UUID ngẫu nhiên, duy nhất (unique)                       |
| user_id     | BIGINT   | Khóa ngoại trỏ tới bảng `users`                             |
| expiry_date | DATETIME | Thời điểm mã hết hạn (hiện tại: **+15 phút** kể từ lúc tạo) |

## 🔗 Quan hệ

- `user_id` → `ManyToOne` → `users.id`

## ⚙️ Luồng nghiệp vụ

1. User gọi `POST /auth/forgot-password` với email của mình.
2. Hệ thống xóa mọi token cũ của User → Tạo token UUID mới → Lưu vào bảng này → Gửi email.
3. User nhận email, lấy mã token → Gọi `POST /auth/reset-password` với token + mật khẩu mới.
4. Hệ thống kiểm tra token tồn tại và chưa hết hạn → Cập nhật mật khẩu mới → **Xóa token** (dùng 1 lần duy nhất).

---

# IV. QUAN HỆ

- User → Event (1-n) : 1 user có thể tạo nhiều event nếu là organizer
- User → Registration (1-n) : 1 user có thể đăng ký nhiều event
- Event → Registration (1-n) : 1 event có thể có nhiều user đăng ký
- Registration → Payment (1-n) : 1 registration có thể có nhiều payment (nếu thanh toán thất bại và thử lại)
- Registration → Ticket (1-1) : 1 registration có thể có 1 ticket
- Một Registration : Một vé chỉ thuộc về đúng 1 user và 1 event
- Event → Staff (n-n) : 1 event có thể có nhiều staff và 1 staff có thể có nhiều event
- Ticket → CheckInLog (1-n) : 1 ticket có thể có nhiều check-in log (nếu check-in nhiều lần khi vé bị lỗi)

---

# V. LUỒNG HỆ THỐNG

## 1. Đăng ký + thanh toán

1. User tạo Registration
2. User thanh toán
3. PayOS webhook
4. Payment SUCCESS
5. Tạo Ticket

---

## 2. Check-in

### QR

- quét → xác nhận → check-in

### Manual

- nhập mã → xác nhận → check-in

---

# VI. RÀNG BUỘC

- Mỗi sự kiện chỉ có một mức giá vé duy nhất.
- Mỗi user chỉ được tạo một registration hợp lệ cho một event.
- Chỉ sinh ticket QR khi payment thành công (sự kiện trả phí) HOẶC khi đăng ký thành công (sự kiện miễn phí có ticket_price = 0).
- Không được phép hủy đăng ký đối với vé đã thanh toán (SUCCESS). Người dùng chỉ được tự hủy vé đối với các sự kiện miễn phí.
- Staff chỉ được check-in các event mình được phân công.
- Admin nhìn thấy toàn bộ event ở mọi trạng thái.
- Organizer chỉ quản lý các event do mình tạo.
- User chỉ nhìn thấy event công khai (PUBLISHED).

# VII. ENUM

- Role: ADMIN, ORGANIZER, STAFF, USER
- UserStatus: ACTIVE, LOCKED
- **Gender: MALE, FEMALE, OTHER** _(Mới thêm)_
- EventStatus: DRAFT, PUBLISHED, CLOSED, CANCELLED, COMPLETED
- RegistrationStatus: PENDING_PAYMENT, CONFIRMED, CHECKED_IN, CANCELLED
- PaymentStatus: PENDING, SUCCESS, FAILED, CANCELLED
- TicketStatus: ACTIVE, USED, INVALID
- CheckInMethod: QR, MANUAL

---

# VIII. KẾT LUẬN

Thiết kế đảm bảo:

- tách rõ nghiệp vụ
- đúng flow thực tế
- dễ mở rộng
- code sạch, dễ hiểu cho người mới
- phù hợp đồ án và production cơ bản
