package com.example.backend.service.impl;

// import com.example.backend.dto.request.EventScheduleRequest;
import com.example.backend.dto.response.EventScheduleResponse;
// import com.example.backend.entity.Event;
import com.example.backend.entity.EventSchedule;
// import com.example.backend.entity.User;
import com.example.backend.exception.AppException;
import com.example.backend.exception.ErrorCode;
import com.example.backend.repository.EventRepository;
import com.example.backend.repository.EventScheduleRepository;
// import com.example.backend.repository.UserRepository;
import com.example.backend.service.EventScheduleService;
import lombok.RequiredArgsConstructor;
// import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
// import org.springframework.transaction.annotation.Transactional;

// import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class EventScheduleServiceImpl implements EventScheduleService {

    private final EventScheduleRepository eventScheduleRepository;
    private final EventRepository eventRepository;
    // private final UserRepository userRepository;

    // private User getCurrentUser() {
    // String email =
    // SecurityContextHolder.getContext().getAuthentication().getName();
    // return userRepository.findByEmail(email)
    // .orElseThrow(() -> new AppException(ErrorCode.UNAUTHENTICATED));
    // }

    // Tiện ích để Mapping Entity sang DTO
    private EventScheduleResponse mapToResponse(EventSchedule schedule) {
        return EventScheduleResponse.builder()
                .id(schedule.getId())
                .eventId(schedule.getEvent().getId())
                .title(schedule.getTitle())
                .startTime(schedule.getStartTime())
                .endTime(schedule.getEndTime())
                .description(schedule.getDescription())
                .build();
    }

    // Hàm chốt chặn trung tâm: Kiểm tra thời gian Lịch trình tuyệt đối không được
    // chọc thủng khung thời gian của Sự Kiện mẹ
    // private void validateScheduleTimeBounds(Event event, EventScheduleRequest
    // request) {
    // if (request.getStartTime().isBefore(event.getStartTime()) ||
    // request.getEndTime().isAfter(event.getEndTime())) {
    // throw new AppException(ErrorCode.TIME_OUT_OF_EVENT_BOUNDS);
    // }
    // if (request.getStartTime().isAfter(request.getEndTime())
    // || request.getStartTime().isEqual(request.getEndTime())) {
    // throw new AppException(ErrorCode.INVALID_TIME_RANGE);
    // }
    // }

    // Lấy Event và đảm bảo Organizer hiện tại là chủ nhân đích thực
    // private Event getEventAndVerifyOwner(Long eventId) {
    // User organizer = getCurrentUser();
    // return eventRepository.findByIdAndOrganizer(eventId, organizer)
    // .orElseThrow(() -> new AppException(ErrorCode.EVENT_NOT_FOUND)); // Văng lỗi
    // nếu Event không tồn tại
    // // hoặc Cố tình trỏ sang Event của
    // // người khác
    // }

    @Override
    public List<EventScheduleResponse> getSchedulesByEventId(Long eventId) {
        // Hàm Public, ai cũng xem được, miễn Event có tồn tại
        if (!eventRepository.existsById(eventId)) {
            throw new AppException(ErrorCode.EVENT_NOT_FOUND);
        }

        return eventScheduleRepository.findByEventIdOrderByStartTimeAsc(eventId)
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    // @Override
    // @Transactional
    // public List<EventScheduleResponse> createSchedules(Long eventId,
    // List<EventScheduleRequest> requests) {
    // Event event = getEventAndVerifyOwner(eventId); // Khép góc bảo mật: Phải đúng
    // chủ Event

    // List<EventSchedule> schedules = requests.stream().map(request -> {
    // validateScheduleTimeBounds(event, request); // Bóp chặt khung thời gian từng
    // cái một
    // return EventSchedule.builder()
    // .event(event)
    // .title(request.getTitle())
    // .startTime(request.getStartTime())
    // .endTime(request.getEndTime())
    // .description(request.getDescription())
    // .build();
    // }).collect(Collectors.toList());

    // List<EventSchedule> savedSchedules =
    // eventScheduleRepository.saveAll(schedules);

    // return savedSchedules.stream()
    // .map(this::mapToResponse)
    // .collect(Collectors.toList());
    // }

    // @Override
    // @Transactional
    // public List<EventScheduleResponse> updateSchedules(Long eventId,
    // List<EventScheduleRequest> requests) {
    // Event event = getEventAndVerifyOwner(eventId); // Khép góc bảo mật

    // // Lấy tất cả lịch trình hiện tại của event từ DB
    // List<EventSchedule> currentSchedules =
    // eventScheduleRepository.findByEventIdOrderByStartTimeAsc(eventId);

    // // Tạo tập hợp các ID được truyền lên từ Frontend
    // java.util.Set<Long> requestIds = requests.stream()
    // .map(EventScheduleRequest::getId)
    // .filter(id -> id != null)
    // .collect(Collectors.toSet());

    // // 1. XÓA: Lịch trình nào có trong DB nhưng không có trong requestIds (không
    // được truyền lên) thì tiến hành xóa
    // List<EventSchedule> schedulesToDelete = currentSchedules.stream()
    // .filter(schedule -> !requestIds.contains(schedule.getId()))
    // .collect(Collectors.toList());
    // eventScheduleRepository.deleteAll(schedulesToDelete);

    // List<EventScheduleResponse> results = new ArrayList<>();

    // // 2. THÊM MỚI hoặc CẬP NHẬT
    // for (EventScheduleRequest request : requests) {
    // validateScheduleTimeBounds(event, request);

    // if (request.getId() == null) {
    // // THÊM MỚI bổ sung
    // EventSchedule newSchedule = EventSchedule.builder()
    // .event(event)
    // .title(request.getTitle())
    // .startTime(request.getStartTime())
    // .endTime(request.getEndTime())
    // .description(request.getDescription())
    // .build();
    // results.add(mapToResponse(eventScheduleRepository.save(newSchedule)));
    // } else {
    // // CẬP NHẬT lịch trình đã có
    // EventSchedule existingSchedule = currentSchedules.stream()
    // .filter(s -> s.getId().equals(request.getId()))
    // .findFirst()
    // .orElseThrow(() -> new AppException(ErrorCode.SCHEDULE_NOT_FOUND));

    // existingSchedule.setTitle(request.getTitle());
    // existingSchedule.setStartTime(request.getStartTime());
    // existingSchedule.setEndTime(request.getEndTime());
    // existingSchedule.setDescription(request.getDescription());

    // results.add(mapToResponse(eventScheduleRepository.save(existingSchedule)));
    // }
    // }

    // return results;
    // }

    // @Override
    // @Transactional
    // public void deleteAllSchedules(Long eventId) {
    // getEventAndVerifyOwner(eventId); // Khép góc bảo mật: Phải là chủ sự kiện mới
    // được xóa
    // eventScheduleRepository.deleteAllByEventId(eventId);
    // }

    // @Override
    // @Transactional
    // public void deleteSchedule(Long eventId, Long scheduleId) {
    // getEventAndVerifyOwner(eventId); // Bảo mật: Xác nhận Event này thuộc về
    // Organizer hiện tại mới được xóa lịch
    // // trình
    // EventSchedule schedule =
    // eventScheduleRepository.findByIdAndEventId(scheduleId, eventId)
    // .orElseThrow(() -> new AppException(ErrorCode.SCHEDULE_NOT_FOUND));
    // eventScheduleRepository.delete(schedule);
    // }
}
