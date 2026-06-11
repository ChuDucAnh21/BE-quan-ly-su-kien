package com.example.backend.service;

import com.example.backend.dto.request.LoginRequest;
import com.example.backend.dto.request.RegisterRequest;
import com.example.backend.entity.User;
import com.example.backend.enums.AuthProvider;
import com.example.backend.enums.Role;
import com.example.backend.enums.UserStatus;
import com.example.backend.exception.AppException;
import com.example.backend.exception.ErrorCode;
import com.example.backend.dto.response.LoginResponse;
import com.example.backend.dto.response.UserProfileResponse;
import com.example.backend.repository.UserRepository;
import com.example.backend.security.CustomUserDetails;
import com.example.backend.security.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.beans.factory.annotation.Value;

import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken.Payload;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import com.example.backend.dto.request.GoogleLoginRequest;

import java.util.Collections;

@Service // Đánh dấu đây là lớp xử lý logic nghiệp vụ, Spring sẽ quản lý nó dưới dạng 1
         // Bean
@RequiredArgsConstructor // Tự động tạo constructor để tiêm các dependency (UserRepository,
                         // PasswordEncoder...)
public class AuthService {

        private final AuthenticationManager authenticationManager; // Quản lý việc xác thực tài khoản login
        private final UserRepository userRepository; // Giao tiếp với bảng users trong Database
        private final PasswordEncoder passwordEncoder; // Công cụ dùng để băm (hash) mật khẩu (Bcrypt)/ tức là mã hóa
                                                       // mật khẩu
        private final JwtTokenProvider tokenProvider; // Công cụ sinh và xác thực mã Token JWT

        @Value("${app.google.client-id}")
        private String googleClientId;

        @Transactional // Đảm bảo tính Toàn Vẹn: Nếu việc lưu User bị lỗi, thì mọi thứ trước đó trong
                       // hàm này cũng sẽ bị hủy bỏ (Rollback).
        public void register(RegisterRequest request) {
                // Bước 1: Bảo mật - Kiểm tra xem email đã tồn tại trong DB chưa để chống trùng
                // lặp dữ liệu
                if (userRepository.existsByEmail(request.getEmail())) {
                        throw new AppException(ErrorCode.USER_EXISTED);
                }

                // Bước 2: Tạo URL ảnh đại diện mặc định thông qua API của UI-Avatars (Tạo ảnh
                // có chứa chữ cái đầu tiên của Tên)
                String defaultAvatarUrl = "https://ui-avatars.com/api/?name=" + request.getFullName().replace(" ", "+")
                                + "&background=random";

                // Bước 3: Khởi tạo đối tượng User mới (Dùng Builder Pattern của Lombok để code
                // cực kỳ ngắn gọn và dễ đọc)
                User user = User.builder()
                                .fullName(request.getFullName())
                                .email(request.getEmail())
                                .phone(request.getPhone())
                                .password(passwordEncoder.encode(request.getPassword()))
                                .avatarUrl(defaultAvatarUrl)
                                .role(Role.USER) // Quyền mặc định cho thành viên đăng ký mới
                                .status(UserStatus.ACTIVE) // Set trạng thái ACTIVE để họ có thể đăng nhập ngay lập tức
                                .build();

                // Bước 4: Lưu thực thể User xuống Database thông qua Spring Data JPA
                userRepository.save(user);
        }

        /**
         * Xử lý luồng Đăng nhập (Login).
         * Trả về JWT Access Token và Refresh Token nếu thông tin chuẩn xác.
         */
        public LoginResponse login(LoginRequest request) {
                // Bước 1: Uỷ quyền xác thực cho Spring Security (AuthenticationManager)
                // Nó sẽ tự động móc nối với CustomUserDetailsService để tìm User theo Email, và
                // dùng PasswordEncoder để so khớp mật khẩu đã băm trong DB.
                // Nếu sai mật khẩu hoặc sai email, dòng code này sẽ ném ra lỗi
                // AuthenticationException ngay lập tức.
                Authentication authentication = authenticationManager.authenticate(
                                new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword()));

                // Bước 2: Nếu bước 1 trôi chảy, lưu thông tin vào SecurityContext
                // (Để các Controller khác biết được "À, người này đã được xác thực và có
                // quyền đi vào trong các controller được bảo vệ").
                SecurityContextHolder.getContext().setAuthentication(authentication);

                // Bước 3: Lấy ra đối tượng Detail (chứa đủ Entity User)
                CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();

                // Bước 4: Chế tạo chìa khoá (Token)
                // Access Token: Dùng để xác thực API hàng ngày (Thời hạn ngắn).
                // Refresh Token: Dùng để xin cấp lại Access Token mới khi nó hết hạn (Thời hạn
                // dài).
                String accessToken = tokenProvider.generateAccessToken(userDetails);
                String refreshToken = tokenProvider.generateRefreshToken(userDetails);

                // Bước 5: Đóng gói thông tin User để trả về Frontend làm Profile hiển thị.
                UserProfileResponse userResponse = UserProfileResponse.builder()
                                .id(userDetails.getUser().getId())
                                .email(userDetails.getUser().getEmail())
                                .fullName(userDetails.getUser().getFullName())
                                .phone(userDetails.getUser().getPhone())
                                .avatarUrl(userDetails.getUser().getAvatarUrl())
                                .role(userDetails.getUser().getRole().name())
                                .status(userDetails.getUser().getStatus().name())
                                .gender(userDetails.getUser().getGender() != null
                                                ? userDetails.getUser().getGender().name()
                                                : null)
                                .dateOfBirth(userDetails.getUser().getDateOfBirth())
                                .address(userDetails.getUser().getAddress())
                                .createdAt(userDetails.getUser().getCreatedAt())
                                .build();

