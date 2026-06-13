# ĐẶC TẢ USE CASE - PHẦN E: TRỢ LÝ ẢO AI CHATBOT (UC41 – UC43)

---

## UC41 – Trò chuyện và tư vấn chung (AI Chat)

| Trường | Nội dung |
|--------|----------|
| **Tên Use Case** | Trò chuyện và giải đáp thắc mắc bằng AI |
| **Tác nhân** | Guest, User, Staff, Organizer, Admin |
| **Mục đích** | Cung cấp một trợ lý ảo thông minh để giải đáp nhanh các câu hỏi về cách sử dụng hệ thống hoặc thông tin sự kiện chung. |
| **Mức độ ưu tiên** | Trung bình |
| **Điều kiện kích hoạt** | Người dùng mở khung Chatbot và gõ nội dung câu hỏi (Ví dụ: "Hệ thống này dùng để làm gì?") |
| **Điều kiện tiên quyết** | Hệ thống Backend đang kết nối ổn định với API của OpenRouter (mô hình Google Gemma). |
| **Điều kiện thất bại** | API trả về lỗi do Rate Limit (hết lượt) hoặc Token Limit (tràn dữ liệu). |
| **Điều kiện thành công** | Chatbot trả về nội dung text tự nhiên, đúng ngữ cảnh tiếng Việt. |
| **Luồng sự kiện chính** | 1. Người dùng nhập tin nhắn và nhấn gửi. 2. Frontend gửi yêu cầu tới API `/api/chatbot/ask`. 3. Hệ thống Backend (Spring AI) tiếp nhận prompt và gọi tới OpenRouter API. 4. OpenRouter xử lý thông điệp dựa trên system prompt ("Bạn là tư vấn viên của hệ thống Eventify..."). 5. AI sinh câu trả lời và Backend trả kết quả dạng JSON về Frontend. 6. Giao diện hiển thị tin nhắn của Chatbot cho người dùng. |
| **Luồng sự kiện thay thế** | Không có |
| **Luồng sự kiện ngoại lệ** | 3a. OpenRouter bị sập hoặc quá tải (Rate Limit) -> Hệ thống báo lỗi "Chatbot đang quá tải, vui lòng thử lại sau". 4a. Request vượt quá giới hạn Token Limit -> Hệ thống tự động báo lỗi cắt bớt nội dung. |

---

## UC42 – Tra cứu và gợi ý danh sách sự kiện tự động (Function Calling)

| Trường | Nội dung |
|--------|----------|
| **Tên Use Case** | AI tự động truy vấn tìm kiếm sự kiện |
| **Tác nhân** | Guest, User, Staff, Organizer, Admin |
| **Mục đích** | Cho phép AI tự động chọc xuống Database để tìm các sự kiện phù hợp nhất với yêu cầu mập mờ của người dùng. |
| **Mức độ ưu tiên** | Cao |
| **Điều kiện kích hoạt** | Người dùng hỏi một câu liên quan tới tìm sự kiện (Ví dụ: "Sắp tới có sự kiện âm nhạc nào không?") |
| **Điều kiện tiên quyết** | AI đã được cấu hình Tool Calling (`@Tool("searchEventsTool")`). |
| **Điều kiện thất bại** | AI không nhận diện được ý đồ tìm kiếm hoặc DB không có sự kiện nào khớp. |
| **Điều kiện thành công** | Chatbot trả về danh sách sự kiện (Tên, địa điểm, giá vé, thời gian) đọc từ Database thực tế. |
| **Luồng sự kiện chính** | 1. Người dùng nhập câu hỏi tìm sự kiện. 2. Backend phân tích câu hỏi bằng LLM. 3. LLM nhận diện cần sử dụng công cụ `searchEventsTool` và tự động trích xuất từ khóa tìm kiếm (Ví dụ: keyword="âm nhạc"). 4. Spring AI kích hoạt hàm Java `searchEventsTool(keyword)`, gọi vào `EventRepository`. 5. Hàm Java truy xuất DB và trả lại danh sách tóm tắt (chỉ lấy id, title, startTime, ticketPrice) cho LLM. 6. LLM tổng hợp lại thành câu trả lời tự nhiên dạng văn bản. 7. Trả kết quả cho người dùng. |
| **Luồng sự kiện thay thế** | 5a. Database không tìm thấy sự kiện nào khớp -> AI tự động trả lời "Hiện tại chưa có sự kiện âm nhạc nào, bạn có muốn xem sự kiện khác không?" |
| **Luồng sự kiện ngoại lệ** | 4a. Lỗi truy vấn Database -> Báo lỗi "Không thể kết nối để lấy dữ liệu sự kiện lúc này." |

---

## UC43 – Tra cứu lịch trình chi tiết sự kiện (Function Calling)

| Trường | Nội dung |
|--------|----------|
| **Tên Use Case** | AI tự động tra cứu lịch trình chi tiết của một sự kiện |
| **Tác nhân** | Guest, User, Staff, Organizer, Admin |
| **Mục đích** | Giúp người dùng hỏi lịch trình cụ thể của một sự kiện mà không cần phải tự bấm vào xem chi tiết. |
| **Mức độ ưu tiên** | Trung bình |
| **Điều kiện kích hoạt** | Người dùng hỏi chi tiết về một sự kiện đã biết tên (Ví dụ: "Lịch trình của sự kiện T-Rex Concert thế nào?") |
| **Điều kiện tiên quyết** | AI đã được cấu hình Tool Calling (`@Tool("getEventScheduleTool")`). Sự kiện phải tồn tại. |
| **Điều kiện thất bại** | AI không nhận diện được tên sự kiện hoặc sự kiện không có lịch trình. |
| **Điều kiện thành công** | Chatbot liệt kê mốc thời gian và hoạt động của sự kiện. |
| **Luồng sự kiện chính** | 1. Người dùng nhập câu hỏi về lịch trình. 2. LLM nhận diện cần sử dụng `getEventScheduleTool` và trích xuất từ khóa tên sự kiện. 3. Spring AI kích hoạt hàm Java, tìm ID sự kiện gần đúng với từ khóa. 4. Truy vấn bảng `EventSchedule` theo ID sự kiện. 5. Hàm Java trả kết quả các mốc thời gian về cho LLM. 6. LLM đúc kết lại bằng ngôn ngữ tự nhiên: "Sự kiện T-Rex Concert có lịch trình như sau: 8h Đón khách, 9h Khai mạc...". 7. Trả về cho Frontend. |
| **Luồng sự kiện thay thế** | 4a. Sự kiện chưa được Organizer thêm lịch trình -> AI trả lời "Sự kiện này hiện chưa có lịch trình cụ thể." |
| **Luồng sự kiện ngoại lệ** | 3a. Không tìm thấy sự kiện nào khớp với tên người dùng gõ -> AI trả lời "Tôi không tìm thấy sự kiện nào có tên như vậy." |
