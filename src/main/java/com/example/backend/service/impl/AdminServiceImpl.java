package com.example.backend.service.impl;

import com.example.backend.dto.response.PageResponse;
import com.example.backend.dto.response.UserProfileResponse;
import com.example.backend.entity.User;
import com.example.backend.enums.Role;
import com.example.backend.enums.UserStatus;
import com.example.backend.exception.AppException;
import com.example.backend.exception.ErrorCode;
import com.example.backend.repository.UserRepository;
import com.example.backend.service.AdminService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;
import org.springframework.dao.DataIntegrityViolationException;

@Service
@RequiredArgsConstructor
public class AdminServiceImpl implements AdminService {

    private final UserRepository userRepository;

    @Override
    @Transactional
    public void changeUserRole(Long userId, Role newRole) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

        // 1. Kiểm tra không cho phép Admin thay đổi quyền của chính mình (đề phòng bất
        // cẩn tự giáng cấp)
        String currentAdminEmail = SecurityContextHolder.getContext().getAuthentication().getName(); //
        if (user.getEmail().equals(currentAdminEmail)) {
            // Có thể tạo ErrorCode mới, tạm thời dùng BadRequest chung hoặc định nghĩa mới
            // (tính năng mở rộng)
            throw new AppException(ErrorCode.CANNOT_MODIFY_YOURSELF);
        }

        user.setRole(newRole);
        userRepository.save(user);
    }

    @Override
    @Transactional
    public void changeUserStatus(Long userId, UserStatus newStatus) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

        // 1. Kiểm tra không cho phép Admin tự khóa chính mình
        String currentAdminEmail = SecurityContextHolder.getContext().getAuthentication().getName();
        if (user.getEmail().equals(currentAdminEmail)) {
            throw new AppException(ErrorCode.CANNOT_MODIFY_YOURSELF);
        }

        user.setStatus(newStatus);
        userRepository.save(user);
    }

    @Override
    public PageResponse<UserProfileResponse> getAllUsers(int page, int size, String keyword, Role role,
            UserStatus status, String sort) {

        /**
         * Chuyển đổi tham số sort ("asc" hoặc "desc") thành chiều sắp xếp.
         * Mặc định sắp xếp theo trường "createdAt".
         */
        Sort.Direction direction = "asc".equalsIgnoreCase(sort) ? Sort.Direction.ASC : Sort.Direction.DESC;
        String sortProperty = "createdAt";

        /**
         * PageRequest.of(): Một tính năng tuyệt vời của Spring Data.
         * Nó đóng gói toàn bộ logic Toán học (Limit, Offset, Order By) để ném vào câu
         * lệnh SQL một cách an toàn.
         */
        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortProperty));

        // Lọc keyword rỗng chuyển thành null để Query JPQL hiểu chính xác tham số IS
        // NULL
        String finalKeyword = (keyword != null && keyword.trim().isEmpty()) ? null : keyword;

        Page<User> usersPage = userRepository.searchUsers(finalKeyword, role, status, pageable);

        // Chuyển đổi từ Entity User sang DTO UserProfileResponse
        List<UserProfileResponse> userResponses = usersPage.getContent().stream()
                .map(this::mapToUserProfileResponse)
                .collect(Collectors.toList());

        return PageResponse.<UserProfileResponse>builder()
                .currentPage(usersPage.getNumber())
                .totalPages(usersPage.getTotalPages())
                .pageSize(usersPage.getSize())
                .totalElements(usersPage.getTotalElements())
                .data(userResponses)
                .build();
    }

    // 6. Xóa tài khoản vĩnh viễn dành cho Super Admin (Cơ chế Hybrid)
    // KHÔNG DÙNG @Transactional ở đây để mỗi thao tác DB chạy một luồng độc lập,
    // tránh việc Spring khóa (rollback-only) toàn bộ giao dịch khi gặp lỗi Khóa
    // ngoại.
    @Override
    public void deleteAccountUser(Long idUser) {
        // Kiểm tra xem User có tồn tại không trước
        userRepository.findById(idUser).orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

        try {
            // BƯỚC 1: Thử xóa cứng (Hard Delete) để dọn rác DB
            // Hàm deleteById tự có Transaction riêng. Nếu dính khóa ngoại, chỉ Transaction
            // của nó bị hủy!
            userRepository.deleteById(idUser);
        } catch (DataIntegrityViolationException e) {
            // BƯỚC 2: Nếu văng lỗi khóa ngoại -> User này ĐÃ CÓ DỮ LIỆU (Vé, Sự kiện, Phân
            // công)
            // Phải fetch lại User mới từ DB để Hibernate không bị lỗi "deleted instance
            // passed to merge"
            User userToUpdate = userRepository.findById(idUser).get();

            // Áp dụng cơ chế Ẩn danh hóa (Anonymization) + Xóa mềm (Soft Delete)
            userToUpdate.setEmail("deleted_" + userToUpdate.getId() + "@domain.local");
            userToUpdate.setFullName("Tài khoản đã xóa");
            userToUpdate.setPhone("");
            userToUpdate.setAddress("");
            userToUpdate.setAvatarUrl("");
            userToUpdate.setPassword(""); // Xóa password
            userToUpdate.setProviderId(null);
            userToUpdate.setStatus(UserStatus.LOCKED); // Khóa vĩnh viễn

            userRepository.save(userToUpdate); // Lưu đè lại thông tin đã bị làm sạch
        }
    }

    // Hàm chuyển đổi Model -> DTO (Tái sử dụng logic hiển thị)
    private UserProfileResponse mapToUserProfileResponse(User user) {

        return UserProfileResponse.builder()
                .id(user.getId())
                .email(user.getEmail())
                .fullName(user.getFullName())
                .phone(user.getPhone())
                .avatarUrl(user.getAvatarUrl())
                .role(user.getRole().name())
                .status(user.getStatus().name())
                .gender(user.getGender() != null ? user.getGender().name() : null)
                .dateOfBirth(user.getDateOfBirth())
                .address(user.getAddress())
                .createdAt(user.getCreatedAt())
                .build();
    }

}
