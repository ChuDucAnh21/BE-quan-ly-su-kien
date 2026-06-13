# TÀI LIỆU HƯỚNG DẪN KỸ THUẬT VÀ CHI TIẾT MÃ NGUỒN

> **Mục tiêu tài liệu:** Tài liệu này được biên soạn với văn phong kỹ thuật tiêu chuẩn. Cung cấp góc nhìn từ TỔNG QUAN danh sách các tập tin, đến giải thích CHI TIẾT rõ ràng mục đích của từng đoạn mã, từ khoá (Keyword) và Annotation cấu hình phần mềm được sử dụng trong dự án Spring Boot.

---

## PHẦN 1: GIAI ĐOẠN 1 (CORE ARCHITECTURE)

### I. TỔNG QUAN CÁC TẬP TIN GIAI ĐOẠN 1

Giai đoạn 1 thiết lập các cấu hình và tính năng cốt lõi cho Backend. Các tệp tin tiêu biểu được khởi tạo bao gồm:

1. **`pom.xml`**: Tệp tin cấu hình Maven chứa toàn bộ các khai báo để tải thư viện mã nguồn mở cần thiết (như Spring Web, Spring Data JPA, Lombok, JWT).
2. **`application.yml`**: Tệp tin chứa cấu hình kết nối của hệ thống ứng dụng với Máy chủ cơ sở dữ liệu MySQL và các thuộc tính quản lý vòng đời cập nhật Data của Hibernate.
3. **`ApiResponse.java`**: Lớp Java đóng vai trò bộ khung truyền tải chuẩn hóa định dạng JSON trả về kết quả cho phía Frontend/Mobile.
4. **`ErrorCode.java` & `AppException.java`**: Nơi dự án quản lý tập trung toàn bộ các mã lỗi nội bộ (Error codes) và ngoại lệ tùy chỉnh, bảo toàn tính thống nhất mã lỗi trên toàn hệ thống API.
5. **`GlobalExceptionHandler.java`**: Lớp đánh chặn toàn cục (Global Interceptor) đứng bao bọc vòng ngoài toàn hệ thống nhằm ứng phó và chuyển đổi các ngoại lệ (Exception) thành tín hiệu JSON thay vì văng mã nguồn lỗi StackTrace trên giao diện.
6. **`BaseEntity.java` & `JpaAuditingConfig.java`**: Kiến trúc cung cấp siêu dữ liệu (Metadata) cấp sẵn mốc thời gian (Ngày tạo, ngày cập nhật dữ liệu) cho các thực thể bảng kế thừa.

### II. GIẢI THÍCH CHI TIẾT MÃ NGUỒN GIAI ĐOẠN 1

#### 1. Khai báo thư viện `pom.xml`

- `<java.version>21</java.version>`: Quy định phiên bản Java Development Kit (JDK) biên dịch dự án là Java 21. Java 21 hỗ trợ Virtual Threads giúp tối ưu hóa luồng xử lý đồng thời.
- `org.projectlombok:lombok`: Thư viện hỗ trợ tự động sinh các phương thức phổ biến của đối tượng Java (như `getter`, `setter`, `constructor`) thông qua các annotation (`@Data`, `@Builder`), giúp mã nguồn ngắn gọn, tối ưu.
- `io.jsonwebtoken:jjwt-api`: Thư viện cung cấp tập hợp các hàm xử lý chuỗi JSON Web Token (JWT). JWT được sử dụng để xác thực và phân quyền người dùng trong các API sau khi họ đăng nhập thành công.

#### 2. Định dạng Tệp Khởi Tạo `application.yml`

- `hibernate.ddl-auto: update`: Thuộc tính này cấu hình tính năng Schema Update của Hibernate. Khi ứng dụng khởi động, Hibernate sẽ kiểm tra cấu trúc cơ sở dữ liệu hiện tại (MySQL) và tự động tạo mới bảng (CREATE TABLE) hoặc thêm cột (ALTER TABLE) dựa trên định nghĩa của các lớp Java được đánh dấu `@Entity`. Lập trình viên không cần viết mã SQL thủ công.

#### 3. Lớp Dữ Liệu Phản Hồi `ApiResponse.java`

```java
@Data
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApiResponse<T> { ... }
```

