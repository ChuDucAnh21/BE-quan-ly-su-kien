package com.example.backend.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * EventRequest: Chứa dữ liệu đầu vào khi Organizer tạo mới hoặc sửa Sự kiện.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EventRequest {

    @NotBlank(message = "Tên sự kiện không được để trống")
    private String title;

    @NotBlank(message = "Danh mục không được để trống")
    private String category;

    @NotBlank(message = "Mô tả ngắn không được để trống")
    private String shortDescription;

    @NotBlank(message = "Mô tả chi tiết không được để trống")
    private String description;

    @NotNull(message = "Sức chứa không được để trống")
    @Min(value = 1, message = "Sức chứa tối thiểu phải là 1 người")
    private Integer capacity;

    @NotNull(message = "Thời gian bắt đầu không được để trống")
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    private LocalDateTime startTime;

    @NotNull(message = "Thời gian kết thúc không được để trống")
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    private LocalDateTime endTime;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    private LocalDateTime registrationStartTime; // Thời gian mở bán vé (Optional, có thể là null)

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    private LocalDateTime registrationEndTime; // Thời gian đóng bán vé (Optional)

    @NotBlank(message = "Địa điểm không được để trống")
    private String location;

    @NotNull(message = "Giá vé không được để trống")
    @Min(value = 0, message = "Giá vé không được âm (bằng 0 nếu là sự kiện miễn phí)")
    private BigDecimal ticketPrice;

    // Ảnh bìa nhận trực tiếp qua MultipartFile thay vì link
    private MultipartFile bannerFile; // dạng file ảnh
}
