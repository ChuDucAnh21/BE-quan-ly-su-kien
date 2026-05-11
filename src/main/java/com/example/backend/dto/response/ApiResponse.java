package com.example.backend.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.UUID;

@Data
@Builder // sinh mã hỗ trợ khởi tạo đối tượng theo mẫu thiết kế Builder Pattern, giúp cú
         // pháp khởi tạo đối tượng rõ ràng hơn bằng cách gọi chuỗi phương thức.
@NoArgsConstructor // sinh mã hỗ trợ khởi tạo đối tượng với giá trị mặc định
@AllArgsConstructor // sinh mã hỗ trợ khởi tạo đối tượng với tất cả các thuộc tính
@JsonInclude(JsonInclude.Include.NON_NULL) // bỏ qua các thuộc tính có giá trị null
public class ApiResponse<T> {

    @Builder.Default
    private Status status = new Status(); // khởi tạo status với giá trị mặc định

    private T data;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class Status {
        // Lớp lồng chứa các thông tin metadata về trạng thái của Request

        @Builder.Default
        private int code = 200; // Mã lỗi nội bộ (ví dụ: 200 là thành công, 1001 là lỗi tài khoản...)
        
        @Builder.Default
        private String message = "Thành công"; // Thông báo mô tả ngắn gọn cho người dùng
        
        @Builder.Default
        private String label = "API_STATUS_SUCCESS"; // Nhãn định danh dùng để phân loại lỗi
        
        @Builder.Default
        private String requestId = UUID.randomUUID().toString().substring(0, 6); // ID định danh duy nhất cho mỗi lượt gọi để dễ tra cứu log
    }
}
