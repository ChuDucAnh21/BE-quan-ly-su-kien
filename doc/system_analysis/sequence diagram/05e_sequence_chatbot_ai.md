# BIỂU ĐỒ TUẦN TỰ - PHẦN E: TRỢ LÝ ẢO AI CHATBOT (UC41 – UC43)

---

## UC41 – Trò chuyện và tư vấn chung (AI Chat)

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

## UC42 – Tra cứu và gợi ý sự kiện tự động (Function Calling)

```mermaid
sequenceDiagram
    actor User as Người dùng
    participant UI as Giao diện Chatbot
    participant HT as Hệ thống (Spring AI)
    participant LLM as API OpenRouter (LLM)
    participant DB as Cơ sở dữ liệu

    User->>UI: 1. Gõ câu hỏi: "Sắp tới có sự kiện âm nhạc nào không?"
    UI->>HT: 2. Gửi yêu cầu tìm kiếm
    HT->>LLM: 3. Gửi câu hỏi lên LLM kèm khai báo các Tool (@Tool)
    LLM-->>HT: 4. LLM nhận diện Function Calling, yêu cầu gọi hàm `searchEventsTool("âm nhạc")`
    HT->>DB: 5. Hàm Java thực thi truy vấn tìm kiếm sự kiện theo từ khóa
    alt Không tìm thấy sự kiện
        DB-->>HT: 6a. Trả về mảng rỗng
        HT->>LLM: 6b. Trả lại kết quả mảng rỗng cho LLM
        LLM-->>HT: 6c. LLM sinh câu trả lời: "Chưa có sự kiện nào..."
    else Tìm thấy sự kiện
        DB-->>HT: 6. Trả về danh sách sự kiện (id, title, time, price)
        HT->>LLM: 7. Trả lại mảng dữ liệu cho LLM phân tích
        LLM-->>HT: 8. LLM tổng hợp thành câu trả lời tự nhiên
    end
    HT-->>UI: 9. Trả kết quả về Frontend
    UI-->>User: 10. Chatbot gợi ý danh sách sự kiện
```

---

## UC43 – Tra cứu lịch trình chi tiết sự kiện (Function Calling)

```mermaid
sequenceDiagram
    actor User as Người dùng
    participant UI as Giao diện Chatbot
    participant HT as Hệ thống (Spring AI)
    participant LLM as API OpenRouter (LLM)
    participant DB as Cơ sở dữ liệu

    User->>UI: 1. Gõ câu hỏi: "Lịch trình sự kiện T-Rex Concert?"
    UI->>HT: 2. Gửi yêu cầu tra cứu
    HT->>LLM: 3. Gửi câu hỏi lên LLM kèm cấu hình Tool
    LLM-->>HT: 4. LLM yêu cầu gọi hàm `getEventScheduleTool("T-Rex Concert")`
    HT->>DB: 5. Tìm kiếm ID sự kiện dựa trên tên
    alt Không tìm thấy sự kiện
        DB-->>HT: 6a. Không có kết quả
        HT->>LLM: 6b. Báo không tìm thấy
        LLM-->>HT: 6c. LLM trả lời "Không tìm thấy sự kiện này."
    else Tìm thấy sự kiện
        DB-->>HT: 6. Trả về ID sự kiện
        HT->>DB: 7. Truy vấn bảng EventSchedule theo ID sự kiện
        alt Sự kiện chưa có lịch trình
            DB-->>HT: 8a. Danh sách rỗng
            HT->>LLM: 8b. Trả lại kết quả rỗng cho LLM
            LLM-->>HT: 8c. LLM sinh câu "Chưa có lịch trình chi tiết."
        else Có lịch trình
            DB-->>HT: 8. Trả về mảng các hoạt động và thời gian
            HT->>LLM: 9. Đưa dữ liệu thô cho LLM
            LLM-->>HT: 10. LLM trình bày thành văn bản dễ đọc
        end
    end
    HT-->>UI: 11. Trả về Frontend
    UI-->>User: 12. Hiển thị lịch trình cho người dùng
```
