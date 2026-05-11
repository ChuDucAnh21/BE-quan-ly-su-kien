package com.example.backend.security;

import com.example.backend.entity.User;
import com.example.backend.enums.UserStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.Collections;

@Data
@AllArgsConstructor

public class CustomUserDetails implements UserDetails {
    // UserDetails: Interface của Spring Security dùng để cung cấp thông tin người
    // dùng cho hệ thống bảo mật

    private final User user; // Đối tượng User gốc từ Database của chúng ta

    // 1. Phân quyền (Authorities): Chuyển đổi vai trò (Role) của User trong DB
    // thành quyền hạn của Spring Security
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        // Ví dụ: Role USER của chúng ta sẽ thành ROLE_USER trong Spring Security
        return Collections.singleton(new SimpleGrantedAuthority("ROLE_" + user.getRole().name()));
    }

    // 2. Trả về mật khẩu đã được mã hóa
    @Override
    public String getPassword() {
        return user.getPassword();
    }

    // 3. Trả về tên đăng nhập (Dự án này dùng Email làm tên đăng nhập)
    @Override
    public String getUsername() {
        return user.getEmail();
    }

    // 4. Các phương thức kiểm tra trạng thái tài khoản
    @Override
    public boolean isAccountNonExpired() {
        return true; // Tài khoản chưa hết hạn
    }

    @Override
    public boolean isAccountNonLocked() {
        // Tài khoản không bị khóa nếu trạng thái là ACTIVE
        return user.getStatus() == UserStatus.ACTIVE;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true; // Mật khẩu chưa hết hạn
    }

    @Override
    public boolean isEnabled() {
        // Tài khoản được phép hoạt động nếu trạng thái là ACTIVE
        return user.getStatus() == UserStatus.ACTIVE;
    }
}
