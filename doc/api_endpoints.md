# TÀI LIỆU KẾT NỐI HỆ THỐNG API (DÀNH CHO FRONTEND)

**Dự án:** Event Management Backend
**Base URL:** `http://localhost:8080/api/v1`
**Authorization:** JWT Header (`Authorization: Bearer <token>`) hoặc HTTP-Only Cookie.

---

## ⚙️ CẤU HÌNH MÔI TRƯỜNG (application.properties)

Các giá trị sau cần được cấu hình trong `application.properties` khi deploy lên môi trường thực tế:

```properties
# URL callback sau thanh toán PayOS (FE nhận kết quả)
app.payment.return-url=https://your-domain.com/payment-success
app.payment.cancel-url=https://your-domain.com/payment-cancel
```

> **Lưu ý:** Nếu không cấu hình, hệ thống sẽ dùng giá trị mặc định `http://localhost:3000/...` (chỉ phù hợp môi trường dev).

---

## 0. QUY CHUẨN ĐỊNH DẠNG TRẢ VỀ CHUNG (RESPONSE FORMAT)

Toàn bộ mọi API đều được Backend bọc trong Format chuẩn:

```json
{
  "status": {
    "code": 200,
    "message": "Thành công",
    "label": "API_STATUS_SUCCESS",
    "requestId": "a1b2c3"
  },
  "data": null
}
```

_Ghi chú: `code` luôn khớp với mã trạng thái HTTP thực tế (200, 400, 401, 404, 500)._

---

## 1. MODULE AUTHENTICATION (`/auth`)

> Các API trong module này **không cần Token** (Public).

### 1.1. Đăng ký tài khoản (Register)

- **Method:** `POST /auth/register`
- **Request Body:**
  ```json
  {
    "fullName": "Nguyen Van A",
    "email": "user@gmail.com",
    "password": "password123",
    "phone": "0987654321"
  }
  ```
- **Ràng buộc (Validation):**
  - `fullName`: Không để trống.
  - `email`: **Bắt buộc** phải có đuôi `@gmail.com`.
  - `password`: Tối thiểu 6 ký tự.
  - `phone`: Bắt đầu bằng `0` hoặc `+84`, đủ 10 số.
- **Ghi chú:** Các trường `gender`, `dateOfBirth`, `address` **không có trong form đăng ký**. User cập nhật sau qua API `PUT /users/me`.
- **Response (200 OK):**
  ```json
  {
    "status": { "code": 200, "message": "User registered successfully", ... },
    "data": null
  }
  ```

### 1.2. Đăng nhập hệ thống (Login)

- **Method:** `POST /auth/login`
- **Request Body:**
  ```json
  {
    "email": "user@gmail.com",
    "password": "password123"
  }
  ```
- **Response (200 OK):** Trả về token kèm thông tin User đầy đủ.
  ```json
  {
    "status": { "code": 200, "message": "Login successful", ... },
    "data": {
      "accessToken": "ey...",
      "refreshToken": "ey...",
      "user": {
        "id": 1,
        "email": "user@gmail.com",
        "fullName": "Nguyen Van A",
        "phone": "0987654321",
        "avatarUrl": "https://...",
        "role": "USER",
        "status": "ACTIVE",
        "gender": "MALE",
        "dateOfBirth": "2000-01-21",
        "address": "123 Đường ABC, Hà Nội",
        "createdAt": "2024-01-01T00:00:00"
      }
    }
  }
  ```
- **Lỗi:** `401` – Sai email hoặc mật khẩu.

### 1.3. Đăng xuất (Logout)

- **Method:** `POST /auth/logout`
- **Mô tả:** Xóa HTTP-Only Cookie chứa `access_token` và `refresh_token` ở phía Client để chấm dứt phiên đăng nhập một cách an toàn.
- **Request Body:** Không có.
- **Response (200 OK):**
  ```json
  {
    "status": { "code": 200, "message": "Đăng xuất thành công", "label": "API_STATUS_SUCCESS"... },
    "data": null
  }
  ```

### 1.4. Làm mới Token (Refresh Token)

- **Method:** `POST /auth/refresh`
- **Mô tả:** Frontend gọi API này khi nhận lỗi 401 do Access Token (24h) hết hạn. Trình duyệt tự động đính kèm Cookie `refresh_token` (hạn 30 ngày) đã lưu từ lúc login. Backend kiểm tra và cấp phát `access_token` mới đè vào Cookie.
- **Quyền:** Public (Không cần Token ở Header)
- **Response (200 OK):**
  ```json
  {
    "status": { "code": 200, "message": "Làm mới Token thành công" }
  }
  ```
- **Lỗi:**
  - `401` – `UNAUTHENTICATED`: Nếu Refresh Token cũng đã hết hạn hoặc không hợp lệ. Khi này Frontend bắt buộc phải đá người dùng ra màn hình Đăng nhập.

### 1.4. Quên mật khẩu (Forgot Password)

- **Method:** `POST /auth/forgot-password`
- **Mô tả:** Gửi email chứa mã UUID đến hộp thư. Mã có hiệu lực **15 phút**.
- **Request Body:**
  ```json
  { "email": "user@gmail.com" }
  ```
- **Response (200 OK):**
  ```json
  {
    "status": { "code": 200, "message": "Mã khôi phục đã được gửi đến email của bạn. Vui lòng kiểm tra hộp thư.", ... },
    "data": null
  }
  ```
- **Lưu ý bảo mật:** Hệ thống luôn trả về 200 (kể cả email không tồn tại) để không lộ ra tài khoản nào đang đăng ký.

### 1.5. Đặt lại mật khẩu (Reset Password)

- **Method:** `POST /auth/reset-password`
- **Mô tả:** FE nhận `token` từ email → gửi kèm mật khẩu mới.
- **Request Body:**
  ```json
  {
    "token": "uuid-token-nhận-từ-email",
    "newPassword": "newpassword123"
  }
  ```
- **Validation:** `newPassword` tối thiểu 6 ký tự.
- **Response (200 OK):**
  ```json
  {
    "status": { "code": 200, "message": "Mật khẩu đã được đặt lại thành công. Vui lòng đăng nhập lại.", ... },
    "data": null
  }
  ```
- **Lỗi có thể xảy ra:**
  - `400` – Token không hợp lệ hoặc không tồn tại.
  - `400` – Token đã hết hạn (quá 15 phút).

---

## 2. MODULE PROFILE CÁ NHÂN (`/users/profile`)

> **Yêu cầu:** Gửi kèm Token trong Header `Authorization: Bearer <token>` hoặc Cookie.

### 2.1. Lấy thông tin tài khoản hiện tại (Get My Profile)

- **Method:** `GET /users/profile`
- **Response (200 OK):**
  ```json
  {
    "status": { "code": 200, "message": "Thành công", ... },
    "data": {
      "id": 1,
      "email": "user@gmail.com",
      "fullName": "Nguyen Van A",
      "phone": "0987654321",
      "avatarUrl": "https://...",
      "role": "USER",
      "status": "ACTIVE",
      "gender": "MALE",
      "dateOfBirth": "2000-01-21",
      "address": "123 Đường ABC, Hà Nội",
      "createdAt": "2024-01-01T00:00:00"
    }
  }
  ```

### 2.2. Cập nhật thông tin cơ bản (Update Profile)

- **Method:** `PUT /users/profile`
- **Request Body:**
  ```json
  {
    "fullName": "Nguyen Van B",
    "phone": "0987654321",
    "gender": "MALE",
    "dateOfBirth": "2000-01-21",
    "address": "123 Đường ABC, Hà Nội"
  }
  ```
