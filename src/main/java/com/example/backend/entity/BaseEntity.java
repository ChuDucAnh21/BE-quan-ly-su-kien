package com.example.backend.entity;

import jakarta.persistence.Column;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.MappedSuperclass;
import lombok.Data;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

//các annotation bên dưới dùng đểAudit (theo dõi) thời gian tạo và cập nhật của đối tượng
@Data // Tự động sinh Getter/Setter cho ngày tạo, ngày cập nhật
@MappedSuperclass // Đánh dấu lớp này là lớp cha trong JPA (Hibernate), các thuộc tính bên dưới sẽ
                  // có mặt trong các bảng thực thể con
@EntityListeners(AuditingEntityListener.class) // Lắng nghe sự kiện để tự động gán giá trị thời gian (Auditing) , tức là
                                               // khi ta save hay update lần đầu thì nó sẽ tự động gán thời gian hiện
                                               // tại vào
public abstract class BaseEntity {

    @CreatedDate // Tự động gán thời điểm tạo lần đầu khi lưu đối tượng vào DB
    @Column(updatable = false) // Chỉ cho phép gán 1 lần, không cho sửa trường này khi update đối tượng
    private LocalDateTime createdAt;

    @LastModifiedDate // Tự động gán/cập nhật thời gian mỗi khi bản ghi có sự thay đổi
    private LocalDateTime updatedAt;
}
