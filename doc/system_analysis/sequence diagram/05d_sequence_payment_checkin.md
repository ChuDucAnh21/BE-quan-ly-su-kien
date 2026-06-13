# BIỂU ĐỒ TUẦN TỰ - PHẦN D: THANH TOÁN, VÉ & CHECK-IN (UC29 – UC40)

---

## UC29 – Cấu hình cổng thanh toán PayOS (Organizer)

```mermaid
sequenceDiagram
    actor Org as Người tổ chức
    participant UI as Màn hình Cài đặt Thanh toán
    participant HT as Hệ thống
    participant DB as Cơ sở dữ liệu

    Org->>UI: 1. Truy cập trang cài đặt thanh toán
    Org->>UI: 2. Nhập thông tin PayOS (Client ID, API Key, Checksum Key) từ tài khoản PayOS
    Org->>UI: 3. Nhấn "Lưu cấu hình"
    UI->>HT: 4. Gửi thông tin cấu hình PayOS (kèm Token Organizer)
    HT->>DB: 5. Lưu hoặc cập nhật cấu hình thanh toán (đánh dấu đang hoạt động)
    DB-->>HT: 6. Lưu thành công
    HT-->>UI: 7. Thông báo cấu hình PayOS thành công
    UI-->>Org: 8. Cổng thanh toán đã được kết nối
```

---

## UC30 – Xem cấu hình thanh toán PayOS (Organizer)

```mermaid
sequenceDiagram
    actor Org as Người tổ chức
    participant UI as Màn hình Cài đặt Thanh toán
    participant HT as Hệ thống
    participant DB as Cơ sở dữ liệu

    Org->>UI: 1. Truy cập trang cài đặt thanh toán
    UI->>HT: 2. Yêu cầu thông tin cấu hình PayOS hiện tại (kèm Token)
    HT->>DB: 3. Tìm cấu hình PayOS theo tài khoản Organizer
    alt Chưa cấu hình
        DB-->>HT: 4a. Không có cấu hình
        HT-->>UI: 4b. Thông báo chưa thiết lập PayOS
        UI-->>Org: 4c. Hiển thị form trống, mời cấu hình
    else Đã có cấu hình
        DB-->>HT: 4. Trả về thông tin cấu hình
        HT-->>UI: 5. Trả về dữ liệu cấu hình
        UI-->>Org: 6. Hiển thị thông tin PayOS đã lưu
    end
```

---

## UC31 – Đăng ký tham gia sự kiện (Mua vé)

```mermaid
sequenceDiagram
    actor User as Người dùng
    participant UI as Màn hình Chi tiết Sự kiện
    participant HT as Hệ thống
    participant DB as Cơ sở dữ liệu
    participant PayOS as Cổng thanh toán PayOS

    User->>UI: 1. Nhấn "Đăng ký" hoặc "Mua vé"
    UI->>HT: 2. Gửi yêu cầu đăng ký (kèm Token)
    HT->>DB: 3. Kiểm tra sự kiện: trạng thái PUBLISHED, chưa qua thời gian bắt đầu
    alt Sự kiện chưa công bố hoặc đã bắt đầu
        HT-->>UI: 4a. Thông báo không thể đăng ký
        UI-->>User: 4b. Hiển thị lỗi
    end
    HT->>DB: 4. Kiểm tra còn chỗ (vé đã bán < sức chứa)
    alt Hết chỗ
        HT-->>UI: 5a. Thông báo hết vé
        UI-->>User: 5b. Hiển thị trạng thái hết vé
    end
    HT->>DB: 5. Kiểm tra người dùng chưa đăng ký sự kiện này
    alt Đã đăng ký rồi
        HT-->>UI: 6a. Thông báo đã đăng ký trước đó
        UI-->>User: 6b. Chuyển đến trang vé
    end
    HT->>DB: 6. Giữ chỗ (tăng số vé đã bán lên 1)
    HT->>HT: 7. Tạo mã đăng ký ngẫu nhiên

    alt Sự kiện miễn phí (giá vé = 0)
        HT->>DB: 8a. Lưu Đơn đăng ký (Trạng thái: ĐÃ XÁC NHẬN)
        HT->>DB: 8b. Ghi nhận thanh toán ảo (Hệ thống, số tiền = 0, Thành công)
        HT->>HT: 8c. Tạo mã QR vé điện tử
        HT->>DB: 8d. Lưu vé (Trạng thái: ACTIVE, kèm mã QR)
        HT-->>UI: 9a. Đăng ký thành công, vé đã được tạo
        UI-->>User: 9b. Hiển thị vé điện tử kèm mã QR
    else Sự kiện có phí
        HT->>DB: 8. Lấy cấu hình PayOS của người tổ chức
        alt Organizer chưa cấu hình PayOS
            DB-->>HT: 8a. Không tìm thấy cấu hình
            HT->>DB: 8b. Hoàn lại chỗ (giảm số vé bán)
            HT-->>UI: 8c. Thông báo lỗi hệ thống thanh toán
        else Có cấu hình PayOS
            HT->>PayOS: 9. Tạo liên kết thanh toán
            PayOS-->>HT: 10. Trả về đường dẫn thanh toán (checkoutUrl)
            HT->>DB: 11. Lưu Đơn đăng ký (Trạng thái: CHỜ THANH TOÁN)
            HT->>DB: 12. Lưu thông tin thanh toán (Trạng thái: PENDING, kèm checkoutUrl)
            HT-->>UI: 13. Trả về đường dẫn thanh toán
            UI-->>User: 14. Chuyển hướng sang trang thanh toán PayOS
        end
    end
```