- `@Data`: Annotation của Lombok tự động sinh các hàm `getCode()`, `getMessage()`, `setResult()` và `toString()` trong quá trình biên dịch mã mguồn.
- `@Builder`: Sinh mã hỗ trợ khởi tạo đối tượng theo mẫu thiết kế Builder Pattern, giúp cú pháp khởi tạo đối tượng rõ ràng hơn bằng cách gọi chuỗi phương thức.
- `@JsonInclude(JsonInclude.Include.NON_NULL)`: Annotation của thư viện Jackson. Yêu cầu Jackson khi chuyển đổi đối tượng Java thành định dạng JSON thì bỏ qua các thuộc tính có giá trị `null` (rỗng). Việc này giúp giảm bớt kích thước tập tin JSON gửi về phía client qua HTTP.
- `<T>` (Generic Type): Cú pháp đa hình Generic của Java. Cho phép lớp `ApiResponse` nhận vào một kiểu dữ liệu bất kỳ làm kết quả trả về (`result`). Ví dụ: `ApiResponse<User>`, `ApiResponse<List<Event>>`.
- `private int code = 200`: Quy ước mã trạng thái nội bộ của hệ thống. Giá trị 200 mang ý nghĩa thao tác thành công. Lập trình viên có thể thay đổi thuộc tính này đối với các tác vụ gây ra lỗi.

#### 4. Lớp Xử Lý Ngoại Lệ Tập Trung `GlobalExceptionHandler.java`

```java
@ControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(value = AppException.class)
    // ...
```

- `@ControllerAdvice`: Một annotation cung cấp cơ chế Aspect-Oriented Programming (AOP). Lớp này đóng vai trò là một bộ đánh chặn toàn cục (global interceptor). Bất cứ Controller nào ném ra Exception chưa được xử lý, lớp này sẽ tự động bắt lấy để chặn việc văng lỗi trực tiếp ra màn hình trình duyệt.
- `@ExceptionHandler(value = AppException.class)`: Phương thức bên dưới thẻ này sẽ chỉ được kích hoạt chịu trách nhiệm xử lý khi hệ thống ném ra đúng loại ngoại lệ tùy chỉnh có tên là `AppException`.
- `ResponseEntity`: Một lớp tiêu chuẩn của hệ sinh thái Spring Boot để đóng gói thân dữ liệu phản hồi (body) cùng với mã HTTP Status (Ví dụ: 200 OK, 400 Bad Request).

#### 5. Cấu trúc Lịch Sử Kiểm Toán `BaseEntity.java`

```java
@MappedSuperclass
@EntityListeners(AuditingEntityListener.class)
public abstract class BaseEntity {
    @CreatedDate
    @Column(updatable = false)
    private LocalDateTime createdAt;
}
```

- `@MappedSuperclass`: Chỉ định lớp `BaseEntity` không phải là một bảng trong hệ cơ sở dữ liệu. Tuy nhiên, bất kỳ lớp nào kế thừa nó (ví dụ: `User`, `Event`) sẽ tự động có thêm các cột chứa thuộc tính đã khai báo (gồm `createdAt` và `updatedAt`).
- `@EntityListeners(AuditingEntityListener.class)`: Kích hoạt tính năng JPA Auditing (Theo dõi lịch sử truy xuất). Hệ thống sẽ lắng nghe sự kiện khi một bản ghi được chèn xuống hoặc cập nhật trên cơ sở dữ liệu và tự động gán giá trị thời gian hiện tại vào các biến.
- `@CreatedDate`: Đánh dấu chức năng biến `createdAt` sẽ lưu trữ thời gian duy nhất tại thời điểm bản ghi được khởi tạo mới trên database (tương đương lệnh INSERT).
- `@Column(updatable = false)`: Tính năng thiết lập cột trong SQL. Lệnh `updatable = false` ra chỉ thị tuyệt đối ngăn chặn câu lệnh SQL `UPDATE` làm thay đổi giá trị của cột `createdAt`, bảo vệ toàn vẹn lịch sử tạo mục.

---

## PHẦN 2: GIAI ĐOẠN 2 (DATABASE ENTITIES MAPPING)

### I. TỔNG QUAN CÁC TẬP TIN GIAI ĐOẠN 2

Giai đoạn 2 tập trung vào việc mô hình hóa (ORM Mapping) hệ thống thực thể thành các bảng thực tế dưới cơ sở dữ liệu. Các tệp tin được xây dựng bao gồm:

