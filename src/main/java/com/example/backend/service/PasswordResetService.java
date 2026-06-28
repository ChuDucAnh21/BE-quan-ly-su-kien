package com.example.backend.service;

import com.example.backend.dto.request.ForgotPasswordRequest;
import com.example.backend.dto.request.ResetPasswordRequest;
import com.example.backend.entity.PasswordResetToken;
import com.example.backend.entity.User;
import com.example.backend.exception.AppException;
import com.example.backend.exception.ErrorCode;
import com.example.backend.repository.PasswordResetTokenRepository;
import com.example.backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class PasswordResetService {
    // Lớp xử lý nghiệp vụ liên quan đến tính năng Quên mật khẩu

    private final UserRepository userRepository;
    private final PasswordResetTokenRepository tokenRepository;
    private final MailService mailService; // Dùng để gửi email chứa mã khôi phục cho người dùng
    private final PasswordEncoder passwordEncoder; // Dùng để mã hóa mật khẩu mới của người dùng

    private static final int EXPIRY_MINUTES = 5; // Quy định mã khôi phục chỉ có hiệu lực trong 5 phút

    // BƯỚC 1: Xử lý yêu cầu Quên mật khẩu (Sinh mã và gửi Email)
    @Transactional
    public int processForgotPassword(ForgotPasswordRequest request) {
        // 1. Kiểm tra Email người dùng cung cấp có tồn tại trong hệ thống không
        // nêu email k tồn tại không trả lỗi, chỉ dừng lại
        User user = userRepository.findByEmail(request.getEmail())
                .orElse(null);
        if (user == null) {
            return EXPIRY_MINUTES;
        }

        // 2. Xóa tất cả các mã khôi phục cũ hòng tránh lỗi hoặc rác dữ liệu cho User
        // này
        tokenRepository.deleteAllByUserId(user.getId());

        // 3. Tạo một chuỗi mã ngẫu nhiên (UUID) duy nhất
        String token = UUID.randomUUID().toString();

        // 4. Lưu mã này vào bảng `password_reset_tokens` cùng thời gian hết hạn
        PasswordResetToken resetToken = PasswordResetToken.builder()
                .token(token)
                .user(user)
                .expiryDate(LocalDateTime.now().plusMinutes(EXPIRY_MINUTES)) // +5 phút so với thời điểm hiện tại
                .build();

        tokenRepository.save(resetToken);

        // 5. Gửi Email chứa mã khôi phục tới hòm thư người dùng
        try {
            mailService.sendPasswordResetEmail(user.getEmail(), token);
            return EXPIRY_MINUTES;
        } catch (Exception e) {
            // Ghi nhận lỗi gửi mail vào log server nhưng không làm sập tiến trình DB
            log.error("Lỗi gửi mail tới {}: {}", user.getEmail(), e.getMessage());
            return 0;
        }
    }

    // BƯỚC 2: Xử lý việc Đặt lại mật khẩu mới (Người dùng nhập mã và mật khẩu mới)
    @Transactional
    public void processResetPassword(ResetPasswordRequest request) {
        // 1. Tìm mã khôi phục mà người dùng vừa gửi lên trong Database
        PasswordResetToken resetToken = tokenRepository.findByToken(request.getToken())
                .orElseThrow(() -> new AppException(ErrorCode.INVALID_RESET_TOKEN));

        // 2. Kiểm tra mã đã hết hạn chưa (Quá 15 phút chưa)
        if (resetToken.isExpired()) {
            tokenRepository.delete(resetToken); // Hiệu lực mã đã hết, xóa luôn bản ghi này trong DB
            throw new AppException(ErrorCode.RESET_TOKEN_EXPIRED);
        }

        // 3. Nếu mọi thứ hợp lệ, tiến hành cập nhật mật khẩu mới cho User
        User user = resetToken.getUser();
        user.setPassword(passwordEncoder.encode(request.getNewPassword())); // Nhớ mã hóa mật khẩu trước khi lưu! bằng
                                                                            // thuật toán băm
        userRepository.save(user);

        // 4. Xóa mã này sau khi đã sử dụng xong (Mã chỉ dùng 1 lần duy nhất)
        tokenRepository.delete(resetToken);
    }
}