---

## UC32 – Hủy vé miễn phí

```mermaid
sequenceDiagram
    actor User as Người dùng
    participant UI as Màn hình Vé của tôi
    participant HT as Hệ thống
    participant DB as Cơ sở dữ liệu

    User->>UI: 1. Nhấn "Hủy vé" trên vé muốn hủy
    UI-->>User: 2. Hộp thoại xác nhận "Bạn có chắc muốn hủy?"
    alt Nhấn "Không"
        User->>UI: 3a. Nhấn "Không"
        UI-->>User: 3b. Đóng hộp thoại
    else Xác nhận hủy
        User->>UI: 3. Xác nhận hủy
        UI->>HT: 4. Gửi yêu cầu hủy vé (kèm Token)
        HT->>DB: 5. Tìm đơn đăng ký và kiểm tra trạng thái
        alt Vé có phí và đã thanh toán (CONFIRMED)
            DB-->>HT: 6a. ticketType = PAID và status != PENDING_PAYMENT
            HT-->>UI: 6b. Không được hủy vé đã thanh toán
            UI-->>User: 6c. Thông báo lỗi (REGISTRATION_CANNOT_BE_CANCELLED)
        else Vé miễn phí hoặc Vé có phí nhưng chưa thanh toán
            HT->>DB: 6. Chuyển đơn đăng ký → CANCELLED
            HT->>DB: 7. Chuyển vé (nếu có) → INVALID
            HT->>DB: 8. Giảm số vé đã bán (nhả lại chỗ)
            DB-->>HT: 9. Lưu thành công
            HT-->>UI: 10. Hủy vé thành công
            UI-->>User: 11. Thông báo đã hủy, chỗ được nhả lại cho người khác
        end
    end
```

---

## UC33 – Xử lý kết quả thanh toán từ PayOS (Webhook)

