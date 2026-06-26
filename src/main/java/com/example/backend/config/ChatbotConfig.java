package com.example.backend.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Description;

import com.example.backend.entity.Event;
import com.example.backend.entity.EventSchedule;
import com.example.backend.entity.User;
import com.example.backend.enums.EventStatus;
import com.example.backend.repository.EventRepository;
import com.example.backend.repository.EventScheduleRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;

// --- DTO RECORDS CHO CÁC CÔNG CỤ (TOOLS) ---

// 1. Tìm kiếm sự kiện
record EventSearchRequest(@JsonPropertyDescription("Từ khóa tìm kiếm (tên sự kiện)") String keyword) {
}

record EventSummary(Long id, String name, String time, String price, String location, String category) {
}

record EventSearchResponse(List<EventSummary> events, String message) {
}

// 2. Chi tiết sự kiện
record EventDetailsRequest(@JsonPropertyDescription("ID của sự kiện cần xem chi tiết") Long eventId) {
}

record EventDetailsResponse(
        Long id,
        String title,
        String category,
        String shortDescription,
        String description,
        String startTime,
        String endTime,
        String location,
        int capacity,
        int soldTickets,
        int remainingTickets,
        String ticketPrice,
        String organizerName,
        String status) {
}

// 3. Lịch trình sự kiện
record EventScheduleRequest(@JsonPropertyDescription("ID của sự kiện cần lấy lịch trình") Long eventId) {
}

record ScheduleSummary(Long id, String title, String startTime, String endTime, String description) {
}

record EventScheduleResponse(List<ScheduleSummary> schedules, String message) {
}

// 4. Sự kiện sắp diễn ra
record UpcomingEventsRequest(@JsonPropertyDescription("Bỏ trống hoặc truyền 'all'") String filter) {
}

// 5. Thông tin nhà tổ chức
record OrganizerInfoRequest(@JsonPropertyDescription("ID của sự kiện cần xem thông tin nhà tổ chức") Long eventId) {
}

record OrganizerInfoResponse(String organizerName, String email, String phone, String address, String message) {
}

// 6. Sự kiện theo thể loại
record CategoryEventsRequest(
        @JsonPropertyDescription("Tên thể loại sự kiện (ví dụ: Âm nhạc, Thể thao, Hội thảo...)") String category) {
}

// 7. Sự kiện theo địa điểm
record LocationEventsRequest(
        @JsonPropertyDescription("Địa điểm hoặc thành phố tổ chức (ví dụ: Hà Nội, TP HCM, Đà Nẵng...)") String location) {
}

// 8. Sự kiện miễn phí
record FreeEventsRequest(@JsonPropertyDescription("Bỏ trống hoặc truyền 'free'") String filter) {
}

// 9. Danh sách thể loại
record AllCategoriesRequest(@JsonPropertyDescription("Bỏ trống hoặc truyền 'all'") String filter) {
}

record CategoriesResponse(List<String> categories, String message) {
}

// 10. Sự kiện trong khoảng thời gian tới (vd 7 ngày, 30 ngày)
record EventsInDaysRequest(@JsonPropertyDescription("Số ngày tới cần tìm sự kiện (ví dụ: 7, 14, 30...)") int days) {
}

// 11. Kiểm tra tình trạng vé
record CheckTicketRequest(@JsonPropertyDescription("ID của sự kiện cần kiểm tra vé") Long eventId) {
}

record TicketStatusResponse(String eventName, String eventStatus, int capacity, int soldTickets, int remainingTickets,
        String ticketPrice, String message) {
}

// 12. Tìm sự kiện theo trạng thái thời gian thực (ONGOING, COMING_SOON_SALE,
// ON_SALE, UPCOMING, ENDED)
record EventsByStatusRequest(
        @JsonPropertyDescription("Trạng thái thời gian thực cần tìm: ONGOING (đang diễn ra), COMING_SOON_SALE (sắp mở bán vé), ON_SALE (đang mở bán vé), UPCOMING (sắp diễn ra), ENDED (đã kết thúc)") String status) {
}

