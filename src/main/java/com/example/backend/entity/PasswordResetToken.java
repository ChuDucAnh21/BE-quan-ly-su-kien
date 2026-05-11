package com.example.backend.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity // Đánh dấu lớp này là một thực thể JPA (Tương ứng với một bảng trong DB)
@Table(name = "password_reset_tokens") // Đặt tên bảng là password_reset_tokens
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PasswordResetToken {
    // Thực thể lưu trữ Mã khôi phục mật khẩu tạm thời cho người dùng

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id; // Khóa chính tự tăng

    @Column(nullable = false, unique = true)
    private String token; // Chuỗi mã ngẫu nhiên (UUID) gửi qua email

    // Thiết lập mối quan hệ N-1 với bảng User (Nhiều mã có thể thuộc về 1 User,
    // nhưng tại một thời điểm chỉ nên dùng 1 mã mới nhất)
    @ManyToOne(fetch = FetchType.LAZY) // Lazy: Chỉ tải User lên khi thực sự cần thiết hòng tiết kiệm tài nguyên
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false)
    private LocalDateTime expiryDate; // Thời điểm mã này hết hiệu lực (ví dụ: sau 15 phút)

    // Hàm tiện ích để kiểm tra xem mã đã "hết đát" hay chưa
    public boolean isExpired() {
        return LocalDateTime.now().isAfter(expiryDate); // nếu thời điểm hiện tại lớn hơn thời điểm hết hạn thì trả về
                                                        // true
    }
}
