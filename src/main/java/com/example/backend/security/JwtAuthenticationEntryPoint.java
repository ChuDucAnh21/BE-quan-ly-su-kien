package com.example.backend.security;

import com.example.backend.dto.response.ApiResponse;
import com.example.backend.exception.ErrorCode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.UUID;

@Component
public class JwtAuthenticationEntryPoint implements AuthenticationEntryPoint {
        // Lớp này dùng để xử lý trường hợp: Người dùng chưa đăng nhập (hoặc Token sai)
        // mà cố tình gọi vào API yêu cầu bảo mật.

        @Override
        public void commence(HttpServletRequest request, HttpServletResponse response,
                        AuthenticationException authException) throws IOException, ServletException {
                // Mặc định Spring sẽ trả về trang trắng hoặc lỗi mặc định, ta sẽ tùy biến để
                // trả JSON đẹp

                ErrorCode errorCode = ErrorCode.UNAUTHENTICATED; // Mã lỗi 401 Unauthorized

                response.setStatus(errorCode.getStatusCode().value()); // 401
                response.setContentType(MediaType.APPLICATION_JSON_VALUE); // Trả về dạng JSON

                // Xây dựng lại cấu trúc ApiResponse để đồng nhất với các lỗi khác
                ApiResponse.Status status = ApiResponse.Status.builder()
                                .code(errorCode.getCode()) // 401
                                .message(errorCode.getMessage()) // UNAUTHENTICATED
                                .label(errorCode.name()) // UNAUTHENTICATED
                                .requestId(UUID.randomUUID().toString().substring(0, 6))
                                .build();

                ApiResponse<?> apiResponse = ApiResponse.builder()
                                .status(status)
                                .build();

                // Dùng ObjectMapper để biến đối tượng Java thành chuỗi JSON và ghi vào Body của
                // Response
                ObjectMapper objectMapper = new ObjectMapper();
                response.getWriter().write(objectMapper.writeValueAsString(apiResponse));
                response.flushBuffer();
        }
}
