# BIỂU ĐỒ HOẠT ĐỘNG - PHẦN D: THANH TOÁN, VÉ & CHECK-IN
**Hệ thống Quản lý Sự kiện & Check-in QR**

---

Tài liệu này chứa các Biểu đồ Hoạt động (Activity Diagrams) mô tả luồng mua vé, thanh toán trực tuyến, quản lý đơn hàng và nghiệp vụ soát vé tại cổng sự kiện.

---

## UC29 – Thiết lập cấu hình thanh toán

```mermaid
flowchart TD
    classDef startNode fill:#000,stroke:#000,color:#fff;
    classDef endNode fill:#fff,stroke:#000,stroke-width:4px,color:#000;

    subgraph TacNhan [Ban tổ chức]
        direction TB
        A[Điền thông tin kết nối cổng thanh toán]
        B[Nhấn nút Lưu cấu hình]
    end

    subgraph HeThong [Hệ thống]
        direction TB
        Start((Bắt đầu)):::startNode
        C[Hiển thị biểu mẫu cài đặt thanh toán]
        D{Kiểm tra tính hợp lệ của thông tin kết nối}
        E[Thông báo lỗi nhập liệu]
        F[Lưu thông tin kết nối vào hệ thống bảo mật]
        G[Thông báo lưu cấu hình thành công]
        End(((Kết thúc))):::endNode
    end

    Start --> C
    C --> A
    A --> B
    B --> D
    
    D -- Không hợp lệ --> E
    E --> A
    
    D -- Hợp lệ --> F
    F --> G
    G --> End
```

---

## UC30 – Xem cấu hình thanh toán hiện hành

```mermaid
flowchart TD
    classDef startNode fill:#000,stroke:#000,color:#fff;
    classDef endNode fill:#fff,stroke:#000,stroke-width:4px,color:#000;

    subgraph TacNhan [Ban tổ chức]
        direction TB
        A[Truy cập trang Cài đặt thanh toán]
    end

    subgraph HeThong [Hệ thống]
        direction TB
        Start((Bắt đầu)):::startNode
        B[Tìm kiếm thông tin cài đặt của người dùng này]
        C[Giải mã thông tin kết nối]
        D[Hiển thị dữ liệu lên màn hình]
        End(((Kết thúc))):::endNode
    end

    Start --> A
    A --> B
    B --> C
    C --> D
    D --> End
```

---

## UC31 – Đăng ký mua vé tham gia sự kiện

```mermaid
flowchart TD
    classDef startNode fill:#000,stroke:#000,color:#fff;
    classDef endNode fill:#fff,stroke:#000,stroke-width:4px,color:#000;

    subgraph TacNhan [Người dùng]
        direction TB
        A[Nhấn nút Đăng ký mua vé]
        B[Thực hiện quét mã QR thanh toán trên ứng dụng ngân hàng]
    end

    subgraph HeThong [Hệ thống & Cổng thanh toán]
        direction TB
        Start((Bắt đầu)):::startNode
        C{Kiểm tra điều kiện: Còn chỗ trống không? Sự kiện còn mở không?}
        D[Thông báo lỗi hết vé hoặc sự kiện đã đóng]
        E[Tạo đơn đăng ký mua vé]
        F{Kiểm tra giá vé của sự kiện này}
        
        %% Nhánh Miễn Phí
        G[Xác nhận thanh toán 0 đồng thành công]
        H[Chuyển trạng thái đơn thành Đã xác nhận]
        I[Tạo mã QR vé điện tử gửi cho người dùng]
        J[Thông báo nhận vé thành công]
        
        %% Nhánh Có Phí
        K[Chuyển trạng thái đơn thành Đang chờ thanh toán]
        L[Kết nối với cổng thanh toán để tạo mã thanh toán]
        M[Lưu thông tin giao dịch chờ xử lý]
        N[Chuyển hướng người dùng sang trang thanh toán ngân hàng]
        
        End(((Kết thúc))):::endNode
    end

    Start --> A
    A --> C
    
    C -- Không đủ điều kiện --> D
    D --> End
    
    C -- Hợp lệ --> E
    E --> F
    
    F -- Vé miễn phí --> G
    G --> H
    H --> I
    I --> J
    J --> End
    
    F -- Vé có tính phí --> K
    K --> L
    L --> M
    M --> N
    N --> B
    B --> End
```

