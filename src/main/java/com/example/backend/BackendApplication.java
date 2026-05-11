package com.example.backend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication // Annotation thần thánh: Gộp @Configuration, @EnableAutoConfiguration và @ComponentScan lại làm một. 
                       // Nó báo hiệu đây là khởi điểm của ứng dụng Spring Boot.
public class BackendApplication {

	public static void main(String[] args) {
	    // Dòng lệnh kích hoạt toàn bộ hệ thống Spring Boot
		SpringApplication.run(BackendApplication.class, args);
	}
}
