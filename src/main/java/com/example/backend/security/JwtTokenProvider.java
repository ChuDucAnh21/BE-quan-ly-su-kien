package com.example.backend.security;

import com.example.backend.entity.User;
import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Date;

@Component // Đánh dấu lớp này là một Component để Spring quản lý và tiêm vào các nơi khác
           // (như SecurityConfig)
@Data
@Slf4j // Hỗ trợ ghi log các lỗi liên quan đến Token
public class JwtTokenProvider {

    // Lấy giá trị cấu hình Key bí mật từ file application.yml
    @Value("${app.jwt.secret:defaultSecretKeyWithAtLeast256BitsLengthForHS256Algorithm}")
    private String jwtSecret;

    // Thời gian hết hạn của Access Token (mặc định 24h) 24*60*60*1000 = 86400000
    // (ms)
    @Value("${app.jwt.expiration:86400000}")
    private long jwtExpirationMs;

    // Thời gian hết hạn của Refresh Token (mặc định 30 ngày) 30*24*60*60*1000 =
    // 2592000000 (ms)
    @Value("${app.jwt.refresh-expiration:2592000000}")
    private long jwtRefreshExpirationMs;

    // Tạo mã Key ký từ chuỗi Secret (Dùng thuật toán HMAC) để ký và xác thực Token
    private Key getSigningKey() {
        byte[] keyBytes = Decoders.BASE64.decode(jwtSecret);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    // Sinh Access Token cho người dùng
    public String generateAccessToken(CustomUserDetails userDetails) {
        return generateToken(userDetails.getUser(), jwtExpirationMs);
    }

    // Sinh Refresh Token (Dùng để lấy lại Access Token mới mà không cần đăng nhập
    // lại)
    public String generateRefreshToken(CustomUserDetails userDetails) {
        return generateToken(userDetails.getUser(), jwtRefreshExpirationMs);
    }

    // Hàm chung để tạo Token (JWT)
    private String generateToken(User user, long expirationMs) { // user này đã được xác thực
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + expirationMs); // Tính thời điểm hết hạn

        return Jwts.builder()
                .setSubject(user.getEmail()) // Lưu Email vào trường 'sub' (Chủ thể của token)
                .claim("id", user.getId()) // Lưu thêm ID vào nội dung Token (Payload)
                .claim("role", user.getRole().name()) // Lưu quyền (Role) của User // VD : user.getRole() = Role.ADMIN
                                                      // -> user.getRole().name() = "ADMIN"
                .setIssuedAt(now) // Ghi ngày phát hành token
                .setExpiration(expiryDate) // Ghi ngày hết hạn
                .signWith(getSigningKey(), SignatureAlgorithm.HS256) // Ký tên bằng thuật toán HS256
                .compact(); // Đóng gói thành chuỗi String
    }

    // Giải mã Token để lấy lại Email người dùng
    public String getEmailFromJwtToken(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(token)
                .getBody()
                .getSubject();
    }

    // Kiểm tra xem Token có hợp lệ, có bị sửa đổi hay hết hạn không
    public boolean validateJwtToken(String authToken) {
        try {
            Jwts.parserBuilder().setSigningKey(getSigningKey()).build().parseClaimsJws(authToken);
            return true; // Token hoàn toàn sạch và hợp lệ
        } catch (SecurityException | MalformedJwtException e) {
            log.error("Chữ ký JWT không hợp lệ (Invalid JWT signature): {}", e.getMessage());
        } catch (ExpiredJwtException e) {
            log.error("Mã Token đã hết hạn (JWT token is expired): {}", e.getMessage());
        } catch (UnsupportedJwtException e) {
            log.error("Mã Token không được hỗ trợ (JWT token is unsupported): {}", e.getMessage());
        } catch (IllegalArgumentException e) {
            log.error("Chuỗi xác thực trống (JWT claims string is empty): {}", e.getMessage());
        }
        return false; // Token không hợp lệ
    }
}