- **Validation:**
  - `fullName`, `phone`: **Bắt buộc**.
  - `gender`, `dateOfBirth`, `address`: **Tuỳ chọn** (nullable). Nếu FE không gửi → hệ thống giữ nguyên giá trị cũ.
  - Giá trị hợp lệ của `gender`: `MALE`, `FEMALE`, `OTHER`.
  - `phone`: đúng chuẩn `(0|+84)[0-9]{9}`.
- **Response (200 OK):**
  ```json
  {
    "status": { "code": 200, "message": "Profile updated successfully", ... },
    "data": {
      "id": 1, "email": "...", "fullName": "Nguyen Van B",
      "gender": "MALE", "dateOfBirth": "2000-01-21", "address": "..."
    }
  }
  ```

### 2.3. Cập nhật Ảnh đại diện (Update Avatar)

- **Method:** `PUT /users/profile/avatar`
- **Content-Type:** `multipart/form-data`
- **Param:** `file` (Multipart image file — JPG/PNG)
- **Response (200 OK):**
  ```json
  {
    "status": { "code": 200, "message": "Avatar updated successfully", ... },
    "data": { "id": 1, "avatarUrl": "https://res.cloudinary.com/...", ... }
  }
  ```

### 2.4. Đổi mật khẩu (Change Password)

- **Method:** `PUT /users/profile/password`
- **Request Body:**
  ```json
  {
    "oldPassword": "password123",
    "newPassword": "newpassword123"
  }
  ```
- **Validation:** `newPassword` tối thiểu 6 ký tự.
- **Response (200 OK):**
  ```json
  {
    "status": { "code": 200, "message": "Password changed successfully", ... },
    "data": null
  }
  ```
- **Lỗi:** `400` – Mật khẩu cũ không đúng.

### 2.5. Tự khóa / Xóa tài khoản của chính mình (Soft Delete)

- **Method:** `DELETE /users/profile-block`
- **Mô tả:** Chuyển trạng thái User thành `LOCKED` (không xóa khỏi DB). Tài khoản bị khóa sẽ không thể đăng nhập.
- **Response (200 OK):**
  ```json
  {
    "status": { "code": 200, "message": "Account deleted/locked successfully", ... },
    "data": null
  }
  ```

---

## 3. MODULE ADMIN (`/admin`)

> **Yêu cầu:** Gửi kèm Token trong Header `Authorization: Bearer <token>` hoặc Cookie. Role của Token bắt buộc phải là `ADMIN` (Lỗi 403 nếu cố tình truy cập bằng Role khác).

### 3.1. Xem danh sách người dùng (Phân trang & Lọc)

- **Method:** `GET /admin/get-all-users`
- **Query Params (Tuỳ chọn):**
  - `page`: Trang hiện tại (Mặc định `0`).
  - `size`: Số dòng trên trang (Mặc định `10`).
  - `search`: Từ khóa tìm kiếm (lọc theo Email, Họ tên, SĐT).
  - `role`: Lọc theo vai trò (`ADMIN`, `ORGANIZER`, `STAFF`, `USER`).
  - `status`: Lọc theo trạng thái (`ACTIVE`, `LOCKED`).
  - `sort`: Cú pháp sắp xếp `<tên_cột>,<hướng>` (ví dụ: `email,asc` hoặc `createdAt,desc`. Mặc định `createdAt,desc`).
- **Response (200 OK):**
  ```json
  {
    "status": { "code": 200, "message": "Lấy danh sách người dùng thành công", ... },
    "data": {
      "currentPage": 0,
      "totalPages": 5,
      "pageSize": 10,
      "totalElements": 48,
      "data": [
        {
          "id": 1,
          "email": "user@gmail.com",
          "fullName": "Nguyen Van A",
          "phone": "0987654321",
          "avatarUrl": "https://...",
          "role": "USER",
          "status": "ACTIVE",
          "createdAt": "2024-01-01T00:00:00"
        }
      ]
    }
  }
  ```

### 3.2. Thay đổi trạng thái người dùng (Khóa / Mở Khóa)

- **Method:** `PUT /admin/users/{id}/status`
- `{id}`: ID của người dùng cần thay đổi trạng thái
- **Request Body:**
  ```json
  {
    "status": "LOCKED"
  }
  ```
- **Response (200 OK):**
  ```json
  {
    "status": { "code": 200, "message": "Cập nhật trạng thái người dùng thành công", ... },
    "data": null
  }
  ```
- **Lỗi:** `400` - `CANNOT_MODIFY_YOURSELF`: Bạn không thể tự khóa của chính mình.

### 3.3. Phân quyền người dùng (Thăng cấp / Giáng cấp)

- **Method:** `PUT /admin/users/{id}/role`
- **Request Body:**
  ```json
  {
    "role": "STAFF"
  }
  ```
- **Response (200 OK):**
  ```json
  {
    "status": { "code": 200, "message": "Cập nhật quyền người dùng thành công", ... },
    "data": null
  }
  ```
- **Lỗi:** `400` - `CANNOT_MODIFY_YOURSELF`: Bạn không thể tự đổi quyền của chính mình.

---

### 3.4. Xóa tài khoản vĩnh viễn (Dành cho Super Admin)

- **Method:** `DELETE /admin/users/{idUser}/delete`
- **Mô tả:** Xóa hoàn toàn một tài khoản ra khỏi hệ thống.
- **Quyền:** `ADMIN` (Super Admin).
- **Response (200 OK):**
  ```json
  {
    "status": {
      "code": 200,
      "message": "Xóa tài khoản thành công",
      "label": "API_STATUS_SUCCESS"
    },
    "data": null
  }
  ```

---

## 4. MODULE EVENTS (`/events`) - PHASE 5

> **Ghi chú:** Module chính để quản lý Sự kiện (Lọc danh sách, Xem chi tiết, Tạo mới, Sửa, Đổi trạng thái, Xóa).

### 4.1. Lấy danh sách Sự Kiện (Public/Admin/Organizer)

- **Method:** `GET /public/events-all`
- **Quyền:** Public (Ai cũng có thể vào xem, tuy nhiên có thể xài cho Admin để xem list sự kiện toàn bộ qua tham số status).
- **Query Params (Tuỳ chọn):**
  - `page`: Trang hiện tại (Mặc định `0`).
  - `size`: Số bản ghi (Mặc định `10`).
  - `keyword`: Tìm theo Tên hoặc Địa điểm sự kiện.
  - `category`: Lọc theo danh mục.
  - `status`: Lọc theo Trạng thái (DRAFT, PUBLISHED...).
  - `organizerId`: Bỏ vào ID của một Organizer cụ thể để lấy luồng sự kiện của họ. Nếu là Admin thì không cần truyền tham số này để lấy tất cả sự kiện.
  - `startTime`: Lọc sự kiện bắt đầu từ thời điểm này (Format chuẩn ISO: `2024-12-31T20:00:00`).
  - `endTime`: Lọc sự kiện kết thúc trước thời điểm này (Format chuẩn ISO).
  - `minPrice`: Giá vé tối thiểu.
  - `maxPrice`: Giá vé tối đa.
  - `isFree`: (Boolean) `true` để lọc vé miễn phí (giá = 0), `false` để lọc vé có phí (giá > 0).