// 13. Tìm sự kiện bằng từ khóa nằm trong Nội dung / Mô tả
record DescriptionKeywordSearchRequest(
        @JsonPropertyDescription("Từ khóa cần tìm trong nội dung chi tiết hoặc mô tả sự kiện") String keyword) {
}

// 14. Sự kiện bán chạy nhất (Top Selling)
record TopSellingEventsRequest(@JsonPropertyDescription("Bỏ trống hoặc truyền 'top'") String filter) {
}

// 15. Đếm số lượng sự kiện theo trạng thái
record CountEventsByStatusRequest(
        @JsonPropertyDescription("Trạng thái sự kiện cần đếm: PUBLISHED, CLOSED, COMPLETED, CANCELLED") String status) {
}

// 16. Tìm sự kiện theo tháng và năm
record EventsByMonthRequest(
        @JsonPropertyDescription("Tháng cần tìm sự kiện (từ 1 đến 12)") int month,
        @JsonPropertyDescription("Năm cần tìm sự kiện (ví dụ: 2026). Nếu khách không nói năm, truyền năm hiện tại") int year) {
}

// 17. Tìm sự kiện theo ngày cụ thể (ngày, tháng, năm)
record EventsBySpecificDateRequest(
        @JsonPropertyDescription("Ngày cần tìm sự kiện (từ 1 đến 31)") int day,
        @JsonPropertyDescription("Tháng cần tìm sự kiện (từ 1 đến 12)") int month,
        @JsonPropertyDescription("Năm cần tìm sự kiện (ví dụ: 2026). Nếu khách không nói năm, truyền năm hiện tại") int year) {
}

record CountEventsResponse(long count, String message) {
}

@Configuration
public class ChatbotConfig {

    private final EventRepository eventRepository;
    private final EventScheduleRepository eventScheduleRepository;

    public ChatbotConfig(EventRepository eventRepository, EventScheduleRepository eventScheduleRepository) {
        this.eventRepository = eventRepository;
        this.eventScheduleRepository = eventScheduleRepository;
    }

    // --- TOOL 1: TÌM KIẾM SỰ KIỆN THEO TÊN ---
    @Bean
    @Description("Tìm kiếm các sự kiện đang mở bán trên website dựa theo từ khóa (tên sự kiện). Trả về danh sách tóm tắt sự kiện kèm ID.")
    public Function<EventSearchRequest, EventSearchResponse> searchEventFunction() {
        return request -> {
            System.out.println("🤖 AI gọi: searchEventFunction với từ khóa: " + request.keyword());
            List<Event> events = eventRepository.findTop3ByTitleContainingIgnoreCaseAndStatus(
                    request.keyword(),
                    EventStatus.PUBLISHED);

            if (events.isEmpty()) {
                return new EventSearchResponse(List.of(),
                        "Không tìm thấy sự kiện nào khớp với từ khóa: " + request.keyword());
            }

            List<EventSummary> summaries = events.stream()
                    .map(e -> new EventSummary(
                            e.getId(),
                            e.getTitle(),
                            e.getStartTime().toString(),
                            e.getTicketPrice().toString() + " VND",
                            e.getLocation(),
                            e.getCategory()))
                    .collect(Collectors.toList());

            return new EventSearchResponse(summaries, "Tìm thấy " + summaries.size() + " sự kiện.");
        };
    }

