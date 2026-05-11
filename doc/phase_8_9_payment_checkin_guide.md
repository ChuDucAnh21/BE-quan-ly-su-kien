# 📘 HƯỚNG DẪN ĐỌC HIỂU CHI TIẾT PHASE 8 VÀ PHASE 9

**Chủ đề:** Tích hợp Thanh toán Thực Tế (PayOS), Webhook an toàn và Hệ thống Kiểm soát Vé QR (Smart Check-in chống gian lận)

---

## 🌟 TỔNG QUAN HỆ THỐNG

**Phase 8 & 9** là trái tim tài chính và an ninh của toàn bộ đồ án. Thay vì bấm nút mua vé xong nhận thông báo "Ảo" như ở các Phase trước, giờ đây hệ thống đã được nhúng giải pháp Đám Mây của ví điện tử (PayOS).

### 📖 THỨ TỰ FILE KHUYẾN NGHỊ ĐỂ ĐỌC (FLOW ĐI THEO DÒNG TIỀN)

Để không bị "tẩu hỏa nhập ma", bạn nên mở các file và đọc theo đúng thứ tự luồng thời gian từ lúc khách hàng móc thẻ ra, đến lúc họ bước qua cổng Sân Vận Động:

1. **Khởi tạo Đơn:** `RegistrationServiceImpl.java` (Hàm `registerEvent()`)
2. **Ngầm nhận Webhook:** `WebhookController.java` ➔ `WebhookServiceImpl.java` (Hàm `processWebhook()`)
3. **Cứu hộ giao dịch:** `PaymentController.java` (Hàm `syncPaymentStatus()`)
4. **Lôi vé ra khỏi túi:** `TicketServiceImpl.java` (Hàm `getMyTickets()`)
5. **Soát vé ở cổng:** `TicketRepository.java` ➔ `TicketServiceImpl.java` (Hàm `processCheckIn()`)

---

## 🚀 CHI TIẾT TICK TẮC TỪNG BƯỚC

### BƯỚC 1: KHỞI TẠO THANH TOÁN (SDK PAYOS v2.0.1)

📍 **Nơi code ngự trị:** `RegistrationServiceImpl.java`

Khi người dùng (Customer) gọi API để mua một Sự kiện có trả phí (Paid Event):

- Backend kết nối DB lôi bộ "Chìa Khóa Bí Mật" (Client ID, API Key, Checksum Key) của _Ban Tổ Chức (Organizer)_ ra.
- **Sử Dụng SDK 2.x:** Code không gọi RestTemplate nhà quê mà gọi thẳng `payOS.paymentRequests().create()`. Các thông số tiền bạc, mã đơn (Order Code) được gói gọn gàng vào `CreatePaymentLinkRequest`.
- **Ràng Buộc An Toàn (The Safety Net):** Nguyên khối code gọi sang máy chủ PayOS được ôm chặt bởi `try-catch`. Vì gọi API ngoại vi xuyên lục địa có thể rớt mạng/Timeout, ta phải bắt lỗi ở đây để tránh App BackEnd sập nguồn. Đẩy lỗi về dạng `500 UNCATEGORIZED_EXCEPTION`.
- **Đầu Ra:** Sau khi hàm chạy thành công, trả về 1 đường link (Checkout URL). Frontend dùng link này để kéo sang màn hình quét QR Vietcombank/Momo của đối tác. Đơn hàng lưu DB mang chữ `PENDING`.

---

### BƯỚC 2: AUTOMATION WEBHOOK (NGỰ LÂM QUÂN BẢO VỆ TIỀN TÀI)

📍 **Nơi code ngự trị:** `WebhookServiceImpl.java`

Đây là linh hồn của hệ thống phi đồng bộ (Asynchronous). Khi khách chuyển tiền xong, PayOS sẽ tự gọi vào Backend để báo tín hiệu thông qua API `POST /api/v1/payments/webhook`.

📝 **Ba kĩ thuật tinh hoa trong file này:**

