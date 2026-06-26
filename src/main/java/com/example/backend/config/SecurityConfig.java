package com.example.backend.config;

import com.example.backend.security.CustomUserDetailsService;
import com.example.backend.security.JwtAuthenticationEntryPoint;
import com.example.backend.security.JwtAuthenticationFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

@Configuration // Đánh dấu đây là lớp cấu hình, Spring sẽ đọc lớp này khi khởi động ứng dụng
@EnableWebSecurity // Kích hoạt tính năng bảo mật Web của Spring Security
@EnableMethodSecurity // Cho phép sử dụng các Annotation như @PreAuthorize trên các hàm trong
                      // Service/Controller
@RequiredArgsConstructor
public class SecurityConfig {

    private final CustomUserDetailsService userDetailsService; // Lớp dùng để lấy thông tin User từ Database
    private final JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint; // Xử lý khi người dùng chưa đăng nhập mà
                                                                           // truy cập tài nguyên yêu cầu quyền
    private final JwtAuthenticationFilter jwtAuthenticationFilter; // Bộ lọc chặn mọi Request để kiểm tra Token JWT

    // 1. Cấu hình công cụ mã hóa mật khẩu (Dùng thuật toán Bcrypt mạnh mẽ)
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    // 2. Cấu hình bộ xác thực (AuthenticationProvider)
    @Bean
    public AuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        authProvider.setUserDetailsService(userDetailsService); // Cung cấp cách lấy User
        authProvider.setPasswordEncoder(passwordEncoder()); // Cung cấp cách so sánh mật khẩu
        return authProvider;
    }

    // 3. Quản lý việc xác thực (AuthenticationManager) - Dùng trong AuthService để
    // login
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    // 4. TRÁI TIM CỦA BẢO MẬT: Cấu hình phân quyền URL
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                // Kích hoạt tích hợp CORS cho Spring Security (BẮT BUỘC ĐỂ KHÔNG BỊ BLOCK
                // OPTIONS REQUEST)
                .cors(Customizer.withDefaults())
                // Tắt CSRF vì chúng ta dùng JWT (Stateless), không dùng Session
                .csrf(AbstractHttpConfigurer::disable)

                // Cấu hình xử lý lỗi khi chưa xác thực
                .exceptionHandling(exception -> exception.authenticationEntryPoint(jwtAuthenticationEntryPoint))

                // Quy định hệ thống KHÔNG sử dụng Session (Mỗi Request phải gửi kèm Token)
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

                // PHÂN QUYỀN TRUY CẬP:
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/api/v1/auth/**").permitAll() // Cho phép tất cả mọi người truy cập các API
                                                                        // login, register, forgot-password
                        .requestMatchers("/api/v1/public/**").permitAll() // Cho phép truy cập dữ liệu công khai (danh
                                                                          // sách sự kiện...)
                        .requestMatchers("/v1/chat/completions/**").permitAll() // Cho phép truy cập chatbot AI không
                                                                                // cần đăng nhập
                        .requestMatchers("/api/v1/users/info-user/**").permitAll() // Truy cập public profile của user
                        .requestMatchers("/api/v1/payments/webhook").permitAll() // Webhook PayOS gọi ngầm không cần
                                                                                 // Token
                        .requestMatchers("/v3/api-docs/**", "/swagger-ui/**", "/swagger-ui.html").permitAll() // Giao
                                                                                                              // diện
                                                                                                              // Swagger
                                                                                                              // UI
                        .requestMatchers("/error").permitAll() // Cho phép truy cập trang báo lỗi hệ thống
                        .anyRequest().authenticated() // Mọi API khác còn lại bắt buộc phải đăng nhập (có Token hợp lệ)
                                                      // mới dùng được
                );

        // Đưa bộ xác thực vào hệ thống
        http.authenticationProvider(authenticationProvider());

        // Cài đặt bộ lọc JwtAuthenticationFilter chạy TRƯỚC bộ lọc mặc định của Spring
        http.addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    // Cấu hình CORS chuẩn cho Spring Security
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(java.util.Arrays.asList(
                "http://localhost:5173",
                "https://hub-events-iota.vercel.app",
                "http://localhost:3000",
                "http://localhost:3001",
                "http://127.0.0.1:5173"));
        configuration.setAllowedMethods(java.util.Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(java.util.Arrays.asList("*"));
        configuration.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}
