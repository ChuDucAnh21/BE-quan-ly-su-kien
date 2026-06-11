package com.example.backend.entity;

import com.example.backend.enums.AuthProvider;
import com.example.backend.enums.Gender;
import com.example.backend.enums.Role;
import com.example.backend.enums.UserStatus;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

@Entity // Đánh dấu lớp này là một thực thể (Entity) ánh xạ với 1 bảng trong Database
@Table(name = "users") // Quy định tên bảng trong DB là "users"
@Data // Tự động sinh Getter, Setter, toString, equals, hashCode (thư viện Lombok)
@Builder // Hỗ trợ khởi tạo đối tượng nhanh qua chuỗi phương thức:
         // User.builder().email(...).build()
@NoArgsConstructor // Tạo constructor không tham số (Bắt buộc cho JPA)
@AllArgsConstructor // Tạo constructor đầy đủ tham số
@EqualsAndHashCode(callSuper = true) // Bao gồm cả các trường của lớp cha BaseEntity khi so sánh đối tượng
public class User extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "full_name") // có thể không cần @Column do Spring từ động chuyển CamelCase sang snake_case
                                // || Trùng tên cột
    private String fullName;

    @Column(unique = true, nullable = false)
    private String email;

    private String phone;

    private String password;

    @Enumerated(EnumType.STRING)
    private Role role;

    @Enumerated(EnumType.STRING)
    private UserStatus status;

    @Column(name = "avatar_url")
    private String avatarUrl;

    @Enumerated(EnumType.STRING)
    private Gender gender;

    @Column(name = "date_of_birth")
    private LocalDate dateOfBirth;

    private String address;

    @Enumerated(EnumType.STRING)
    @Column(name = "auth_provider")
    @Builder.Default
    private AuthProvider authProvider = AuthProvider.LOCAL; // Nguồn đăng nhập

    @Column(name = "provider_id")
    private String providerId; // ID từ Google
}