1. **Xác thực chữ ký Hacker-proof (`payOS.webhooks().verify()`):**
   Nếu 1 anh Hacker cố tình rào đường mạng, viết tool POST thẳng về `/webhook` báo đơn 1 triệu đã nạp thành công dù chưa trả xu nào. Hàm `verify(webhookType)` của SDK sẽ dùng thuật toán băm (HMAC-SHA256) trộn với Checksum Secret để lật tẩy đây là tín hiệu giả. SDK tự bắn exception Exception từ chối!
2. **Khóa Đơn Hàng Ngừa Nhân Bản (PESSIMISTIC_WRITE):**
   Trong `PaymentRepository`, hàm `findByOrderCode` có annotation `@Lock`.
   _Tại sao?_ Nếu mạng chập chờn, máy chủ PayOS ngáo gửi liền mạch 3 tín hiệu Webhook song song vào. Nếu Java đọc thông thường, nó sẽ thấy hóa đơn Đều là `PENDING` và in đè cho khách ... 3 tấm vé. Pessimistic Lock giúp chặn kẹt xe, bắt các Webhook đi thành 1 hàng dọc. Webhook 1 xong thì biến đơn thành `SUCCESS` hoặc `FAILED`, Webhook văng vào sau thấy trạng thái KHÁC `PENDING` rồi sẽ Cút luôn (Đây gọi là tính Idempotency - Đẳng Cấu, ngăn chặn cả lỗi đúp vé lẫn lỗi trừ âm `soldTickets`).
3. **Sinh Vé Thông Minh:**
   Mọi thứ ổn thỏa $\rightarrow$ Cập nhật hóa đơn `SUCCESS` $\rightarrow$ Sinh thêm bản ghi `Ticket`. Tạo mã QR bằng lệnh `UUID.randomUUID()` sinh chuỗi ngẫu nhiên dài 36 kí tự khó đoán tuyệt đối.

---

### BƯỚC 3: CỨU HỘ ĐƠN TRỄ MẠNG (MANUAL SYNC)

📍 **Nơi code ngự trị:** `PaymentController.java` (`GET /api/v1/payments/sync/{orderCode}`)

_Tình huống thật:_ Sếp Test Localhost (Máy PayOS không trả Webhook về nhà sếp được), hoặc mạng viễn thông nghẽn cáp quang biển. Người dùng đã chuyển khoản, tải app reload nhưng vẫn báo `PENDING`. Nổi khùng!

- API này sinh ra để Frontend gắn vào nút "Đồng bộ giao dịch" hoặc tự làm chìm (Polling).
- **Hành động:** BackEnd đóng vai chủ nợ xách xe xuống nhà PayOS bằng lệnh dò sổ: `payOS.paymentRequests().get(orderCode)`.
- Trạng thái trả về từ PayOS là `PAID`: BackEnd tự đóng cửa, tự xé Vé Điện Tử, cứu khách hàng một bàn thua rớt mạng mà không cần ai phải kiện cáo refund đau luồng! Cuối cùng trả về thông báo `PAID_AND_TICKET_CREATED`.

---

### BƯỚC 4: KHÁCH HÀNG KIỂM TRA TÚI VÉ (MY TICKETS)

📍 **Nơi code ngự trị:** `TicketController.java` & `TicketServiceImpl.java` (Hàm `getMyTickets`)

- Để Frontend tự tin lấy cái chuỗi `qrCode` đem đi bọc 1 thư viện QR đen trắng. Frontend sẽ gọi `GET /api/v1/tickets/my-tickets`.
- Tại đây, hệ thống chọc vào Spring Security, soi `SecurityContextHolder` móc được `Email` của người gọi API, và truy sát trong bảng Database tất cả những Ticket nào thuộc nhân thân anh này (Có `Pageable` phân trang siêu tốc). Việc truy vấn bằng Email hiện tại an toàn 100%, không bị tình trạng lấy lén vé của ông hàng xóm.

---

### BƯỚC 5: SMART CHECK-IN (BẤT TỬ SCANNER BUG KHI VÀO CỔNG)

