package com.example.backend.repository;

import com.example.backend.entity.PasswordResetToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PasswordResetTokenRepository extends JpaRepository<PasswordResetToken, Long> {
    // Repository quản lý bảng `password_reset_tokens` (Lưu mã quên mật khẩu)

    // Tìm kiếm một mã khôi phục theo chuỗi Token (Dùng để kiểm tra khi người dùng
    // nhập mã)
    Optional<PasswordResetToken> findByToken(String token);

    // Xóa tất cả các mã cũ của một User (Dùng để bắt đầu một lượt yêu cầu Quên mật
    // khẩu mới)
    @Modifying // Đánh dấu đây là truy vấn làm thay đổi dữ liệu (Cập nhật hoặc Xóa)
    @Query("DELETE FROM PasswordResetToken t WHERE t.user.id = :userId") // Câu lệnh JPQL
    void deleteAllByUserId(@Param("userId") Long userId);
}
