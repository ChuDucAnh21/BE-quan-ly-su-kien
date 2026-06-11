package com.example.backend.enums;

public enum PaymentStatus {
    PENDING, // Chờ thanh toán
    SUCCESS, // Đã thanh toán thành công
    FAILED, // Giao dịch thất bại
    CANCELLED// Giao dịch bị hủy bởi người dùng
}