📍 **Nơi code ngự trị:** `TicketRepository.java` & `TicketServiceImpl.java` (Hàm `processCheckIn`)

Khách đến cổng, Mở app chìa cái hình vuông đen xì có chứa lõi `qrCode`. Bảo vệ cầm App điện thoại rọi Camera quét vào đó nó bắn HTTP Request đến `POST /api/v1/checkin`.

📝 **Lại một siêu bẫy cần phá:**
Gian lận kinh điển ở Việt Nam: 1 Đội trộm vé Vip, mua 1 vé, Xong chụp ảnh màn hình ném hết mẹ vào Group Chat 10 thằng, 10 thằng dàn hàng ngang ở cả 10 cửa (có 10 bảo vệ), chúng nó hô đếm "1...2...3... CHÌA!", 10 ông bảo vệ dí máy vào quét **TẠI CÙNG 1 MILI GIÂY**.

- **Nếu Code non MySQL:** Cả 10 Thread Java lao tới Query hỏi xem vé trạng thái sao. Do chưa Ticket nào kịp nhích lên chữ `USED` nên SQL báo với cả 10 ông "Ừ vé còn sống đấy, `ACTIVE`!". Thế là 10 gã lọt cổng. Toang!
- **Khắc tinh (Pessimistic Lock lần thứ 2):** Hàm `findByQrCodeWithLock` được gắn xích trên Interface Reposiory.
  - Cổng 1 chọc vào nhanh nhất 0.001s: MySQL đóng rào, dán bảng "Cấm động".
  - Cổng 2, Cổng 3, Cổng 10 phi vào: Nhìn thấy bảng cấm đụng, đứng hóng hớt chờ đợi lôi Thread.
  - Cổng 1 xử lí logic Java (vài ms sau): Check vé thành công, Xịt chữ `USED`, Database Lưu, Mở chốt xích.
  - Cổng 2 lập tức túm được vé, nhưng khi load lên RAM thì thấy ticket mang chữ `USED` ➔ Java ném vỡ mặt mã Error HTTP: `TICKET_ALREADY_USED`. Mã lỗi văng lên máy Cổng 2, 3, 4, 10 đỏ chóe màn hình bảo vệ ➔ 9 tay còn lại bị bảo an tống cổ!

**Đây chính là sức mạnh tối thượng xử lý Lock Row mà mọi sinh viên làm System BackEnd Tốt Nguyện Vọng Phải Làm Bằng Được!**

---

## 🎯 DANH SÁCH LỖI VÀ MÃ ĐÁP TRẢ (ErrorCode.java)

Khi tích hợp với Frontend, bạn dặn đội Front xử lí theo các nhãn tên Enum Error sau đây:

| Tên Lỗi Enum               | HTTP Status | Trường Hợp Ý Nghĩa Kĩ Thuật Ném Lỗi                                             |
| :------------------------- | :---------- | :------------------------------------------------------------------------------ |
| `PAYMENT_CONFIG_NOT_FOUND` | 404         | Thằng Organizer lười biếng tạo Sự Kiện nhưng chưa lên Set ID với Key của PayOS. |
| `PAYMENT_NOT_FOUND`        | 404         | Front cố tính gọi Sync đơn hàng mạn mã linh tinh không có trong CSDL.           |
| `TICKET_NOT_FOUND`         | 404         | Cổng soát vé Scanner quét ra 1 cái mã Fake không thuộc hệ thống.                |
| `TICKET_ALREADY_USED`      | 400         | Có kẻ mua vé xong quét rồi lại đòi vào lại, hoặc gian lận đúp ảnh.              |
| `TICKET_INVALID`           | 400         | Vé nằm trong Sổ đen bị Organizer báo Hủy, hoặc chưa kịp thành công đã lỗi.      |
| `UNCATEGORIZED_EXCEPTION`  | 500         | Tín hiệu Mạng PayOS chập chờn, API Crash tại bộ mạch (Try..catch bắn rớt)       |

**Chúc bạn làm đồ án tuyệt đối an toàn và vững tay code nhất với kiến trúc này! (End Of Phase 8 & 9 Document)**
