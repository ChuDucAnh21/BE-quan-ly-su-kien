package com.example.backend.service.impl;

import com.example.backend.dto.request.EventRequest;
import com.example.backend.dto.response.EventResponse;
import com.example.backend.dto.response.EventScheduleResponse;
import com.example.backend.dto.response.PageResponse;
import com.example.backend.entity.Event;
import com.example.backend.entity.User;
import com.example.backend.enums.EventStatus;
import com.example.backend.enums.RegistrationStatus;
import com.example.backend.enums.Role;
import com.example.backend.exception.AppException;
import com.example.backend.exception.ErrorCode;
import com.example.backend.repository.EventRepository;
import com.example.backend.repository.EventScheduleRepository;
import com.example.backend.repository.PaymentConfigRepository;
import com.example.backend.repository.RegistrationRepository;
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
import java.util.List;
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
    private final com.example.backend.repository.EventStaffAssignmentRepository eventStaffAssignmentRepository;

    // Helper: Lấy User đang đăng nhập dựa trên email trong Token
    private User getCurrentUser() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));
    }

    // Helper: Chuyển đổi Entity sang DTO
    private EventResponse mapToEventResponse(Event event) {
        // Tính số lượng vé đã bán
        long ticketsSold = registrationRepository.countByEventIdAndStatusIn(
                event.getId(),
                List.of(RegistrationStatus.CONFIRMED, RegistrationStatus.CHECKED_IN));

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

    @Override
    public PageResponse<EventResponse> getAllEvents(int page, int size, String keyword, String category,
            EventStatus status, Long organizerId, java.time.LocalDateTime startTime, java.time.LocalDateTime endTime,
            java.math.BigDecimal minPrice, java.math.BigDecimal maxPrice, Boolean isFree) {
        // Tự động sắp xếp mới nhất lên đầu
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));

        // Chuyển rỗng thành null để Query JPQL lờ đi
        String finalKeyword = (keyword != null && keyword.trim().isEmpty()) ? null : keyword;
        String finalCategory = (category != null && category.trim().isEmpty()) ? null : category;

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

        Page<Event> eventPage = eventRepository.searchEvents(finalKeyword, finalCategory, finalStatus, organizerId,
                startTime, endTime, minPrice, maxPrice, isFree, pageable);

        List<EventResponse> responses = eventPage.getContent().stream()
                .map(this::mapToEventResponse)
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
        return mapToEventResponse(savedEvent);
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
        return mapToEventResponse(updatedEvent);
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
            // Không xóa vật lý, ép sang trạng thái Hủy Bỏ
            event.setStatus(EventStatus.CANCELLED);
            eventRepository.save(event);
            log.warn("Chuyển trạng thái EVENT_ID {} sang CANCELLED vì đã có khách mua vé", eventId);
        } else {
            // Xóa vật lý
            eventRepository.delete(event);
            log.info("Đã xóa vĩnh viễn EVENT_ID {} ra khỏi hệ thống", eventId);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<EventResponse> getAssignedEventsByStaffId(Long staffId, int page, int size) {
        User currentUser = getCurrentUser();
        
        boolean isAdmin = currentUser.getRole() == Role.ADMIN;
        boolean isOrganizer = currentUser.getRole() == Role.ORGANIZER;
        boolean isSelf = currentUser.getId().equals(staffId);
        
        // Staff chỉ xem được lịch của chính mình, Admin và Organizer xem được của bất kỳ ai
        if (!isAdmin && !isOrganizer && !isSelf) {
            throw new AppException(ErrorCode.ACCESS_DENIED);
        }

        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<Event> eventPage = eventStaffAssignmentRepository.findEventsByStaffId(staffId, pageable);
        
        List<EventResponse> data = eventPage.getContent().stream()
                .map(this::mapToEventResponse)
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