1. **Nhóm Kiểu Liệt Kê Thuộc Tính Trạng Thái (Package `com.example.backend.enums`):**
   - Bao gồm 8 tệp Java: `Role`, `UserStatus`, `EventStatus`, `RegistrationStatus`, `PaymentStatus`, `PaymentProvider`, `TicketStatus`, `CheckInMethod`.
   - Tác dụng: Xác định các tham số hằng số hợp lệ giới hạn cho các thuộc tính dưới Database, ngăn chặn lưu chuỗi dữ liệu ngẫu nhiên sai nghiệp vụ.
2. **Nhóm Thực Thể Bản Đồ Ánh Xạ (Package `com.example.backend.entity`):**
   - `User.java`: Đại diện cho cấu trúc định danh thông tin bảng người dùng.
   - `Event.java` & `EventSchedule.java`: Lớp thiết kế lưu trữ đặc tả thông tin sự kiện chính và lịch trình.
   - `Registration.java`: Chứa thông tin giao dịch ghi danh đăng ký mua vé của một người dùng đối với một sự kiện.
   - `Payment.java` & `PaymentConfig.java`: Lưu lại dấu vết phần cứng hóa đơn giao dịch tích hợp cổng thanh toán PayOS và khóa mã liên kết cá nhân của người tổ chức (Organizer).
   - `Ticket.java`: Mã định danh dữ liệu thẻ tham dự điện tử dùng sinh mã QR.
   - `EventStaffAssignment.java` & `CheckInLog.java`: Kiến trúc hệ thống kiểm duyệt, phân công nhân viên soát vé quản lý lưu lượng khách vảo cửa.

### II. GIẢI THÍCH CHI TIẾT MÃ NGUỒN GIAI ĐOẠN 2

#### 1. Kiểu Liệt Kê Trạng Thái `Enums`

```java
@Enumerated(EnumType.STRING)
private UserStatus status;
```

- `@Enumerated(EnumType.STRING)`: Annotation bắt buộc JPA lưu trữ giá trị của các tập Enum dưới định dạng kiểu chuỗi ký tự (Varchar) trong CSDL thay vì ghi nhớ theo chỉ mục số nguyên (Ordinal: 0, 1, 2...). Việc lưu trữ bằng chuỗi văn bản (Ví dụ lưu chữ `ACTIVE` thay vì số `0`) giúp CSDL trực quan hóa giá trị và ngăn ngừa hỏng hóc dữ liệu nền tảng khi lập trình viên sửa đổi các phần tử Enum cũ làm sai lệch số hiệu ban đầu.

#### 2. Thực Thể Người Dùng `User.java`

```java
@Entity
@Table(name = "users")
public class User extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String email;
}
```

- `@Entity`: Annotation do Java Persistence API (JPA) quy định. Đánh dấu một lớp Java là một đối tượng thực thể, tương ứng ánh xạ trực tiếp với một Bảng trong cơ sở dữ liệu.
- `@Table(name = "users")`: Quy định rõ tên Bảng thực thể xuất bản trong cơ sở dữ liệu là `users` thay vì lấy theo cấu trúc tên mặc định dựa theo tên gọi của lớp Java (`User`).
- `@Id`: Đánh dấu định danh chỉ định thuộc tính `id` bên dưới làm Khóa chính (Primary Key) độc nhất của toàn bộ bảng dữ liệu.
- `@GeneratedValue(strategy = GenerationType.IDENTITY)`: Cấu hình mã khóa chính tự động tăng dần (Autoincrement). Chiến lược `IDENTITY` phó thác toàn quyền bộ khởi tạo chuỗi khóa cho cơ sở dữ liệu MySQL, giúp hệ thống ngăn chặn lỗi phát sinh trùng mã khóa ở môi trường đa luồng liên tục.
- `@Column(...)`: Annotation thao tác các cấu hình tính toàn vẹn phụ thuộc cột.
  - `unique = true`: Khởi tạo cấu trúc Unique Constraint (Chỉ mục định dạng Ràng buộc Duy nhất) tại CSDL, định tuyến bắt buộc MySQL nghiêm cấm lưu 2 bản ghi có cùng chung 1 giá trị Email.
  - `nullable = false`: Bật khóa ràng buộc NOT NULL. Yêu cầu dữ liệu trong biến Email phải bắt buộc có thông tin giá trị, không cho phép nhập liệu biến rỗng (Null).

#### 3. Thực Thể Đơn Đăng Ký `Registration.java`

```java
@Table(name = "registrations", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"event_id", "user_id"})
})
```

