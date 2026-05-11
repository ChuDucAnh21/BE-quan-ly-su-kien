# BIỂU ĐỒ HOẠT ĐỘNG - PHẦN B: QUẢN TRỊ & SỰ KIỆN
**Hệ thống Quản lý Sự kiện & Check-in QR**

---

Tài liệu này chứa các Biểu đồ Hoạt động (Activity Diagrams) mô tả các chức năng dành cho Quản trị viên (Admin) và Ban tổ chức sự kiện (Organizer).

---

## UC11 – Xem danh sách người dùng

```mermaid
flowchart TD
    classDef startNode fill:#000,stroke:#000,color:#fff;
    classDef endNode fill:#fff,stroke:#000,stroke-width:4px,color:#000;

    subgraph TacNhan [Quản trị viên]
        direction TB
        A[Truy cập trang Quản lý người dùng]
        B[Nhập thông tin tìm kiếm hoặc chọn bộ lọc]
    end

    subgraph HeThong [Hệ thống]
        direction TB
        Start((Bắt đầu)):::startNode
        C{Kiểm tra quyền quản trị}
        D[Thông báo không có quyền truy cập]
        E[Tìm kiếm dữ liệu người dùng theo yêu cầu]
        F[Hiển thị danh sách người dùng]
        End(((Kết thúc))):::endNode
    end

    Start --> A
    A --> C
    B --> C
    
    C -- Không có quyền --> D
    D --> End
    
    C -- Được phép --> E
    E --> F
    F --> End
```

---

## UC12 – Khóa / Mở khóa tài khoản

```mermaid
flowchart TD
    classDef startNode fill:#000,stroke:#000,color:#fff;
    classDef endNode fill:#fff,stroke:#000,stroke-width:4px,color:#000;

    subgraph TacNhan [Quản trị viên]
        direction TB
        A[Chọn một người dùng cụ thể]
        B[Nhấn nút Khóa hoặc Mở khóa]
    end

    subgraph HeThong [Hệ thống]
        direction TB
        Start((Bắt đầu)):::startNode
        C[Hiển thị danh sách người dùng]
        D{Kiểm tra tài khoản có phải là chính quản trị viên không}
        E[Thông báo không thể tự khóa bản thân]
        F[Cập nhật trạng thái mới cho tài khoản]
        G[Thông báo thay đổi thành công]
        End(((Kết thúc))):::endNode
    end

    Start --> C
    C --> A
    A --> B
    B --> D
    D -- Là bản thân --> E
    E --> A
    
    D -- Không phải bản thân --> F
    F --> G
    G --> End
```

---

## UC13 – Phân quyền người dùng

```mermaid
flowchart TD
    classDef startNode fill:#000,stroke:#000,color:#fff;
    classDef endNode fill:#fff,stroke:#000,stroke-width:4px,color:#000;

    subgraph TacNhan [Quản trị viên]
        direction TB
        A[Chọn người dùng cần thay đổi quyền]
        B[Chọn vai trò mới và Nhấn Xác nhận]
    end

    subgraph HeThong [Hệ thống]
        direction TB
        Start((Bắt đầu)):::startNode
        C[Hiển thị danh sách người dùng]
        D{Kiểm tra quyền thao tác hợp lệ}
        E[Thông báo thao tác không hợp lệ]
        F[Cập nhật vai trò mới vào hệ thống]
        G[Thông báo phân quyền thành công]
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

## UC13a – Xem thống kê tổng quan (Dashboard)

```mermaid
flowchart TD
    classDef startNode fill:#000,stroke:#000,color:#fff;
    classDef endNode fill:#fff,stroke:#000,stroke-width:4px,color:#000;

    subgraph TacNhan [Quản trị viên / Ban tổ chức]
        direction TB
        A[Truy cập trang Bảng điều khiển]
    end

    subgraph HeThong [Hệ thống]
        direction TB
        Start((Bắt đầu)):::startNode
        B{Kiểm tra quyền hạn}
        C[Thông báo không có quyền truy cập]
        D[Tổng hợp dữ liệu: Số người dùng, doanh thu, sự kiện...]
        E[Hiển thị biểu đồ và số liệu thống kê]
        End(((Kết thúc))):::endNode
    end

    Start --> A
    A --> B
    
    B -- Không có quyền --> C
    C --> End
    
    B -- Được phép --> D
    D --> E
    E --> End
