# THỐNG KÊ DASHBOARD HỆ THỐNG QUẢN LÝ SỰ KIỆN

Tài liệu này đặc tả cấu trúc dữ liệu JSON cần thiết để vẽ biểu đồ bằng thư viện **Ant Design Charts (G2Plot)**.

> **Lưu ý về bộ lọc thời gian:** Các API có hỗ trợ `from`/`to` nhận tham số dạng `yyyy-MM-dd`. Nếu không truyền, Backend tự động lấy **30 ngày gần nhất** làm mặc định.

---

# 1. DÀNH CHO ADMIN (QUẢN TRỊ TOÀN HỆ THỐNG)

## A. Thống kê Người dùng (Users & Roles)

### 1. Biểu đồ Tròn (Donut Chart) - Cơ cấu tài khoản

Hiển thị tỷ lệ các loại tài khoản trên hệ thống: Admin, Organizer, Staff, Customer.

- **API:** `GET /api/v1/dashboards/admin/role-distribution` (Không cần lọc thời gian)
- **JSON Format:**

```json
[
  { "label": "ADMIN", "count": 5 },
  { "label": "ORGANIZER", "count": 45 },
  { "label": "STAFF", "count": 150 },
  { "label": "USER", "count": 1300 }
]
```

### 2. Biểu đồ Đường (Line Chart) - Tăng trưởng người dùng

Theo dõi số lượng tài khoản đăng ký mới theo ngày, phân loại theo Role.

- **API:** `GET /api/v1/dashboards/admin/user-growth?from=2024-05-01&to=2024-05-31` (Hỗ trợ lọc `from`/`to`)
- **JSON Format:**

```json
[
  { "date": "2024-05-01", "count": 10, "type": "USER" },
  { "date": "2024-05-01", "count": 2,  "type": "ORGANIZER" },
  { "date": "2024-05-02", "count": 15, "type": "USER" }
]
```

### 3. Biểu đồ Cột (Bar Chart) - Top 5 Organizer

Thống kê các Organizer có nhiều sự kiện nhất nhằm đánh giá quy mô hoạt động.

- **API:** `GET /api/v1/dashboards/admin/top-organizers` (Không cần lọc thời gian)
- **JSON Format:**

```json
[
  { "label": "Tên nhà tổ chức A", "count": 25 },
  { "label": "Tên nhà tổ chức B", "count": 18 }
]
```

---

## B. Thống kê Sự kiện (Events)

### 1. Biểu đồ Cột (Column Chart) - Sự kiện mới hàng tháng

Theo dõi tần suất tạo sự kiện trên hệ thống theo từng tháng.

- **API:** `GET /api/v1/dashboards/admin/monthly-events?from=2024-01-01&to=2024-12-31` (Hỗ trợ lọc `from`/`to`)
- **JSON Format:**

```json
[
  { "label": "2024-01", "count": 12 },
  { "label": "2024-02", "count": 20 }
]
```

### 2. Biểu đồ Tròn (Pie Chart) - Trạng thái sự kiện

Hiển thị tỷ lệ các trạng thái sự kiện: Đang diễn ra, Chờ duyệt, Đã kết thúc, Đã hủy.

- **API:** `GET /api/v1/dashboards/admin/event-status` (Không cần lọc thời gian)
- **JSON Format:**

```json
[
  { "label": "PUBLISHED", "count": 30 },
  { "label": "DRAFT",     "count": 5 },
  { "label": "ENDED",     "count": 80 },
  { "label": "CANCELLED", "count": 10 }
]
```

### 3. Biểu đồ Cột ngang (Horizontal Bar Chart) - Phân loại theo danh mục

Thống kê số lượng sự kiện theo từng danh mục để xác định chủ đề phổ biến nhất.

- **API:** `GET /api/v1/dashboards/admin/event-category` (Không cần lọc thời gian)
- **JSON Format:**

```json
[
  { "label": "Âm nhạc",  "count": 45 },
  { "label": "Công nghệ", "count": 32 }
]
```

---

## C. Thống kê Vé & Tài chính (Tickets & Finance)

### 1. Biểu đồ Cột (Bar Chart) - Doanh thu PayOS theo Ngày