- `@UniqueConstraint`: Khởi tạo một Khóa liên minh đa chiều (Composite Unique Constraint) cấu kết trực tiếp 2 trường dữ liệu `event_id` và `user_id`. Mục đích là chặn đứng tính trạng Tranh chấp tài nguyên ảo (Race condition) trên nền Database. Quá trình kiểm nghiệm tính toàn vẹn khẳng định: Ngay cả khi mã Java bỏ lọt và gửi xuống 10 yêu cầu đăng ký API liên tiếp cho cùng 1 cá nhân nhắm tới 1 sự kiện, cơ sở dữ liệu MySQL vẫn ưu tiên nhận diện khóa Ràng Buộc từ chối ghi Database, đánh bật lại ngoại lệ `DataIntegrityViolationException`, đảm bảo tính cứng cấp rằng 1 User chỉ được nhận cấp phát đăng ký tối đa 1 Event trên toàn vòng đời sự kiện.

#### 4. Thực Thể Sự Kiện Tổ Chức `Event.java`

```java
@ManyToOne(fetch = FetchType.LAZY)
@JoinColumn(name = "organizer_id", nullable = false)
private User organizer;
```

- `@ManyToOne`: Annotation đánh dấu hình thái của một mối quan hệ Nhiều - Một (Many-to-One). Ở thông số này, nhiều `Event` (Sự kiện) sẽ móc hệ chỉ trỏ đến một `User` có danh tính vai trò là người lập tổ chức (organizer).
- `@JoinColumn(name = "organizer_id")`: Khẳng định tạo ra một cột đóng vai trò là cột Khóa Ngoại Ràng buộc (Foreign Key) cài trên thân bảng `events` mang tên là `organizer_id` với chức năng trực tiếp liên kết đọc cấp thông số sang cột ID khóa chính của bảng `users`.
- `fetch = FetchType.LAZY`: Thuộc tính ấn định chế độ khởi sinh dữ liệu Tải chậm (Lazy loading). Khi hệ thống gọi tập lệnh truy xuất vòng List thông tin bảng `Event`, Hibernate chặn đứng không kéo ngay dữ liệu đồ sộ của bảng liên kết `User` liền kề để bảo quản tối đa tài nguyên bộ nhớ (được gọi là tối ưu hóa giải chặn nút thắt N+1 Query Problem). Nó sử dụng thủ thuật thay thế Đối Tượng Ảo Đóng Dấu (Proxy object). Câu lệnh Query Database móc Data nội tại của User từ MySQL thực tế chỉ được đánh thức chạy lại khi phía lập trình viên có lời gọi hàm cấp thiết (`event.getOrganizer().getEmail()`).

#### 5. Thực Thể Vé Điện Tử `Ticket.java`

```java
@OneToOne(fetch = FetchType.LAZY)
@JoinColumn(name = "registration_id", nullable = false)
private Registration registration;
```

- `@OneToOne`: Nhận định chỉ thị bảo toàn chặt chẽ mối quan hệ Một - Một (One-to-One). Một vé Điện tử (`Ticket`) phát hành vĩnh viễn mang cấu trúc định danh tương ứng duy nhất một thẻ Đơn thanh toán đăng ký (`Registration`). Cơ cấu này khóa luân phiên chu trình cung cấp mã QR hóa mạng, cấm đoán tuyệt đối vi phạm lỗi Data Overrun (Nhân bảng kép thẻ tham gia giả mạo vượt qua biên của duy nhất 1 Hóa Đơn đăng ký lưu hành).

---

## PHẦN 3: GIAI ĐOẠN 3 (SECURITY & AUTHENTICATION)

### I. TỔNG QUAN CÁC TẬP TIN GIAI ĐOẠN 3

Giai đoạn 3 tập trung vào việc xây dựng hàng rào bảo mật hệ thống API, quản lý Danh tính Người dùng (Identity) và định tuyến xác thực (Authentication) thông qua JSON Web Token. Các tệp tin được cấp mới bao gồm:

1. **Nhóm Định Cấu Hình Bảo Mật (Package `com.example.backend.config` và `security`):**
   - `SecurityConfig.java`: Lớp cấu hình mạng lõi Spring Security, gỡ bỏ phiên truy cập bộ nhớ và phong tỏa giới hạn chặt đường dẫn API.
   - `JwtTokenProvider.java`: Công cụ sinh mã thẻ Token chuẩn mực chứa chuỗi mã hóa HMAC để phát định danh và đo lường thời gian tồn tại của tài khoản trên hệ thống.
   - `JwtAuthenticationFilter.java`: Màng lọc lớp giữa (Middleware) đứng trực tiếp tại ngõ vào luồng mạng máy chủ, lột lấy mã Token từ HTTP Header hoặc truy vết ngầm qua Cookie để kiểm chứng xác thực danh tính.
   - `JwtAuthenticationEntryPoint.java`: Tấm lưới vây bắt tất cả các vòng truy cập trái phép không mang thẻ Token hợp lệ và trả về mã lỗi HTTP 401 chuỗi JSON thay vì trang trống.

