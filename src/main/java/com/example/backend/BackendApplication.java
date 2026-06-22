package com.example.backend;

import jakarta.annotation.PostConstruct;
import java.util.TimeZone;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication // Annotation thần thánh: Gộp @Configuration, @EnableAutoConfiguration và
						// @ComponentScan lại làm một.
						// Nó báo hiệu đây là khởi điểm của ứng dụng Spring Boot.
public class BackendApplication {

	public static void main(String[] args) {
		// Dòng lệnh kích hoạt toàn bộ hệ thống Spring Boot
		SpringApplication.run(BackendApplication.class, args);
	}

	// Thêm đoạn này để ép múi giờ
	@PostConstruct
	public void init() {
		TimeZone.setDefault(TimeZone.getTimeZone("Asia/Ho_Chi_Minh"));
	}
}