- **Bảo mật ngầm:** Nếu 1 tài khoản ẩn danh gọi API này nhưng truyền `status=DRAFT`, hệ thống tự động khóa lại và sẽ chèn ép chọc lệnh thành `status=PUBLISHED` nhằm ngăn chặn tính trạng Hacker rình rập xem lén sự kiện mật.
- **Response (200 OK):**
  ```json
  {
    "status": { "code": 200, "message": "Lấy danh sách sự kiện thành công" },
    "data": {
      "currentPage": 0,
      "totalPages": 2,
      "pageSize": 10,
      "totalElements": 15,
      "data": [
        {
          "id": 1,
          "organizerId": 12,
          "organizerName": "Công ty TNHH Sự Kiện Giải Trí",
          "title": "Đêm nhạc Sân Hàng Đẫy",
          "category": "Âm Nhạc",
          "shortDescription": "Đêm nhạc sôi động đầu năm",
          "bannerUrl": "https://cloudinary.com/event_banner.jpg",
          "registrationStartTime": "2024-12-01T00:00:00",
          "registrationEndTime": "2024-12-30T23:59:00",
          "startTime": "2024-12-31T20:00:00",
          "endTime": "2025-01-01T00:30:00",
          "location": "Sân vận động Hàng Đẫy, Hà Nội",
          "capacity": 1000,
          "ticketPrice": 500000.0,
          "status": "PUBLISHED",
          "ticketsSold": 150,
          "createdAt": "2024-05-15T10:00:00",
          "updatedAt": "2024-05-15T12:00:00"
        }
      ]
    }
  }
  ```

### 4.2. Lấy chi tiết Sự kiện (Public)

- **Method:** `GET /events/{id}`
- **Quyền:** Public — Không cần Token.
- **Điểm đặc biệt so với API Danh sách:** Response của API này có thêm trường **`schedules`** chứa toàn bộ lịch trình con của sự kiện, được sắp xếp tăng dần theo `startTime`. Frontend dùng để render Timeline / Agenda cho trang chi tiết.
- **Response (200 OK):**
  ```json
  {
    "status": { "code": 200, "message": "Lấy thông tin sự kiện thành công" },
    "data": {
      "id": 1,
      "organizerId": 12,
      "organizerName": "Công ty TNHH Sự Kiện Giải Trí",
      "title": "Đêm nhạc Sân Hàng Đẫy",
      "category": "Âm Nhạc",
      "shortDescription": "Đêm nhạc sôi động đầu năm",
      "description": "Chi tiết toàn bộ dàn khách mời hạng A...",
      "bannerUrl": "https://cloudinary.com/event_banner.jpg",
      "registrationStartTime": "2024-12-01T00:00:00",
      "registrationEndTime": "2024-12-30T23:59:00",
      "startTime": "2024-12-31T19:00:00",
      "endTime": "2025-01-01T00:30:00",
      "location": "Hà Nội",
      "capacity": 1000,
      "ticketPrice": 500000.0,
      "status": "PUBLISHED",
      "ticketsSold": 150,
      "createdAt": "2024-05-15T10:00:00",
      "updatedAt": "2024-05-15T12:00:00",
      "schedules": [
        {
          "id": 101,
          "eventId": 1,
          "title": "Khai mạc & Check-in",
          "startTime": "2024-12-31T19:00:00",
          "endTime": "2024-12-31T20:00:00",
          "description": "Quét mã QR và ổn định chỗ ngồi"
        },
        {
          "id": 102,
          "eventId": 1,
          "title": "Biểu diễn ca nhạc chính",
          "startTime": "2024-12-31T20:00:00",
          "endTime": "2024-12-31T23:30:00",
          "description": "Dàn nhạc và nghệ sĩ biểu diễn"
        },
        {
          "id": 103,
          "eventId": 1,
          "title": "Bế mạc & Countdown",
          "startTime": "2024-12-31T23:30:00",
          "endTime": "2025-01-01T00:30:00",
          "description": "Đếm ngược và pháo hoa chào năm mới"
        }
      ]
    }
  }
  ```
- **Lỗi:** `404` - `EVENT_NOT_FOUND` (Nếu ID không tồn tại).

### 4.3. Tạo Sự Kiện Mới (Organizer)

- **Method:** `POST /create-event`
- **Quyền:** `ORGANIZER`.
- **Content-Type:** `multipart/form-data`.
- **Tham số Form-Data:**
  - `title` (String, Bắt buộc)
  - `category` (String, Bắt buộc)
  - `shortDescription` (String, Bắt buộc)
  - `description` (String, Bắt buộc)
  - `capacity` (Integer, Bắt buộc, tối thiểu `1`)
  - `ticketPrice` (BigDecimal, Bắt buộc, `0` nếu miễn phí)
  - `registrationStartTime` (ISO DateTime, Tùy chọn)
  - `registrationEndTime` (ISO DateTime, Tùy chọn — mặc định lấy `startTime`)
  - `startTime` (ISO DateTime, Bắt buộc)
  - `endTime` (ISO DateTime, Bắt buộc)
  - `location` (String, Bắt buộc)
  - `bannerFile` (MultipartFile, Tùy chọn — tự upload lên Cloudinary)
  - **`schedules`** (JSON Array, **Tùy chọn**): Danh sách lịch trình con tạo kèm theo. Xem định dạng bên dưới.
- **Định dạng `schedules` (gửi kèm dưới dạng JSON string trong FormData):**
  ```json
  [
    {
      "title": "Khai mạc & Check-in",
      "startTime": "2024-12-31T19:00:00",
      "endTime": "2024-12-31T20:00:00",
      "description": "Quét mã QR tại cổng chính"
    },
    {
      "title": "Biểu diễn chính",
      "startTime": "2024-12-31T20:00:00",
      "endTime": "2024-12-31T23:30:00",
      "description": "Dàn nhạc biểu diễn"
    }
  ]
  ```
- **Logic Ngầm:** Trạng thái mặc định là `DRAFT`.
- **Lỗi:** `400` - `TIME_OUT_OF_EVENT_BOUNDS` nếu lịch trình vượt khung giờ sự kiện.
- **Response (200 OK):** Trả về EventResponse đầy đủ, bao gồm cả `schedules`.
  ```json
  {
    "status": { "code": 200, "message": "Tạo sự kiện bản nháp thành công" },
    "data": { "id": 2, "title": "...", "status": "DRAFT", "schedules": [...] }
  }
  ```

### 4.4. Cập nhật nội dung Sự Kiện (Organizer)

- **Method:** `PUT /update-event/{id}`
- **Quyền:** `ORGANIZER` — phải là chủ sự kiện.
- **Content-Type:** `multipart/form-data` (giống form Tạo sự kiện).
- **Ràng buộc:**
  - Không được giảm `capacity` nhỏ hơn số vé đã bán (`CAPACITY_CANNOT_BE_LESS_THAN_SOLD`).
- **Đồng bộ lịch trình (`schedules`):**
  - **Không gửi field `schedules`** → Giữ nguyên lịch trình cũ.
  - **Gửi `schedules: []`** → Xóa toàn bộ lịch trình.
  - **Gửi `schedules` có dữ liệu** → Đồng bộ hoàn toàn:
    - `id = null` → Thêm mới lịch trình.
    - `id` có giá trị → Cập nhật lịch trình đó.
    - Lịch trình đang có trong DB nhưng không xuất hiện trong mảng → **Tự động xóa**.
- **Response (200 OK):** Trả về EventResponse đầy đủ kèm `schedules` sau khi đồng bộ.
  ```json
  {
    "status": { "code": 200, "message": "Cập nhật sự kiện thành công" },
    "data": { "id": 2, "title": "...", "schedules": [...] }
  }
  ```

