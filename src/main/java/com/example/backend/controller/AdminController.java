package com.example.backend.controller;

import com.example.backend.dto.request.ChangeRoleRequest;
import com.example.backend.dto.request.ChangeStatusRequest;
import com.example.backend.dto.response.ApiResponse;
import com.example.backend.dto.response.PageResponse;
import com.example.backend.dto.response.UserProfileResponse;
import com.example.backend.enums.Role;
import com.example.backend.enums.UserStatus;
import com.example.backend.service.AdminService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

/**
 * Ký hiệu @RestController: Biến lớp này thành một bộ điều khiển API.
 * Nó sẽ tự động chuyển đổi các kết quả trả về của hàm (objects) thành chuỗi
 * JSON.
 */
@RestController
/**
 * @RequestMapping: Quy định mọi API trong lớp này đều bắt buộc phải có tiền tố
 *                  là "/api/v1/admin/users".
 */
@RequestMapping("/api/v1/admin")
/**
 * @RequiredArgsConstructor: Tự động khởi tạo hằng số (final) thông qua
 *                           Dependency Injection.
 *                           Thay vì phải viết hàm khởi tạo (constructor) dài
 *                           dòng cho adminService.
 */
@RequiredArgsConstructor
/**
 * @PreAuthorize("hasRole('ADMIN')"): CHIẾC KHIÊN BẢO VỆ TUYỆT ĐỐI.
 * Dòng này ra lệnh cho Spring Security: "Bất kể là ai, nếu Token gửi lên không
 * chứa chữ 'ROLE_ADMIN'
 * thì cấm không cho chạy bất kì hàm nào bên dưới, lập tức trả về lỗi 403
 * Forbidden".
 */
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {

        private final AdminService adminService;

        // 1. Xem danh sách người dùng (Pagination, Search, Filter)
        /**
         * @GetMapping: Đón nhận Request HTTP GET dùng để ĐỌC dữ liệu.
         * @RequestParam: Bóc tách các tham số gắn trên thanh địa chỉ URL (Ví dụ:
         *                ?page=0&size=10&search=nam).
         *                Từ khóa 'required = false' nghĩa là nếu người dùng không
         *                truyền lên thì không sao (sẽ nhận giá trị null).
         *                Từ khóa 'defaultValue' dùng để gài mặc định nếu người dùng
         *                không điền.
         */
        @GetMapping("/get-all-users")
        @PreAuthorize("hasAnyRole('ADMIN', 'ORGANIZER')")
        public ApiResponse<PageResponse<UserProfileResponse>> getAllUsers(
                        @RequestParam(defaultValue = "0") int page,
                        @RequestParam(defaultValue = "10") int size,
                        @RequestParam(required = false) String search,
                        @RequestParam(required = false) Role role,
                        @RequestParam(required = false) UserStatus status,
                        @RequestParam(defaultValue = "desc") String sort) {

                // Security check & Role forcing: Organizer chỉ được phép xem list STAFF
                Authentication auth = SecurityContextHolder.getContext().getAuthentication();
                if (auth != null && auth.getAuthorities().stream()
                                .anyMatch(a -> a.getAuthority().equals("ROLE_ORGANIZER"))) {
                        role = Role.STAFF; // Ép buộc Organizer chỉ được xem danh sách STAFF
                }

                PageResponse<UserProfileResponse> users = adminService.getAllUsers(page, size, search, role, status,
                                sort);
                return ApiResponse.<PageResponse<UserProfileResponse>>builder()
                                .status(ApiResponse.Status.builder()
                                                .message("Lấy danh sách người dùng thành công")
                                                .build())
                                .data(users)
                                .build();
        }

        // 2. Thay đổi trạng thái người dùng (Khóa/Mở Khóa)
        /**
         * @PutMapping("/{id}/status"): Bắt HTTP PUT với đường dẫn động chứa {id}.
         * 
         * @PathVariable: Móc id ra khỏi đường dẫn (Ví dụ: /users/5/status -> id = 5).
         * @RequestBody: Lọc lấy thân JSON gửi lên.
         * @Valid: Bật cơ chế kiểm duyệt dữ liệu tự động (chặn các giá trị rỗng/sai ngay
         *         từ vòng gửi xe rác).
         */
        @PutMapping("/users/{id}/status")
        public ApiResponse<Void> changeUserStatus(@PathVariable Long id,
                        @RequestBody @Valid ChangeStatusRequest request) {
                adminService.changeUserStatus(id, request.getStatus());
                return ApiResponse.<Void>builder()
                                .status(ApiResponse.Status.builder()
                                                .message("Cập nhật trạng thái người dùng thành công")
                                                .build())
                                .build();
        }

        // 3. Phân quyền người dùng (Thăng cấp/Giáng cấp)
        @PutMapping("/users/{id}/role")
        public ApiResponse<Void> changeUserRole(@PathVariable Long id, @RequestBody @Valid ChangeRoleRequest request) {
                adminService.changeUserRole(id, request.getRole());
                return ApiResponse.<Void>builder()
                                .status(ApiResponse.Status.builder()
                                                .message("Cập nhật quyền người dùng thành công")
                                                .build())
                                .build();
        }

        // 4. API Xóa tài khoản vĩnh viễn (Dành cho Super Admin)
        @DeleteMapping("/users/{idUser}/delete")
        public ApiResponse<Void> deleteAccountUser(@PathVariable Long idUser) {
                adminService.deleteAccountUser(idUser);
                return ApiResponse.<Void>builder()
                                .status(ApiResponse.Status.builder().message("Xóa tài khoản thành công").build())
                                .build();
        }
}
