package com.example.backend.service.impl;

import com.example.backend.dto.request.EventRequest;
import com.example.backend.dto.request.EventScheduleRequest;
import com.example.backend.dto.response.EventResponse;
import com.example.backend.dto.response.EventScheduleResponse;
import com.example.backend.dto.response.PageResponse;
import com.example.backend.entity.Event;
import com.example.backend.entity.EventSchedule;
import com.example.backend.entity.EventStaffAssignment;
import com.example.backend.entity.Registration;
import com.example.backend.entity.User;
import com.example.backend.enums.EventStatus;
import com.example.backend.enums.RegistrationStatus;
import com.example.backend.enums.Role;
import com.example.backend.exception.AppException;
import com.example.backend.exception.ErrorCode;
import com.example.backend.repository.EventRepository;
import com.example.backend.repository.EventScheduleRepository;
import com.example.backend.repository.EventStaffAssignmentRepository;
import com.example.backend.repository.PaymentConfigRepository;
import com.example.backend.repository.PaymentRepository;
import com.example.backend.repository.RegistrationRepository;
import com.example.backend.repository.TicketRepository;
import com.example.backend.repository.UserRepository;
import com.example.backend.service.CloudinaryService;
import com.example.backend.service.EventService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class EventServiceImpl implements EventService {

    private final EventRepository eventRepository;
    private final UserRepository userRepository;
    private final PaymentConfigRepository paymentConfigRepository;
    private final RegistrationRepository registrationRepository;
    private final CloudinaryService cloudinaryService;

    // Dùng để lấy danh sách lịch trình con khi trả về chi tiết sự kiện
    private final EventScheduleRepository eventScheduleRepository;
    private final EventStaffAssignmentRepository eventStaffAssignmentRepository;
    private final TicketRepository ticketRepository;
    private final PaymentRepository paymentRepository;

    // Helper: Lấy User đang đăng nhập dựa trên email trong Token
    private User getCurrentUser() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));
    }

    /**
     * Helper: Đồng bộ toàn bộ lịch trình con của sự kiện.
     * - Nếu request.schedules = null → Không làm gì (giữ nguyên dữ liệu cũ).
     * - Nếu request.schedules = [] → Xóa toàn bộ.
     * - Nếu request.schedules có data → Sync (xóa cũ/thêm mới/cập nhật).
     */
    private List<EventScheduleResponse> syncSchedules(Event event, List<EventScheduleRequest> scheduleRequests) {
        if (scheduleRequests == null) {
            // Không gửi field → giữ nguyên
            return eventScheduleRepository.findByEventIdOrderByStartTimeAsc(event.getId())
                    .stream().map(this::mapScheduleToResponse).collect(Collectors.toList());
        }

        // Lấy danh sách hiện tại từ DB
        List<EventSchedule> currentSchedules = eventScheduleRepository.findByEventIdOrderByStartTimeAsc(event.getId());

        // Xác định những ID được giữ lại
        Set<Long> keptIds = scheduleRequests.stream()
                .map(EventScheduleRequest::getId)
                .filter(id -> id != null)
                .collect(Collectors.toSet());

        // Xóa những lịch trình không có trong danh sách mới gửi lên
        List<EventSchedule> toDelete = currentSchedules.stream()
                .filter(s -> !keptIds.contains(s.getId()))
                .collect(Collectors.toList());
        if (!toDelete.isEmpty()) {
            eventScheduleRepository.deleteAll(toDelete);
        }

        List<EventScheduleResponse> results = new ArrayList<>();
        for (EventScheduleRequest req : scheduleRequests) {
            // Validation: không được vượt ngoài khung giờ của sự kiện mẹ
            if (req.getStartTime().isBefore(event.getStartTime()) || req.getEndTime().isAfter(event.getEndTime())) {
                throw new AppException(ErrorCode.TIME_OUT_OF_EVENT_BOUNDS);
            }
            if (!req.getStartTime().isBefore(req.getEndTime())) {
                throw new AppException(ErrorCode.INVALID_TIME_RANGE);
            }

            if (req.getId() == null) {
                // Thêm mới
                EventSchedule newSchedule = EventSchedule.builder()
                        .event(event)
                        .title(req.getTitle())
                        .startTime(req.getStartTime())
                        .endTime(req.getEndTime())
                        .description(req.getDescription())
                        .build();
                results.add(mapScheduleToResponse(eventScheduleRepository.save(newSchedule)));
            } else {
                // Cập nhật
                EventSchedule existing = currentSchedules.stream()
                        .filter(s -> s.getId().equals(req.getId()))
                        .findFirst()
                        .orElseThrow(() -> new AppException(ErrorCode.SCHEDULE_NOT_FOUND));
                existing.setTitle(req.getTitle());
                existing.setStartTime(req.getStartTime());
                existing.setEndTime(req.getEndTime());
                existing.setDescription(req.getDescription());
                results.add(mapScheduleToResponse(eventScheduleRepository.save(existing)));
            }
        }
        return results;
    }

    // Helper: map EventSchedule entity sang DTO
    private EventScheduleResponse mapScheduleToResponse(EventSchedule schedule) {
        return EventScheduleResponse.builder()
                .id(schedule.getId())
                .eventId(schedule.getEvent().getId())
                .title(schedule.getTitle())
                .startTime(schedule.getStartTime())
                .endTime(schedule.getEndTime())
                .description(schedule.getDescription())
                .build();
    }

    // Helper: Chuyển đổi Entity sang DTO, nhận thêm Map đếm vé đã tính sẵn để tránh
    // N+1
    private EventResponse mapToEventResponse(Event event, Map<Long, Long> ticketsSoldMap) {
        long ticketsSold = ticketsSoldMap.getOrDefault(event.getId(), 0L);

        return EventResponse.builder()
                .id(event.getId())
                .organizerId(event.getOrganizer().getId())
                .organizerName(event.getOrganizer().getFullName())
                .title(event.getTitle())
                .category(event.getCategory())
                .shortDescription(event.getShortDescription())
                .description(event.getDescription())
                .bannerUrl(event.getBannerUrl())
                .startTime(event.getStartTime())
                .endTime(event.getEndTime())
                .registrationStartTime(event.getRegistrationStartTime())
                .registrationEndTime(event.getRegistrationEndTime())
                .location(event.getLocation())
                .capacity(event.getCapacity())
                .ticketPrice(event.getTicketPrice())
                .status(event.getStatus())
                .ticketsSold(ticketsSold)
                .createdAt(event.getCreatedAt())
                .updatedAt(event.getUpdatedAt())
                .build();
    }

    // Overload: Dùng cho getEventById (chỉ 1 event) — gọi DB đếm 1 lần là đủ
    private EventResponse mapToEventResponse(Event event) {
        long ticketsSold = registrationRepository.countByEventIdAndStatusIn(
                event.getId(),
                List.of(RegistrationStatus.CONFIRMED, RegistrationStatus.CHECKED_IN,
                        RegistrationStatus.PENDING_PAYMENT));
        return mapToEventResponse(event, Map.of(event.getId(), ticketsSold));
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<EventResponse> getAllEvents(int page, int size, String keyword,
            List<String> categories,
            EventStatus status, Long organizerId, LocalDateTime startTime, LocalDateTime endTime,
            BigDecimal minPrice, BigDecimal maxPrice, Boolean isFree, String sort) {
        // Tùy chỉnh sắp xếp mới nhất (desc) hoặc cũ nhất (asc)
        Sort.Direction direction = "asc".equalsIgnoreCase(sort) ? Sort.Direction.ASC : Sort.Direction.DESC;
        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, "createdAt"));

        // Chuyển rỗng thành null để Query JPQL lờ đi
        String finalKeyword = (keyword != null && keyword.trim().isEmpty()) ? null : keyword;
        List<String> finalCategories = (categories != null && !categories.isEmpty()) ? categories : null;

        // BẢO MẬT HIỂN THỊ (VISIBILITY SECURITY)
        // Nếu không phải ADMIN, và cũng không phải ORGANIZER đang truy cập danh sách
        // của chính mình -> ÉP BUỘC CHỈ HIỂN THỊ "PUBLISHED"
        EventStatus finalStatus = status;
        boolean isAdmin = false;
        boolean isOwner = false;

        // Lấy thông tin user hiện tại
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated() && !auth.getPrincipal().equals("anonymousUser")) {
            String email = auth.getName();
            User user = userRepository.findByEmail(email).orElse(null);
            if (user != null) {
                if (user.getRole() == Role.ADMIN)
                    isAdmin = true;
                if (organizerId != null && user.getId().equals(organizerId))
                    isOwner = true;
            }
        }

        if (!isAdmin && !isOwner) {
            finalStatus = EventStatus.PUBLISHED; // Lọc cứng những sự kiện thuộc về mảng Public
        }

        boolean hasCategories = (finalCategories != null);

        Page<Event> eventPage = eventRepository.searchEvents(finalKeyword, hasCategories, finalCategories, finalStatus,
                organizerId,
                startTime, endTime, minPrice, maxPrice, isFree, pageable);

        List<Long> eventIds = eventPage.getContent().stream()
                .map(Event::getId).collect(Collectors.toList());

        // 1 query duy nhất đếm vé cho tất cả event trong trang — giải quyết N+1 Query
        Map<Long, Long> ticketsSoldMap = eventIds.isEmpty() ? Map.of()
                : registrationRepository.countTicketsSoldGroupByEventId(
                        eventIds,
                        List.of(RegistrationStatus.CONFIRMED, RegistrationStatus.CHECKED_IN,
                                RegistrationStatus.PENDING_PAYMENT))
                        .stream()
                        .collect(Collectors.toMap(
                                row -> (Long) row[0],
                                row -> (Long) row[1]));

        List<EventResponse> responses = eventPage.getContent().stream()
                .map(e -> mapToEventResponse(e, ticketsSoldMap))
                .collect(Collectors.toList());

        return PageResponse.<EventResponse>builder()
                .currentPage(eventPage.getNumber())
                .totalPages(eventPage.getTotalPages())
                .pageSize(eventPage.getSize())
                .totalElements(eventPage.getTotalElements())
                .data(responses)
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public EventResponse getEventById(Long id) {
        // Bước 1: Lấy dữ liệu Sự kiện chính, văng 404 nếu không tồn tại
        Event event = eventRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.EVENT_NOT_FOUND));

        // Bước 2: Map thông tin cơ bản của Sự kiện thành DTO trả về
        EventResponse response = mapToEventResponse(event);

        // Bước 3: Lấy danh sách lịch trình con (Timeline) của Sự kiện
        // - Chỉ thực hiện ở đây (API chi tiết) để tránh N+1 Query khi gọi API danh sách
        // - Kết quả đã được sắp xếp tăng dần theo startTime nhờ tên hàm Repository
        List<EventScheduleResponse> schedules = eventScheduleRepository
                .findByEventIdOrderByStartTimeAsc(id)
                .stream()
                .map(schedule -> EventScheduleResponse.builder()
                        .id(schedule.getId())
                        .eventId(schedule.getEvent().getId())
                        .title(schedule.getTitle())
                        .startTime(schedule.getStartTime())
                        .endTime(schedule.getEndTime())
                        .description(schedule.getDescription())
                        .build())
                .collect(Collectors.toList());

        // Bước 4: Gắn danh sách lịch trình vào Response
        // Frontend sẽ đọc field "schedules" này để render Timeline / Agenda
        response.setSchedules(schedules);

        return response;
    }

    @Override
    @Transactional
    public EventResponse createEvent(EventRequest request) {
        User organizer = getCurrentUser();

        // Validation Thời gian
        if (request.getStartTime().isBefore(LocalDateTime.now())) {
            throw new AppException(ErrorCode.INVALID_TIME_RANGE);
        }
        if (request.getStartTime().isAfter(request.getEndTime())
                || request.getStartTime().isEqual(request.getEndTime())) {
            throw new AppException(ErrorCode.INVALID_TIME_RANGE);
        }

        // Nếu không truyền Registration End Time, mặc định lấy Start Time của sự kiện
        LocalDateTime regEndTime = request.getRegistrationEndTime() != null ? request.getRegistrationEndTime()
                : request.getStartTime();

        // Nếu truyền Registration Start Time, nó phải nhỏ hơn Reg End Time
        // Hoặc Registration End Time phải nhỏ hơn Start Time
        if (request.getRegistrationStartTime() != null && request.getRegistrationStartTime().isAfter(regEndTime)
                || request.getRegistrationEndTime() != null
                        && request.getRegistrationEndTime().isAfter(request.getStartTime())) {
            throw new AppException(ErrorCode.INVALID_REGISTRATION_TIME_RANGE);
        }

        // Upload ảnh bìa nếu có
        String bannerUrl = null;
        if (request.getBannerFile() != null && !request.getBannerFile().isEmpty()) {
            try {
                bannerUrl = cloudinaryService.uploadImage(request.getBannerFile());
            } catch (Exception e) {
                log.error("Lỗi khi tải ảnh bìa lên Cloudinary", e);
                throw new AppException(ErrorCode.UNCATEGORIZED_EXCEPTION);
            }
        }

        Event event = Event.builder()
                .organizer(organizer)
                .title(request.getTitle())
                .category(request.getCategory())
                .shortDescription(request.getShortDescription())
                .description(request.getDescription())
                .startTime(request.getStartTime())
                .endTime(request.getEndTime())
                .registrationStartTime(request.getRegistrationStartTime())
                .registrationEndTime(regEndTime)
                .location(request.getLocation())
                .capacity(request.getCapacity())
                .ticketPrice(request.getTicketPrice())
                .bannerUrl(bannerUrl)
                .status(EventStatus.DRAFT) // Mặc định là Bản Nháp
                .build();

        Event savedEvent = eventRepository.save(event);

        // Đồng bộ lịch trình nếu Organizer có gửi kèm
        List<EventScheduleResponse> schedules = syncSchedules(savedEvent, request.getSchedules());

        EventResponse response = mapToEventResponse(savedEvent);
        response.setSchedules(schedules);
        return response;
    }

    @Override
    @Transactional
    public EventResponse updateEvent(Long eventId, EventRequest request) {
        User organizer = getCurrentUser();
        Event event = eventRepository.findByIdAndOrganizer(eventId, organizer)
                .orElseThrow(() -> new AppException(ErrorCode.EVENT_NOT_FOUND));

        // Validation Thời gian
        if (request.getStartTime().isAfter(request.getEndTime())
                || request.getStartTime().isEqual(request.getEndTime())) {
            throw new AppException(ErrorCode.INVALID_TIME_RANGE);
        }

        LocalDateTime regEndTime = request.getRegistrationEndTime() != null ? request.getRegistrationEndTime()
                : request.getStartTime();

        if (request.getRegistrationStartTime() != null && request.getRegistrationStartTime().isAfter(regEndTime)
                || request.getRegistrationEndTime() != null
                        && request.getRegistrationEndTime().isAfter(request.getStartTime())) {
            throw new AppException(ErrorCode.INVALID_REGISTRATION_TIME_RANGE);
        }

        // Đếm số vé Đã Bán để chặn giảm sức chứa (Capacity)
        long ticketsSold = registrationRepository.countByEventIdAndStatusIn(
                eventId,
                List.of(RegistrationStatus.CONFIRMED, RegistrationStatus.CHECKED_IN,
                        RegistrationStatus.PENDING_PAYMENT));

        if (request.getCapacity() < ticketsSold) {
            throw new AppException(ErrorCode.CAPACITY_CANNOT_BE_LESS_THAN_SOLD);
        }

        // Bịt lỗ hổng Lách Luật PayOS: Đang là sự kiện PUBLISH mà dám bẻ lái từ Free
        // thành Thu Phí
        if (event.getStatus() == EventStatus.PUBLISHED && request.getTicketPrice().compareTo(BigDecimal.ZERO) > 0) {
            boolean hasPayOS = paymentConfigRepository.existsByUserAndIsActiveTrue(organizer);
            if (!hasPayOS) {
                throw new AppException(ErrorCode.MUST_SETUP_PAYMENT_FIRST);
            }
        }

        // Set lại dữ liệu
        event.setTitle(request.getTitle());
        event.setCategory(request.getCategory());
        event.setShortDescription(request.getShortDescription());
        event.setDescription(request.getDescription());
        event.setStartTime(request.getStartTime());
        event.setEndTime(request.getEndTime());
        event.setRegistrationStartTime(request.getRegistrationStartTime());
        event.setRegistrationEndTime(regEndTime);
        event.setLocation(request.getLocation());
        event.setCapacity(request.getCapacity());
        event.setTicketPrice(request.getTicketPrice());

        // Nếu có gửi file ảnh mới thì ghi đè, nếu không giữ banner cũ
        if (request.getBannerFile() != null && !request.getBannerFile().isEmpty()) {
            try {
                String newBannerUrl = cloudinaryService.uploadImage(request.getBannerFile());
                event.setBannerUrl(newBannerUrl);
            } catch (Exception e) {
                log.error("Lỗi khi tải ảnh bìa lên Cloudinary", e);
                throw new AppException(ErrorCode.UNCATEGORIZED_EXCEPTION);
            }
        }

        Event updatedEvent = eventRepository.save(event);

        // Đồng bộ lịch trình nếu Organizer có gửi kèm
        List<EventScheduleResponse> schedules = syncSchedules(updatedEvent, request.getSchedules());

        EventResponse response = mapToEventResponse(updatedEvent);
        response.setSchedules(schedules);
        return response;
    }

    @Override
    @Transactional
    public void changeEventStatus(Long eventId, EventStatus newStatus) {
        User currentUser = getCurrentUser();
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new AppException(ErrorCode.EVENT_NOT_FOUND));

        // Quyền lực Admin: Được quyền can thiệp nếu là trường hợp xấu
        boolean isAdmin = currentUser.getRole() == Role.ADMIN;
        boolean isOwner = event.getOrganizer().getId().equals(currentUser.getId());

        if (!isAdmin && !isOwner) {
            throw new AppException(ErrorCode.ACCESS_DENIED);
        }

        // Logic check nếu bật cấu hình PUBLISHED cho sự kiện Bán Vé
        if (newStatus == EventStatus.PUBLISHED && event.getTicketPrice().compareTo(BigDecimal.ZERO) > 0) {
            boolean hasPayOS = paymentConfigRepository.existsByUserAndIsActiveTrue(event.getOrganizer());
            if (!hasPayOS) {
                throw new AppException(ErrorCode.MUST_SETUP_PAYMENT_FIRST);
            }
        }

        event.setStatus(newStatus);
        eventRepository.save(event);
    }

    @Override
    @Transactional
    public void deleteEvent(Long eventId) {
        User currentUser = getCurrentUser();
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new AppException(ErrorCode.EVENT_NOT_FOUND));

        boolean isAdmin = currentUser.getRole() == Role.ADMIN;
        boolean isOwner = event.getOrganizer().getId().equals(currentUser.getId()); // Kiểm tra xem có phải chủ sở hữu
                                                                                    // không

        if (!isAdmin && !isOwner) {
            throw new AppException(ErrorCode.ACCESS_DENIED);
        }

        // Chặn quyền Xóa Cứng (Hard Delete) nếu đã lỡ bán vé thành công
        long ticketsSold = registrationRepository.countByEventIdAndStatusIn(
                eventId,
                List.of(RegistrationStatus.CONFIRMED, RegistrationStatus.CHECKED_IN));

        if (ticketsSold > 0) {
            // KHÔNG xóa vật lý kể cả là Admin để bảo toàn dữ liệu (Tickets, Payments,
            // Registrations...)
            // Ép sang trạng thái CANCELLED
            event.setStatus(EventStatus.CANCELLED);
            eventRepository.save(event);
            log.warn("Chuyển trạng thái EVENT_ID {} sang CANCELLED vì đã có khách mua vé (Người thực hiện: {})",
                    eventId, currentUser.getId());
        } else {
            // Xóa vật lý khi chưa có ai mua vé thành công
            // 1. Xóa toàn bộ Staff Assignments
            List<EventStaffAssignment> assignments = eventStaffAssignmentRepository
                    .findByEventId(eventId);
            eventStaffAssignmentRepository.deleteAll(assignments);

            // 2. Xóa toàn bộ Registration rác (PENDING_PAYMENT, CANCELLED)
            List<Registration> junkRegistrations = registrationRepository.findByEventId(eventId);
            for (Registration reg : junkRegistrations) {
                ticketRepository.findByRegistrationId(reg.getId()).ifPresent(ticketRepository::delete);
                paymentRepository.findByRegistrationId(reg.getId()).ifPresent(paymentRepository::delete);
                registrationRepository.delete(reg);
            }

            // 3. Xóa các lịch trình con
            eventScheduleRepository.deleteAllByEventId(eventId);

            // 4. Xóa sự kiện
            eventRepository.delete(event);
            log.info("Đã xóa vĩnh viễn EVENT_ID {} ra khỏi hệ thống", eventId);
        }
    }

    @Override
    @Transactional(readOnly = true)
    // Lấy danh sách sự kiện được gán cho nhân viên (staff) theo id
    public PageResponse<EventResponse> getAssignedEventsByStaffId(Long staffId, int page, int size, String sort,
            String search) {
        User currentUser = getCurrentUser();

        boolean isAdmin = currentUser.getRole() == Role.ADMIN;
        boolean isOrganizer = currentUser.getRole() == Role.ORGANIZER;
        boolean isSelf = currentUser.getId().equals(staffId);

        // Staff chỉ xem được lịch của chính mình, Admin và Organizer xem được của bất
        // kỳ ai
        if (!isAdmin && !isOrganizer && !isSelf) {
            throw new AppException(ErrorCode.ACCESS_DENIED);
        }

        Sort.Direction direction = "asc".equalsIgnoreCase(sort) ? Sort.Direction.ASC : Sort.Direction.DESC;
        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, "createdAt"));

        Page<Event> eventPage = eventStaffAssignmentRepository.findEventsByStaffIdWithFilters(staffId, search,
                pageable);

        List<Long> eventIds = eventPage.getContent().stream()
                .map(Event::getId).collect(Collectors.toList());

        // 1 query duy nhất đếm vé cho tất cả event trong trang — giải quyết N+1 Query
        Map<Long, Long> ticketsSoldMap = eventIds.isEmpty() ? Map.of()
                : registrationRepository.countTicketsSoldGroupByEventId(
                        eventIds,
                        List.of(RegistrationStatus.CONFIRMED, RegistrationStatus.CHECKED_IN,
                                RegistrationStatus.PENDING_PAYMENT))
                        .stream()
                        .collect(Collectors.toMap(
                                row -> (Long) row[0],
                                row -> (Long) row[1]));

        List<EventResponse> data = eventPage.getContent().stream()
                .map(e -> mapToEventResponse(e, ticketsSoldMap))
                .collect(Collectors.toList());

        return PageResponse.<EventResponse>builder()
                .currentPage(eventPage.getNumber())
                .totalPages(eventPage.getTotalPages())
                .pageSize(eventPage.getSize())
                .totalElements(eventPage.getTotalElements())
                .data(data)
                .build();
    }
}