---

## UC32 – Hủy vé đã đăng ký

```mermaid
flowchart TD
    classDef startNode fill:#000,stroke:#000,color:#fff;
    classDef endNode fill:#fff,stroke:#000,stroke-width:4px,color:#000;

    subgraph TacNhan [Người dùng]
        direction TB
        A[Nhấn nút Hủy vé]
        B[Xác nhận muốn hủy vé này]
    end

    subgraph HeThong [Hệ thống]
        direction TB
        Start((Bắt đầu)):::startNode
        C[Hiển thị hộp thoại xác nhận hủy]
        D{Kiểm tra loại vé: Có phải vé miễn phí không?}
        E[Thông báo hệ thống chưa hỗ trợ hoàn tiền cho vé có phí]
        F[Hủy hiệu lực của mã QR vé điện tử]
        G[Chuyển trạng thái đơn thành Đã hủy]
        H[Trả lại chỗ trống cho người khác đăng ký]
        I[Thông báo hủy vé thành công]
        End(((Kết thúc))):::endNode
    end

    Start --> A
    A --> C
    C --> B
    
    B -- Đồng ý --> D
    D -- Vé có phí --> E
    E --> End
    
    D -- Vé miễn phí --> F
    F --> G
    G --> H
    H --> I
    I --> End
```

---

## UC33 – Xử lý thông báo thanh toán tự động (Webhook)

```mermaid
flowchart TD
    classDef startNode fill:#000,stroke:#000,color:#fff;
    classDef endNode fill:#fff,stroke:#000,stroke-width:4px,color:#000;

    subgraph TacNhan [Cổng thanh toán]
        direction TB
        A[Gửi tín hiệu kết quả giao dịch về hệ thống]
    end

    subgraph HeThong [Hệ thống]
        direction TB
        Start((Bắt đầu)):::startNode
        B[Tiếp nhận tín hiệu]
        C{Xác thực chữ ký bảo mật của tín hiệu}
        D[Bỏ qua tín hiệu giả mạo]
        E{Kiểm tra trạng thái đơn hàng hiện tại}
        F[Bỏ qua do đơn hàng đã được xử lý trước đó]
        G{Kiểm tra kết quả giao dịch}
        
        %% Thành công
        H[Cập nhật đơn hàng thành Đã thanh toán]
        I[Tạo mã QR vé điện tử]
        J[Gửi email vé điện tử cho người dùng]
        
        %% Thất bại
        K[Cập nhật đơn hàng thành Đã hủy do lỗi thanh toán]
        L[Hoàn trả lại chỗ trống của sự kiện]
        
        End(((Kết thúc))):::endNode
    end

    Start --> A
    A --> B
    B --> C
    
    C -- Sai chữ ký --> D
    D --> End
    
    C -- Đúng chữ ký --> E
    E -- Không phải đang chờ --> F
    F --> End
    
    E -- Đang chờ thanh toán --> G
    
    G -- Thanh toán thành công --> H
    H --> I
    I --> J
    J --> End
    
    G -- Thanh toán thất bại / Hủy --> K
    K --> L
    L --> End
```

---

## UC34 – Đồng bộ trạng thái đơn hàng thủ công