### 4.5. Đổi trạng thái Sự Kiện / Công bố (Organizer / Admin)

- **Method:** `PUT /events/{id}/status`
- **Quyền:** `ORGANIZER` hoặc `ADMIN`.
- **Query Params:**
  - `newStatus`: Trạng thái mục tiêu (`PUBLISHED`, `CLOSED`, `CANCELLED`...).
- **Chốt Chặn Bán Vé Lậu (Payment Guard):** Nếu sự kiện thu phí (`ticket_price > 0`) và cố tình đổi từ `DRAFT` sang `PUBLISHED`, Backend sẽ đá văng yêu cầu nếu tài khoản Organizer thiếu Link thông tin ngân hàng PayOS (`PaymentConfigs`). Lệnh này bảo kê luồng tài chính của Khách Hàng.
- Nếu sự kiện đang ở trạng thái `PUBLISHED` mà sửa giá vé từ free sang có thu phí thì báo lỗi `MUST_SETUP_PAYMENT_FIRST` nếu chưa setup link ngân hàng `PAYOS`
- **Response (200 OK):**
  ```json
  {
    "status": {
      "code": 200,
      "message": "Cập nhật trạng thái sự kiện thành: PUBLISHED"
    },
    "data": null
  }
  ```
- **Lỗi:** `400` - `MUST_SETUP_PAYMENT_FIRST` (Bắt phải gắn ID PayOS trước).

### 4.6. Xóa event Hệ Thống (Smart Delete)

- **Method:** `DELETE /delete-event/{id}`
- **Quyền:** `ORGANIZER` hoặc `ADMIN`.
- **Logic Xoá Kép (Smart Delete):**
  - **Trường hợp 1:** Nếu CSDL trắng xóa (Chưa bán được Vé nào) $\Rightarrow$ SQL thi triển lệnh XÓA CỨNG (Hard-Delete).
  - **Trường hợp 2:** Nếu bị vấy đen (Có người Mua Vé) $\Rightarrow$ SQL quay ngoắt thi triển lệnh XÓA MỀM (Soft-Delete) giáng trạng thái dập về `CANCELLED`. Vé đã lỡ chốt đơn trong ví Khách hàng vẫn hiển thị nhưng không mua thêm được nữa.
- **Response (200 OK):**
  ```json
  {
    "status": { "code": 200, "message": "Thao tác loại bỏ sự kiện thành công" },
    "data": null
  }
  ```

### 4.7. Lấy danh sách Lịch trình (Public — Dự phòng)

> **Lưu ý:** Lịch trình đã được trả về trong `GET /public/event-detail/{id}` (field `schedules`). Endpoint này chỉ dùng khi cần lấy riêng lịch trình mà không fetch toàn bộ sự kiện.

- **Method:** `GET /public/events/{eventId}/schedules`
- **Quyền:** Public.
- **Response (200 OK):**
  ```json
  {
    "status": { "code": 200, "message": "Lấy danh sách lịch trình thành công" },
    "data": [
      {
        "id": 1,
        "eventId": 1,
        "title": "Khai mạc",
        "startTime": "2024-12-31T20:00:00",
        "endTime": "2024-12-31T20:30:00",
        "description": "Lễ khai mạc và tuyên bố sự kiện"
      }
    ]
  }
  ```

> ~~4.8 POST /events/{eventId}/schedules~~  
> ~~4.9 PUT /events/{eventId}/schedules~~  
> ~~4.10 DELETE /events/{eventId}/schedules~~  
> ~~4.11 DELETE /events/{eventId}/schedules/{scheduleId}~~  
> **Đã được gộp vào 4.3 (Tạo) và 4.4 (Sửa) — Truyền field `schedules` trong FormData của sự kiện.**

### 4.8. Lấy danh sách giao dịch thanh toán của Sự Kiện (Organizer/Admin)

- **Method:** `GET /events/{eventId}/payments`
- **Quyền:** `ADMIN` hoặc `ORGANIZER` tạo ra sự kiện đó.
- **Mô tả:** Trả về danh sách chi tiết các lượt mua vé thành công, thất bại, đang chờ thanh toán.
- **Query Params (Tuỳ chọn):**
  - `page`: Trang hiện tại (Mặc định `0`).
  - `size`: Số bản ghi (Mặc định `10`).
  - `keyword`: Tìm kiếm theo mã đơn hàng, email, họ tên người mua.
  - `status`: Lọc theo trạng thái thanh toán (VD: `SUCCESS`, `PENDING`, `FAILED`, `CANCELLED`).
  - `from`: Lọc từ ngày (Format chuẩn: `2024-05-01`).
  - `to`: Lọc đến ngày (Format chuẩn: `2024-05-31`).
- **Response (200 OK):**
  ```json
  {
    "status": { "code": 200, "message": "Lấy danh sách thanh toán thành công" },
    "data": {
      "currentPage": 0,
      "totalPages": 1,
      "pageSize": 10,
      "totalElements": 2,
      "data": [
        {
          "paymentId": 12,
          "orderCode": "ORDER-123456",
          "amount": 500000.0,
          "status": "SUCCESS",
          "paidAt": "2024-05-10T15:30:00",
          "createdAt": "2024-05-10T15:28:00",
          "registrationCode": "REG-ABCXYZ",
          "userEmail": "khachhang@gmail.com",
          "userFullName": "Nguyen Van A"
        }
      ]
    }
  }
  ```


---

## 5. PHÂN CÔNG TỔ CHỨC (STAFF DELEGATION)

Chỉ có Organizer mới được phép phân công nhân viên kiểm vé (Staff) vào Sự kiện do chính mình tổ chức.

### 5.1. Lấy toàn bộ danh sách Staff hệ thống (Admin & Organizer)

- **Method:** `GET /users/staff-list`
- **Mô tả:** Lấy rổ nhân viên có quyền `STAFF` đang `ACTIVE` trên toàn hệ thống để Organizer chọn lựa lúc phân công.
- **Quyền:** `ADMIN` hoặc `ORGANIZER`.
- **Response (200 OK):**
  ```json
  {
    "status": { "code": 200, "message": "Lấy rổ nhân viên thành công" },
    "data": [
      {
        "id": 15,
        "fullName": "Nguyễn Văn Bảo Vệ",
        "email": "baove@gmail.com",
        "phone": "0987654321",
        "avatarUrl": "https://cloudinary.com/avatar1.jpg"
      }
    ]
  }
  ```

### 5.2. Lấy danh sách Staff đang phục vụ một Sự kiện

- **Method:** `GET /events/{eventId}/staffs`
- **Mô tả:** Trả về danh sách những người Staff đã được gắn vào thẻ phụ trách sự kiện.
- **Quyền:** `ADMIN`, `ORGANIZER` hoặc chính thức làm `STAFF` được phân công trong sự kiện đó. Normal User nếu cố gọi sẽ ăn lỗi 403 Access Denied.
- **Response (200 OK):** (Trả về Array tương tự API 5.1)

### 5.3. Đồng bộ phân công Staff vào Sự kiện (Organizer)

