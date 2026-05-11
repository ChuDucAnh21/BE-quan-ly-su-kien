package com.example.backend.exception;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AppException extends RuntimeException {
    // Đây là lớp Exception tự định nghĩa cho toàn hệ thống
    // Thay vì ném các lỗi chung chung, ta sẽ ném AppException kèm theo mã lỗi ErrorCode cụ thể

    private ErrorCode errorCode; // Chứa mã lỗi (ví dụ: USER_NOT_FOUND)

    public AppException(ErrorCode errorCode) {
        super(errorCode.getMessage()); // Truyền thông báo lỗi lên lớp cha RuntimeException
        this.errorCode = errorCode;
    }
}