    // --- TOOL 2: LẤY CHI TIẾT SỰ KIỆN ---
    @Bean
    @Description("Lấy thông tin chi tiết đầy đủ của một sự kiện cụ thể bằng ID sự kiện (bao gồm giá vé, địa điểm, mô tả chi tiết, số lượng vé còn lại).")
    public Function<EventDetailsRequest, EventDetailsResponse> getEventDetailsFunction() {
        return request -> {
            System.out.println("🤖 AI gọi: getEventDetailsFunction với ID: " + request.eventId());
            Event e = eventRepository.findById(request.eventId()).orElse(null);
            if (e == null) {
                return new EventDetailsResponse(null, "Không tìm thấy sự kiện với ID này", "", "", "", "", "", "", 0, 0,
                        0, "0", "", "NOT_FOUND");
            }
            int remaining = Math.max(0, e.getCapacity() - e.getSoldTickets());
            return new EventDetailsResponse(
                    e.getId(),
                    e.getTitle(),
                    e.getCategory(),
                    e.getShortDescription(),
                    e.getDescription(),
                    e.getStartTime().toString(),
                    e.getEndTime().toString(),
                    e.getLocation(),
                    e.getCapacity(),
                    e.getSoldTickets(),
                    remaining,
                    e.getTicketPrice().toString() + " VND",
                    e.getOrganizer() != null ? e.getOrganizer().getFullName() : "N/A",
                    e.getStatus().name());
        };
    }

    // --- TOOL 3: LẤY LỊCH TRÌNH SỰ KIỆN ---
    @Bean
    @Description("Lấy lịch trình chi tiết (timeline, các mốc thời gian diễn ra hoạt động con) của một sự kiện dựa theo ID sự kiện.")
    public Function<EventScheduleRequest, EventScheduleResponse> getEventScheduleFunction() {
        return request -> {
            System.out.println("🤖 AI gọi: getEventScheduleFunction với ID sự kiện: " + request.eventId());
            List<EventSchedule> schedules = eventScheduleRepository.findByEventIdOrderByStartTimeAsc(request.eventId());
            if (schedules.isEmpty()) {
                return new EventScheduleResponse(List.of(), "Sự kiện này hiện chưa cập nhật lịch trình chi tiết.");
            }
            List<ScheduleSummary> summaries = schedules.stream()
                    .map(s -> new ScheduleSummary(
                            s.getId(),
                            s.getTitle(),
                            s.getStartTime().toString(),
                            s.getEndTime().toString(),
                            s.getDescription()))
                    .collect(Collectors.toList());
            return new EventScheduleResponse(summaries, "Lấy lịch trình thành công.");
        };
    }

    // --- TOOL 4: LẤY CÁC SỰ KIỆN SẮP DIỄN RA ---
    @Bean
    @Description("Lấy danh sách tối đa 5 sự kiện sắp diễn ra trong tương lai gần nhất (đang mở bán vé).")
    public Function<UpcomingEventsRequest, EventSearchResponse> getUpcomingEventsFunction() {
        return request -> {
            System.out.println("🤖 AI gọi: getUpcomingEventsFunction");
            // Lấy danh sách sự kiện ACTIVE hoặc PUBLISHED trong tương lai
            // Ở đây tạm dùng findUpcomingEventsStr hoặc viết Query mới tùy bạn.
            // Để thống nhất ta lấy top 5 sự kiện PUBLISHED sắp tới:
            List<Event> events = eventRepository.findTop5ByStatusAndStartTimeAfterOrderByStartTimeAsc(
                    EventStatus.PUBLISHED,
                    java.time.LocalDateTime.now());

            if (events.isEmpty()) {
                return new EventSearchResponse(List.of(), "Hiện tại không có sự kiện sắp diễn ra nào.");
            }

            List<EventSummary> summaries = events.stream()
                    .map(e -> new EventSummary(
                            e.getId(),
                            e.getTitle(),
                            e.getStartTime().toString(),
                            e.getTicketPrice().toString() + " VND",
                            e.getLocation(),
                            e.getCategory()))
                    .collect(Collectors.toList());

            return new EventSearchResponse(summaries, "Lấy danh sách sự kiện sắp tới thành công.");
        };
    }

