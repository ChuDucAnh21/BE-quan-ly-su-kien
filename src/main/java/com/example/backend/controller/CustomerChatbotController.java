package com.example.backend.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;

@RestController
@RequestMapping("/api/v1/public/customer/chat")
public class CustomerChatbotController {

    private static final Logger log = LoggerFactory.getLogger(CustomerChatbotController.class);
    private final ChatClient chatClient;

    public CustomerChatbotController(ChatClient.Builder chatClientBuilder) {
        this.chatClient = chatClientBuilder.build();
    }

    @GetMapping(value = "/stream") // Bạn có thể cân nhắc đổi tên endpoint thành "/ask" cho chuẩn REST
    public String streamChat(@RequestParam String message) {

        String systemPrompt = """
                Bạn là trợ lý ảo AI chuyên nghiệp của nền tảng bán vé sự kiện HubEvent.
                Nhiệm vụ duy nhất của bạn là hỗ trợ khách hàng tìm kiếm sự kiện, xem giá vé và lịch trình.

                HÃY TUÂN THỦ NGHIÊM NGẶT CÁC QUY TẮC SAU:
                1. Khi khách hỏi về sự kiện, BẮT BUỘC phải gọi các công cụ (functions) được cung cấp để lấy dữ liệu.
                2. KHÔNG BAO GIỜ được tự bịa ra tên sự kiện, giá vé hoặc thời gian nếu công cụ không trả về dữ liệu đó.
                3. Nếu công cụ trả về kết quả rỗng, hãy nói: "Dạ, hiện tại HubEvent chưa có sự kiện nào như vậy ạ."
                4. Nếu khách hỏi những câu ngoài lề (ví dụ: thời tiết, chính trị, toán học, code...), hãy từ chối lịch sự: "Xin lỗi, mình là trợ lý của HubEvent nên chỉ có thể giải đáp các thông tin về sự kiện thôi ạ."
                5. Trả lời bằng tiếng Việt, giọng điệu vui vẻ, thân thiện và có sử dụng emoji.
                """;

        try {
            // Thực hiện gọi AI đồng bộ (đợi lấy trọn bộ kết quả)
            return this.chatClient.prompt()
                    .system(systemPrompt)
                    .user(message)
                    .functions(
                            "searchEventFunction",
                            "getEventDetailsFunction",
                            "getEventScheduleFunction",
                            "getUpcomingEventsFunction",
                            "getOrganizerInfoFunction",
                            "getEventsByCategoryFunction",
                            "getEventsByLocationFunction",
                            "getFreeEventsFunction",
                            "getAllCategoriesFunction",
                            "getEventsInDaysFunction",
                            "checkTicketStatusFunction")
                    .call() // Dùng CALL thay vì STREAM
                    .content();

        } catch (Exception e) {
            // Bắt mọi lỗi văng ra (bao gồm cả lỗi 429 Too Many Requests từ OpenRouter)
            // log.error("Lỗi xảy ra trong quá trình gọi chatbot: ", e); // Mở comment nếu
            // bạn có khai báo @Slf4j
            System.err.println("Lỗi gọi AI: " + e.getMessage());

            return "Xin lỗi, hiện tại trợ lý AI đang quá tải. Vui lòng thử lại sau ít phút!";
        }
    }
}