package com.example.backend.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EventScheduleRequest {

    // ID của lịch trình cần cập nhật (chỉ dùng khi gọi PUT để Sửa).
    // Nếu null -> Bộ API POST sẽ hiểu là Tạo mới, PUT hiểu là cần ID để sửa.
    private Long id;

    @NotBlank(message = "Tên lịch trình không được để trống")
    private String title;

    @NotNull(message = "Thời gian bắt đầu không được để trống")
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    private LocalDateTime startTime;

    @NotNull(message = "Thời gian kết thúc không được để trống")
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    private LocalDateTime endTime;

    @NotBlank(message = "Chi tiết lịch trình không được để trống")
    private String description;
}