    // --- TOOL 5: THÔNG TIN LIÊN HỆ ĐƠN VỊ TỔ CHỨC ---
    @Bean
    @Description("Lấy thông tin liên hệ (Email, Số điện thoại, Địa chỉ) của nhà tổ chức (Organizer) của một sự kiện dựa theo ID sự kiện.")
    public Function<OrganizerInfoRequest, OrganizerInfoResponse> getOrganizerInfoFunction() {
        return request -> {
            System.out.println("🤖 AI gọi: getOrganizerInfoFunction với ID sự kiện: " + request.eventId());
            Event e = eventRepository.findById(request.eventId()).orElse(null);
            if (e == null || e.getOrganizer() == null) {
                return new OrganizerInfoResponse("", "", "", "", "Không tìm thấy thông tin đơn vị tổ chức.");
            }
            User org = e.getOrganizer();
            return new OrganizerInfoResponse(
                    org.getFullName(),
                    org.getEmail(),
                    org.getPhone(),
                    org.getAddress(),
                    "Lấy thông tin thành công.");
        };
    }

    // --- TOOL 6: TÌM KIẾM SỰ KIỆN THEO THỂ LOẠI ---
    @Bean
    @Description("Tìm kiếm các sự kiện theo một thể loại cụ thể (Ví dụ: Âm nhạc, Thể thao, Hội thảo, ...). Trả về danh sách sự kiện.")
    public Function<CategoryEventsRequest, EventSearchResponse> getEventsByCategoryFunction() {
        return request -> {
            System.out.println("🤖 AI gọi: getEventsByCategoryFunction với thể loại: " + request.category());
            List<Event> events = eventRepository.findTop5ByCategoryContainingIgnoreCaseAndStatus(
                    request.category(),
                    EventStatus.PUBLISHED);

            if (events.isEmpty()) {
                return new EventSearchResponse(List.of(),
                        "Không tìm thấy sự kiện nào thuộc thể loại: " + request.category());
            }

            List<EventSummary> summaries = events.stream()
                    .map(e -> new EventSummary(e.getId(), e.getTitle(), e.getStartTime().toString(),
                            e.getTicketPrice().toString() + " VND", e.getLocation(), e.getCategory()))
                    .collect(Collectors.toList());

            return new EventSearchResponse(summaries,
                    "Tìm thấy " + summaries.size() + " sự kiện thuộc thể loại " + request.category());
        };
    }

    // --- TOOL 7: TÌM KIẾM SỰ KIỆN THEO ĐỊA ĐIỂM ---
    @Bean
    @Description("Tìm kiếm các sự kiện đang diễn ra tại một địa điểm cụ thể (Ví dụ: Hà Nội, TP HCM, Đà Nẵng, tên sân vận động, ...).")
    public Function<LocationEventsRequest, EventSearchResponse> getEventsByLocationFunction() {
        return request -> {
            System.out.println("🤖 AI gọi: getEventsByLocationFunction với địa điểm: " + request.location());
            List<Event> events = eventRepository.findTop5ByLocationContainingIgnoreCaseAndStatus(
                    request.location(),
                    EventStatus.PUBLISHED);

            if (events.isEmpty()) {
                return new EventSearchResponse(List.of(),
                        "Không tìm thấy sự kiện nào tại địa điểm: " + request.location());
            }

            List<EventSummary> summaries = events.stream()
                    .map(e -> new EventSummary(e.getId(), e.getTitle(), e.getStartTime().toString(),
                            e.getTicketPrice().toString() + " VND", e.getLocation(), e.getCategory()))
                    .collect(Collectors.toList());

            return new EventSearchResponse(summaries,
                    "Tìm thấy " + summaries.size() + " sự kiện tại " + request.location());
        };
    }

    // --- TOOL 8: LẤY CÁC SỰ KIỆN MIỄN PHÍ ---
    @Bean
    @Description("Tìm danh sách các sự kiện đang mở bán nhưng hoàn toàn miễn phí (giá vé = 0).")
    public Function<FreeEventsRequest, EventSearchResponse> getFreeEventsFunction() {
        return request -> {
            System.out.println("🤖 AI gọi: getFreeEventsFunction");
            List<Event> events = eventRepository.findTop5ByTicketPriceAndStatus(
                    java.math.BigDecimal.ZERO,
                    EventStatus.PUBLISHED);

            if (events.isEmpty()) {
                return new EventSearchResponse(List.of(), "Hiện tại không có sự kiện miễn phí nào sắp diễn ra.");
            }

            List<EventSummary> summaries = events.stream()
                    .map(e -> new EventSummary(e.getId(), e.getTitle(), e.getStartTime().toString(), "Miễn phí",
                            e.getLocation(), e.getCategory()))
                    .collect(Collectors.toList());

            return new EventSearchResponse(summaries, "Tìm thấy " + summaries.size() + " sự kiện miễn phí.");
        };
    }

