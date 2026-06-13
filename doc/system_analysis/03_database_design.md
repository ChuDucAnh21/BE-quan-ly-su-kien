# THIẾT KẾ CƠ SỞ DỮ LIỆU (DATABASE DESIGN)
Hệ thống quản lý sự kiện, thanh toán PayOS và check-in QR

---

## I. BIỂU ĐỒ THỰC THỂ LIÊN KẾT (ERD)

```mermaid
erDiagram
    NGUOI_DUNG ||--o{ SU_KIEN : "1-n (người tổ chức)"
    NGUOI_DUNG ||--o{ DON_DANG_KY : "1-n (người đăng ký)"
    NGUOI_DUNG ||--o{ PHAN_CONG_NV : "1-n (nhân viên)"
    NGUOI_DUNG ||--o| CAU_HINH_TT : "1-1 (cấu hình PayOS)"
    NGUOI_DUNG ||--o{ NHAT_KY_SOATVE : "1-n (người soát vé)"
    NGUOI_DUNG ||--o{ MA_KHOI_PHUC : "1-n (yêu cầu khôi phục)"

    SU_KIEN ||--o{ LICH_TRINH : "1-n (lịch trình con)"
    SU_KIEN ||--o{ DON_DANG_KY : "1-n (đơn đăng ký)"
    SU_KIEN ||--o{ PHAN_CONG_NV : "1-n (phân công)"

    DON_DANG_KY ||--o{ THANH_TOAN : "1-n (giao dịch)"
    DON_DANG_KY ||--o| VE_DIEN_TU : "1-1 (vé điện tử)"

    VE_DIEN_TU ||--o{ NHAT_KY_SOATVE : "1-n (lịch sử soát vé)"

    NGUOI_DUNG {
        BIGINT      MaDinhDanh          PK  "Mã định danh (tự tăng)"
        VARCHAR     HoTen                   "Họ và tên"
        VARCHAR     Email                   "Email (duy nhất)"
        VARCHAR     SoDienThoai             "Số điện thoại"
        VARCHAR     MatKhau                 "Mật khẩu (BCrypt)"
        VARCHAR     VaiTro                  "Vai trò: ADMIN/ORGANIZER/STAFF/USER"
        VARCHAR     TrangThai               "Trạng thái: ACTIVE/LOCKED"
        VARCHAR     AnhDaiDien              "URL ảnh đại diện (Cloudinary)"
        VARCHAR     GioiTinh                "Giới tính: MALE/FEMALE/OTHER"
        DATE        NgaySinh                "Ngày sinh"
        VARCHAR     DiaChi                  "Địa chỉ"
        VARCHAR     NguonDangNhap           "Nguồn: LOCAL / GOOGLE"
        VARCHAR     MaNguonDangNhap         "ID trả về từ Google (nếu có)"
        DATETIME    NgayTao                 "Ngày tạo tài khoản"
        DATETIME    NgayCapNhat             "Ngày cập nhật gần nhất"
    }

    SU_KIEN {
        BIGINT      MaDinhDanh          PK  "Mã định danh (tự tăng)"
        BIGINT      MaNguoiToChuc       FK  "Khoá ngoại → Người dùng"
        VARCHAR     TenSuKien               "Tên sự kiện"
        VARCHAR     DanhMuc                 "Danh mục (Âm nhạc, Công nghệ...)"
        VARCHAR     MoTaNgan                "Mô tả ngắn"
        TEXT        MoTaChiTiet             "Mô tả đầy đủ"
        VARCHAR     AnhBia                  "URL ảnh bìa (Cloudinary)"
        DATETIME    ThoiGianBatDau          "Thời gian bắt đầu"
        DATETIME    ThoiGianKetThuc         "Thời gian kết thúc"
        VARCHAR     DiaDiem                 "Địa điểm tổ chức"
        INT         SucChua                 "Sức chứa tối đa (số vé)"
        DECIMAL     GiaVe                   "Giá vé (0 = miễn phí)"
        INT         SoVeDaBan               "Số vé đã bán (tự động tăng)"
        VARCHAR     TrangThai               "Trạng thái: DRAFT/PUBLISHED/CLOSED/CANCELLED/COMPLETED"
        DATETIME    NgayTao                 "Ngày tạo"
        DATETIME    NgayCapNhat             "Ngày cập nhật"
    }

    LICH_TRINH {
        BIGINT      MaDinhDanh          PK  "Mã định danh"
        BIGINT      MaSuKien            FK  "Khoá ngoại → Sự kiện"
        VARCHAR     TenHoatDong             "Tên hoạt động"
        DATETIME    ThoiGianBatDau          "Thời gian bắt đầu hoạt động"
        DATETIME    ThoiGianKetThuc         "Thời gian kết thúc hoạt động"
        TEXT        MoTa                    "Mô tả hoạt động"
        DATETIME    NgayTao                 "Ngày tạo"
        DATETIME    NgayCapNhat             "Ngày cập nhật"
    }

    DON_DANG_KY {
        BIGINT      MaDinhDanh          PK  "Mã định danh"
        BIGINT      MaSuKien            FK  "Khoá ngoại → Sự kiện"
        BIGINT      MaNguoiDung         FK  "Khoá ngoại → Người dùng"
        VARCHAR     MaDangKy                "Mã đăng ký (UUID 8 ký tự, duy nhất)"
        DATETIME    ThoiGianCheckIn         "Thời gian check-in thực tế"
        VARCHAR     TrangThai               "Trạng thái: PENDING_PAYMENT/CONFIRMED/CHECKED_IN/CANCELLED"
        DATETIME    NgayTao                 "Ngày tạo"
        DATETIME    NgayCapNhat             "Ngày cập nhật"
    }

    THANH_TOAN {
        BIGINT      MaDinhDanh          PK  "Mã định danh"
        BIGINT      MaDonDangKy         FK  "Khoá ngoại → Đơn đăng ký"
        VARCHAR     MaDonHang               "Mã đơn hàng trên PayOS (duy nhất)"
        VARCHAR     NhaCungCap              "Nhà cung cấp: PAYOS / SYSTEM"
        DECIMAL     SoTien                  "Số tiền giao dịch"
        VARCHAR     TrangThai               "Trạng thái: PENDING/SUCCESS/FAILED/CANCELLED"
        VARCHAR     DuongDanThanhToan       "URL trang thanh toán PayOS"
        VARCHAR     MaLienKetThanhToan      "ID link thanh toán trên PayOS"
        DATETIME    ThoiGianThanhToan       "Thời điểm thanh toán thành công"
        TEXT        PhanHoiTho              "Dữ liệu thô từ PayOS Webhook"
        DATETIME    NgayTao                 "Ngày tạo"
        DATETIME    NgayCapNhat             "Ngày cập nhật"
    }

    VE_DIEN_TU {
        BIGINT      MaDinhDanh          PK  "Mã định danh"
        BIGINT      MaDonDangKy         FK  "Khoá ngoại → Đơn đăng ký (duy nhất)"
        VARCHAR     MaQR                    "Mã QR dùng để soát vé (UUID 36 ký tự)"
        DATETIME    ThoiGianPhatHanh        "Thời điểm vé được tạo ra"
        DATETIME    ThoiGianCheckIn         "Thời điểm vé được quét check-in"
        VARCHAR     TrangThai               "Trạng thái: ACTIVE/USED/INVALID"
        DATETIME    NgayTao                 "Ngày tạo"
        DATETIME    NgayCapNhat             "Ngày cập nhật"
    }

    PHAN_CONG_NV {
        BIGINT      MaDinhDanh          PK  "Mã định danh"
        BIGINT      MaSuKien            FK  "Khoá ngoại → Sự kiện"
        BIGINT      MaNhanVien          FK  "Khoá ngoại → Người dùng (vai trò STAFF)"
        BIGINT      MaNguoiPhanCong     FK  "Khoá ngoại → Người dùng (Organizer)"
        DATETIME    NgayTao                 "Ngày phân công"
    }

    CAU_HINH_TT {
        BIGINT      MaDinhDanh          PK  "Mã định danh"
        BIGINT      MaNguoiDung         FK  "Khoá ngoại → Người dùng (Organizer)"
        VARCHAR     MaKhachHang             "Client ID từ PayOS"
        VARCHAR     KhoaAPI                 "API Key từ PayOS"
        VARCHAR     KhoaXacMinh             "Checksum Key từ PayOS"
        BOOLEAN     DangHoatDong            "Cấu hình đang được kích hoạt"
        DATETIME    NgayTao                 "Ngày tạo"
        DATETIME    NgayCapNhat             "Ngày cập nhật"
    }

    NHAT_KY_SOATVE {
        BIGINT      MaDinhDanh          PK  "Mã định danh"
        BIGINT      MaVe                FK  "Khoá ngoại → Vé điện tử"
        BIGINT      MaNguoiSoatVe       FK  "Khoá ngoại → Người dùng (Staff)"
        VARCHAR     PhuongThuc              "Phương thức: QR / MANUAL"
        DATETIME    NgayTao                 "Thời điểm soát vé"
    }

    MA_KHOI_PHUC {
        BIGINT      MaDinhDanh          PK  "Mã định danh"
        VARCHAR     MaToken                 "Chuỗi UUID ngẫu nhiên (duy nhất)"
        BIGINT      MaNguoiDung         FK  "Khoá ngoại → Người dùng"
        DATETIME    ThoiHanHetHan           "Thời điểm hết hạn (tạo + 15 phút)"
    }
```