Theo dõi doanh thu thanh toán thực tế qua PayOS theo từng ngày.

- **API:** `GET /api/v1/dashboards/admin/revenue-stats?from=2024-05-01&to=2024-05-31` (Hỗ trợ lọc `from`/`to`)
- **JSON Format:**

```json
[
  { "date": "2024-05-01", "revenue": 5000000 },
  { "date": "2024-05-02", "revenue": 8000000 }
]
```

### 2. Biểu đồ Cột chồng (Stacked Column Chart) - Số lượng vé phát hành

So sánh vé miễn phí (0đ) và vé trả phí trên toàn hệ thống theo từng tháng.

- **API:** `GET /api/v1/dashboards/admin/ticket-type-stats?from=2024-01-01&to=2024-12-31` (Hỗ trợ lọc `from`/`to`)
- **JSON Format:**

```json
[
  { "month": "2024-05", "count": 450, "type": "VÉ TRẢ PHÍ" },
  { "month": "2024-05", "count": 200, "type": "VÉ MIỄN PHÍ" }
]
```

### 3. Thẻ số liệu (Statistic Cards)

Hiển thị nhanh các chỉ số quan trọng nhất:

- **API:** `GET /api/v1/dashboards/admin`
- Tổng tiền thanh toán thành công, Tổng vé đã bán, Tổng sự kiện, Tổng người dùng.

---

# 2. DÀNH CHO ORGANIZER (NHÀ TỔ CHỨC)

## A. Thống kê Vé (Ticket Sales)

### 1. Biểu đồ Tròn (Pie Chart) - Tỷ lệ lấp đầy

Hiển thị phần trăm số vé đã bán trên tổng sức chứa (capacity) của tất cả sự kiện.

- **API:** `GET /api/v1/dashboards/organizer/occupancy` (Không cần lọc thời gian)
- **JSON Format:**

```json
[
  { "label": "Đã bán",    "count": 850 },
  { "label": "Còn trống", "count": 150 }
]
```

### 2. Biểu đồ Đường (Line Chart) - Biến động bán vé

Theo dõi số lượng vé bán được theo từng ngày kể từ khi mở đăng ký.

- **API:** `GET /api/v1/dashboards/organizer/sales-trend?from=2024-05-01&to=2024-05-31` (Hỗ trợ lọc `from`/`to`)
- **JSON Format:**

```json
[
  { "date": "2024-05-01", "value": 12 },
  { "date": "2024-05-02", "value": 45 }
]
```

---

## B. Thống kê Khách hàng & Người tham gia (Attendees)

### 1. Biểu đồ Tròn (Pie Chart) - Cơ cấu khách tham gia

Hiển thị tỷ lệ khách mua vé trả phí và khách nhận vé miễn phí.

- **API:** `GET /api/v1/dashboards/organizer/attendee-structure` (Không cần lọc thời gian)
- **JSON Format:**

```json
[
  { "label": "Khách mua vé",   "count": 700 },
  { "label": "Khách mời/Free", "count": 150 }
]
```

---

## C. Thống kê Vận hành (Operations & Check-in)

### 1. Biểu đồ Tròn (Pie Chart) - Trạng thái Check-in

So sánh số lượng khách đã vào cổng (USED) và chưa đến (ACTIVE).

- **API:** `GET /api/v1/dashboards/organizer/checkin-status` (Không cần lọc thời gian)
- **JSON Format:**

```json
[
  { "label": "Đã vào cổng", "count": 600 },
  { "label": "Chưa đến",   "count": 250 }
]
```

### 2. Biểu đồ Cột (Column Chart) - Tần suất Check-in theo khung giờ

Thống kê lượng khách check-in theo từng khung giờ (mỗi cột = 1 tiếng) để hỗ trợ điều phối nhân sự.

- **API:** `GET /api/v1/dashboards/organizer/checkin-frequency?from=2024-05-20&to=2024-05-20` (Hỗ trợ lọc `from`/`to`)
- **Ghi chú:** `hour` là số nguyên 0–23, Frontend hiển thị thành "18:00 - 19:00".
- **JSON Format:**

```json
[
  { "hour": 18, "count": 120 },
  { "hour": 19, "count": 450 },
  { "hour": 20, "count": 30 }
]
```

---