    // --- TOOL 9: LẤY TẤT CẢ DANH MỤC SỰ KIỆN ---
    @Bean
    @Description("Liệt kê tất cả các thể loại/danh mục sự kiện đang có trong hệ thống (để có thể gợi ý cho người dùng chọn).")
    public Function<AllCategoriesRequest, CategoriesResponse> getAllCategoriesFunction() {
        return request -> {
            System.out.println("🤖 AI gọi: getAllCategoriesFunction");
            List<String> categories = eventRepository.findDistinctCategoryByStatus(EventStatus.PUBLISHED);
            if (categories.isEmpty()) {
                return new CategoriesResponse(List.of(), "Hệ thống hiện chưa có danh mục sự kiện nào.");
            }
            return new CategoriesResponse(categories, "Tìm thấy các danh mục sự kiện đang mở bán.");
        };
    }

    // --- TOOL 10: TÌM SỰ KIỆN TRONG N NGÀY TỚI ---
    @Bean
    @Description("Tìm kiếm các sự kiện sẽ diễn ra trong vòng N ngày tới (Ví dụ: 7 ngày, 30 ngày, 3 ngày tới). Input là số ngày (Integer).")
    public Function<EventsInDaysRequest, EventSearchResponse> getEventsInDaysFunction() {
        return request -> {
            System.out.println("🤖 AI gọi: getEventsInDaysFunction với số ngày: " + request.days());
            java.time.LocalDateTime now = java.time.LocalDateTime.now();
            java.time.LocalDateTime endDate = now.plusDays(request.days());

            List<Event> events = eventRepository.findTop5ByStatusAndStartTimeBetweenOrderByStartTimeAsc(
                    EventStatus.PUBLISHED, now, endDate);

            if (events.isEmpty()) {
                return new EventSearchResponse(List.of(),
                        "Không có sự kiện nào sẽ diễn ra trong vòng " + request.days() + " ngày tới.");
            }

            List<EventSummary> summaries = events.stream()
                    .map(e -> new EventSummary(e.getId(), e.getTitle(), e.getStartTime().toString(),
                            e.getTicketPrice().toString() + " VND", e.getLocation(), e.getCategory()))
                    .collect(Collectors.toList());

            return new EventSearchResponse(summaries,
                    "Tìm thấy " + summaries.size() + " sự kiện diễn ra trong " + request.days() + " ngày tới.");
        };
    }

    // --- TOOL 11: KIỂM TRA TÌNH TRẠNG VÉ ---
    @Bean
    @Description("Kiểm tra xem một sự kiện (theo ID) còn vé hay không, số lượng vé đã bán, tổng sức chứa và giá vé hiện tại.")
    public Function<CheckTicketRequest, TicketStatusResponse> checkTicketStatusFunction() {
        return request -> {
            System.out.println("🤖 AI gọi: checkTicketStatusFunction với ID sự kiện: " + request.eventId());
            Event e = eventRepository.findById(request.eventId()).orElse(null);
            if (e == null) {
                return new TicketStatusResponse("", "NOT_FOUND", 0, 0, 0, "", "Không tìm thấy sự kiện với ID này.");
            }
            int remaining = Math.max(0, e.getCapacity() - e.getSoldTickets());
            String status = remaining > 0 ? "Còn vé" : "Hết vé";
            if (e.getStatus() != EventStatus.PUBLISHED) {
                status = "Ngừng bán vé";
            }
            return new TicketStatusResponse(
                    e.getTitle(),
                    status,
                    e.getCapacity(),
                    e.getSoldTickets(),
                    remaining,
                    e.getTicketPrice().toString() + " VND",
                    "Đã kiểm tra tình trạng vé thành công.");
        };
    }