```

---

## UC13b – Xem thống kê của một sự kiện

```mermaid
flowchart TD
    classDef startNode fill:#000,stroke:#000,color:#fff;
    classDef endNode fill:#fff,stroke:#000,stroke-width:4px,color:#000;

    subgraph TacNhan [Ban tổ chức]
        direction TB
        A[Chọn xem thống kê của một sự kiện cụ thể]
    end

    subgraph HeThong [Hệ thống]
        direction TB
        Start((Bắt đầu)):::startNode
        B{Kiểm tra quyền sở hữu sự kiện}
        C[Thông báo không có quyền]
        D[Tổng hợp dữ liệu vé bán, check-in, doanh thu của sự kiện]
        E[Hiển thị biểu đồ chi tiết sự kiện]
        End(((Kết thúc))):::endNode
    end

    Start --> A
    A --> B
    
    B -- Không có quyền --> C
    C --> End
    
    B -- Là chủ sự kiện --> D
    D --> E
    E --> End
```

---

## UC14 – Xem danh sách sự kiện

```mermaid
flowchart TD
    classDef startNode fill:#000,stroke:#000,color:#fff;
    classDef endNode fill:#fff,stroke:#000,stroke-width:4px,color:#000;

    subgraph TacNhan [Người dùng / Khách]
        direction TB
        A[Truy cập trang Danh sách sự kiện]
        B[Nhập từ khóa tìm kiếm hoặc lọc chuyên mục]
    end

    subgraph HeThong [Hệ thống]
        direction TB
        Start((Bắt đầu)):::startNode
        C[Lọc các sự kiện đang được công khai]
        D[Hiển thị danh sách sự kiện]
        End(((Kết thúc))):::endNode
    end

    Start --> A
    A --> C
    B --> C
    C --> D
    D --> End
```

---

## UC15 – Xem chi tiết sự kiện

```mermaid
flowchart TD
    classDef startNode fill:#000,stroke:#000,color:#fff;
    classDef endNode fill:#fff,stroke:#000,stroke-width:4px,color:#000;

    subgraph TacNhan [Người dùng / Khách]
        direction TB
        A[Nhấn chọn một sự kiện trong danh sách]
    end

    subgraph HeThong [Hệ thống]
        direction TB
        Start((Bắt đầu)):::startNode
        B{Kiểm tra sự kiện có tồn tại và đang mở không}
        C[Thông báo sự kiện không tồn tại hoặc đã ẩn]
        D[Tập hợp thông tin chi tiết và lịch trình của sự kiện]
        E[Hiển thị trang chi tiết sự kiện]
        End(((Kết thúc))):::endNode
    end

    Start --> A
    A --> B
    B -- Đã ẩn/Xóa --> C
    C --> End
    
    B -- Hợp lệ --> D
    D --> E
    E --> End
```

---

## UC16 – Tạo sự kiện mới

```mermaid
flowchart TD
    classDef startNode fill:#000,stroke:#000,color:#fff;
    classDef endNode fill:#fff,stroke:#000,stroke-width:4px,color:#000;

    subgraph TacNhan [Ban tổ chức]
        direction TB
        A[Điền thông tin sự kiện lên biểu mẫu]
        B[Tải lên ảnh bìa cho sự kiện]
        C[Nhấn nút Tạo sự kiện]
    end

    subgraph HeThong [Hệ thống]
        direction TB
        Start((Bắt đầu)):::startNode
        D[Hiển thị biểu mẫu tạo sự kiện]
        E{Kiểm tra tính hợp lệ của thời gian và thông tin}
        F[Hiển thị lỗi nhập liệu]
        G{Kiểm tra có ảnh đính kèm không}
        H[Tải ảnh lên máy chủ lưu trữ trực tuyến]
        I[Lưu thông tin sự kiện ở trạng thái Nháp]
        J[Thông báo tạo sự kiện thành công]
        End(((Kết thúc))):::endNode
    end

    Start --> D
    D --> A
    A --> B
    B --> C
    C --> E
    
    E -- Lỗi thông tin --> F
    F --> A
    
    E -- Hợp lệ --> G
    G -- Có ảnh --> H
    H --> I
    G -- Không có ảnh --> I
    I --> J
    J --> End
