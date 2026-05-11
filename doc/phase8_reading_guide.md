# 📘 HƯỚNG DẪN ĐỌC HIỂU CHI TIẾT PHASE 8 (WEBHOOK & TICKETING)

Tài liệu này được viết riêng để giúp sếp và team nắm được dòng chảy dữ liệu (Data Flow) của hệ thống Thanh toán và Cấp vé (Phase 8) trước khi chính thức đặt chân sang Phase 9 (Check-in QR).

Hãy theo sát thứ tự từ 1 đến 5 dưới đây để dễ hình dung nhất:

---

## BƯỚC 1: Khởi nguồn Đơn Hàng (Registration)

📍 **File cần đọc:** `src/main/java/com/example/backend/service/impl/RegistrationServiceImpl.java`
👉 **Focus vào hàm:** `registerEvent()`

- **Vấn đề cũ:** Tại Phase 7, mã `orderCode` được sinh bằng chuỗi `UUID` xen lẫn chữ cái (VD: `ORDER-A1B2C3`).
- **Thay đổi Phase 8:** Ngân hàng PayOS chỉ chấp nhận mã hóa đơn là kiểu **Số Nguyên Khổng Lồ (long)** tối đa 53-bit. Do đó, mã sinh ra hiện tại là sự kết hợp của: `Epoch Timestamp (CurrentTimeMillis) + số ngẫu nhiên 2 chữ số` -> Tạo ra chuỗi số đủ 15 ký tự an toàn tuyệt đối.
- **Xử lý nhanh Vé Miễn Phí:**
  - Thay vì bắt thẻ vé Free phải chờ thanh toán, thuật toán sẽ cắm một bản ghi `Dummy Payment` (Hóa đơn mồi, giá 0đ, trạng thái SUCCESS).
  - Ngay tại chỗ đó, hệ thống sẽ gọi Entity `Ticket` để khắc ngay một vé QR Code (UUID 36 ký tự ngẫu nhiên) và bọc nó vào kho `ticketRepository.save()`.

---

## BƯỚC 2: Cánh Cổng Chờ Phản Hồi (Payment Repository)

📍 **File cần đọc:** `src/main/java/com/example/backend/repository/PaymentRepository.java`
👉 **Focus vào hàm:** `findByOrderCode()`

- Khi khách hàng quét QR Code thanh toán trên màn hình, phía PayOS sẽ ghi nhận tiền vào và lập tức dội ngược một gói tín hiệu báo cáo về lại Server của chúng ta. Tín hiệu này gọi là **Webhook**.
- **Hiểm họa (Race-Condition):** Đôi khi mạng lag, hoặc lỗi phần cứng viễn thông, PayOS có thể vô tình bắn Webhook _2 lần liên tiếp trong cùng 1 giây_. Điều này sẽ dẫn luồng Spring Boot song song cập nhật hóa đơn 2 lần, và sinh trùng 2 vé QR liên tụ cho một khách hàng.
- **Cách khắc phục:** Ta gắn chữ bùa yểm `@Lock(LockModeType.PESSIMISTIC_WRITE)` vào đầu hàm. Lúc này Database sẽ khóa vặn cánh cửa lại để từ chối phục vụ mọi truy vấn thứ 2 của cùng một đơn hàng, bắt nó xếp hàng đằng sau, tạo ra môi trường _Idempotency (Miễn nhiễm thao tác lặp)_.

---

## BƯỚC 3: Cổng Chào Webhook (Entry Point)

📍 **File cần đọc:** `src/main/java/com/example/backend/controller/WebhookController.java`
👉 **Focus vào hàm:** `handlePayOSWebhook()`

- API `POST /api/v1/payments/webhook` chính là cái rổ hứng tín hiệu từ PayOS dội về.
- Hàm đón lõng tại Controller này đặc biệt cởi mở, không sử dụng cơ chế bảo mật JWT Token thông thường và cho phép `permitAll()`.
- Nó đón nhận bản JSON gốc dưới dạng `ObjectNode` (Thay cho các Class Dto POJO tĩnh) để bảo toàn tuyệt đối 100% cấu trúc cây Json hỗ trợ giải Hash `Signature`.
- Thông thường nếu API chạy qua `ApiResponse` của dự án sẽ bị bọc vào format `{ status: ..., data: ... }`. Tuy nhiên Máy chủ Bot của PayOS vô cùng khô khan, nó chỉ yêu cầu đúng cục Json `{ "success": true, "message": "Ok" }`. Cho nên API này không return `ResponseEntity` như các Controller khác.

---

## BƯỚC 4: Bộ Não Xử Lý Giao Dịch (Webhook Service)