```mermaid
flowchart TD
    classDef startNode fill:#000,stroke:#000,color:#fff;
    classDef endNode fill:#fff,stroke:#000,stroke-width:4px,color:#000;

    subgraph TacNhan [Người dùng / Ban tổ chức]
        direction TB
        A[Nhấn nút Kiểm tra trạng thái thanh toán]
    end

    subgraph HeThong [Hệ thống]
        direction TB
        Start((Bắt đầu)):::startNode
        B[Gửi yêu cầu truy vấn đến cổng thanh toán]
        C[Nhận kết quả trả về từ cổng thanh toán]
        D{So sánh với trạng thái đơn hàng hiện tại}
        E[Cập nhật trạng thái mới nhất cho đơn hàng]
        F[Thông báo đồng bộ trạng thái thành công]
        End(((Kết thúc))):::endNode
    end

    Start --> A
    A --> B
    B --> C
    C --> D
    
    D -- Có thay đổi --> E
    E --> F
    F --> End
    
    D -- Không có thay đổi --> F
```

---

## UC35 – Xem danh sách đơn hàng chờ thanh toán

```mermaid
flowchart TD
    classDef startNode fill:#000,stroke:#000,color:#fff;
    classDef endNode fill:#fff,stroke:#000,stroke-width:4px,color:#000;

    subgraph TacNhan [Người dùng]
        direction TB
        A[Truy cập Lịch sử giao dịch]
    end

    subgraph HeThong [Hệ thống]
        direction TB
        Start((Bắt đầu)):::startNode
        B[Tìm kiếm các đơn đăng ký mua vé của người dùng]
        C[Phân loại và lọc các đơn Đang chờ thanh toán]
        D[Hiển thị danh sách kèm nút Tiếp tục thanh toán]
        End(((Kết thúc))):::endNode
    end

    Start --> A
    A --> B
    B --> C
    C --> D
    D --> End
```

---

## UC36 – Xem danh sách vé của tôi (My Tickets)

```mermaid
flowchart TD
    classDef startNode fill:#000,stroke:#000,color:#fff;
    classDef endNode fill:#fff,stroke:#000,stroke-width:4px,color:#000;

    subgraph TacNhan [Người dùng]
        direction TB
        A[Truy cập mục Vé của tôi]
        B[Nhấn vào một vé cụ thể để xem mã QR]
    end

    subgraph HeThong [Hệ thống]
        direction TB
        Start((Bắt đầu)):::startNode
        C[Tìm kiếm các vé điện tử thuộc sở hữu của người dùng]
        D[Hiển thị danh sách các vé hợp lệ]
        E[Phóng to mã QR để chuẩn bị soát vé]
        End(((Kết thúc))):::endNode
    end

    Start --> A
    A --> C
    C --> D
    D --> B
    B --> E
    E --> End
```

---

## UC37 – Xem danh sách vé đã bán của sự kiện

```mermaid
flowchart TD
    classDef startNode fill:#000,stroke:#000,color:#fff;
    classDef endNode fill:#fff,stroke:#000,stroke-width:4px,color:#000;

    subgraph TacNhan [Ban tổ chức]
        direction TB
        A[Truy cập mục Quản lý vé của sự kiện]
        B[Lọc vé theo trạng thái: Đã dùng, Chưa dùng, Bị hủy]
    end

    subgraph HeThong [Hệ thống]
        direction TB
        Start((Bắt đầu)):::startNode
        C[Truy vấn dữ liệu vé điện tử của sự kiện]
        D[Áp dụng bộ lọc và sắp xếp]
        E[Hiển thị danh sách tổng quát lên màn hình]
        End(((Kết thúc))):::endNode
    end

    Start --> A
    A --> C
    B --> C
    C --> D
    D --> E
    E --> End
```

---

## UC38 – Soát vé điện tử (Check-in bằng QR)