```

---

## UC17 – Cập nhật thông tin sự kiện

```mermaid
flowchart TD
    classDef startNode fill:#000,stroke:#000,color:#fff;
    classDef endNode fill:#fff,stroke:#000,stroke-width:4px,color:#000;

    subgraph TacNhan [Ban tổ chức]
        direction TB
        A[Chỉnh sửa thông tin trên biểu mẫu]
        B[Nhấn nút Cập nhật sự kiện]
    end

    subgraph HeThong [Hệ thống]
        direction TB
        Start((Bắt đầu)):::startNode
        C[Hiển thị thông tin sự kiện hiện tại]
        D{Kiểm tra tính hợp lệ của thông tin thay đổi}
        E[Hiển thị lỗi nhập liệu]
        F[Cập nhật thông tin mới vào hệ thống]
        G[Thông báo cập nhật thành công]
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

## UC18 – Thay đổi trạng thái sự kiện

```mermaid
flowchart TD
    classDef startNode fill:#000,stroke:#000,color:#fff;
    classDef endNode fill:#fff,stroke:#000,stroke-width:4px,color:#000;

    subgraph TacNhan [Ban tổ chức]
        direction TB
        A[Chọn trạng thái mới: Đăng tải / Dừng / Kết thúc]
        B[Xác nhận thay đổi]
    end

    subgraph HeThong [Hệ thống]
        direction TB
        Start((Bắt đầu)):::startNode
        C[Hiển thị trạng thái hiện hành]
        D{Kiểm tra điều kiện mở bán vé có phí}
        E[Thông báo yêu cầu thiết lập thanh toán trước khi đăng tải]
        F[Lưu trạng thái mới vào hệ thống]
        G[Thông báo thay đổi thành công]
        End(((Kết thúc))):::endNode
    end

    Start --> C
    C --> A
    A --> B
    B --> D
    
    D -- Thiếu thiết lập thanh toán --> E
    E --> End
    
    D -- Hợp lệ --> F
    F --> G
    G --> End
```

---

## UC19 – Xóa sự kiện

```mermaid
flowchart TD
    classDef startNode fill:#000,stroke:#000,color:#fff;
    classDef endNode fill:#fff,stroke:#000,stroke-width:4px,color:#000;

    subgraph TacNhan [Ban tổ chức / Quản trị viên]
        direction TB
        A[Nhấn nút Xóa sự kiện]
        B[Xác nhận đồng ý xóa]
    end

    subgraph HeThong [Hệ thống]
        direction TB
        Start((Bắt đầu)):::startNode
        C[Hiển thị hộp thoại cảnh báo]
        D[Đóng hộp thoại]
        E{Kiểm tra sự kiện đã có người đăng ký chưa}
        F[Xóa hoàn toàn sự kiện khỏi dữ liệu]
        G[Chuyển trạng thái sự kiện thành Đã hủy]
        H[Thông báo thao tác thành công]
        End(((Kết thúc))):::endNode
    end

    Start --> A
    A --> C
    C --> B
    B -- Hủy bỏ --> D
    D --> End
    
    B -- Xác nhận --> E
    E -- Chưa ai đăng ký --> F
    E -- Đã có người đăng ký --> G
    
    F --> H
    G --> H
    H --> End
```
