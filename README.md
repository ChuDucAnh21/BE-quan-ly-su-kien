# 📌 Event Management & QR Check-in System (Backend)

Hệ thống quản lý sự kiện thông minh tích hợp thanh toán trực tuyến và check-in bằng mã QR, được xây dựng trên nền tảng Spring Boot.

---

## 🚀 Tổng quan dự án

Dự án này cung cấp bộ giải pháp Backend toàn diện cho việc tổ chức và quản lý sự kiện. Hệ thống hỗ trợ đa vai trò người dùng (Admin, Organizer, Staff, User) với các quy trình khép kín từ đăng ký, thanh toán đến soát vé.

### ✨ Tính năng chính

#### 👤 Đối với Người tham gia (User)
* **Đăng ký & Xác thực:** Đăng ký tài khoản, đăng nhập (JWT), quản lý hồ sơ cá nhân.
* **Khám phá sự kiện:** Xem danh sách sự kiện public, chi tiết lịch trình và giá vé.
* **Đăng ký tham gia:** Quy trình ghi danh nhanh chóng, hỗ trợ sự kiện miễn phí và trả phí.
* **Thanh toán trực tuyến:** Tích hợp cổng thanh toán **PayOS** tiện lợi.
* **Vé điện tử QR:** Nhận mã QR Code ngay sau khi đăng ký/thanh toán thành công để check-in.

#### 👷 Đối với Nhân viên soát vé (Staff)
* **Check-in QR:** Quét mã QR của người tham gia để xác nhận vào cửa nhanh chóng.
* **Quản lý danh sách:** Tra cứu người tham gia theo email/mã đăng ký khi cần check-in thủ công.
* **Phạm vi công việc:** Chỉ truy cập được các sự kiện được phân công bởi Organizer.

#### 🎪 Đối với Người tổ chức (Organizer)
* **Quản lý sự kiện (CRUD):** Tạo, chỉnh sửa, đóng hoặc hủy sự kiện.
* **Quản lý lịch trình:** Thiết lập timeline chi tiết cho từng giai đoạn của sự kiện.
* **Quản lý nhân sự:** Phân công Staff hỗ trợ soát vé cho từng sự kiện cụ thể.
* **Thống kê:** Theo dõi số lượng đăng ký, doanh thu và tỷ lệ tham gia thực tế.

#### 🛡️ Đối với Quản trị viên (Admin)
* **Quản lý người dùng:** Phân quyền (Role), khóa/mở khóa tài khoản toàn hệ thống.
* **Giám sát toàn diện:** Theo dõi mọi sự kiện và giao dịch thanh toán.
* **Thống kê hệ thống:** Báo cáo tổng quan về quy mô người dùng và hiệu quả kinh doanh.

---

## 🛠️ Công nghệ sử dụng

*   **Ngôn ngữ:** Java 21
*   **Framework:** Spring Boot 3.3.4
*   **Cơ sở dữ liệu:** MySQL 8.0
*   **Bảo mật:** Spring Security, JWT (JSON Web Token), BCrypt Password Hashing
*   **Lưu trữ hình ảnh:** Cloudinary (dùng cho Avatar và Poster sự kiện)
*   **Thanh toán:** PayOS SDK
*   **Thông báo:** Spring Boot Starter Mail (SMTP Gmail)
*   **Tài liệu API:** Swagger/OpenAPI 3.0
*   **Công cụ hỗ trợ:** Lombok, MapStruct (nếu có), Maven

---

## 🏗️ Kiến trúc hệ thống

Hệ thống được thiết kế theo mô hình **Layered Architecture**:
1.  **Controller:** Tiếp nhận Request và điều phối dữ liệu.
2.  **Service:** Xử lý logic nghiệp vụ (Business Logic).
3.  **Repository:** Tương tác với cơ sở dữ liệu qua Spring Data JPA.
4.  **Entity:** Ánh xạ các bảng trong MySQL thành đối tượng Java.
5.  **DTO (Data Transfer Object):** Chuẩn hóa dữ liệu truyền tải giữa Client và Server.

### 🔐 Bảo mật nổi bật
*   **Stateless Authentication:** Sử dụng JWT để xác thực mà không cần lưu Session trên Server.
*   **HTTP-Only Cookie:** Lưu Access Token an toàn, chống tấn công XSS.
*   **Role-based Access Control (RBAC):** Phân quyền chặt chẽ đến từng Endpoint API.
*   **Data Validation:** Kiểm soát chặt chẽ đầu vào (Email Gmail, định dạng SĐT Việt Nam...) bằng Regex.

---

## ⚙️ Hướng dẫn cài đặt

### 1. Yêu cầu hệ thống
*   JDK 21 trở lên
*   MySQL Server 8.0
*   Maven 3.x

### 2. Cấu hình môi trường
Chỉnh sửa file `src/main/resources/application.yml` với các thông tin sau:
*   **Database:** Cấu hình `url`, `username`, `password` của MySQL.
*   **Mail:** Cấu hình Gmail và App Password để sử dụng tính năng Quên mật khẩu.
*   **Cloudinary:** Cung cấp Cloud Name, API Key và API Secret để upload ảnh.
*   **JWT:** Thay đổi `secret` key để đảm bảo an toàn.

### 3. Chạy ứng dụng
Mở terminal tại thư mục gốc dự án và chạy lệnh:
```bash
mvn spring-boot:run
```
Sau khi khởi động thành công, ứng dụng sẽ chạy tại: `http://localhost:8080`

---

## 📖 Tài liệu tham khảo

Để hiểu sâu hơn về dự án, bạn có thể tham khảo các tài liệu chi tiết trong thư mục `/doc`:
*   [Hướng dẫn kỹ thuật & Giải thích mã nguồn](file:///e:/DATN/BackEnd/doc/technical_guide.md)
*   [Chi tiết chức năng hệ thống](file:///e:/DATN/BackEnd/doc/feature_doc.md)
*   [Tài liệu các Endpoint API](file:///e:/DATN/BackEnd/doc/api_endpoints.md)
*   [Tài liệu Swagger UI](http://localhost:8080/swagger-ui/index.html) (khi ứng dụng đang chạy)

---

## 🤝 Liên hệ
*   **Tác giả:** Chu Đức Anh
*   **Email:** chucongducanh@gmail.com
*   **GitHub:** [ChuDucAnh21](https://github.com/ChuDucAnh21)

---
*Chúc bạn có những trải nghiệm tuyệt vời với hệ thống quản lý sự kiện này!*