2. **Nhóm Cơ Chế Ánh Xạ Phân Danh Tính (Package `com.example.backend.security`):**
   - `CustomUserDetails.java`: Lớp định nghĩa vỏ bọc thuộc tính chi tiết cho biểu mẫu User khi tồn tại lơ lửng trong luồng dịch thuật của Spring Security.
   - `CustomUserDetailsService.java`: Lớp mô phỏng cung cấp dịch vụ chuyển tiếp dữ liệu quy hoạch của Hệ Sinh Thái Spring tới luồng Cơ sở dữ liệu JPA (Thực hiện lệnh SQL Tìm Email trong MySQL để đối chứng kiểm tra tồn tại).
3. **Nhóm Giao Tiếp Hệ Thống Thông Điệp (Package `dto`, `controller`, `service`):**
   - `LoginRequest.java`, `RegisterRequest.java`: Lớp Đóng gói Mẫu Dữ Liệu Chuyên Khảo (Data Transfer Object) cho chuẩn cấu trúc phía ứng dụng gốc mạng Client kết xuất thông tin qua.
   - `AuthService.java`: Khoang chứa các mệnh lệnh logic nòng cốt từ chối thao tác (Ví dụ: Từ chối ghi khi dính lỗi Trùng lặp Email) và trộn mã hóa hàm băm Bcrypt đổ trực diện trên biến mật khẩu người dùng lưu tại thiết bị CSDL. Tích hợp thuật toán tự động cấp phát Ảnh đại diện chữ cái ngẫu nhiên (Dynamic Avatar UI) thông qua việc thao tác chuỗi Tên của Request và khởi gán giá trị Avatar cho User ngay tại thời điểm tạo tài khoản.
   - `AuthController.java`: Điểm tiếp nhận khai mở Request mạng nội API quy chuẩn nơi hệ thống mã Token được trích dẫn đóng gói kín rồi đẩy ngược sinh ra vào lòng an toàn của các tệp HTTP-Only Cookie.

### II. GIẢI THÍCH CHI TIẾT MÃ NGUỒN GIAI ĐOẠN 3

#### 1. Lớp Cấu Hình Bảo Mật Tổng Thể `SecurityConfig.java`

```java
@Bean
public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
    http.csrf(AbstractHttpConfigurer::disable)
        .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
// ...
```

- `@Bean`: Lệnh khai báo cho Spring Inversion of Control (IoC Container) biết biến/phương thức này là một khối linh kiện (Component) sẽ được lưu lên bộ nhớ ứng dụng RAM để cung ứng tái sử dụng xuyên suốt vòng vận hành của Ứng dụng.
- `.csrf(AbstractHttpConfigurer::disable)`: Lệnh vô hiệu hóa mạch kiểm tra Rã Tính giả mạo Cross-Site Request Forgery. Do kiến trúc của chúng ta phân bổ bảo mật theo quy trình Phi Lộ Trình Máy Chủ (Stateless Authentication), hệ mật độ này cho phép giải giới bớt khóa CSRF Token.
- `.sessionCreationPolicy(SessionCreationPolicy.STATELESS)`: Cấu hình quy tắc tước bỏ luồng thao tác khai báo bộ nhớ giữ trạng thái hệ Phiên liên lạc máy Server (Session Memory). Cơ cấu này mở đường tăng tốc mạng hệ RESTful API duy trì công năng không gánh nhớ cục bộ (Memory Overhead) về một Client đã chọc nối trước đó trên cổng port.

#### 2. Kích Hoạt Bộ Bọc Lọc Authentication `JwtAuthenticationFilter.java`

```java
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    @Override
    protected void doFilterInternal(...) {
        // ... FilterChain.doFilter
    }
}
```