```mermaid
sequenceDiagram
    participant PayOS as Cổng thanh toán PayOS
    participant HT as Hệ thống
    participant DB as Cơ sở dữ liệu

    note over PayOS,HT: Sau khi người dùng hoàn tất hoặc hủy thanh toán,<br/>PayOS tự động gửi thông báo về hệ thống
    PayOS->>HT: 1. Gửi thông báo kết quả thanh toán (kèm chữ ký bảo mật HMAC)
    HT->>DB: 2. Lấy khóa bảo mật của Organizer để xác thực
    HT->>HT: 3. Kiểm tra chữ ký bảo mật HMAC-SHA256
    alt Chữ ký không hợp lệ (giả mạo)
        HT-->>PayOS: 4a. Từ chối - chữ ký không đúng
    else Chữ ký hợp lệ
        HT->>DB: 4. Tìm đơn hàng theo mã đơn (khóa ghi độc quyền để tránh xử lý trùng lặp)
        alt Đơn đã xử lý trước đó (không phải PENDING)
            DB-->>HT: 5a. Đơn hàng đã xử lý
            HT-->>PayOS: 5b. Bỏ qua, trả về thành công (chống xử lý trùng)
        else Đơn còn PENDING
            alt Thanh toán thành công
                HT->>DB: 5. Cập nhật thanh toán → THÀNH CÔNG
                HT->>DB: 6. Cập nhật đơn đăng ký → ĐÃ XÁC NHẬN
                HT->>HT: 7. Tạo mã QR vé điện tử
                HT->>DB: 8. Lưu vé (Trạng thái: ACTIVE)
                HT-->>PayOS: 9. Xác nhận đã xử lý thành công
            else Thanh toán thất bại hoặc bị hủy
                HT->>DB: 5. Cập nhật thanh toán → THẤT BẠI
                HT->>DB: 6. Cập nhật đơn đăng ký → ĐÃ HỦY
                HT->>DB: 7. Giảm số vé đã bán (nhả lại chỗ)
                HT-->>PayOS: 8. Xác nhận đã xử lý
            end
        end
    end
```

---

## UC34 – Đồng bộ trạng thái đơn hàng thủ công

```mermaid
sequenceDiagram
    actor User as Người dùng
    participant UI as Màn hình Kết quả thanh toán
    participant HT as Hệ thống
    participant DB as Cơ sở dữ liệu
    participant PayOS as Cổng thanh toán PayOS

    note over User,PayOS: Dùng khi Webhook bị mất do lỗi mạng
    User->>UI: 1. Nhấn "Kiểm tra trạng thái thanh toán"
    UI->>HT: 2. Yêu cầu đồng bộ trạng thái đơn hàng (kèm mã đơn)
    HT->>DB: 3. Tìm đơn hàng theo mã
    alt Đơn không tồn tại
        DB-->>HT: 4a. Không tìm thấy
        HT-->>UI: 4b. Thông báo không tìm thấy đơn hàng
    else Đơn đã xử lý (THÀNH CÔNG / THẤT BẠI / ĐÃ HỦY)
        DB-->>HT: 4. Trả về trạng thái hiện tại
        HT-->>UI: 5. Trả về kết quả ngay (không cần gọi PayOS)
        UI-->>User: 6. Hiển thị trạng thái đơn hàng
    else Đơn vẫn PENDING
        HT->>PayOS: 5. Hỏi trực tiếp PayOS về trạng thái đơn hàng
        alt PayOS báo đã thanh toán
            PayOS-->>HT: 6a. ĐÃ THANH TOÁN
            HT->>DB: 6b. Cập nhật Thanh toán → THÀNH CÔNG, Đăng ký → XÁC NHẬN
            HT->>DB: 6c. Tạo và lưu vé (ACTIVE)
            HT-->>UI: 7a. Thanh toán thành công, vé đã được tạo
            UI-->>User: 7b. Hiển thị vé điện tử
        else PayOS báo đã hủy / hết hạn
            PayOS-->>HT: 6. ĐÃ HỦY / HẾT HẠN
            HT->>DB: 7. Cập nhật → THẤT BẠI, nhả lại chỗ
            HT-->>UI: 8. Thanh toán thất bại
            UI-->>User: 9. Thông báo đơn bị hủy
        else PayOS vẫn báo PENDING
            PayOS-->>HT: 6. VẪN ĐANG CHỜ
            HT-->>UI: 7. Đơn hàng đang chờ thanh toán
            UI-->>User: 8. Mời người dùng thử lại sau
        end
    end
```

---

## UC35 – Xem danh sách đơn hàng đang chờ thanh toán

```mermaid
sequenceDiagram
    actor User as Người dùng
    participant UI as Màn hình Đơn hàng của tôi
    participant HT as Hệ thống
    participant DB as Cơ sở dữ liệu

    User->>UI: 1. Truy cập trang "Đơn hàng chờ thanh toán"
    UI->>HT: 2. Yêu cầu danh sách đơn hàng PENDING (kèm Token)
    HT->>HT: 3. Đọc danh tính người dùng từ Token
    HT->>DB: 4. Truy vấn đơn hàng PENDING của tài khoản hiện tại (sắp xếp mới nhất trước)
    DB-->>HT: 5. Trả về danh sách đơn hàng (kèm đường dẫn thanh toán)
    HT-->>UI: 6. Trả về dữ liệu
    UI-->>User: 7. Hiển thị danh sách đơn hàng chưa thanh toán
    User->>UI: 8. Nhấn vào đường dẫn thanh toán để tiếp tục
    UI-->>User: 9. Chuyển hướng sang trang PayOS
```