- **Method:** `PUT /events/{eventId}/staffs`
- **Mô tả:** Chỉ cần truyền lên mảng `[ID1, ID2...]`. Backend tự động đối chiếu: **Có trong DB mà gửi thiếu thì XOÁ**, **Chưa có thì THÊM MỚI**, **Đã có thì GIỮ NGUYÊN**. Không cần gọi riêng lẻ lệnh xoá/thêm.
- **Quyền:** `ORGANIZER` hiện đang làm chủ Sự kiện này.
- **Request Body:** List danh sách ID nhân viên:
  ```json
  [15, 18, 22]
  ```
- **Lỗi:**
  - `400` - `INVALID_STAFF_ROLE`: Bắt quả tang có ID truyền lên không mang thẻ `STAFF` hoặc tài khoản nhân viên đang bị Khoá.

### 5.4. Xoá lẻ thẻ phân công của 1 nhân viên (Organizer)

- **Method:** `DELETE /events/{eventId}/staffs/{staffId}`
- **Mô tả:** Xoá thẻ phụ trách (Staff Assignment) của nhân viên này ra khỏi Event (hỗ trợ cho chức năng xoá nhanh nếu Organizer ấn nhầm trên UI thay vì gửi mảng để đồng bộ).
- **Quyền:** `ORGANIZER` hiện đang làm chủ Sự kiện này.

### 5.5. Xem danh sách Sự kiện phân công của 1 Staff (Dành cho Admin/Organizer/Staff)

- **Method:** `GET /events/assigned-to-staff/{staffId}`
- **Mô tả:** 
  - Nhân viên (Staff) tự truyền ID của mình để xem danh sách các sự kiện được phân công.
  - Ban quản trị (Admin) hoặc Người tổ chức (Organizer) có thể truyền ID của bất kỳ Staff nào để quản lý lịch trình.
- **Quyền:** `ADMIN`, `ORGANIZER` hoặc `STAFF` (chỉ được xem của chính mình).
- **Query Params:**
  - `page`: Trang hiện tại (Mặc định `0`).
  - `size`: Kích thước (Mặc định `10`).
- **Response (200 OK):** Trả về danh sách Sự kiện (EventResponse) có phân trang `PageResponse`.

---

## 6. MODULE ĐĂNG KÝ VÀ THANH TOÁN (PHASE 7)

### 6.1. Thiết lập Cấu hình Thanh Toán PayOS (Organizer)

- **Method:** `PUT /payment-configs/me`
- **Quyền:** `ORGANIZER`.
- **Mô tả:** Organizer phải cấu hình khóa PayOS để có thể xuất bản Sự kiện thu phí.
- **Request Body:**
  ```json
  {
    "clientId": "your-client-id",
    "apiKey": "your-api-key",
    "checksumKey": "your-checksum-key"
  }
  ```
- **Response (200 OK):**
  ```json
  {
    "status": {
      "code": 200,
      "message": "Cập nhật cấu hình thanh toán thành công"
    },
    "data": {
      "isActive": true
    }
  }
  ```

### 6.2. Xem Cấu hình Thanh Toán của bản thân

- **Method:** `GET /payment-configs/me`
- **Quyền:** `ORGANIZER`.

### 6.3. Đăng ký tham gia Sự kiện (User)

- **Method:** `POST /registrations`
- **Quyền:** Tài khoản đã xác thực (`USER`, `ORGANIZER`, `ADMIN`).
- **Mô tả:** Hệ thống tự tách luồng: Nếu Vé Miễn phí $\Rightarrow$ Thành công luôn (Sinh Dummy Payment giá 0đ). Nếu Vé Trả phí $\Rightarrow$ Trả về Link Checkout PayOS.
- **Ràng buộc Mạng:** Số vé (`soldTickets`) tự động tăng lên 1 để bảo vệ giỏ hàng. Quá 5 phút không thanh toán, hệ thống dọn đơn và tự trả lại sức chứa.
- **Request Body:**
  ```json
  {
    "eventId": 1
  }
  ```
- **Response (201 Created):**
  ```json
  {
    "status": {
      "code": 201,
      "message": "Đăng ký thành công. Vui lòng thanh toán nếu cần."
    },
    "data": {
      "registrationId": 100,
      "eventId": 1,
      "eventStartTime": "2024-12-31T20:00:00",
      "userId": 5,
      "registrationCode": "ABC123XY",
      "status": "PENDING_PAYMENT",
      "createdAt": "2024-05-30T10:00:00",
      "checkoutUrl": "https://payos.vn/checkout/ORDER-XXXXX", // đây là link thanh toán PAYOS
      "orderCode": "ORDER-XXXXX",
      "amount": 500000.0
    }
  }
  ```
- **Lỗi:**
  - `400` - `EVENT_NOT_STARTED`: Sự kiện đã kết thúc hoặc bắt đầu diễn ra.
  - `400` - `TICKET_SOLD_OUT`: Hết chỗ.
  - `400` - `ALREADY_REGISTERED`: Bạn đã mua vé này rồi.
  - `404` - `PAYMENT_CONFIG_NOT_FOUND`: Ban tổ chức chưa cấu hình cổng thanh toán.

### 6.4. Hủy vé (Chỉ áp dụng cho vé Free)

- **Method:** `POST /api/v1/registrations/{id}/cancel`
- **Quyền:** Tài khoản đã mua vé.
- **Ràng buộc:** Chỉ cho phép ủy đối với các vé miễn phí. Nếu vé có tính phí sẽ báo lỗi.
- **Request Body:** Không có Body JSON. (Chỉ truyền biến {id} là ID của Vé đăng ký ngay trên URL, ví dụ: `POST /api/v1/registrations/100/cancel`)
- **Response (200 OK):**
  ```json
  {
    "status": {
      "code": 200,
      "message": "Đã hủy vé thành công. Hệ thống đã nhả lại suất trống cho những người khác.",
      "label": "API_STATUS_SUCCESS"
    },
    "data": null
  }
  ```
- **Lỗi:** `400` - `CANNOT_CANCEL_PAID_TICKET`: Không thể hủy vé trả phí.

### 6.5. Đón Webhook từ Hệ thống PayOS

- **Method:** `POST /api/v1/payments/webhook`
- **Quyền:** Mở công khai nhưng bảo mật bằng cơ chế Checksum `HMAC-SHA256` bằng thư viện `payos-java` nội bộ.
- **Mô tả:** Nhận Json báo cáo giao dịch từ PayOS dội về. Tự động kiểm tra HMAC-SHA256, so sánh `orderCode` để gán trạng thái (`SUCCESS` hoặc `FAILED`) vào Database. Đồng bộ cấp thẻ thẻ Ticket kèm mã QR nếu như được thanh toán tiền thành công. Tự nhả ghế nếu đơn quá hạn hoặc lỗi. Đan xen cơ chế chống đua lệnh Database `@Lock` để bảo đảm Idempotency.
- **Request Body mẫu (Webhook PayOS gửi lên):**
  ```json
  {
    "code": "00",
    "desc": "success",
    "success": true,
    "data": {
      "orderCode": 172901235612,
      "amount": 500000.0,
      "description": "Thanh toan ve Son Tung",
      "accountNumber": "0123xxx",
      "transactionDateTime": "2024-05-30 08:00:00"
    },
    "signature": "hmac_sha256_hash_here...."
  }
  ```
- **Lưu lý quan trọng (Frontend không cần quan tâm):**
  - **Idempotency:** Nếu chọc vào API Hook 2 lần liên tục cho cùng 1 đơn hàng hóa, hệ thống sẽ phát tín hiệu ngầm chặn ngay và không cho phép chốt xuất ra Tới 2 Vé Lậu.
  - **Multi-Tenant:** API có phân biệt nhiều tài khoản ngân hàng riêng biệt ứng với từng Organizer khác nhau.