    private String calculateOngoingStatus(LocalDateTime regStart, LocalDateTime regEnd, LocalDateTime eventStart,
            LocalDateTime eventEnd) {
        LocalDateTime now = LocalDateTime.now();
        if (eventStart == null || eventEnd == null)
            return "ENDED";

        // 1. Đang diễn ra
        if ((now.isEqual(eventStart) || now.isAfter(eventStart)) && (now.isEqual(eventEnd) || now.isBefore(eventEnd))) {
            return "ONGOING";
        }

        // 2. Có mốc thời gian đăng ký (registration time)
        if (regStart != null && regEnd != null) {
            if (now.isBefore(regStart)) {
                return "COMING_SOON_SALE";
            }
            if ((now.isEqual(regStart) || now.isAfter(regStart)) && (now.isEqual(regEnd) || now.isBefore(regEnd))) {
                return "ON_SALE";
            }
            if (now.isAfter(regEnd) && now.isBefore(eventStart)) {
                return "UPCOMING";
            }
        }

        // 3. Không có registration time -> chỉ check event time
        if (now.isBefore(eventStart)) {
            return "UPCOMING";
        }

        // 4. Mặc định
        return "ENDED";
    }

    // --- TOOL 12: TÌM SỰ KIỆN THEO TRẠNG THÁI THỜI GIAN THỰC ĐỘNG ---
    @Bean
    @Description("Tìm kiếm danh sách sự kiện theo trạng thái tính toán thực tế dựa trên thời gian hiện tại: ONGOING (đang diễn ra), COMING_SOON_SALE (sắp mở bán vé), ON_SALE (đang mở bán vé), UPCOMING (sắp diễn ra), ENDED (đã kết thúc).")
    public Function<EventsByStatusRequest, EventSearchResponse> getEventsByStatusFunction() {
        return request -> {
            System.out.println("🤖 AI gọi: getEventsByStatusFunction với trạng thái động: " + request.status());
            String targetStatus = request.status() != null ? request.status().trim().toUpperCase() : "";

            List<Event> allEvents = eventRepository.findAll();
            List<Event> matchedEvents = allEvents.stream()
                    .filter(e -> e.getStatus() != EventStatus.CANCELLED)
                    .filter(e -> targetStatus.equals(calculateOngoingStatus(
                            e.getRegistrationStartTime(),
                            e.getRegistrationEndTime(),
                            e.getStartTime(),
                            e.getEndTime())))
                    .limit(5)
                    .collect(Collectors.toList());

            if (matchedEvents.isEmpty()) {
                return new EventSearchResponse(List.of(),
                        "Hiện tại không có sự kiện nào đang ở trạng thái: " + targetStatus);
            }

            List<EventSummary> summaries = matchedEvents.stream()
                    .map(e -> new EventSummary(e.getId(), e.getTitle(), e.getStartTime().toString(),
                            e.getTicketPrice() != null ? e.getTicketPrice().toString() + " VND" : "Miễn phí",
                            e.getLocation(), e.getCategory()))
                    .collect(Collectors.toList());

            return new EventSearchResponse(summaries,
                    "Tìm thấy " + summaries.size() + " sự kiện ở trạng thái " + targetStatus);
        };
    }