```mermaid
flowchart TD
    classDef startNode fill:#000,stroke:#000,color:#fff;
    classDef endNode fill:#fff,stroke:#000,stroke-width:4px,color:#000;

    subgraph TacNhan [Nhân viên soát vé]
        direction TB
        A[Dùng máy ảnh điện thoại quét mã QR của khách]
    end

    subgraph HeThong [Hệ thống]
        direction TB
        Start((Bắt đầu)):::startNode
        B[Tiếp nhận đoạn mã từ máy ảnh]
        C{Tìm kiếm đoạn mã này trong dữ liệu}
        D[Thông báo lỗi mã QR không tồn tại]
        E{Kiểm tra quyền của nhân viên soát vé}
        F[Thông báo lỗi nhân viên không có quyền]
        G{Kiểm tra trạng thái sử dụng của vé}
        H[Thông báo lỗi vé đã được sử dụng]
        I[Thông báo lỗi vé đã bị hủy]
        J[Ghi nhận thời gian người dùng tham gia]
        K[Chuyển trạng thái vé thành Đã sử dụng]
        L[Lưu thông tin lịch sử soát vé]
        M[Hiển thị màu xanh và báo CỔNG MỞ thành công]
        End(((Kết thúc))):::endNode
    end

    Start --> A
    A --> B
    B --> C
    
    C -- Không tìm thấy --> D
    D --> End
    
    C -- Có tồn tại --> E
    E -- Không có quyền --> F
    F --> End
    
    E -- Được phép --> G
    G -- Đã sử dụng --> H
    H --> End
    
    G -- Bị hủy --> I
    I --> End
    
    G -- Chưa sử dụng --> J
    J --> K
    K --> L
    L --> M
    M --> End
```

---

## UC39 – Đồng bộ dữ liệu Check-in ngoại tuyến (Offline)

```mermaid
flowchart TD
    classDef startNode fill:#000,stroke:#000,color:#fff;
    classDef endNode fill:#fff,stroke:#000,stroke-width:4px,color:#000;

    subgraph TacNhan [Nhân viên soát vé]
        direction TB
        A[Kết nối lại mạng Internet]
        B[Nhấn nút Đồng bộ dữ liệu Check-in lên máy chủ]
    end

    subgraph HeThong [Hệ thống]
        direction TB
        Start((Bắt đầu)):::startNode
        C[Lấy danh sách các vé đã quét khi không có mạng]
        D[Gửi danh sách lên hệ thống máy chủ]
        E{Kiểm tra từng vé trong danh sách}
        F[Bỏ qua các vé đã được cập nhật hoặc không hợp lệ]
        G[Cập nhật trạng thái các vé thành Đã sử dụng]
        H[Lưu thông tin lịch sử soát vé tập thể]
        I[Thông báo hoàn tất đồng bộ dữ liệu]
        End(((Kết thúc))):::endNode
    end

    Start --> A
    A --> B
    B --> C
    C --> D
    D --> E
    E --> F
    F --> G
    G --> H
    H --> I
    I --> End
```

---

## UC40 – Tự động hủy đơn hàng chưa thanh toán quá hạn

```mermaid
flowchart TD
    classDef startNode fill:#000,stroke:#000,color:#fff;
    classDef endNode fill:#fff,stroke:#000,stroke-width:4px,color:#000;

    subgraph TacNhan [Máy chủ tự động]
        direction TB
        A[Kích hoạt kiểm tra định kỳ mỗi phút]
    end

    subgraph HeThong [Hệ thống]
        direction TB
        Start((Bắt đầu)):::startNode
        B[Tìm kiếm các đơn đăng ký đang chờ thanh toán]
        C{Đơn hàng đã treo quá 15 phút chưa?}
        D[Bỏ qua, tiếp tục chờ thanh toán]
        E[Cập nhật đơn hàng thành Đã hủy]
        F[Hủy bỏ thông tin giao dịch chờ]
        G[Hoàn trả lại chỗ trống cho sự kiện]
        End(((Kết thúc))):::endNode
    end

    Start --> A
    A --> B
    B --> C
    
    C -- Chưa quá hạn --> D
    D --> End
    
    C -- Đã quá 15 phút --> E
    E --> F
    F --> G
    G --> End
```