📍 **File cần đọc:** `src/main/java/com/example/backend/service/impl/WebhookServiceImpl.java`
👉 **Focus vào hàm:** `processWebhook()`

Đây là trung tâm nội tạng mổ xẻ gói hàng từ PayOS dội về, thứ tự đọc hiểu logic nằm trong hàm như sau:

1. **Dò tìm Giao dịch:** Bóc lõi mã `orderCode` bên trong gói JSON được mã hóa dội về. Lôi vào Database đối chiếu xem đơn hàng này có thật hay không? Đồng thời ụp Khóa Bi Quan (Đã đề cập ở Bước 2) xuống hàng Record đó.
2. **Kiểm tra lặp (Idempotency Check):** Nếu lôi đơn hàng ra mà thấy trạng thái đang là `SUCCESS` rồi, chứng tỏ luồng Webhook một đã chạy xong rồi $\Rightarrow$ Hàm sẽ tự đẩy luồng số hai đi về nhẹ nhàng và ném ra chữ `success: true` cho PayOS đỡ hỏi thăm.
3. **Dò tìm Ngân Hàng của Organizer (Multi-Tenant):** Lấy `clientId`, `apiKey`, `checksumKey` của thư mục `PaymentConfigs` ứng với **tác giả (Organizer)** tạo ra Event. Thay vì dùng một tài khoản thu ngân ngân hàng tập trung như các dự án thông thường, Event Management của chúng ta chia nhỏ tiền thẳng về ví cá nhân của từng Organizer (Nên mới cần nạp cấu hình ngân hàng lúc tạo luồng).
4. **Giải mã Chữ ký (Verify Signature):** Nạp toàn bộ dữ liệu vào Class PayOS từ thư viện `vn.payos`. Lệnh `payOS.verifyPaymentWebhookData()` sẽ làm toán Hash `HMAC-SHA256` để kiểm duyệt nội dung theo bảng mã ASCII. Nếu Hacker (giả làm PayOS nhằm bắn Webhook ảo) thì thư viện sẽ tự động văng Exception ở Try Catch.
5. **Đổ khuôn Vé (Ticketing):** Nếu qua cửa ải Verify, tiền thật sự đã ting ting vào túi của Organizer. Cập nhật Status sang `SUCCESS` và khởi tạo mã Định danh cho thực thể `<Ticket>` làm Vé vào cửa (Status `ACTIVE`).
6. **Trong trường hợp thất bại:** Gắn mác `FAILED` cho Giao dịch, nhưng quan trọng nhất phải ngầm trả lại suất chỗ `event.getSoldTickets() - 1` để nhường cơ hội lại cho máy khách kế tiếp.

---

## BƯỚC 5: Ổn định và Dọn Rác Đơn Hàng (Cancellation & Timeout)

Hai khâu cuối cùng bảo vệ vận hành dự án.

1. Khách tự bỏ vé (📍 **File**: `RegistrationServiceImpl` -> `cancelRegistration()`).
   Khách được quyền dùng API `/{id}/cancel` để hủy đơn FREE nhằm nhường ghế cho người khác. Đặc biệt logic này sẽ thảy ra Exception `CANNOT_CANCEL_PAID_TICKET` nếu khách cố hủy đơn CÓ TRẢ PHÍ (Do backend chưa mở cổng Refund qua PayOS lúc này).
2. Xóa Giao dịch chìm ngâm (📍 **File**: `PaymentTimeoutScheduler` -> `cancelUnpaidOrders()`).
   Mỗi vòng xoay đồng hồ kim phút tới mức **5 phút**, Spring Boot sẽ tự kích hoạt chuông đập. Những giỏ hàng bị om quá lâu dưới đáy trạng thái PENDING (quá 5 phút) sẽ tự động dập tát về `CANCELLED`, và nhả đi suất trống khỏi `event.soldTickets`. Đây là kĩ thuật dọn vệ sinh Background Workers định kỳ hoàn toàn vô hình với người dùng.

---

🎉 **LỜI KẾT PHASE 8:**
Phase 8 hoàn toàn tập trung vào kiến trúc hệ thống Back-end vững chãi để xây một chiếc hộp an toàn ôm lấy Đồng tiền Thanh Toán. Ở Phase 9 tiếp theo, chúng ta chỉ cần kết nối một sợi chỉ mỏng từ Spring Boot gọi đến API `https://api-merchant.payos.vn/v2/payment-requests` (Nơi thay thế URL Mock lúc trước) là 100% Khách Hàng có thể rút điện thoại ra chuyển tiền mua vé ngay cực mạnh!