    // --- TOOL 13: TÌM SỰ KIỆN QUA TỪ KHÓA MÔ TẢ (Câu quan trọng, Highlights) ---
    @Bean
    @Description("Tìm kiếm các sự kiện mà trong phần nội dung chi tiết hoặc mô tả có chứa từ khóa được cung cấp (dùng để tìm các câu quan trọng, điểm nổi bật của sự kiện).")
    public Function<DescriptionKeywordSearchRequest, EventSearchResponse> searchEventsByDescriptionKeywordFunction() {
        return request -> {
            System.out.println("🤖 AI gọi: searchEventsByDescriptionKeywordFunction với từ khóa: " + request.keyword());
            // Tìm kiếm trong Description nhưng ưu tiên các sự kiện đang mở bán
            List<Event> events = eventRepository.findTop5ByDescriptionContainingIgnoreCase(request.keyword());
            if (events.isEmpty()) {
                return new EventSearchResponse(List.of(),
                        "Không tìm thấy sự kiện nào có nội dung chứa từ khóa: " + request.keyword());
            }
            List<EventSummary> summaries = events.stream()
                    .filter(e -> e.getStatus() == EventStatus.PUBLISHED || e.getStatus() == EventStatus.CLOSED)
                    .map(e -> new EventSummary(e.getId(), e.getTitle(), e.getStartTime().toString(),
                            e.getTicketPrice().toString() + " VND", e.getLocation(), e.getCategory()))
                    .collect(Collectors.toList());
            return new EventSearchResponse(summaries, "Tìm thấy các sự kiện có nhắc đến từ khóa này.");
        };
    }

    // --- TOOL 14: SỰ KIỆN BÁN CHẠY NHẤT (HOT EVENTS) ---
    @Bean
    @Description("Lấy danh sách top 5 sự kiện Hot nhất, bán chạy nhất (số lượng vé bán ra cao nhất) hiện đang mở bán trên hệ thống.")
    public Function<TopSellingEventsRequest, EventSearchResponse> getTopSellingEventsFunction() {
        return request -> {
            System.out.println("🤖 AI gọi: getTopSellingEventsFunction");
            List<Event> events = eventRepository.findTop5ByStatusOrderBySoldTicketsDesc(EventStatus.PUBLISHED);
            if (events.isEmpty()) {
                return new EventSearchResponse(List.of(), "Hệ thống hiện chưa có sự kiện mở bán nào.");
            }
            List<EventSummary> summaries = events.stream()
                    .map(e -> new EventSummary(e.getId(), e.getTitle() + " (Đã bán: " + e.getSoldTickets() + " vé)",
                            e.getStartTime().toString(), e.getTicketPrice().toString() + " VND", e.getLocation(),
                            e.getCategory()))
                    .collect(Collectors.toList());
            return new EventSearchResponse(summaries, "Top sự kiện bán chạy nhất hệ thống.");
        };
    }

    // --- TOOL 15: ĐẾM SỐ LƯỢNG SỰ KIỆN THEO TRẠNG THÁI ---
    @Bean
    @Description("Đếm tổng số lượng sự kiện hiện có trên hệ thống theo một trạng thái (ví dụ muốn biết có bao nhiêu sự kiện đang mở bán, bao nhiêu đã kết thúc).")
    public Function<CountEventsByStatusRequest, CountEventsResponse> countEventsByStatusFunction() {
        return request -> {
            System.out.println("🤖 AI gọi: countEventsByStatusFunction với trạng thái: " + request.status());
            EventStatus status;
            try {
                status = EventStatus.valueOf(request.status().toUpperCase());
            } catch (Exception e) {
                return new CountEventsResponse(0,
                        "Trạng thái không hợp lệ. Các trạng thái: PUBLISHED, CLOSED, COMPLETED, CANCELLED.");
            }
            long count = eventRepository.countByStatus(status);
            return new CountEventsResponse(count,
                    "Hệ thống có tổng cộng " + count + " sự kiện ở trạng thái " + status.name());
        };
    }