- `OncePerRequestFilter`: Một lớp trừu tượng gốc rễ hệ thống Servlet áp mức tín hiệu ép chỉ đạo Hệ thống luồng vận kết Framework Spring Security. Rằng cơ chế cản luồng ngăn truy cập xác tín phía bên dưới mã này **cần nghiêm mật đảm bảo chỉ khơi chạy gọi hàm cản lướt ngang mạng đúng nhát một lần** trực tuyến trước mọi dòng gọi của Luồng Request cấp phát trỏ vào, tránh rò rỉ vòng treo đúp tín phễu chạy hệ thống vòng tròn lặp.
- **Bảo mật ngầm chặn Token còn hạn của tài khoản bị khóa (Gatekeeper):** Lớp này cũng đảm nhiệm một chức trách "Gác cổng" sống còn: Khi một Token (JWT) được giải mã hợp lệ, hệ thống sẽ thực hiện truy vấn thông tin User dưới Database. Nếu phát hiện trường `status` của tài khoản là `LOCKED` (đã bị Admin khóa), lớp này sẽ lập tức đá văng Token và từ chối cập nhật danh tính (Authentication) vào ngữ cảnh chung `SecurityContextHolder`. Thủ thuật này đá văng hoàn toàn luồng Tấn Căng lách luật bằng các Token còn rủng rỉnh hạn sử dụng.

#### 3. Phân Khối Nhận Diện Tài Khoản `JwtTokenProvider.java`

```java
return Jwts.builder()
        .setSubject(user.getEmail())
        .signWith(getSigningKey(), SignatureAlgorithm.HS256)
        .compact();
```

- `setSubject()`: Gắn dán mã hóa tham số tiêu chuẩn Subject định mức Tên Định Danh Gốc trên JWT Payload (Sử dụng chèn thẳng mã định danh Email độc bản để phân định dấu User thay vì để hở tham số Mật khẩu lưu động).
- `signWith(..., SignatureAlgorithm.HS256)`: Chấn đóng khóa phong ấn toàn bộ thân chuỗi chữ tạo JWT Token bằng tiêu chuẩn bảo chứng toàn cầu Băm Khóa Mật Mã 256-bit (Hết hiệu lực Crypto HMAC HS256). Điều này giăng bẫy cản phá thẳng tay đánh sập lỗi khi phát hiện Client tùy động sửa chèn chữ sai dán thông số độ chế làm giả cấp đặc quyền quy nhập.

#### 4. Thẩm Trục Cookie Xác Thực `AuthController.java`

```java
Cookie accessCookie = new Cookie("access_token", accessToken);
accessCookie.setHttpOnly(true);
```

- `setHttpOnly(true)`: Đoạn đinh chốt cắm mốc rào ngắt lập lệnh Cờ Bảo Vệ Cookie (HTTPOnly Flag). Phát Lệnh cấm tuyệt đối trình giải mã truy xét chui trực tiếp vào Document Object Model duyệt tệp. Lệnh dập tắt ngầm hoàn toàn bất kì một dòng thao tác JavaScript nào gắn rác ngụy tạo vớt lấy chuỗi mã lưu (Dạng Lỗ Hổng Cross-Site Scripting Attacks) trực trích biến tệp `access_token` nội tuyến trú ẩn tại Trình Duyệt.

### III. PHÂN TÍCH MODULE QUẢN TRỊ HỒ SƠ & CLOUDINARY (USER PROFILE)

1. **`CloudinaryConfig.java`**: Thiết lập cấu tạo Bean để mở cổng giao tiếp lên đám mây Cloudinary dựa trên thông số `api-key` và `api-secret`. Việc gắn Annotation `@Value` hỗ trợ bảo mật hệ thống, tránh vết lưu Hardcode tham số chuỗi API Key ngay lên bề mặt Class.
2. **`CloudinaryService.java`**: Phân lớp logic quy hoạch thực thi việc đẩy mạng (`upload()`) mảng nhị phân Byte Array của tập tin thông qua đối tượng `MultipartFile` lên đường truyền máy chủ lưu trữ khối ảnh, phục vụ trả về chuỗi trực diện URL ảnh.
3. **`UserService.java` & Đối soát Tường Mật khẩu Bcrypt**:
   - Hàm `changePassword()`: Ứng dụng vòng mã hóa Bcrypt nền thuật toán băm một chiều phi nghịch đảo (One-way Hashing function) qua thao tác đối hàm `passwordEncoder.matches(oldPassword, DbPassword)` đánh bật lại các rủi ro đọc trộm chuỗi mã tại cơ sở. Hệ thống tuyệt đối không biết Password thực của người dùng hiện là gì.
   - Cấu Trúc Soft-Delete Đóng Khóa: Tại hàm `softDeleteMyAccount()`, hệ thống triệt để từ chối gọi thi hành lệnh truy vấn xóa `SQL DELETE`. Nó dùng thao tác ánh xa, chuyển lệnh bẻ cờ Trạng Thái `status` thành `LOCKED` (Khóa lại), duy trì liên kết vòng đóng, bảo toàn 100% nguyên vẹn cấu trúc tham chiếu của mạng Hồ Sơ Các Giao Dịch thanh toán hóa đơn dội ngầm trong bảng sự kiện.
