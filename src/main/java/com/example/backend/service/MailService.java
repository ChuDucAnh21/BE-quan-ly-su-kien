package com.example.backend.service;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class MailService {
    // Lớp hỗ trợ gửi Email từ hệ thống (Dùng giao thức SMTP)

    private final JavaMailSender mailSender; // Công cụ của Spring Boot dùng để kết nối với Server Mail (Gmail)

    @Value("${spring.mail.username}") // Lấy địa chỉ email người gửi từ file application.yml
    private String fromEmail;

    // Hàm gửi mã khôi phục mật khẩu
    public void sendPasswordResetEmail(String toEmail, String token) {
        SimpleMailMessage message = new SimpleMailMessage(); // Đối tượng chứa nội dung email đơn giản

        message.setFrom(fromEmail); // Người gửi
        message.setTo(toEmail); // Người nhận
        message.setSubject("[Event Management] Yêu cầu đặt lại mật khẩu"); // Tiêu đề thư

        // Nội dung chi tiết của bức thư
        message.setText(
                "Chào bạn,\n\n" +
                        "Chúng tôi nhận được yêu cầu đặt lại mật khẩu cho tài khoản của bạn.\n\n" +
                        "Mã khôi phục của bạn là:\n\n" +
                        "   " + token + "\n\n" +
                        "Mã này sẽ hết hạn sau 15 phút.\n\n" +
                        "Nếu bạn không yêu cầu đặt lại mật khẩu, hãy bỏ qua email này.\n\n" +
                        "Trân trọng,\nĐội ngũ Event Management");

        // Lệnh thực hiện gửi thư đi
        mailSender.send(message);
    }
}