    // --- TOOL 16: TÌM SỰ KIỆN THEO THÁNG VÀ NĂM ---
    @Bean
    @Description("Tìm kiếm danh sách sự kiện diễn ra trong một tháng và năm cụ thể (Ví dụ: sự kiện tháng 6 năm 2026, sự kiện trong tháng tới...).")
    public Function<EventsByMonthRequest, EventSearchResponse> getEventsByMonthFunction() {
        return request -> {
            System.out.println("🤖 AI gọi: getEventsByMonthFunction với tháng: " + request.month() + ", năm: " + request.year());
            int year = request.year() > 2000 ? request.year() : java.time.LocalDateTime.now().getYear();
            int month = (request.month() >= 1 && request.month() <= 12) ? request.month() : java.time.LocalDateTime.now().getMonthValue();

            java.time.YearMonth yearMonth = java.time.YearMonth.of(year, month);
            java.time.LocalDateTime startOfMonth = yearMonth.atDay(1).atStartOfDay();
            java.time.LocalDateTime endOfMonth = yearMonth.atEndOfMonth().atTime(23, 59, 59);

            List<Event> allEvents = eventRepository.findAll();
            List<Event> matchedEvents = allEvents.stream()
                .filter(e -> e.getStatus() != EventStatus.CANCELLED)
                .filter(e -> e.getStartTime() != null && !e.getStartTime().isBefore(startOfMonth) && !e.getStartTime().isAfter(endOfMonth))
                .limit(10)
                .collect(Collectors.toList());

            if (matchedEvents.isEmpty()) {
                return new EventSearchResponse(List.of(), "Không có sự kiện nào diễn ra trong tháng " + month + "/" + year);
            }

            List<EventSummary> summaries = matchedEvents.stream()
                .map(e -> new EventSummary(e.getId(), e.getTitle(), e.getStartTime().toString(), e.getTicketPrice() != null ? e.getTicketPrice().toString() + " VND" : "Miễn phí", e.getLocation(), e.getCategory()))
                .collect(Collectors.toList());

            return new EventSearchResponse(summaries, "Tìm thấy " + summaries.size() + " sự kiện diễn ra trong tháng " + month + "/" + year);
        };
    }

    // --- TOOL 17: TÌM SỰ KIỆN THEO NGÀY CỤ THỂ ---
    @Bean
    @Description("Tìm kiếm danh sách sự kiện diễn ra vào một ngày cụ thể chính xác (Ví dụ: sự kiện ngày 18 tháng 6, sự kiện hôm nay, sự kiện ngày mai...).")
    public Function<EventsBySpecificDateRequest, EventSearchResponse> getEventsBySpecificDateFunction() {
        return request -> {
            System.out.println("🤖 AI gọi: getEventsBySpecificDateFunction với ngày: " + request.day() + "/" + request.month() + "/" + request.year());
            int year = request.year() > 2000 ? request.year() : java.time.LocalDateTime.now().getYear();
            int month = (request.month() >= 1 && request.month() <= 12) ? request.month() : java.time.LocalDateTime.now().getMonthValue();
            int day = (request.day() >= 1 && request.day() <= 31) ? request.day() : java.time.LocalDateTime.now().getDayOfMonth();

            java.time.LocalDate targetDate;
            try {
                targetDate = java.time.LocalDate.of(year, month, day);
            } catch (Exception e) {
                return new EventSearchResponse(List.of(), "Ngày " + day + "/" + month + "/" + year + " không hợp lệ.");
            }

            java.time.LocalDateTime startOfDay = targetDate.atStartOfDay();
            java.time.LocalDateTime endOfDay = targetDate.atTime(23, 59, 59);

            List<Event> allEvents = eventRepository.findAll();
            List<Event> matchedEvents = allEvents.stream()
                .filter(e -> e.getStatus() != EventStatus.CANCELLED)
                .filter(e -> e.getStartTime() != null && !e.getStartTime().isBefore(startOfDay) && !e.getStartTime().isAfter(endOfDay))
                .limit(10)
                .collect(Collectors.toList());

            if (matchedEvents.isEmpty()) {
                return new EventSearchResponse(List.of(), "Không có sự kiện nào diễn ra vào ngày " + day + "/" + month + "/" + year);
            }

            List<EventSummary> summaries = matchedEvents.stream()
                .map(e -> new EventSummary(e.getId(), e.getTitle(), e.getStartTime().toString(), e.getTicketPrice() != null ? e.getTicketPrice().toString() + " VND" : "Miễn phí", e.getLocation(), e.getCategory()))
                .collect(Collectors.toList());

            return new EventSearchResponse(summaries, "Tìm thấy " + summaries.size() + " sự kiện diễn ra vào ngày " + day + "/" + month + "/" + year);
        };
    }
}