4. **`UserController.java` & Dập bẫy Lỗ Hổng Kênh API**:
   - Triệu gọi gốc định danh `Authentication authentication.getName()`: Kỹ thuật khai thác bóc tách nội hàm cấp thiết lấy thẳng thuộc tính Email Object Security Context được Hệ Điều hướng Đính lên Luồng kết nối (ThreadLocal). Tương đương việc ứng dụng đục rút trực tiếp danh tính tự xác tín lấy làm tham số thay vì hớ hênh thiết kế cổng API nhận mã ID gửi lậu hở lộ dưới Payload phía Web Client. Bẻ khóa sập hoàn toàn viễn cảnh Tấn Công Tham Số Vượt Tuyến Can Thiệp Người Khác (Lỗ hổng Insecure Direct Object References - IDOR).

### IV. QUY TẮC BẢO MẬT & XÁC THỰC DỮ LIỆU ĐẦU VÀO (DATA VALIDATION RULES)

Tất cả các API nhận dữ liệu từ Client thông qua Data Transfer Objects (DTO) đều được thắt chặt bằng quy tắc Biểu thức chính quy (Regex) và thông báo Tiếng Việt:

1. **Quản lý Email độc quyền Gmail:** Bắt buộc phải có định dạng đuôi `@gmail.com`. Thắt chặt khắt khe bằng Regex: `^[a-zA-Z0-9+_.-]+@gmail\.com$`
2. **Định dạng Số điện thoại Việt Nam:** Bắt buộc nhập 10 chữ số, định tuyến từ nhà mạng bắt đầu bằng `0` hoặc mã vùng `+84`. Cơ cấu Regex: `^(0|\+84)[0-9]{9}$`
3. **Độ dài Mật khẩu:** Tối thiểu 6 ký tự.

---

## PHẦN 4: TÍNH NĂNG QUÊN MẬT KHẨU (FORGOT PASSWORD)

### I. TỔNG QUAN CÁC TẬP TIN

| File                                | Vị trí         | Tác dụng                              |
| ----------------------------------- | -------------- | ------------------------------------- |
| `PasswordResetToken.java`           | `entity/`      | Bảng lưu mã khôi phục mật khẩu        |
| `PasswordResetTokenRepository.java` | `repository/`  | Truy vấn bảng `password_reset_tokens` |
| `ForgotPasswordRequest.java`        | `dto/request/` | DTO nhận email yêu cầu reset          |
| `ResetPasswordRequest.java`         | `dto/request/` | DTO nhận token và mật khẩu mới        |
| `MailService.java`                  | `service/`     | Gửi email chứa mã UUID qua SMTP Gmail |
| `PasswordResetService.java`         | `service/`     | Logic xử lý toàn bộ quy trình reset   |

### II. LUỒNG XỬ LÝ

1. **Bước 1 – Yêu cầu (`/forgot-password`):**
   - Nhận email từ FE → Tìm User trong DB.
   - Xóa toàn bộ mã reset cũ của User này (tránh rác DB).
   - Sinh mã `UUID` ngẫu nhiên, lưu kèm thời gian hết hạn **15 phút** vào bảng `password_reset_tokens`.
   - Gửi email chứa mã UUID qua `JavaMailSender`.
2. **Bước 2 – Đặt lại (`/reset-password`):**
   - Nhận `token` và `newPassword` từ FE.
   - Tìm token trong bảng `password_reset_tokens`.
   - Kiểm tra hết hạn chưa (gọi `resetToken.isExpired()`).
   - Mã hóa mật khẩu mới bằng Bcrypt và ghi đè vào User.
   - **Xóa token ngay sau khi dùng** (mỗi mã chỉ được dùng 1 lần duy nhất).

### III. CẤU HÌNH EMAIL (application.yml)

Để gửi email thực tế, cần điền thông tin SMTP Gmail vào `application.yml`:

```yaml
spring:
  mail:
    username: your_email@gmail.com
    password: your_app_password # Mật khẩu ứng dụng (App Password) của Google
```

