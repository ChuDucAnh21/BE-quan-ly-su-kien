package com.example.backend.exception;

import com.example.backend.dto.response.ApiResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import java.util.UUID;

// Khi có lỗi xảy ra các lỗi sẽ được tự động vào các hàm @ExceptionHandler tương
// ứng
// theo thứ tự từ trên xuống dưới.

@ControllerAdvice // Đánh dấu lớp này là nơi tập trung xử lý toàn bộ exception của các Controller
public class GlobalExceptionHandler {

        private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

        // Hàm bổ trợ để tạo cấu trúc Status chuẩn cho ApiResponse
        private ApiResponse.Status buildStatus(int code, String message, String label) {
                return ApiResponse.Status.builder()
                                .code(code)
                                .message(message)
                                .label(label)
                                .requestId(UUID.randomUUID().toString().substring(0, 6)) // Tạo ra ID định danh duy nhất
                                                                                         // cho mỗi lỗi để dễ tra cứu
                                                                                         // log
                                .build();
        }

        // 1. Xử lý các lỗi Runtime chưa được định nghĩa (Cú chốt cuối cùng để tránh sập
        // app)
        @ExceptionHandler(value = Exception.class)
        public ResponseEntity<ApiResponse<?>> handlingRuntimeException(RuntimeException exception) {
                log.error("Exception: ", exception); // Ghi log lỗi vào server
                ErrorCode errorCode = ErrorCode.UNCATEGORIZED_EXCEPTION; // Mã lỗi 500 mặc định
                ApiResponse<?> apiResponse = ApiResponse.builder() // ? là generic có thể trả về bất kì kiểu dữ liệu nào
                                .status(buildStatus(
                                                errorCode.getCode(),
                                                errorCode.getMessage(),
                                                errorCode.name())) // là tên của enum ErrorCode : VD:
                                                                   // UNCATEGORIZED_EXCEPTION
                                .build();
                return ResponseEntity.status(errorCode.getStatusCode()).body(apiResponse);
        }

        // 2. Xử lý các lỗi nghiệp vụ do chúng ta chủ động ném ra (AppException)
        @ExceptionHandler(value = AppException.class)
        public ResponseEntity<ApiResponse<?>> handlingAppException(AppException exception) {
                ErrorCode errorCode = exception.getErrorCode(); // Lấy mã lỗi tương ứng (ví dụ: USER_EXISTED)
                ApiResponse<?> apiResponse = ApiResponse.builder()
                                .status(buildStatus(
                                                errorCode.getCode(),
                                                errorCode.getMessage(),
                                                errorCode.name()))
                                .build();
                return ResponseEntity.status(errorCode.getStatusCode()).body(apiResponse);
        }

        // 3. Xử lý lỗi khi đăng nhập sai (AuthenticationException của Spring Security)
        @ExceptionHandler(value = AuthenticationException.class)
        public ResponseEntity<ApiResponse<?>> handlingAuthenticationException(
                        AuthenticationException exception) {
                ErrorCode errorCode = ErrorCode.UNAUTHENTICATED;
                ApiResponse<?> apiResponse = ApiResponse.builder()
                                .status(buildStatus(
                                                errorCode.getCode(),
                                                "Email hoặc mật khẩu không chính xác",
                                                "INVALID_CREDENTIALS"))
                                .build();
                return ResponseEntity.status(errorCode.getStatusCode()).body(apiResponse);
        }

        // 3.1 Xử lý lỗi khi truy cập tài nguyên không có quyền (AccessDeniedException)
        @ExceptionHandler(value = AccessDeniedException.class)
        public ResponseEntity<ApiResponse<?>> handlingAccessDeniedException(AccessDeniedException exception) {
                ErrorCode errorCode = ErrorCode.ACCESS_DENIED;
                ApiResponse<?> apiResponse = ApiResponse.builder()
                                .status(buildStatus(
                                                errorCode.getCode(),
                                                errorCode.getMessage(),
                                                errorCode.name()))
                                .build();
                return ResponseEntity.status(errorCode.getStatusCode()).body(apiResponse);
        }

        // 4. Xử lý lỗi validate dữ liệu đầu vào và lỗi ép kiểu (Binding Error)
        // Các lỗi phát sinh từ việc sử dụng @valid
        @ExceptionHandler(value = { MethodArgumentNotValidException.class, BindException.class })
        public ResponseEntity<ApiResponse<?>> handlingValidation(Exception exception) {
                FieldError fieldError = null;
                if (exception instanceof MethodArgumentNotValidException ex) {
                        fieldError = ex.getFieldError();
                } else if (exception instanceof BindException ex) {
                        fieldError = ex.getFieldError();
                }

                String message = "Validation error";
                ErrorCode errorCode = ErrorCode.INVALID_KEY;

                if (fieldError != null) {
                        // Nếu lỗi do sai định dạng (ví dụ: nhập chữ vào trường Integer như 'capacity')
                        if ("typeMismatch".equals(fieldError.getCode())) {
                                String fieldName = fieldError.getField();
                                // Một số map tên trường sang tiếng Việt cho dễ đọc
                                if ("capacity".equals(fieldName))
                                        fieldName = "Sức chứa";
                                if ("ticketPrice".equals(fieldName))
                                        fieldName = "Giá vé";

                                message = String.format("Trường '%s' không đúng định dạng dữ liệu", fieldName);

                        } else {
                                // Nếu là lỗi validate từ annotation (@NotBlank, @Min...)
                                message = fieldError.getDefaultMessage();
                                try {
                                        errorCode = ErrorCode.valueOf(message);
                                        message = errorCode.getMessage();
                                } catch (IllegalArgumentException e) {
                                        // Giữ nguyên message gốc
                                }
                        }
                }

                ApiResponse<?> apiResponse = ApiResponse.builder()
                                .status(buildStatus(
                                                errorCode.getCode(),
                                                message,
                                                "VALIDATION_ERROR"))
                                .build();
                return ResponseEntity.badRequest().body(apiResponse);
        }

        // 5. Xử lý lỗi sai kiểu dữ liệu trên URL (ví dụ: /events/abc thay vì /events/1)
        @ExceptionHandler(value = MethodArgumentTypeMismatchException.class)
        public ResponseEntity<ApiResponse<?>> handlingTypeMismatch(MethodArgumentTypeMismatchException exception) {
                String message = String.format("Tham số '%s' không đúng định dạng", exception.getName());
                ApiResponse<?> apiResponse = ApiResponse.builder()
                                .status(buildStatus(
                                                ErrorCode.INVALID_KEY.getCode(),
                                                message,
                                                "TYPE_MISMATCH"))
                                .build();
                return ResponseEntity.badRequest().body(apiResponse);
        }

        // 6. Xử lý lỗi JSON không hợp lệ hoặc sai cấu trúc
        // (HttpMessageNotReadableException)
        @ExceptionHandler(value = HttpMessageNotReadableException.class)
        public ResponseEntity<ApiResponse<?>> handlingHttpMessageNotReadable(
                        HttpMessageNotReadableException exception) {
                String message = "Dữ liệu gửi lên không hợp lệ hoặc sai định dạng";
                ApiResponse<?> apiResponse = ApiResponse.builder()
                                .status(buildStatus(
                                                ErrorCode.INVALID_KEY.getCode(),
                                                message,
                                                "JSON_PARSE_ERROR"))
                                .build();
                return ResponseEntity.badRequest().body(apiResponse);
        }
}