---

## II. DANH SÁCH BẢNG VÀ MÔ TẢ

| STT | Tên bảng (Backend) | Tên tiếng Việt | Mô tả chức năng |
|---|---|---|---|
| 1 | `users` | **Người dùng** | Lưu thông tin tài khoản, mật khẩu, vai trò và nguồn đăng nhập (LOCAL/GOOGLE OAuth2) |
| 2 | `events` | **Sự kiện** | Thông tin sự kiện do Người tổ chức (Organizer) tạo ra |
| 3 | `event_schedules` | **Lịch trình sự kiện** | Các hoạt động chi tiết trong timeline của một sự kiện |
| 4 | `registrations` | **Đơn đăng ký** | Yêu cầu đăng ký tham gia của người dùng (ràng buộc: 1 user/1 sự kiện) |
| 5 | `payments` | **Thanh toán** | Giao dịch thanh toán qua PayOS gắn với đơn đăng ký |
| 6 | `tickets` | **Vé điện tử** | Vé QR sinh ra sau khi thanh toán/đăng ký thành công |
| 7 | `event_staff_assignments` | **Phân công nhân viên** | Gán quyền soát vé cho Staff tại từng sự kiện |
| 8 | `payment_configs` | **Cấu hình thanh toán** | Thông tin tài khoản PayOS của Organizer để nhận tiền vé |
| 9 | `checkin_logs` | **Nhật ký soát vé** | Ghi lại từng lượt soát vé (QR/thủ công), thời gian và nhân viên thực hiện |
| 10 | `password_reset_tokens` | **Mã khôi phục mật khẩu** | Token UUID một lần, hết hạn sau 15 phút, dùng để đặt lại mật khẩu |