> **Lưu ý:** Cần kích hoạt **2-Step Verification** trên Gmail và tạo **App Password** riêng. Không dùng mật khẩu Gmail thật.

```java
@Enumerated(EnumType.STRING)
@Column(name = "auth_provider")
@Builder.Default
private AuthProvider authProvider = AuthProvider.LOCAL; // Nguồn đăng nhập

@Column(name = "provider_id")
private String providerId; // ID từ Google
```

Câu hỏi: Tôi thấy trong entity User có them 2 trường này , có cần thiết phải lưu 2 trường này không. ?

```
Câu hỏi: Tôi thấy trong entity User có them 2 trường này , có cần thiết phải lưu 2 trường này không. ?

Câu trả lời ngắn gọn là: CÓ, 2 trường này là cực kỳ cần thiết (thậm chí là bắt buộc) khi hệ thống của bạn tích hợp tính năng Đăng nhập bằng Mạng xã hội (OAuth2).

Để bảo vệ đồ án trước giảng viên, nếu bị hỏi câu này, bạn hãy tự tin giải thích dựa trên 3 lý do sống còn dưới đây:

1. Phân biệt người dùng "Hệ thống" và người dùng "Google" (authProvider)
Khi người dùng đăng ký bằng tài khoản Google, họ không có mật khẩu (Password = null hoặc chuỗi rỗng/mã băm giả).

    Nếu không có trường authProvider, hệ thống sẽ không phân biệt được tài khoản này được tạo ra từ đâu.
    Giả sử một người dùng đăng ký bằng Google, nhưng ngày mai họ lại ra form Đăng nhập truyền thống nhập Email và gõ bừa 1 mật khẩu. Nhờ có authProvider = GOOGLE, hệ thống sẽ chặn lại ngay lập tức và báo lỗi: "Tài khoản này được đăng ký qua Google, vui lòng chọn Đăng nhập bằng Google" thay vì văng lỗi sai mật khẩu thông thường.
    Tính mở rộng: Sau này nếu bạn muốn tích hợp thêm Facebook, Apple, Github... bạn chỉ cần thêm giá trị vào Enum (Ví dụ: FACEBOOK, APPLE) là xong, kiến trúc database không bị xáo trộn.

2. Chống rủi ro "Chiếm đoạt tài khoản" (Account Takeover) nhờ providerId
Nhiều người nghĩ: "Chỉ cần lấy Email từ Google về rồi kiểm tra trong Database là đủ, cần gì providerId?". Đây là một tư duy bảo mật khá nguy hiểm.

    providerId là chuỗi ID định danh độc nhất và vĩnh viễn do chính Google cấp cho mỗi người (nó nằm trong trường sub của mã Google Token).
    Dù người dùng có đổi tên, đổi ảnh đại diện, hay thậm chí đổi cả địa chỉ Email trên tài khoản Google của họ, thì cái providerId này không bao giờ thay đổi.
    Nhờ lưu providerId, hệ thống của bạn sẽ biết chính xác "người đang đăng nhập này chính là người hôm trước", từ đó tự động cập nhật lại Email mới cho họ mà không làm mất dữ liệu mua vé cũ.
3. Đồng bộ hóa (Merge Account) an toàn
Giả sử người dùng ban đầu đăng ký thủ công bằng email nguyenvana@gmail.com và có mật khẩu. Tháng sau họ lười gõ mật khẩu, họ bấm nút "Đăng nhập bằng Google" (Google cũng xài đúng email nguyenvana@gmail.com).

    Hệ thống sẽ tìm thấy email này đã tồn tại trong DB (lúc này authProvider đang là LOCAL).
    Backend của bạn sẽ thực hiện Merge (Hợp nhất): Cập nhật providerId từ Google vào tài khoản này, và giữ nguyên authProvider = LOCAL.
    Từ đó về sau, người dùng này có đặc quyền đăng nhập bằng cả 2 cách (vừa gõ mật khẩu được, vừa bấm nút Google được) mà dữ liệu mua vé không bị tách làm 2 tài khoản khác nhau.
Tóm lại: Hai trường này chính là "Căn cước công dân kỹ thuật số" để hệ thống kết nối an toàn với máy chủ của Google, Facebook, Apple... Mọi hệ thống lớn (Shopee, Momo, Facebook,...) khi cho phép đăng nhập qua bên thứ 3 đều bắt buộc phải có kiến trúc này.
```