---

## UC36 – Xem vé cá nhân (My Tickets)

```mermaid
sequenceDiagram
    actor User as Người dùng
    participant UI as Màn hình Vé của tôi
    participant HT as Hệ thống
    participant DB as Cơ sở dữ liệu

    User->>UI: 1. Truy cập trang "Vé của tôi"
    UI->>HT: 2. Yêu cầu danh sách vé cá nhân (kèm Token)
    HT->>HT: 3. Đọc danh tính từ Token
    HT->>DB: 4. Truy vấn tất cả vé thuộc tài khoản hiện tại (có phân trang)
    DB-->>HT: 5. Trả về danh sách vé (kèm mã QR thật, tên sự kiện, trạng thái, thời gian check-in)
    HT-->>UI: 6. Trả về dữ liệu
    UI-->>User: 7. Hiển thị danh sách vé kèm mã QR để đi check-in
```

---

## UC37 – Xem danh sách vé của sự kiện (Admin / Organizer / Staff)

```mermaid
sequenceDiagram
    actor User as Admin / Người tổ chức / Nhân viên
    participant UI as Màn hình Quản lý Khách tham dự
    participant HT as Hệ thống
    participant DB as Cơ sở dữ liệu

    User->>UI: 1. Truy cập trang "Danh sách khách tham dự" của sự kiện
    UI->>HT: 2. Yêu cầu danh sách vé của sự kiện (kèm Token)
    HT->>DB: 3. Kiểm tra quyền truy cập
    note over HT,DB: Admin: Có quyền với mọi sự kiện<br/>Organizer: Chỉ sự kiện do mình tạo<br/>Staff: Chỉ sự kiện được phân công
    alt Không có quyền
        DB-->>HT: 4a. Không có quyền
        HT-->>UI: 4b. Từ chối truy cập (403)
        UI-->>User: 4c. Thông báo không có quyền
    else Có quyền
        HT->>DB: 4. Truy vấn danh sách vé theo sự kiện (có phân trang)
        DB-->>HT: 5. Trả về danh sách vé (Họ tên, Email, Trạng thái vé, Đã check-in chưa)
        HT-->>UI: 6. Trả về danh sách
        UI-->>User: 7. Hiển thị bảng danh sách khách tham dự
    end
```

---

## UC38 – Quét mã QR Check-in

```mermaid
sequenceDiagram
    actor Staff as Nhân viên soát vé
    participant App as Ứng dụng Quét mã
    participant HT as Hệ thống
    participant DB as Cơ sở dữ liệu

    Staff->>App: 1. Mở tính năng quét mã QR
    App-->>Staff: 2. Bật camera, sẵn sàng quét
    Staff->>App: 3. Hướng camera vào mã QR trên điện thoại khách
    App->>HT: 4. Gửi mã QR lên hệ thống để xác thực (kèm Token nhân viên)
    HT->>DB: 5. Tìm vé tương ứng với mã QR (khóa ghi để tránh check-in trùng)
    alt Mã QR không tồn tại
        DB-->>HT: 6a. Không tìm thấy vé
        HT-->>App: 6b. Mã QR không hợp lệ
        App-->>Staff: 6c. ❌ Hiển thị cảnh báo màu đỏ
    else Vé đã được sử dụng (check-in rồi)
        DB-->>HT: 6. Vé trạng thái USED
        HT-->>App: 7a. Vé đã được check-in trước đó
        App-->>Staff: 7b. ⚠️ Cảnh báo vé đã dùng
    else Vé bị hủy (INVALID)
        DB-->>HT: 6. Vé trạng thái INVALID
        HT-->>App: 7a. Vé không còn hiệu lực
        App-->>Staff: 7b. ❌ Hiển thị lỗi
    else Nhân viên không có quyền với sự kiện này
        HT->>DB: 6. Kiểm tra nhân viên được phân công vào sự kiện chưa
        DB-->>HT: 6a. Chưa được phân công
        HT-->>App: 7a. Không có quyền check-in sự kiện này
        App-->>Staff: 7b. ❌ Từ chối
    else Vé hợp lệ (ACTIVE)
        HT->>DB: 6. Cập nhật vé → ĐÃ SỬ DỤNG, ghi thời gian check-in
        HT->>DB: 7. Ghi nhật ký check-in (nhân viên thực hiện, phương thức: Quét QR)
        DB-->>HT: 8. Lưu thành công
        HT-->>App: 9. Trả về thông tin khách (Họ tên, Email, Trạng thái)
        App-->>Staff: 10. ✅ Cổng mở - Hiển thị tên khách
    end
```

