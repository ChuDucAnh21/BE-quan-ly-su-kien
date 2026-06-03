package com.example.backend.service.impl;

import com.example.backend.dto.response.StaffResponse;
import com.example.backend.entity.Event;
import com.example.backend.entity.EventStaffAssignment;
import com.example.backend.entity.User;
import com.example.backend.enums.Role;
import com.example.backend.enums.UserStatus;
import com.example.backend.exception.AppException;
import com.example.backend.exception.ErrorCode;
import com.example.backend.repository.EventRepository;
import com.example.backend.repository.EventStaffAssignmentRepository;
import com.example.backend.repository.UserRepository;
import com.example.backend.service.EventStaffService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class EventStaffServiceImpl implements EventStaffService {

    private final EventStaffAssignmentRepository eventStaffAssignmentRepository;
    private final EventRepository eventRepository;
    private final UserRepository userRepository;

    // Lấy thông tin user hiện tại từ token đang đăng nhập (Thường là Staff hoặc
    // Organizer)
    private User getCurrentUser() {
        // lấy email từ token được lưu trong SecurityContextHolder
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new AppException(ErrorCode.UNAUTHENTICATED));
    }

    // Hàm phụ trợ map User sang StaffResponse DTO
    private StaffResponse mapToStaffResponse(User user) {
        return StaffResponse.builder()
                .id(user.getId())
                .fullName(user.getFullName())
                .email(user.getEmail())
                .phone(user.getPhone())
                .isActive(user.getStatus() == UserStatus.ACTIVE)
                .avatarUrl(user.getAvatarUrl())
                .build();
    }

    // Xác nhận chủ sự kiện
    private Event getEventAndVerifyOwner(Long eventId) {
        User organizer = getCurrentUser();
        // Kiểm tra Event tồn tại và người đang thao tác phải là người tạo sự kiện
        // (organizer_id)
        return eventRepository.findByIdAndOrganizer(eventId, organizer)
                .orElseThrow(() -> new AppException(ErrorCode.EVENT_NOT_FOUND));
    }

    @Override
    public List<StaffResponse> getAllAvailableStaffs() {
        // Tìm toàn bộ User trong DB có Role = STAFF và Status = ACTIVE
        List<User> staffs = userRepository.findByRoleAndStatus(Role.STAFF, UserStatus.ACTIVE);
        return staffs.stream()
                .map(this::mapToStaffResponse)
                .collect(Collectors.toList());
    }

    // Lấy danh sách nhân viên đã được phân công vào sự kiện
    @Override
    public com.example.backend.dto.response.PageResponse<StaffResponse> getStaffsByEventId(Long eventId, String search,
            com.example.backend.enums.UserStatus status, org.springframework.data.domain.Pageable pageable) {
        if (!eventRepository.existsById(eventId)) {
            throw new AppException(ErrorCode.EVENT_NOT_FOUND);
        }

        // --- BẢO MẬT PHÂN QUYỀN TRUY CẬP (Theo yêu cầu: ADMIN, ORGANIZER và Staff
        // thuộc Event) ---
        User currentUser = getCurrentUser();
        Role role = currentUser.getRole();

        // Cản bước USER thường
        if (role == Role.USER) {
            throw new AppException(ErrorCode.ACCESS_DENIED);
        }
        // Nếu là STAFF thì phải chắc chắn nằm trong danh sách phân công của Event này
        if (role == Role.STAFF) {
            boolean isAssignedToThisEvent = eventStaffAssignmentRepository.existsByEventIdAndStaff_Email(eventId,
                    currentUser.getEmail());

            if (!isAssignedToThisEvent) {
                // Trả về lỗi không có quyền truy cập
                throw new AppException(ErrorCode.ACCESS_DENIED);
            }
        }
        // Admin và Organizer được xem thoải mái

        org.springframework.data.domain.Page<EventStaffAssignment> assignmentPage = eventStaffAssignmentRepository
                .findStaffsByEventIdWithFilters(eventId, search, status, pageable);

        org.springframework.data.domain.Page<StaffResponse> staffPage = assignmentPage
                .map(assignment -> mapToStaffResponse(assignment.getStaff()));

        return com.example.backend.dto.response.PageResponse.<StaffResponse>builder()
                .currentPage(staffPage.getNumber())
                .totalPages(staffPage.getTotalPages())
                .pageSize(staffPage.getSize())
                .totalElements(staffPage.getTotalElements())
                .data(staffPage.getContent())
                .build();
    }

    @Override
    @Transactional
    /*
     * @Transactional: Từ khóa vô cùng quan trọng để đảm bảo "Tính Toàn Vẹn"
     * (A-C-I-D) của thao tác.
     * Ở đây chúng ta vừa XÓA (delete) vừa THÊM MỚI (insert) dữ liệu vào bảng
     * EventStaffAssignment.
     * Nếu trong quá trình thực thi bị đứt mạng hoặc gặp lỗi Runtime, toàn bộ thao
     * tác Xóa/Thêm
     * sẽ bị rớt lại (Rollback) như thời điểm ban đầu, tránh việc dữ liệu bị rỗng
     * hoặc mất mát một nửa.
     */
    // cơ chế thêm mới và xóa bớt nhân viên khỏi sự kiện
    public List<StaffResponse> syncEventStaffs(Long eventId, Set<Long> staffIds) {
        Event event = getEventAndVerifyOwner(eventId);

        // Lấy danh sách nhân viên hiện tại đã được phân công vào sự kiện
        List<EventStaffAssignment> currentAssignments = eventStaffAssignmentRepository.findByEventId(eventId);
        Set<Long> currentStaffIds = currentAssignments.stream()
                .map(a -> a.getStaff().getId())
                .collect(Collectors.toSet());

        // 1. Phân loại nhóm CẦN XOÁ (Trong DB có mà Request không gửi lên)
        // Sử dụng filter của Java 8 Stream để sàng lọc mảng cực nhanh và không tốn
        // memory do không
        // phải clone ra mảng list mới một cách thủ công qua for-loop.
        List<EventStaffAssignment> toRemoveStaff = currentAssignments.stream()
                .filter(a -> !staffIds.contains(a.getStaff().getId()))
                .collect(Collectors.toList());
        eventStaffAssignmentRepository.deleteAll(toRemoveStaff);

        // 2. Phân loại nhóm CẦN THÊM (Request gửi tới mà DB chưa lưu)
        Set<Long> toAddIds = staffIds.stream()
                .filter(id -> !currentStaffIds.contains(id))
                .collect(Collectors.toSet());

        if (!toAddIds.isEmpty()) {
            List<User> usersToAdd = userRepository.findAllById(toAddIds);

            // Kiểm tra bảo mật: Nếu truyền ID bậy/ảo thì size sẽ không khớp
            if (usersToAdd.size() != toAddIds.size()) {
                throw new AppException(ErrorCode.USER_NOT_FOUND);
            }

            // Kiểm tra bảo mật: Những account đó có đúng là ROLE.STAFF và đang ACTIVE hay
            // không
            boolean hasInvalidStaff = usersToAdd.stream()
                    .anyMatch(u -> u.getRole() != Role.STAFF || u.getStatus() != UserStatus.ACTIVE);

            if (hasInvalidStaff) {
                throw new AppException(ErrorCode.INVALID_STAFF_ROLE);
            }

            // Map qua Entity để lưu
            User assigner = getCurrentUser();
            List<EventStaffAssignment> newAssignments = usersToAdd.stream()
                    .map(staff -> EventStaffAssignment.builder()
                            .event(event)
                            .staff(staff)
                            .assignedBy(assigner)
                            .build())
                    .collect(Collectors.toList());

            eventStaffAssignmentRepository.saveAll(newAssignments);
        }

        // Trả về danh sách Staff sau khi đã Sync
        return eventStaffAssignmentRepository.findByEventId(eventId).stream()
                .map(assignment -> mapToStaffResponse(assignment.getStaff()))
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void removeStaffFromEvent(Long eventId, Long staffId) {
        getEventAndVerifyOwner(eventId);

        EventStaffAssignment assignment = eventStaffAssignmentRepository.findByEventIdAndStaffId(eventId, staffId)
                .orElseThrow(() -> new AppException(ErrorCode.STAFF_NOT_ASSIGNED));

        eventStaffAssignmentRepository.delete(assignment);
    }
}
