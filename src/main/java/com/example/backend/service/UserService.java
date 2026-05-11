package com.example.backend.service;

import com.example.backend.dto.request.ChangePasswordRequest;
import com.example.backend.dto.request.UpdateProfileRequest;
import com.example.backend.dto.response.UserProfileResponse;
import com.example.backend.entity.User;
import com.example.backend.enums.UserStatus;
import com.example.backend.exception.AppException;
import com.example.backend.exception.ErrorCode;
import com.example.backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserService {
    // Lớp xử lý logic liên quan đến thông tin cá nhân của người dùng

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    // 1. Lấy thông tin hồ sơ của tôi
    public UserProfileResponse getMyProfile(String email) {
        // Tìm User dựa trên Email được trích xuất từ Token
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

        // Chuyển đổi từ Entity sang DTO để trả về cho Frontend
        return buildProfileResponse(user);
    }

    // 2. Cập nhật thông tin cá nhân (Họ tên, SĐT, Giới tính, Ngày sinh, Địa chỉ)
    @Transactional // Transactional nghĩa là nếu có lỗi xảy ra thì sẽ rollback lại
    public UserProfileResponse updateProfile(String email, UpdateProfileRequest request) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

        // Cập nhật các trường thông tin cơ bản
        user.setFullName(request.getFullName());
        user.setPhone(request.getPhone());

        // Cập nhật các trường tùy chọn (chỉ cập nhật nếu Frontend có gửi dữ liệu lên)
        if (request.getGender() != null) {
            user.setGender(request.getGender());
        }
        if (request.getDateOfBirth() != null) {
            user.setDateOfBirth(request.getDateOfBirth());
        }
        if (request.getAddress() != null) {
            user.setAddress(request.getAddress());
        }

        // Lưu thay đổi vào DB và trả về kết quả sau khi cập nhật
        return buildProfileResponse(userRepository.save(user));
    }

    // 3. Cập nhật ảnh đại diện (Lưu URL ảnh nhận được từ Cloudinary)
    @Transactional
    public UserProfileResponse updateAvatar(String email, String avatarUrl) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

        user.setAvatarUrl(avatarUrl);
        return buildProfileResponse(userRepository.save(user));
    }

    // 4. Đổi mật khẩu (Yêu cầu mật khẩu cũ để xác thực)
    @Transactional
    public void changePassword(String email, ChangePasswordRequest request) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

        // Kiểm tra mật khẩu cũ người dùng nhập vào có khớp với mật khẩu trong DB không
        if (!passwordEncoder.matches(request.getOldPassword(), user.getPassword())) {
            throw new AppException(ErrorCode.INVALID_PASSWORD);
        }

        // Mã hóa mật khẩu mới trước khi lưu
        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);
    }

    // 5. Xử lý "Xóa tài khoản" (Dùng cơ chế Soft Delete - chỉ khóa lại chứ không
    // xóa hẳn khỏi DB)
    @Transactional
    public void softDeleteMyAccount(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

        // Chuyển trạng thái sang LOCKED (Người dùng sẽ không thể login được nữa)
        user.setStatus(UserStatus.LOCKED);
        userRepository.save(user);
    }

    // Hàm bổ trợ: Chuyển đổi dữ liệu từ thực thể User sang UserProfileResponse
    // (DTO)
    private UserProfileResponse buildProfileResponse(User user) {
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
