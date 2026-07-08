package com.example.backend.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public enum ErrorCode {
        // Danh sách các mã lỗi định danh dùng chung cho toàn bộ hệ thống ()
        // Cấu trúc: Tên_Lỗi(Mã_Số, Thông_Báo, Trạng_Thái_HTTP)
        // các ErrorCode này là do ta tự định nghĩa ra
        UNCATEGORIZED_EXCEPTION(500, "Lỗi hệ thống không xác định", HttpStatus.INTERNAL_SERVER_ERROR),
        INVALID_KEY(400, "Từ khóa thông báo không hợp lệ", HttpStatus.BAD_REQUEST),
        USER_EXISTED(400, "Người dùng đã tồn tại trong hệ thống", HttpStatus.BAD_REQUEST),
        USER_NOT_FOUND(404, "Không tìm thấy người dùng", HttpStatus.NOT_FOUND),
        INVALID_PASSWORD(400, "Mật khẩu không chính xác", HttpStatus.BAD_REQUEST),
        UNAUTHENTICATED(401, "Người dùng chưa được xác thực", HttpStatus.UNAUTHORIZED),
        ACCESS_DENIED(403, "Không có quyền truy cập", HttpStatus.FORBIDDEN),
        NO_CAN_BLOCK_ADMIN_ACCOUNT(400, "Tài khoản ADMIN không thể bị khóa", HttpStatus.BAD_REQUEST),

        // Lỗi liên quan đến Sự kiện (Event Phase 5)
        EVENT_NOT_FOUND(404, "Không tìm thấy sự kiện", HttpStatus.NOT_FOUND),
        TICKET_SOLD_OUT(400, "Sự kiện đã bán hết vé hoặc đang chờ thanh toán, hãy thử lại sau ít phút",
                        HttpStatus.BAD_REQUEST),
        ALREADY_REGISTERED(400, "Bạn đã đăng ký sự kiện này rồi", HttpStatus.BAD_REQUEST),
        UPLOAD_IMG_ERROR(400, "Ảnh tải lên không hợp lệ", HttpStatus.BAD_REQUEST),
        CAPACITY_CANNOT_BE_LESS_THAN_SOLD(400, "Sức chứa tối đa không thể nhỏ hơn số vé đã bán",
                        HttpStatus.BAD_REQUEST),
        INVALID_TIME_RANGE(400, "Thời gian bắt đầu, kết thúc không hợp lệ", HttpStatus.BAD_REQUEST),
        INVALID_REGISTRATION_TIME_RANGE(400,
                        "Thời gian bắt đầu, kết thúc đăng ký phải nằm trong khoảng thời gian diễn ra sự kiện",
                        HttpStatus.BAD_REQUEST),
        MUST_SETUP_PAYMENT_FIRST(400, "Bạn phải thiết lập tài khoản PayOS trước khi cho phép bán vé",
                        HttpStatus.BAD_REQUEST),
        SCHEDULE_NOT_FOUND(404, "Không tìm thấy lịch trình sự kiện", HttpStatus.NOT_FOUND),
        TIME_OUT_OF_EVENT_BOUNDS(400, "Thời gian lịch trình con vượt ra khỏi khuôn khổ sự kiện cha",
                        HttpStatus.BAD_REQUEST),
        EVENT_CANNOT_CHANGE_DRAFT(400, "Không thể thay đổi trạng thái sự kiện về bản nháp", HttpStatus.BAD_REQUEST),
        CHECKIN_TIME_INVALID(400, "Thời gian quét vé không hợp lệ", HttpStatus.BAD_REQUEST),
        EVENT_CANNOT_BE_UPDATED_MOST_FIELDS(400, "Không thể cập nhật một số trường của sự kiện",
                        HttpStatus.BAD_REQUEST),
        EVENT_CANNOT_BE_UPDATED_AFTER_END(400, "Không thể cập nhật sự kiện đã kết thúc",
                        HttpStatus.BAD_REQUEST),
        EVENT_CANNOT_CHANGE_STATUS_AFTER_END(400, "Không thể thay đổi trạng thái sự kiện đã kết thúc",
                        HttpStatus.BAD_REQUEST),

        // Lỗi liên quan đến Quên mật khẩu
        INVALID_RESET_TOKEN(400, "Mã khôi phục không hợp lệ hoặc không tồn tại", HttpStatus.BAD_REQUEST),
        RESET_TOKEN_EXPIRED(400, "Mã khôi phục đã hết hạn, vui lòng thực hiện lại", HttpStatus.BAD_REQUEST),

        // Lỗi liên quan đến Quản trị hệ thống (Admin)
        CANNOT_MODIFY_YOURSELF(400, "Bạn không thể tự khóa hoặc đổi quyền của chính mình", HttpStatus.BAD_REQUEST),

        // Lỗi liên quan đến Phân công Staff (Phase 6)
        INVALID_STAFF_ROLE(400, "Một hoặc nhiều người dùng không có quyền STAFF, hoặc tài khoản đã bị khóa",
                        HttpStatus.BAD_REQUEST),
        STAFF_NOT_ASSIGNED(404, "Nhân viên này chưa được phân công vào sự kiện", HttpStatus.NOT_FOUND),
        STAFF_CANNOT_REGISTER(403, "Nhân viên không thể đăng ký sự kiện được phân công", HttpStatus.FORBIDDEN),

        // Lỗi liên quan đến Thanh toán và Đăng ký (Phase 7)
        PAYMENT_CONFIG_INVALID(400, "Cổng thanh toán không tồn tại, hãy thử lại",
                        HttpStatus.BAD_REQUEST),
        PAYMENT_CONFIG_NOT_FOUND(404, "Org/Admin chưa thiết lập Cổng Thanh Toán PayOS", HttpStatus.NOT_FOUND),
        PAYMENT_NOT_FOUND(404, "Không tìm thấy giao dịch thanh toán", HttpStatus.NOT_FOUND),
        REGISTRATION_NOT_FOUND(404, "Không tìm thấy thông tin đăng ký", HttpStatus.NOT_FOUND),
        EVENT_NOT_STARTED(400, "Sự kiện chưa bắt đầu hoặc đã kết thúc đăng ký", HttpStatus.BAD_REQUEST),
        REGISTRATION_NOT_OPENED(400, "Sự kiện chưa đến giờ mở bán vé!", HttpStatus.BAD_REQUEST),
        REGISTRATION_CLOSED(400, "Sự kiện đã kết thúc thời gian bán vé!", HttpStatus.BAD_REQUEST),
        CANNOT_CANCEL_PAID_TICKET(400, "Bạn không thể hủy vé đã thanh toán, đã sử dụng hoặc hết hạn!",
                        HttpStatus.BAD_REQUEST),

        // Lỗi liên quan đến Check-in & Ticketing (Phase 9)
        TICKET_NOT_FOUND(404, "Mã vé không tồn tại", HttpStatus.NOT_FOUND),
        TICKET_ALREADY_USED(400, "Vé đã được checkin trước đó", HttpStatus.BAD_REQUEST),
        TICKET_INVALID(400, "Vé KHÔNG HỢP LỆ hoặc đã bị hệ thống hủy", HttpStatus.BAD_REQUEST),
        WRONG_EVENT_TICKET(400, "CẢNH BÁO: Vé không thuộc sự kiện này", HttpStatus.BAD_REQUEST);

        private final int code; // Mã lỗi nội bộ để Frontend dễ xử lý logic
        private final String message; // Thông báo hiển thị cho người dùng
        private final HttpStatus statusCode; // Mã trạng thái HTTP chuẩn

        ErrorCode(int code, String message, HttpStatus statusCode) {
                this.code = code;
                this.message = message;
                this.statusCode = statusCode;
        }
}
