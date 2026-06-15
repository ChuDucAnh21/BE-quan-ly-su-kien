# BIỂU ĐỒ TUẦN TỰ - PHẦN E: TÍNH NĂNG NÂNG CAO (UC23)

---

## UC23 – Tương tác với Trợ lý ảo AI

```mermaid
sequenceDiagram
    actor Actor
    participant Màn hình
    participant Hệ thống
    participant AI_Service

    Actor->>Màn hình: Nhập câu hỏi và nhấn Gửi
    Màn hình->>Hệ thống: Gửi nội dung câu hỏi
    Hệ thống->>Hệ thống: Nhúng thêm ngữ cảnh nội bộ (Prompt)
    Hệ thống->>AI_Service: Gửi truy vấn qua API của AI
    alt Dịch vụ AI lỗi / Quá tải
        AI_Service-->>Hệ thống: Báo lỗi kết nối
        Hệ thống-->>Màn hình: Trả về lỗi hệ thống bận
        Màn hình-->>Actor: Hiển thị "Trợ lý ảo đang bận, vui lòng thử lại sau"
    else Dịch vụ AI phản hồi tốt
        AI_Service-->>Hệ thống: Sinh và trả về câu trả lời
        Hệ thống-->>Màn hình: Truyền câu trả lời tư vấn
        Màn hình-->>Actor: Hiển thị tin nhắn lên khung Chatbot
    end
```