---

## UC39 – Đồng bộ Check-in hàng loạt khi Offline (Batch Sync)

```mermaid
sequenceDiagram
    actor Staff as Nhân viên soát vé
    participant App as Ứng dụng Quét mã (Offline)
    participant HT as Hệ thống
    participant DB as Cơ sở dữ liệu

    note over Staff,App: Trước khi mất mạng: Nhân viên đã tải danh sách vé về máy
    Staff->>App: 1. Quét vé offline khi mất kết nối mạng
    App->>App: 2. Lưu danh sách mã đã quét vào bộ nhớ máy

    note over App,HT: Khi có mạng trở lại
    App->>HT: 3. Gửi toàn bộ danh sách mã đã quét offline lên hệ thống
    HT->>DB: 4. Kiểm tra quyền nhân viên với sự kiện
    alt Không có quyền
        HT-->>App: 5a. Từ chối
    else Có quyền
        loop Xử lý từng mã (mỗi mã là một giao dịch độc lập)
            HT->>DB: 5. Tìm vé theo mã
            alt Mã không tìm thấy
                HT->>HT: 5a. Ghi nhận lỗi: mã không tồn tại
            else Vé đã check-in
                HT->>HT: 5b. Ghi nhận lỗi: vé đã được sử dụng
            else Vé bị hủy
                HT->>HT: 5c. Ghi nhận lỗi: vé không hợp lệ
            else Vé hợp lệ
                HT->>DB: 6. Cập nhật vé → ĐÃ SỬ DỤNG
                HT->>DB: 7. Ghi nhật ký check-in (phương thức: Đồng bộ Offline)
                HT->>HT: 8. Ghi nhận thành công
            end
        end
        HT-->>App: 9. Trả về kết quả tổng hợp (Tổng: X, Thành công: Y, Thất bại: Z, Chi tiết lỗi)
        App-->>Staff: 10. Hiển thị kết quả đồng bộ
    end
```

---

## UC40 – Tự động hủy đơn hàng quá hạn (Scheduler)

```mermaid
sequenceDiagram
    participant SCH as Bộ lên lịch tự động (Chạy mỗi 5 phút)
    participant HT as Hệ thống
    participant DB as Cơ sở dữ liệu

    note over SCH: Tự kích hoạt mỗi 5 phút (300.000 ms)
    SCH->>HT: 1. Kích hoạt tác vụ dọn dẹp đơn hàng quá hạn
    HT->>DB: 2. Tìm tất cả đơn hàng PENDING đã tạo quá 30 phút
    alt Không có đơn nào hết hạn
        DB-->>HT: 3a. Danh sách rỗng
        HT->>HT: 3b. Kết thúc sớm, không thực hiện gì
    else Có đơn hàng hết hạn
        DB-->>HT: 3. Trả về danh sách đơn hàng hết hạn
        loop Mỗi đơn hàng hết hạn
            HT->>DB: 4. Cập nhật đơn hàng → ĐÃ HỦY
            HT->>DB: 5. Cập nhật đơn đăng ký tương ứng → ĐÃ HỦY
            HT->>DB: 6. Giảm số vé đã bán (nhả lại chỗ cho người khác đặt)
        end
        DB-->>HT: 7. Lưu tất cả thay đổi
        HT->>HT: 8. Ghi log số đơn đã xử lý
    end
    HT-->>SCH: 9. Hoàn thành chu kỳ, chờ đến lần tiếp theo
```
