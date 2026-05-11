package com.example.backend.config;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class CloudinaryConfig {
    // Lớp cấu hình Cloudinary: Kết nối ứng dụng của chúng ta tới Server ảnh
    // Cloudinary

    @Value("${cloudinary.cloud-name}") // Lấy thông tin Tên Cloud từ file application.yml
    private String cloudName;

    @Value("${cloudinary.api-key}") // Lấy API Key
    private String apiKey;

    @Value("${cloudinary.api-secret}") // Lấy API Secret
    private String apiSecret;

    // Khởi tạo Bean Cloudinary để hệ thống có thể sử dụng (tiêm vào các Service)
    @Bean
    public Cloudinary cloudinary() {
        return new Cloudinary(ObjectUtils.asMap(
                "cloud_name", cloudName,
                "api_key", apiKey,
                "api_secret", apiSecret,
                "secure", true)); // Luôn chạy qua giao thức https
    }
}
