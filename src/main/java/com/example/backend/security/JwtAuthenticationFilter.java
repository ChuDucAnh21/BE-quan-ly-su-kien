package com.example.backend.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Arrays;

@Component // Biến lớp này thành một Bean để Spring Security có thể sử dụng tiêm vào
           // các nơi khác
@RequiredArgsConstructor
@Slf4j // Để in log lỗi
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    // OncePerRequestFilter: Đảm bảo bộ lọc này chỉ chạy đúng 1 lần cho mỗi Request
    // gửi lên

    private final JwtTokenProvider tokenProvider; // Công cụ giải mã và kiểm tra Token
    private final CustomUserDetailsService customUserDetailsService; // Công cụ lấy thông tin User từ DB

    @Override
    // dùng để kiểm tra token mỗi khi request được gửi lên (hàm này chạy trước khi
    // request đến controller, và nó chạy tự động chứ không cần gọi do kế thừa
    // OncePerRequestFilter)
    //
    protected void doFilterInternal(HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain) throws ServletException, IOException {
        try {
            // 1. Trích xuất mã JWT từ Request (Header hoặc Cookie)
            String jwt = getJwtFromRequest(request);

            // 2. Nếu có Token và Token đó hợp lệ (chưa hết hạn, đúng chữ ký)
            if (StringUtils.hasText(jwt) && tokenProvider.validateJwtToken(jwt)) {

                // 3. Lấy Email từ trong Token
                String email = tokenProvider.getEmailFromJwtToken(jwt);

                // 4. Tìm kiếm thông tin User dạng CustomUserDetails mà Spring Security cần
                // trong DB dựa trên Email đó
                UserDetails userDetails = customUserDetailsService.loadUserByUsername(email);

                // 5. Kiểm tra xem tài khoản có đang bị khóa hay không
                if (!userDetails.isAccountNonLocked()) {
                    log.warn("Cảnh báo bảo mật: Tài khoản {} đang bị khóa nhưng vẫn phát sinh giao dịch", email);
                    filterChain.doFilter(request, response);
                    return;
                }

                // 6. Nếu User tồn tại và đang ACTIVE, tạo đối tượng xác thực (Authentication)
                // userDetails.getAuthorities() : lấy quyền hạn (ROLE_ADMIN/ROLE_USER/...)
                UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                        userDetails, null, userDetails.getAuthorities());

                // Gắn thêm chi tiết về Request (như địa chỉ IP) vào đối tượng xác thực
                authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                // 7. QUAN TRỌNG: Lưu thông tin xác thực vào SecurityContextHolder
                // Kể từ dòng này, Spring Security sẽ coi như User này đã được xác thực và
                // có quyền đi vào trong các controller được bảo vệ
                SecurityContextHolder.getContext().setAuthentication(authentication);
            }
        } catch (Exception ex) {
            log.error("Không thể xác thực người dùng trong Security Context: ", ex);
        }

        // 7. Chuyển Request sang bộ lọc tiếp theo (hoặc đến Controller)
        filterChain.doFilter(request, response);
    }

    // Hàm bổ trợ để lấy chuỗi Token từ Request
    private String getJwtFromRequest(HttpServletRequest request) {
        // Cách 1: Thử lấy từ Header "Authorization: Bearer <token>"
        String bearerToken = request.getHeader("Authorization");
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }

        // Cách 2: Thử lấy từ HttpOnly Cookie có tên là "access_token" (An toàn hơn cho
        // Frontend)
        if (request.getCookies() != null) {
            return Arrays.stream(request.getCookies())
                    .filter(c -> "access_token".equals(c.getName()))
                    .map(c -> c.getValue())
                    .findFirst()
                    .orElse(null);
        }
        return null;
    }
}
