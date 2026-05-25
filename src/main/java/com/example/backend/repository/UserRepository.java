package com.example.backend.repository;

import com.example.backend.entity.User;
import com.example.backend.enums.Role;
import com.example.backend.enums.UserStatus;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.time.LocalDateTime;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

@Repository // Đánh dấu đây là tầng Repository, nơi giao tiếp trực tiếp với Database
public interface UserRepository extends JpaRepository<User, Long> {
        // JpaRepository<User, Long>:
        // - User: Thực thể bảng sẽ làm việc
        // - Long: Kiểu dữ liệu của Khóa chính (ID)

        // Các câu lệnh : findBy... , existsBy...

        // Spring Data JPA sẽ tự động tạo câu lệnh SQL tương ứng: SELECT * FROM users
        // WHERE email = ?
        Optional<User> findByEmail(String email);

        // SQL tương ứng: SELECT COUNT(*) > 0 FROM users WHERE email = ?
        boolean existsByEmail(String email);

        /**
         * @Query: Cho phép tự viết câu lệnh truy vấn JPQL (Java Persistence Query
         *         Language).
         *         JPQL thao tác trên Entity (User) và Thuộc tính (u.fullName) thay vì
         *         gọi trực tiếp cấu trúc của Bảng dưới MySQL.
         * 
         *         Giải thích Logic:
         *         - `(:keyword IS NULL OR ...)`: Mẹo "Optional Filter". Nếu truyền
         *         keyword là null thì bỏ qua điều kiện lọc này, lấy tất cả.
         *         - `@Param`: Ánh xạ biến từ Java (ví dụ String keyword) vào tham số
         *         tên là `:keyword` trong câu truy vấn JPQL.
         *         - `Pageable`: Đối tượng chứa thông tin "Đang xem trang số mấy", "Mỗi
         *         trang mấy dòng", "Sắp xếp theo cột nào".
         *         - Trả về đối tượng `Page<User>` đã được gói gọn thông số Toán Học
         *         như: Tổng số trang, Tổng số phần tử.
         */
        @Query("SELECT u FROM User u WHERE " +
                        "(:keyword IS NULL OR LOWER(u.fullName) LIKE LOWER(CONCAT('%', :keyword, '%')) OR LOWER(u.email) LIKE LOWER(CONCAT('%', :keyword, '%')) OR u.phone LIKE CONCAT('%', :keyword, '%')) "
                        +
                        "AND (:role IS NULL OR u.role = :role) " +
                        "AND (:status IS NULL OR u.status = :status)")
        Page<User> searchUsers(@Param("keyword") String keyword,
                        @Param("role") Role role,
                        @Param("status") UserStatus status,
                        Pageable pageable);

        // Lấy toàn bộ user theo Role và Status
        List<User> findByRoleAndStatus(Role role,
                        UserStatus status);

        long countByRole(Role role);

        /**
         * Đếm số lượng User mới đăng ký theo từng ngày, phân loại theo Role.
         * Dùng cho biểu đồ Đường (Line Chart) - Tăng trưởng người dùng.
         * Kết quả trả về dạng: [{ date: "2024-05-01", role: "USER", count: 5 }, ...]
         * @param startDate  Ngày bắt đầu lọc (LocalDateTime)
         * @param endDate    Ngày kết thúc lọc (LocalDateTime)
         */
        @Query("SELECT CAST(u.createdAt AS date) as date, u.role as role, COUNT(u) as count " +
                "FROM User u " +
                "WHERE u.createdAt BETWEEN :startDate AND :endDate " +
                "GROUP BY CAST(u.createdAt AS date), u.role " +
                "ORDER BY CAST(u.createdAt AS date)")
        List<Object[]> countByDateAndRole(@Param("startDate") LocalDateTime startDate,
                        @Param("endDate") LocalDateTime endDate);

        /** Đếm số User theo Role trong khoảng thời gian */
        @Query("SELECT COUNT(u) FROM User u WHERE u.role = :role AND u.createdAt BETWEEN :startDate AND :endDate")
        long countByRoleInRange(@Param("role") Role role,
                                @Param("startDate") LocalDateTime startDate,
                                @Param("endDate") LocalDateTime endDate);
}
