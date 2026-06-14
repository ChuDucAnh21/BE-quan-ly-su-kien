# BIỂU ĐỒ TUẦN TỰ - PHẦN F: TRỢ LÝ ẢO AI CHATBOT & BÁO CÁO (UC37 – UC40)

---

## UC38 – Trò chuyện và tư vấn chung (AI Chat)

```mermaid
sequenceDiagram
    actor User as Người dùng
    participant UI as Giao diện Chatbot
    participant HT as Hệ thống (Spring AI)
    participant LLM as API OpenRouter (LLM)

    User->>UI: 1. Gõ câu hỏi và gửi (vd: "Hệ thống này làm gì?")
    UI->>HT: 2. Gửi yêu cầu POST /api/chatbot/ask
    HT->>HT: 3. Tiền xử lý (đính kèm System Prompt)
    HT->>LLM: 4. Gửi prompt lên OpenRouter
    alt Quá tải (Rate Limit / Lỗi kết nối)
        LLM-->>HT: 5a. Báo lỗi 429 hoặc 500
        HT-->>UI: 5b. Trả về thông báo lỗi "Hệ thống quá tải"
        UI-->>User: 5c. Hiển thị thông báo lỗi
    else Xử lý thành công
        LLM-->>HT: 5. Trả về câu trả lời bằng ngôn ngữ tự nhiên
        HT-->>UI: 6. Trả kết quả JSON
        UI-->>User: 7. Hiển thị phản hồi
    end
```

---

## UC39 – Xem danh sách sự kiện được phân công (Dành cho Staff)

```mermaid
sequenceDiagram
    actor Staff as Nhân viên soát vé
    participant UI as Màn hình Sự kiện được phân công
    participant HT as Hệ thống
    participant DB as Cơ sở dữ liệu

    Staff->>UI: 1. Truy cập mục "Công việc của tôi"
    UI->>HT: 2. Gửi yêu cầu lấy danh sách sự kiện (kèm Token)
    HT->>HT: 3. Kiểm tra User ID hiện tại có khớp với tham số yêu cầu không
    alt ID không khớp
        HT-->>UI: 4a. Báo lỗi truy cập trái phép
        UI-->>Staff: 4b. Thông báo không được phép xem lịch của người khác
    else Hợp lệ
        HT->>DB: 4. Truy vấn các sự kiện trong bảng phân công
        DB-->>HT: 5. Trả về thông tin sự kiện (chưa kết thúc)
        HT-->>UI: 6. Trả về danh sách sự kiện tóm tắt
        UI-->>Staff: 7. Hiển thị danh sách sự kiện để chuẩn bị làm việc
    end
```

---

## UC40 – Xem danh sách giao dịch thanh toán của sự kiện

```mermaid
sequenceDiagram
    actor Org as Admin / Organizer
    participant UI as Màn hình Giao dịch thanh toán
    participant HT as Hệ thống
    participant DB as Cơ sở dữ liệu

    Org->>UI: 1. Truy cập tab "Giao dịch thanh toán" của sự kiện
    UI->>HT: 2. Gửi yêu cầu danh sách thanh toán (kèm Token)
    HT->>DB: 3. Kiểm tra quyền truy cập (Admin hoặc Organizer của sự kiện)
    alt Không có quyền
        DB-->>HT: 4a. Không có quyền
        HT-->>UI: 4b. Từ chối truy cập (403)
        UI-->>Org: 4c. Báo lỗi không có quyền
    else Có quyền
        HT->>DB: 4. Truy vấn các đơn hàng Payment thuộc eventId
        DB-->>HT: 5. Trả về danh sách đơn hàng (Trạng thái, số tiền, ngày tạo)
        HT-->>UI: 6. Trả về dữ liệu kèm tổng doanh thu
        UI-->>Org: 7. Hiển thị danh sách giao dịch
    end
```

---

## UC41 – Xem lịch sử điểm danh (Check-in) của sự kiện

```mermaid
sequenceDiagram
    actor Org as Admin / Organizer
    participant UI as Màn hình Lịch sử Check-in
    participant HT as Hệ thống
    participant DB as Cơ sở dữ liệu

    Org->>UI: 1. Truy cập tab "Lịch sử Check-in" của sự kiện
    UI->>HT: 2. Gửi yêu cầu lịch sử soát vé (kèm Token)
    HT->>DB: 3. Kiểm tra quyền truy cập
    alt Không có quyền
        DB-->>HT: 4a. Từ chối
        HT-->>UI: 4b. Báo lỗi 403
    else Có quyền
        HT->>DB: 4. Truy vấn bảng CheckInLog theo eventId
        DB-->>HT: 5. Trả về danh sách (Tên khách, Nhân viên soát, Thời gian)
        HT-->>UI: 6. Trả về dữ liệu
        UI-->>Org: 7. Hiển thị nhật ký soát vé
    end
```
