package com.example.backend.dto.response;

import lombok.Builder;
import lombok.Data;

import java.util.List;

/**
 * @Data: Lombok tự động sinh Getter, Setter, toString... cho class này
 * @Builder: Cho phép khởi tạo đối tượng nhanh bằng cú pháp PageResponse.builder()...build()
 * <T>: Cú pháp Đa hình (Generic) của Java. 
 * Kí tự T đại diện cho bất kỳ kiểu dữ liệu nào (Ví dụ: Danh sách User, Danh sách Event...)
 * Giúp class này có thể dùng chung cho mọi api cần phân trang mà không phải viết lại code.
 */
@Data
@Builder
public class PageResponse<T> {
    private int currentPage; // Trang hiện tại (0-indexed)
    private int totalPages; // Tổng số trang
    private int pageSize; // Số lượng phần tử mỗi trang
    private long totalElements; // Tổng số phần tử trong toàn DB
    private List<T> data; // Danh sách dữ liệu của trang hiện tại
}
