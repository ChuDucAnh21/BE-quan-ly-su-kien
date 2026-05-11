package com.example.backend.security;

import com.example.backend.entity.User;
import com.example.backend.exception.AppException;
import com.example.backend.exception.ErrorCode;
import com.example.backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {
    // UserDetailsService: Lớp dùng để định nghĩa cách Spring Security tìm kiếm
    // thông tin người dùng

    private final UserRepository userRepository; // Giao tiếp trực tiếp với Database (bảng users)

    // Phương thức bắt buộc phải ghi đè để Spring Security nạp được User vào hệ
    // thống
    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        // Tìm User trong DB dựa trên Email được gửi lên
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND)); // Ném lỗi nếu không thấy

        // Trả về một đối tượng CustomUserDetails (Bọc đối tượng User của chúng ta lại
        // cho đúng chuẩn Spring Security)
        return new CustomUserDetails(user);
    }
}