- **Response trả về mẫu (Báo hiệu cho PayOS biết là Server đã nhận được tiền):**

```json
{
  "code": 200,
  "message": "success"
}
```

### 6.6. Đồng bộ trạng thái đơn hàng chủ động (Sync)

- **Method:** `GET /api/v1/payments/sync/{orderCode}`
- **Mô tả:** Trong trường hợp Webhook của PayOS bị lỗi mạng (hoặc khi chạy ở môi trường localhost nhưng Ngrok bị sập), Frontend có thể gọi API này để hệ thống chủ động chọc lên PayOS bằng SDK v2 lấy tình trạng đơn hàng về và xé vé cho khách ngay lập tức (Nếu status = PAID).
- **Quyền:** Public.
- **Response (200 OK):**
  ```json
  {
    "status": {
      "code": 200,
      "message": "Kiểm tra thấy đơn hàng đã thanh toán. Đã sinh vé!",
      "label": "API_STATUS_SUCCESS"
    },
    "data": "PAID_AND_TICKET_CREATED"
  }
  ```

### 6.7. Lấy danh sách các đơn hàng chờ thanh toán (My pending payments)

- **Method:** `GET /api/v1/payments/my-tickets-pending`
- **Mô tả:** Lấy danh sách các đơn hàng đang ở trạng thái chờ thanh toán để người mua có thể bấm lại link PayOS tiếp tục nộp tiền nếu lỡ tắt mất trình duy`ệt.
- **Quyền:** Đã đăng nhập (`USER`, `ORGANIZER`, `ADMIN`).
- **Response (200 OK):**
  ```json
  {
    "status": {
      "code": 200,
      "message": "Lấy danh sách các khoản chờ thanh toán thành công",
      "label": "API_STATUS_SUCCESS"
    },
    "data": [
      {
        "registrationId": 105,
        "eventId": 1,
        "eventStartTime": "2024-12-31T20:00:00",
        "userId": 5,
        "registrationCode": "REG-ABCXYZ",
        "status": "PENDING_PAYMENT",
        "createdAt": "2024-05-30T10:00:00",
        "checkoutUrl": "https://payos.vn/checkout/...",
        "orderCode": "ORDER-...",
        "amount": 500000.0
      }
    ]
  }
  ```

---

## 7. MODULE KIỂM SOÁT VÉ VÀ CHECK-IN (PHASE 9)

### 7.1. Lấy danh sách Vé của cá nhân (My Tickets)

- **Method:** `GET /api/v1/tickets/my-tickets`
- **Mô tả:** Người dùng đã mua vé thành công (hoặc nhận vé miễn phí) có thể gọi API này để xem toàn bộ vé trong kho vé cá nhân của mình, kèm mã QR để đi quét qua cửa.
- **Quyền:** Lệnh kẹp thẻ `Authorization: Bearer <Token>`. Dành cho bất kỳ tài khoản có quyền `USER`, `ORGANIZER`, `ADMIN`.
- **Response (200 OK):**
  ```json
  {
    "status": {
      "code": 200,
      "message": "Lấy danh sách vé thành công"
    },
    "data": {
      "currentPage": 0,
      "totalPages": 1,
      "pageSize": 10,
      "totalElements": 2,
      "data": [
        {
          "id": 1,
          "qrCode": "c4d32a8-12ab-...",
          "hashedQrCode": null,
          "eventId": 1,
          "eventTitle": "Lễ hội Âm nhạc mùa hè",
          "ticketPrice": 500000.0,
          "userId": 5,
          "registrationId": 100,
          "userEmail": "khachhang@gmail.com",
          "userFullName": "Nguyen Van A",
          "status": "ACTIVE",
          "checkedInAt": null,
          "issuedAt": "2024-05-20T10:00:00"
        }
      ]
    }
  }
  ```

### 7.1.5. Lấy lịch sử Check-in (Check-in History)

- **Method:** `GET /api/v1/events/{eventId}/checkin-history`
- **Mô tả:** Lấy danh sách lịch sử các mã QR đã được quét qua cổng.
- **Quyền:** `ADMIN`, hoặc `ORGANIZER` tạo ra sự kiện, hoặc `STAFF` được phân công.
- **Query Params:**
  - `page`: Trang hiện tại (Mặc định `0`).
  - `size`: Kích thước (Mặc định `10`).
  - `search`: Từ khóa tìm kiếm (Lọc theo tên khách, email, mã đăng ký, mã băm QR). (Tùy chọn)
  - `method`: Phương thức check-in (`QR` hoặc `MANUAL`). (Tùy chọn)
  - `fromDate`: Thời gian bắt đầu (Ví dụ: `2024-05-01T00:00:00`). (Tùy chọn)
  - `toDate`: Thời gian kết thúc (Ví dụ: `2024-05-31T23:59:59`). (Tùy chọn)
- **Response (200 OK):**
  ```json
  {
    "status": { "code": 200, "message": "Lấy lịch sử Check-in thành công" },
    "data": {
      "currentPage": 0,
      "totalPages": 1,
      "totalElements": 5,
      "data": [
        {
          "id": 1,
          "hashedQrCode": "e3b0c4429...",
          "customerEmail": "khachhang@gmail.com",
          "customerFullName": "Nguyen Van A",
          "checkedInAt": "2024-05-20T10:05:00",
          "staffEmail": "staff@gmail.com",
          "checkInMethod": "QR"
        }
      ]
    }
  }
  ```

### 7.2. Lấy danh sách Toàn bộ Vé của Sự kiện

- **Method:** `GET /api/v1/events/{eventId}/tickets`
- **Mô tả:** Phục vụ giao diện Quản lý Khách tham gia của Ban Tổ Chức. Hiển thị danh sách vé, thông tin cá nhân khách đã mua và tình trạng Vé (`ACTIVE`, `USED`, `INVALID`). Có thể tìm kiếm theo mã QR code, mã vé thủ công dành cho Staff check-in tại sự kiện.
- **Quyền:** Lệnh kẹp thẻ `Authorization: Bearer <Token>`. Yêu cầu Role là `ADMIN` hoặc chính tác giả `ORGANIZER` tạo ra Sự kiện đó, hoặc `STAFF` được phân công vào sự kiện đó.
- **Query Params:**
  - `page`: Trang hiện tại (Mặc định `0`).
  - `size`: Kích thước (Mặc định `10`).
  - `search`: Từ khóa tìm kiếm (Tùy chọn - Lọc theo tên, email, số điện thoại, hoặc mã đăng ký).
- **Response (200 OK):**
  ```json
  {
    "status": { "code": 200, "message": "Lấy danh sách vé thành công" },
    "data": {
      "currentPage": 0,
      "totalPages": 2,
      "pageSize": 10,
      "totalElements": 20,
      "data": [
        {
          "id": 1,
          "qrCode": null,
          "hashedQrCode": "e3b0c44298fc1c149afbf4c8996fb92427ae41e4649b934ca495991b7852b855",
          "eventId": 1,
          "eventTitle": "Lễ hội Âm nhạc mùa hè",
          "ticketPrice": 500000.0,
          "userId": 5,
          "registrationId": 100,
          "registrationCode": "REG-ABCXYZ",
          "userEmail": "khachhang@gmail.com",
          "userFullName": "Nguyen Van A",
          "phoneNumber": "0912345678",
          "status": "ACTIVE",
          "checkedInAt": null,
          "issuedAt": "2024-05-20T10:00:00"
        }
      ]
    }
  }
  ```

### 7.3. Quét mã QR Check-in (Cổng Kiểm soát)

- **Method:** `POST /api/v1/checkin`
- **Mô tả:** API nối với Camera quẹt mã vạch trên điện thoại của Nhân viên an ninh (Staff) hoặc check-in thủ công qua danh sách vé.
- **Quyền:** `ADMIN`, hoặc `ORGANIZER` tạo ra sự kiện, hoặc `STAFF` được phân công vào sự kiện đó.
- **Request Body:**
  ```json
  {
    "qrCode": "c4d32a8-12ab-..."
  }
  ```
- **Response (200 OK - Cửa mở):**
  ```json
  {
    "status": {
      "code": 200,
      "message": "Check-in thành công! Cổng mở."
    },
    "data": {
      "id": 1,
      "qrCode": null,
      "hashedQrCode": "e3b0c44298fc1c14...",
      "eventId": 1,
      "eventTitle": "Lễ hội Âm nhạc mùa hè",
      "ticketPrice": 500000.0,
      "userId": 5,
      "registrationId": 100,
      "userEmail": "khachhang@gmail.com",
      "userFullName": "Nguyen Van A",
      "status": "USED",
      "checkedInAt": "2024-05-20T19:30:00",
      "issuedAt": "2024-05-20T10:00:00"
    }
  }
  ```
- **Các Mã Cảnh Báo An Ninh Cổng (Error Codes):**
  - `404` - `TICKET_NOT_FOUND`: Mã QR tào lao, không có trong cơ sở dữ liệu.
  - `400` - `TICKET_ALREADY_USED`: Vé giả hoặc có người đã xài vé này để vào cổng trước đó rồi. Đuổi khách hoặc bắt mua vé chui.
  - `400` - `TICKET_INVALID`: Vé này là hàng dỏm do khách đã chủ động chọn nút Hủy Đơn để hoàn vé rồi. Không cho vào.

### 7.4. Đồng bộ danh sách Check-in Offline (Batch Sync)

- **Method:** `POST /api/v1/checkin/batch`
- **Mô tả:** Đồng bộ hàng loạt mã QR (đã băm) mà Staff quét được lúc điện thoại mất mạng. Mỗi vé được xử lý như một giao dịch độc lập để chống đứt gãy.
- **Quyền:** `ADMIN`, `ORGANIZER`, hoặc `STAFF` được phân công.
- **Request Body:**
  ```json
  {
    "hashedQrCodes": [
      "e3b0c44298fc1c149afbf4c8996fb92427ae41e4649b934ca495991b7852b855",
      "b5d120a233... (Mã băm khác)"
    ]
  }
  ```
- **Response (200 OK):**
  ```json
  {
    "status": {
      "code": 200,
      "message": "Đồng bộ danh sách Check-in hoàn tất!"
    },
    "data": {
      "total": 2,
      "successfulCount": 1,
      "failedCount": 1,
      "successfulTickets": [
        {
          "id": 1,
          "hashedQrCode": "e3b0c442...",
          "status": "USED",
          "userFullName": "Nguyen Van A"
        }
      ],
      "errors": [
        {
          "hashedQrCode": "b5d120a233...",
          "errorMessage": "TICKET_ALREADY_USED"
        }
      ]
    }
  }
  ```

---

## 8. Dashboards Thống Kê (Dashboards)

### 8.1. Thống kê toàn hệ thống (Dành cho Admin)

- **Method:** `GET /dashboards/admin`
- **Quyền:** `ADMIN`
- **Mô tả:** Lấy dữ liệu tổng quan cho trang chủ Admin.
- **Response (200 OK):**
  ```json
  {
    "status": {
      "code": 200,
      "message": "Lấy thống kê Admin thành công"
    },
    "data": {
      "totalUsers": 1500,
      "totalOrganizers": 50,
      "totalEvents": 120,
      "totalTickets": 4500,
      "totalRevenue": 1500000000
    }
  }
  ```

### 8.2. Thống kê tổng quan Organizer

- **Method:** `GET /dashboards/organizer`
- **Quyền:** `ORGANIZER`
- **Mô tả:** Lấy các chỉ số hiệu suất cá nhân của Organizer (Statistic Cards).
- **Response (200 OK):**
  ```json
  {
    "data": {
      "totalEvents": 5,
      "totalTicketsSold": 1200,
      "totalRevenue": 250000000,
      "totalCheckedIn": 980
    }
  }
  ```

---

## 9. API BIỂU ĐỒ ADMIN (`/dashboards/admin/...`)

> Tất cả API trong nhóm này yêu cầu quyền `ADMIN`.
> Các API hỗ trợ lọc thời gian nhận tham số `from` và `to` dạng `yyyy-MM-dd`.
> Nếu không truyền, mặc định là **30 ngày gần nhất**.

### 9.1. Cơ cấu tài khoản (Donut Chart)

- **Method:** `GET /dashboards/admin/role-distribution`
- **Tác dụng**: Biểu đồ tròn thể hiện thị phần các loại tài khoản trên toàn hệ thống (ADMIN, ORGANIZER, STAFF, USER).
- **Query Params:** Không có
- **Response Data:**
  ```json
  [
    { "label": "ADMIN", "count": 5 },
    { "label": "ORGANIZER", "count": 45 },
    { "label": "STAFF", "count": 150 },
    { "label": "USER", "count": 1300 }
  ]
  ```

### 9.2. Tăng trưởng người dùng (Line Chart)

- **Method:** `GET /dashboards/admin/user-growth?from=2024-05-01&to=2024-05-31`
- **Tác dụng**: Biểu đồ đường thể hiện tốc độ gia tăng tài khoản (tách riêng USER và ORGANIZER) theo thời gian.
- **Query Params:** `from` (tùy chọn), `to` (tùy chọn)
- **Response Data:**
  ```json
  [
    { "date": "2024-05-01", "count": 10, "type": "USER" },
    { "date": "2024-05-01", "count": 2, "type": "ORGANIZER" },
    { "date": "2024-05-02", "count": 15, "type": "USER" }
  ]
  ```

### 9.3. Top 5 Organizer (Bar Chart)

- **Method:** `GET /dashboards/admin/top-organizers`
- **Tác dụng**: Biểu đồ cột ngang xếp hạng 5 nhà tổ chức có nhiều sự kiện nhất.
- **Query Params:** Không có
- **Response Data:**
  ```json
  [
    { "label": "Tên nhà tổ chức A", "count": 25 },
    { "label": "Tên nhà tổ chức B", "count": 18 }
  ]
  ```

### 9.4. Sự kiện mới theo Tháng (Column Chart)

- **Method:** `GET /dashboards/admin/monthly-events?from=2024-01-01&to=2024-12-31`
- **Tác dụng**: Biểu đồ cột dọc thống kê số lượng sự kiện được tạo mới trong từng tháng (dùng để thấy xu hướng mùa vụ).
- **Query Params:** `from` (tùy chọn), `to` (tùy chọn)
- **Response Data:**
  ```json
  [
    { "label": "2024-01", "count": 12 },
    { "label": "2024-02", "count": 20 }
  ]
  ```

### 9.5. Trạng thái sự kiện (Pie Chart)

- **Method:** `GET /dashboards/admin/event-status`
- **Tác dụng**: Biểu đồ tròn thể hiện tỉ lệ các trạng thái của sự kiện (Sắp diễn ra, Đang diễn ra, Đã kết thúc, Đã hủy).
- **Query Params:** Không có
- **Response Data:**
  ```json
  [
    { "label": "PUBLISHED", "count": 30 },
    { "label": "DRAFT", "count": 5 },
    { "label": "ENDED", "count": 80 },
    { "label": "CANCELLED", "count": 10 }
  ]
  ```

### 9.6. Sự kiện theo Danh mục (Horizontal Bar Chart)

- **Method:** `GET /dashboards/admin/event-category`
- **Tác dụng**: Biểu đồ cột ngang liệt kê số lượng sự kiện thuộc từng danh mục (Music, Tech, Fashion...).
- **Query Params:** Không có
- **Response Data:**
  ```json
  [
    { "label": "Âm nhạc", "count": 45 },
    { "label": "Công nghệ", "count": 32 }
  ]
  ```

### 9.7. Doanh thu theo Ngày (Bar Chart)

- **Method:** `GET /dashboards/admin/revenue-stats?from=2024-05-01&to=2024-05-31`
- **Tác dụng**: Biểu đồ cột dọc thể hiện biến động doanh thu thực nhận (sau khi trừ phí cổng) qua từng ngày.
- **Query Params:** `from` (tùy chọn), `to` (tùy chọn)
- **Response Data:**
  ```json
  [
    { "date": "2024-05-01", "revenue": 5000000 },
    { "date": "2024-05-02", "revenue": 8000000 }
  ]
  ```

### 9.8. Vé Free vs Trả phí theo Tháng (Stacked Column Chart)

- **Method:** `GET /dashboards/admin/ticket-type-stats?from=2024-01-01&to=2024-12-31`
- **Tác dụng**: Biểu đồ cột chồng thể hiện cơ cấu vé Miễn phí và Trả phí theo từng tháng (giúp Admin thấy xu hướng người dùng ưu tiên loại vé nào).
- **Query Params:** `from` (tùy chọn), `to` (tùy chọn)
- **Response Data:**
  ```json
  [
    { "month": "2024-05", "count": 450, "type": "VÉ TRẢ PHÍ" },
    { "month": "2024-05", "count": 200, "type": "VÉ MIỄN PHÍ" }
  ]
  ```

---

## 10. API BIỂU ĐỒ ORGANIZER (`/dashboards/organizer/...`)

> Tất cả API trong nhóm này yêu cầu quyền `ORGANIZER`.
> Dữ liệu trả về chỉ tính trên các sự kiện do chính Organizer đó tổ chức.

### 10.1. Tỉ lệ lấp đầy (Pie Chart)

- **Method:** `GET /dashboards/organizer/occupancy`
- **Tác dụng**: Biểu đồ tròn so sánh tỉ lệ vé Đã bán so với Vé Còn trống, giúp Organizer đánh giá sức hút của sự kiện.
- **Query Params:** Không có
- **Response Data:**
  ```json
  [
    { "label": "Đã bán", "count": 850 },
    { "label": "Còn trống", "count": 150 }
  ]
  ```

### 10.2. Biến động bán vé (Line Chart)

- **Method:** `GET /dashboards/organizer/sales-trend?from=2024-05-01&to=2024-05-31`
- **Tác dụng**: Biểu đồ đường thể hiện tốc độ bán vé theo ngày (bao gồm cả vé Free và Paid), giúp Organizer nắm bắt được khung giờ cao điểm mua vé.
- **Query Params:** `from` (tùy chọn), `to` (tùy chọn)
- **Response Data:**
  ```json
  [
    { "date": "2024-05-01", "value": 12 },
    { "date": "2024-05-02", "value": 45 }
  ]
  ```

### 10.3. Cơ cấu khách tham gia (Pie Chart)

- **Method:** `GET /dashboards/organizer/attendee-structure`
- **Tác dụng**: Biểu đồ tròn phân loại khách tham gia dựa trên loại vé họ sở hữu (Vé Trả phí, Vé Miễn phí, Vé Đặc biệt).
- **Query Params:** Không có
- **Response Data:**
  ```json
  [
    { "label": "Khách mua vé", "count": 700 },
    { "label": "Khách mời/Free", "count": 150 }
  ]
  ```

### 10.4. Trạng thái Check-in (Pie Chart)

- **Method:** `GET /dashboards/organizer/checkin-status`
- **Tác dụng**: Biểu đồ tròn thống kê số lượng khách đã check-in so với số lượng vé đã phát hành (thể hiện hiệu quả công tác tổ chức cổng soát vé).
- **Query Params:** Không có
- **Response Data:**
  ```json
  [
    { "label": "Đã vào cổng", "count": 600 },
    { "label": "Chưa đến", "count": 250 }
  ]
  ```

### 10.5. Tần suất Check-in theo Giờ (Column Chart)

- **Method:** `GET /dashboards/organizer/checkin-frequency?from=2024-05-20&to=2024-05-20`
- **Tác dụng**: Biểu đồ cột dọc thống kê số lượt check-in diễn ra vào từng khung giờ trong ngày, giúp Organizer biết được giờ cao điểm đông khách nhất để bố trí nhân lực.
- **Query Params:** `from` (tùy chọn), `to` (tùy chọn)
- **Ghi chú:** `hour` là số nguyên từ 0 đến 23. Frontend tự quy đổi sang dạng "18:00 - 19:00".
- **Response Data:**
  ```json
  [
    { "hour": 18, "count": 120 },
    { "hour": 19, "count": 450 },
    { "hour": 20, "count": 30 }
  ]
  ```

## 11. HỆ THỐNG MÃ LỖI CHUẨN (ERROR HANDLING)

### 8.1. Lỗi Xác thực (401 Unauthorized)

Xảy ra khi: Sai mật khẩu, Tài khoản không tồn tại, Token hết hạn.

```json
{ "status": { "code": 401, "message": "Email hoặc mật khẩu không chính xác", "label": "INVALID_CREDENTIALS"... }, "data": null }
```

### 7.2. Lỗi Nhập liệu & Logic Nghiệp Vụ (400 Bad Request)

Xảy ra khi:

- Sai định dạng đầu vào.
- `CAPACITY_CANNOT_BE_LESS_THAN_SOLD`: Bạn chèn ép sức chứa sai nguyên lý Event.
- `MUST_SETUP_PAYMENT_FIRST`: Trốn lậu nộp thuế không khai báo thẻ thụ hưởng PayOS.
- `INVALID_TIME_RANGE`: Ngày kết thúc lại nằm trước vạch đích.
- `INVALID_STAFF_ROLE`: Chọn nhân viên không có quyền STAFF.
- `TICKET_SOLD_OUT`: Mua trễ, đã hết chỗ.

```json
{ "status": { "code": 400, "message": "Sức chứa không phù hợp...", "label": "VALIDATION_ERROR"... }, "data": null }
```

### 7.3. Lỗi Server (500 Internal Server Error)

Xảy ra khi: Mất kết nối DB, API Cloudinary đứt cáp, vv...

**Bảng Tra Cứu HTTP Status:**
| Code | Ý nghĩa |
|------|---------|
| `200` | Thành công |
| `201` | Tạo mới tài nguyên / Đăng ký thành công |
| `400` | Lỗi dữ liệu đầu vào (Validation, logic) |
| `401` | Chưa xác thực / Sai thông tin đăng nhập |
| `403` | Đã đăng nhập nhưng không có quyền (Forbidden) |
| `404` | Không tìm thấy tài nguyên |
| `500` | Lỗi nghiêm trọng phía máy chủ |

---