                return LoginResponse.builder()
                                .accessToken(accessToken)
                                .refreshToken(refreshToken)
                                .user(userResponse)
                                .build();
        }

        /**
         * Xử lý luồng Refresh Token (Khi Access Token hết hạn).
         */
        public String refreshToken(String refreshToken) {
                // 1. Kiểm tra tính hợp lệ của Refresh Token
                if (refreshToken == null || !tokenProvider.validateJwtToken(refreshToken)) {
                        throw new AppException(ErrorCode.UNAUTHENTICATED);
                }

                // 2. Lấy Email từ Refresh Token
                String email = tokenProvider.getEmailFromJwtToken(refreshToken);

                // 3. Kiểm tra xem User có tồn tại và đang ACTIVE không
                User user = userRepository.findByEmail(email)
                                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

                if (user.getStatus() != UserStatus.ACTIVE) {
                        throw new AppException(ErrorCode.UNAUTHENTICATED);
                }

                // 4. Sinh Access Token mới
                CustomUserDetails userDetails = new CustomUserDetails(user);
                return tokenProvider.generateAccessToken(userDetails);
        }

        /**
         * Xử lý luồng Đăng nhập bằng Google.
         * Xác minh idToken từ Google, nếu email chưa tồn tại thì tạo tài khoản mới
         * (Provider = GOOGLE).
         * Nếu đã tồn tại (dù là LOCAL hay GOOGLE), hợp nhất và cho phép đăng nhập luôn.
         */
        @Transactional
        public LoginResponse googleLogin(GoogleLoginRequest request) {
                try {
                        GoogleIdTokenVerifier verifier = new GoogleIdTokenVerifier.Builder(new NetHttpTransport(),
                                        new GsonFactory())
                                        .setAudience(Collections.singletonList(googleClientId))
                                        .build(); // tạo trình xác thực, mục đích xác thực token từ Google

                        GoogleIdToken idToken = verifier.verify(request.getIdToken()); // xác thức idToken có hợp lệ hay
                                                                                       // không
                        if (idToken != null) {
                                Payload payload = idToken.getPayload(); // thông tin người dùng Google trả về
                                                                        // (email,name, pictureUrl, googleId)

                                String email = payload.getEmail();
                                String name = (String) payload.get("name");
                                String pictureUrl = (String) payload.get("picture");
                                String googleId = payload.getSubject();

                                // Kiểm tra xem user đã tồn tại chưa
                                User user = userRepository.findByEmail(email).orElse(null);
                                if (user == null) {
                                        // Tạo mới hoàn toàn (Mật khẩu = null vì đăng nhập Google)
                                        user = User.builder()
                                                        .email(email)
                                                        .fullName(name)
                                                        .avatarUrl(pictureUrl)
                                                        .role(Role.USER)
                                                        .status(UserStatus.ACTIVE)
                                                        .authProvider(AuthProvider.GOOGLE)
                                                        .providerId(googleId)
                                                        .build();
                                        user = userRepository.save(user);
                                } else {
                                        // Nếu User đã tồn tại (dù trước đó là LOCAL), ta tự động map và hợp nhất.
                                        // Có thể cập nhật lại avatar nếu chưa có
                                        if (user.getAvatarUrl() == null
                                                        || user.getAvatarUrl().contains("ui-avatars.com")) {
                                                user.setAvatarUrl(pictureUrl);
                                        }
                                        // Cập nhật lại provider Id cho chắc chắn
                                        if (user.getProviderId() == null) {
                                                user.setProviderId(googleId);
                                                user.setAuthProvider(AuthProvider.GOOGLE);
                                        }
                                        userRepository.save(user);
                                }

                                // Sau khi lấy hoặc tạo User thành công -> Xác thực ảo với Spring Security
                                CustomUserDetails userDetails = new CustomUserDetails(user);
                                UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                                                userDetails, null, userDetails.getAuthorities());
                                SecurityContextHolder.getContext().setAuthentication(authentication);

                                // Trả về Token và Profile
                                String accessToken = tokenProvider.generateAccessToken(userDetails);
                                String refreshToken = tokenProvider.generateRefreshToken(userDetails);

                                UserProfileResponse userResponse = UserProfileResponse.builder()
                                                .id(user.getId())
                                                .email(user.getEmail())
                                                .fullName(user.getFullName())
                                                .phone(user.getPhone())
                                                .avatarUrl(user.getAvatarUrl())
                                                .role(user.getRole().name())
                                                .status(user.getStatus().name())
                                                .gender(user.getGender() != null ? user.getGender().name() : null)
                                                .dateOfBirth(user.getDateOfBirth())
                                                .address(user.getAddress())
                                                .createdAt(user.getCreatedAt())
                                                .build();

                                return LoginResponse.builder()
                                                .accessToken(accessToken)
                                                .refreshToken(refreshToken)
                                                .user(userResponse)
                                                .build();
                        } else {
                                throw new AppException(ErrorCode.UNAUTHENTICATED);
                        }
                } catch (Exception e) {
                        e.printStackTrace();
                        throw new AppException(ErrorCode.UNAUTHENTICATED); // Báo lỗi nếu token Google giả mạo hoặc hết
                                                                           // hạn
                }
        }
}
