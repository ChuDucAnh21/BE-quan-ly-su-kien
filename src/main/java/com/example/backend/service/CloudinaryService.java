package com.example.backend.service;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class CloudinaryService {
    // Lớp trợ giúp việc tải ảnh lên nền tảng Cloudinary (Lưu trữ ảnh online)

    private final Cloudinary cloudinary; // Đối tượng Java Client của Cloudinary

    // Hàm nhận file ảnh từ người dùng và đẩy lên Cloud
    public String uploadImage(MultipartFile file) throws IOException {
        // Thực hiện upload và nhận về một tấm bản đồ (Map) kết quả
        Map<?, ?> uploadResult = cloudinary.uploader().upload(file.getBytes(), ObjectUtils.emptyMap());

        // Trích xuất đường dẫn ảnh bảo mật (secure_url - https) để lưu vào Database
        return uploadResult.get("secure_url").toString();
    }
}