---

## III. GIẢI THÍCH CHI TIẾT CÁC MỐI QUAN HỆ

### 1. Mối quan hệ 1 – Nhiều

| Bảng cha | Bảng con | Khoá ngoại | Ý nghĩa |
|---|---|---|---|
| Người dùng | Sự kiện | `organizer_id` | 1 Organizer tạo nhiều sự kiện; mỗi sự kiện chỉ có 1 chủ |
| Người dùng | Đơn đăng ký | `user_id` | 1 User đăng ký nhiều sự kiện; mỗi đơn chỉ thuộc 1 user |
| Người dùng | Phân công nhân viên | `staff_id` | 1 Staff được phân công nhiều sự kiện; mỗi bản ghi gắn 1 Staff |
| Người dùng | Nhật ký soát vé | `checked_in_by` | 1 Staff thực hiện nhiều lần soát vé |
| Người dùng | Mã khôi phục MK | `user_id` | 1 User có thể yêu cầu khôi phục mật khẩu nhiều lần |
| Sự kiện | Lịch trình | `event_id` | 1 Sự kiện có nhiều hoạt động trong timeline |
| Sự kiện | Đơn đăng ký | `event_id` | 1 Sự kiện nhận nhiều đơn đăng ký (giới hạn sức chứa) |
| Sự kiện | Phân công nhân viên | `event_id` | 1 Sự kiện có nhiều nhân viên soát vé |
| Đơn đăng ký | Thanh toán | `registration_id` | 1 Đơn đăng ký có thể có nhiều giao dịch (retry khi thất bại) |
| Vé điện tử | Nhật ký soát vé | `ticket_id` | 1 Vé lưu toàn bộ lịch sử lần soát |

### 2. Mối quan hệ 1 – 1

| Bảng A | Bảng B | Khoá ngoại | Ý nghĩa |
|---|---|---|---|
| Người dùng | Cấu hình thanh toán | `user_id` | Mỗi Organizer chỉ có 1 tài khoản PayOS |
| Đơn đăng ký | Vé điện tử | `registration_id` | Mỗi đơn đăng ký hợp lệ tạo ra đúng 1 mã QR vé |

---

## IV. CÁC RÀNG BUỘC NGHIỆP VỤ CHÍNH

| # | Ràng buộc | Mô tả |
|---|---|---|
| 1 | **Đăng ký duy nhất** | `(event_id, user_id)` trong bảng **Đơn đăng ký** là duy nhất — 1 user chỉ mua 1 vé cho 1 sự kiện |
| 2 | **Luồng phát vé** | Khi **Thanh toán** = THÀNH CÔNG → Tự động tạo **Vé điện tử** = ACTIVE; khi soát vé thành công → Vé = USED |
| 3 | **Phân quyền truy cập** | Admin thấy tất cả; Organizer chỉ quản lý sự kiện của mình; Staff chỉ soát vé sự kiện được phân công; User chỉ thấy sự kiện PUBLISHED |
| 4 | **Hủy tự động** | Đơn **Thanh toán** còn PENDING quá 5 phút bị hủy tự động bởi Scheduler, nhả lại chỗ |
| 5 | **Hủy vé trả phí** | Vé sự kiện có phí **không** được phép hủy sau khi thanh toán thành công |
