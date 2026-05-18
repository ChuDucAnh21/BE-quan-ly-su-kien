# 📘 HƯỚNG DẪN ĐỌC HIỂU CHI TIẾT PHASE 9 (THANH TOÁN THỰC & SMART CHECK-IN)

Phase 9 là mảnh ghép cuối cùng giải quyết 2 bài toán lớn:

1. Kết nối Backend gọi PayOS để sinh mã QR Chuyển khoản thực tế cho Khách hàng (Thay vì Mock URL).
2. Xây dựng Cổng gác Cửa sự kiện, cấp phép cho nhân viên soát vé QR và ngăn chặn Hacker quét vé lậu nhờ Cơ chế **Pessimistic Lock**.

Dưới đây là sơ đồ luồng dữ liệu của Phase 9 để bạn dễ đọc code:

---

## 1. MÓC NỐI API NGÂN HÀNG (THỰC TẾ)

📍 **File cần đọc:** `RegistrationServiceImpl.java`
👉 **Focus vào đoạn code gọi PayOS SDK ở nhánh vé Thu Phí (Paid)**

Trong Phase 7 và Phase 8, `checkoutUrl` chỉ là 1 String cứng dạng `https://payos.vn/checkout/...`. Ở Phase 9, chúng ta đã thiết lập kịch bản kết nối thật sự như sau:

- Khởi tạo thư viện `vn.payos.PayOS` bằng `ClientId`, `ApiKey`, `ChecksumKey` được cấu hình riêng trong Database của Organizer.
- Gói gọn `CreatePaymentLinkRequest` và cấu hình Return URL (URL mà PayOS sẽ điều hướng khách về lại Web Frontend sau khi chuyển tiền).
- **Điểm sáng ở Code (v2.x SDK):** Chúng ta sử dụng interface mới `payOS.paymentRequests().create(request)` thay cho các hàm cũ của v1.x để tương thích hoàn toàn với các trường dữ liệu mới nhất từ Ngân hàng (như `expiredAt`).
- Toàn bộ quá trình gọi ngoại vi được bọc trong vòng `try-catch`. Nếu viễn thông lỗi, hàm ném về cục lỗi `500` bảo vệ hệ thống không bị crash đột ngột.

---

## 2. API TRÍCH XUẤT DANH SÁCH VÉ (ADMIN / ORGANIZER)

📍 **File cần đọc:** `TicketServiceImpl.java`
👉 **Focus vào hàm:** `getTicketsCustomerByEventId()`

- **Nhiệm vụ:** Trút dữ liệu kho vé phục vụ màn hình List Người Tham Gia trên App của Ban tổ chức.
- **Bảo mật:** Dùng lệnh `SecurityContextHolder` móc ngược Email của người đang soi chiếu hiện tại ra. Chặn thao tác cướp danh sách vé Sự kiện của Organizer khác (`ACCESS_DENIED` logic ngầm nếu chưa pass điều kiện if-else).

---

## 3. THIẾT KẾ Ổ KHÓA CỔNG KIỂM SOÁT VÉ (CHECK-IN)

📍 **File cần đọc số 1:** `TicketRepository.java`
📍 **File cần đọc số 2:** `TicketServiceImpl.java` (Hàm `processCheckIn`)

Đây là trung tâm giá trị học thuật nhất của Phase 9 nhằm bảo vệ dòng tiền Vé:

- **Tình huống mô phỏng Bug Thực Tế:** Nếu anh Khách hàng mua được vé V.I.P, anh chụp lại màn hình mã QR đó gửi cho 10 người bạn bè. 10 người này xông thẳng cổng kiểm soát và đưa mã QR ra vào **cùng một Mili-giây** trước mặt 10 Nhân viên Staff cầm Scanner.
- **Nếu code theo cách thông thường:** Java xử dụng đa luồng (Multi-Threads) nên 10 nhân viên đó đều chọc vào MySQL truy vấn câu tìm `findByQrCode` và đều thấy `Status = ACTIVE` (vì chưa ai kịp lưu nó sang `USED`). Kết quả: 1 vé lọt 10 người.
- **Trấn Phái Bằng `@Lock(LockModeType.PESSIMISTIC_WRITE)`:** Tại Phase 9, tôi đã đính nó lên `TicketRepository`.
  - Khi cái Máy quét QrCode đầu tiên tinh tinh ở cổng số 1, luồng Java lập tức ép MySQL Khóa cánh cửa giam bản ghi ID vé đó lại.
  - Các Máy quét QrCode cửa số 2,3,4 bị chặn đứng ở cửa Database và phải xếp hàng đứng đợi cánh cửa mở ra.
  - Khi Máy quét số 1 đi qua hoàn tất và Cập nhật Vé sang chữ `USED`, cánh cổng DB mở ra. Lúc này Máy quét số 2 lôi Vé ra thì thấy dữ liệu DB đã bị Cập nhật mất rồi ($\Rightarrow$ Báo lỗi Trùng Lặp Vé). Bugs quét trùng được khóa chết!
- **Xác thực Webhook (v2.x SDK):** Trong `WebhookServiceImpl.java`, tôi đã chuyển sang dùng `payOS.webhooks().verify(webhook)` - Đây là cơ chế xác thực rảnh tay mới nhất của SDK 2.x, giúp tự động đối soát chữ ký an toàn 100% giúp phát hiện ngay cả khi Hacker cố tình sửa đổi số tiền trong Payload dội về.

### Quá trình Check-in trả về báo lỗi gì?

Bạn có thể tham khảo `ErrorCode.java`:

- `TICKET_INVALID`: Bị hệ thống chặn vì Vé đã bị Hủy theo dạng Request Refund hoặc ngâm tiền PayOS quá 5 phút.
- `TICKET_ALREADY_USED`: Bị chặn do mã vé lặp (Như tình huống giải thích phía trên).

---

## 4. API LẤY VÉ & ĐỒNG BỘ CHỮA CHÁY THANH TOÁN (BACKUP NETWORK)

📍 **File cần đọc:** `TicketController.java` & `PaymentController.java`

Đây là bộ đôi API giúp bảo đảm luồng trả nghiệm Mua - Nhận Vé của người dùng được xuyên suốt:

1. **Lấy Vé (`GET /api/v1/tickets/my-tickets`)**
   - Sau khi Frontend nhận tín hiệu ở trang `payment-success`, cần gọi API này để hệ thống trả về toàn bộ vé (cùng với `qrCode`) của chính người dùng (User) đang Login.
2. **Cứu Hộ Đơn Trễ Mạng (`GET /api/v1/payments/sync/{orderCode}`)**
   - Nếu sếp test dưới Localhost, hoặc mạng sếp rớt, Webhook của PayOS không trả về được.
   - Hàm này chủ động cầm `orderCode` chạy sang PayOS SDK check lại lần cuối. Nếu status PayOS là `PAID`, API sẽ tự động làm nhiệm vụ của Webhook: Sinh Vé Điện Tử + Đóng Hóa Đơn ngay lập tức. Đây là giải pháp chốt chặn cực kì uy tín!

---

_Lưu ý: Chức năng `Batch Offline Sync` (Đồng bộ nhồi file Offline lúc đứt mạng) là Module Mở Rộng tùy chọn theo năng lực đội ngũ ở phần đồ án._
