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

import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

// --- DTO RECORDS CHO CÁC CÔNG CỤ (TOOLS) ---

// 1. Tìm kiếm sự kiện
record EventSearchRequest(String keyword) {}
record EventSummary(Long id, String name, String time, String price, String location, String category) {}
record EventSearchResponse(List<EventSummary> events, String message) {}

// 2. Chi tiết sự kiện
record EventDetailsRequest(Long eventId) {}
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
    String status
) {}

// 3. Lịch trình sự kiện
record EventScheduleRequest(Long eventId) {}
record ScheduleSummary(Long id, String title, String startTime, String endTime, String description) {}
record EventScheduleResponse(List<ScheduleSummary> schedules, String message) {}

// 4. Sự kiện sắp diễn ra
record UpcomingEventsRequest() {}

// 5. Thông tin nhà tổ chức
record OrganizerInfoRequest(Long eventId) {}
record OrganizerInfoResponse(String organizerName, String email, String phone, String address, String message) {}
// 6. Sự kiện theo thể loại
record CategoryEventsRequest(String category) {}

// 7. Sự kiện theo địa điểm
record LocationEventsRequest(String location) {}

// 8. Sự kiện miễn phí
record FreeEventsRequest() {}

// 9. Danh sách thể loại
record AllCategoriesRequest() {}
record CategoriesResponse(List<String> categories, String message) {}

// 10. Sự kiện trong khoảng thời gian tới (vd 7 ngày, 30 ngày)
record EventsInDaysRequest(int days) {}

// 11. Kiểm tra tình trạng vé
record CheckTicketRequest(Long eventId) {}
record TicketStatusResponse(String eventName, String eventStatus, int capacity, int soldTickets, int remainingTickets, String ticketPrice, String message) {}


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
                EventStatus.PUBLISHED
            );
            
            if (events.isEmpty()) {
                return new EventSearchResponse(List.of(), "Không tìm thấy sự kiện nào khớp với từ khóa: " + request.keyword());
            }

            List<EventSummary> summaries = events.stream()
                .map(e -> new EventSummary(
                    e.getId(),
                    e.getTitle(),
                    e.getStartTime().toString(),
                    e.getTicketPrice().toString() + " VND",
                    e.getLocation(),
                    e.getCategory()
                ))
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
                return new EventDetailsResponse(null, "Không tìm thấy sự kiện với ID này", "", "", "", "", "", "", 0, 0, 0, "0", "", "NOT_FOUND");
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
                e.getStatus().name()
            );
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
                    s.getDescription()
                ))
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
                java.time.LocalDateTime.now()
            );

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
                    e.getCategory()
                ))
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
                "Lấy thông tin thành công."
            );
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
                EventStatus.PUBLISHED
            );
            
            if (events.isEmpty()) {
                return new EventSearchResponse(List.of(), "Không tìm thấy sự kiện nào thuộc thể loại: " + request.category());
            }

            List<EventSummary> summaries = events.stream()
                .map(e -> new EventSummary(e.getId(), e.getTitle(), e.getStartTime().toString(), e.getTicketPrice().toString() + " VND", e.getLocation(), e.getCategory()))
                .collect(Collectors.toList());

            return new EventSearchResponse(summaries, "Tìm thấy " + summaries.size() + " sự kiện thuộc thể loại " + request.category());
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
                EventStatus.PUBLISHED
            );
            
            if (events.isEmpty()) {
                return new EventSearchResponse(List.of(), "Không tìm thấy sự kiện nào tại địa điểm: " + request.location());
            }

            List<EventSummary> summaries = events.stream()
                .map(e -> new EventSummary(e.getId(), e.getTitle(), e.getStartTime().toString(), e.getTicketPrice().toString() + " VND", e.getLocation(), e.getCategory()))
                .collect(Collectors.toList());

            return new EventSearchResponse(summaries, "Tìm thấy " + summaries.size() + " sự kiện tại " + request.location());
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
                EventStatus.PUBLISHED
            );
            
            if (events.isEmpty()) {
                return new EventSearchResponse(List.of(), "Hiện tại không có sự kiện miễn phí nào sắp diễn ra.");
            }

            List<EventSummary> summaries = events.stream()
                .map(e -> new EventSummary(e.getId(), e.getTitle(), e.getStartTime().toString(), "Miễn phí", e.getLocation(), e.getCategory()))
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
                EventStatus.PUBLISHED, now, endDate
            );
            
            if (events.isEmpty()) {
                return new EventSearchResponse(List.of(), "Không có sự kiện nào sẽ diễn ra trong vòng " + request.days() + " ngày tới.");
            }

            List<EventSummary> summaries = events.stream()
                .map(e -> new EventSummary(e.getId(), e.getTitle(), e.getStartTime().toString(), e.getTicketPrice().toString() + " VND", e.getLocation(), e.getCategory()))
                .collect(Collectors.toList());

            return new EventSearchResponse(summaries, "Tìm thấy " + summaries.size() + " sự kiện diễn ra trong " + request.days() + " ngày tới.");
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
            if(e.getStatus() != EventStatus.PUBLISHED) {
                status = "Ngừng bán vé";
            }
            return new TicketStatusResponse(
                e.getTitle(),
                status,
                e.getCapacity(),
                e.getSoldTickets(),
                remaining,
                e.getTicketPrice().toString() + " VND",
                "Đã kiểm tra tình trạng vé thành công."
            );
        };
    }
}
