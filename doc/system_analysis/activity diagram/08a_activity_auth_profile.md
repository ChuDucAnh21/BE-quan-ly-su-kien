# BIỂU ĐỒ HOẠT ĐỘNG - PHẦN A: XÁC THỰC & HỒ SƠ
**Hệ thống Quản lý Sự kiện & Check-in QR**

---

Tài liệu này chứa các Biểu đồ Hoạt động (Activity Diagrams) mô tả chi tiết từng bước thực hiện của các chức năng. Biểu đồ được chia thành 2 cột rõ ràng: **Tác nhân** (Người dùng thực hiện) và **Hệ thống** (Xử lý ngầm), sử dụng ngôn ngữ tự nhiên dễ hiểu.

---

## UC01 – Đăng ký tài khoản

```mermaid
flowchart TD
    classDef startNode fill:#000,stroke:#000,color:#fff;
    classDef endNode fill:#fff,stroke:#000,stroke-width:4px,color:#000;

    subgraph TacNhan [Người dùng]
        direction TB
        A[Điền thông tin đăng ký lên biểu mẫu]
        B[Nhấn nút Đăng ký]
    end

    subgraph HeThong [Hệ thống]
        direction TB
        Start((Bắt đầu)):::startNode
        C[Hiển thị biểu mẫu đăng ký]
        D{Kiểm tra tính hợp lệ của thông tin}
        E[Hiển thị thông báo lỗi nhập liệu]
        F{Kiểm tra email đã được sử dụng chưa}
        G[Hiển thị thông báo email đã tồn tại]
        H[Mã hóa mật khẩu bảo mật]
        I[Lưu thông tin tài khoản mới]
        J[Hiển thị thông báo đăng ký thành công]
        End(((Kết thúc))):::endNode
    end

    Start --> C
    C --> A
    A --> B
    B --> D
    D -- Thông tin sai --> E
    E --> A
    D -- Thông tin đúng --> F
    F -- Đã tồn tại --> G
    G --> A
    F -- Chưa tồn tại --> H
    H --> I
    I --> J
    J --> End
```

---

## UC02 – Đăng nhập hệ thống

```mermaid
flowchart TD
    classDef startNode fill:#000,stroke:#000,color:#fff;
    classDef endNode fill:#fff,stroke:#000,stroke-width:4px,color:#000;

    subgraph TacNhan [Người dùng]
        direction TB
        A[Nhập Email và Mật khẩu]
        B[Nhấn nút Đăng nhập]
    end

    subgraph HeThong [Hệ thống]
        direction TB
        Start((Bắt đầu)):::startNode
        C[Hiển thị màn hình đăng nhập]
        D{Kiểm tra email có tồn tại không}
        E[Thông báo sai thông tin đăng nhập]
        F{Kiểm tra mật khẩu có đúng không}
        G{Kiểm tra trạng thái tài khoản}
        H[Thông báo tài khoản đang bị khóa]
        I[Tạo phiên đăng nhập an toàn]
        J[Chuyển hướng vào trang chủ]
        End(((Kết thúc))):::endNode
    end

    Start --> C
    C --> A
    A --> B
    B --> D
    D -- Không tồn tại --> E
    E --> A
    
    D -- Tồn tại --> F
    F -- Không đúng --> E
    
    F -- Đúng --> G
    G -- Bị khóa --> H
    H --> End
    
    G -- Hoạt động --> I
    I --> J
    J --> End
```

---

## UC02a – Đăng nhập bằng Google

```mermaid
flowchart TD
    classDef startNode fill:#000,stroke:#000,color:#fff;
    classDef endNode fill:#fff,stroke:#000,stroke-width:4px,color:#000;

    subgraph TacNhan [Người dùng]
        direction TB
        A[Nhấn nút Đăng nhập với Google]
        B[Chọn tài khoản và cho phép truy cập]
    end

    subgraph HeThong [Hệ thống]
        direction TB
        Start((Bắt đầu)):::startNode
        C[Chuyển hướng sang trang xác thực Google]
        D{Nhận và kiểm tra mã xác thực từ Google}
        E[Hiển thị thông báo lỗi xác thực]
        F{Kiểm tra email này đã có trong hệ thống chưa}
        G[Tạo tài khoản mới tự động]
        H{Kiểm tra trạng thái tài khoản}
        I[Thông báo tài khoản đang bị khóa]
        J[Tạo phiên đăng nhập an toàn]
        K[Chuyển hướng vào trang chủ]
        End(((Kết thúc))):::endNode
    end

    Start --> A
    A --> C
    C --> B
    B --> D
    D -- Lỗi xác thực --> E
    E --> End
    
    D -- Thành công --> F
    F -- Chưa có --> G
    G --> J
    
    F -- Đã có --> H
    H -- Bị khóa --> I
    I --> End
    
    H -- Hoạt động --> J
    J --> K
    K --> End
```

---

## UC03 – Đăng xuất

```mermaid
flowchart TD
    classDef startNode fill:#000,stroke:#000,color:#fff;
    classDef endNode fill:#fff,stroke:#000,stroke-width:4px,color:#000;

    subgraph TacNhan [Người dùng]
        direction TB
        A[Nhấn nút Đăng xuất]
    end

    subgraph HeThong [Hệ thống]
        direction TB
        Start((Bắt đầu)):::startNode
        B[Xóa dữ liệu phiên đăng nhập]
        C[Hiển thị thông báo đăng xuất thành công]
        D[Chuyển hướng về màn hình đăng nhập]
        End(((Kết thúc))):::endNode
    end

    Start --> A
    A --> B
    B --> C
    C --> D
    D --> End
```

---

## UC04 – Quên mật khẩu

```mermaid
flowchart TD
    classDef startNode fill:#000,stroke:#000,color:#fff;
    classDef endNode fill:#fff,stroke:#000,stroke-width:4px,color:#000;

    subgraph TacNhan [Người dùng]
        direction TB
        A[Nhập email để khôi phục mật khẩu]
        B[Nhấn nút Gửi yêu cầu]
    end

    subgraph HeThong [Hệ thống]
        direction TB
        Start((Bắt đầu)):::startNode
        C[Hiển thị màn hình quên mật khẩu]
        D{Kiểm tra email có tồn tại không}
        E[Tạo mã khôi phục an toàn]
        F[Gửi email chứa đường dẫn khôi phục]
        G[Thông báo đã gửi email thành công]
        End(((Kết thúc))):::endNode
    end

    Start --> C
    C --> A
    A --> B
    B --> D
    
    D -- Không tồn tại --> G
    D -- Tồn tại --> E
    E --> F
    F --> G
    G --> End
```

---

## UC05 – Đặt lại mật khẩu mới

```mermaid
flowchart TD
    classDef startNode fill:#000,stroke:#000,color:#fff;
    classDef endNode fill:#fff,stroke:#000,stroke-width:4px,color:#000;

    subgraph TacNhan [Người dùng]
        direction TB
        A[Nhấn vào đường dẫn trong email]
        B[Nhập mật khẩu mới]
        C[Nhấn Xác nhận]
    end

    subgraph HeThong [Hệ thống]
        direction TB
        Start((Bắt đầu)):::startNode
        D{Kiểm tra tính hợp lệ và thời hạn của đường dẫn}
        E[Thông báo đường dẫn không hợp lệ hoặc hết hạn]
        F[Hiển thị màn hình đặt mật khẩu mới]
        G[Mã hóa mật khẩu mới]
        H[Cập nhật mật khẩu vào hệ thống]
        I[Xóa đường dẫn khôi phục đã sử dụng]
        J[Thông báo đổi mật khẩu thành công]
        End(((Kết thúc))):::endNode
    end

    Start --> A
    A --> D
    D -- Không hợp lệ --> E
    E --> End
    
    D -- Hợp lệ --> F
    F --> B
    B --> C
    C --> G
    G --> H
    H --> I
    I --> J
    J --> End
```

---

## UC06 – Đổi mật khẩu chủ động

```mermaid
flowchart TD
    classDef startNode fill:#000,stroke:#000,color:#fff;
    classDef endNode fill:#fff,stroke:#000,stroke-width:4px,color:#000;

    subgraph TacNhan [Người dùng]
        direction TB
        A[Nhập mật khẩu hiện tại và mật khẩu mới]
        B[Nhấn Lưu thay đổi]
    end

    subgraph HeThong [Hệ thống]
        direction TB
        Start((Bắt đầu)):::startNode
        C[Hiển thị màn hình đổi mật khẩu]
        D{Kiểm tra mật khẩu hiện tại có đúng không}
        E[Thông báo mật khẩu hiện tại sai]
        F[Mã hóa mật khẩu mới]
        G[Cập nhật mật khẩu vào hệ thống]
        H[Thông báo đổi mật khẩu thành công]
        End(((Kết thúc))):::endNode
    end

    Start --> C
    C --> A
    A --> B
    B --> D
    D -- Không đúng --> E
    E --> A
    
    D -- Đúng --> F
    F --> G
    G --> H
    H --> End
```

---

## UC07 – Xem thông tin cá nhân

```mermaid
flowchart TD
    classDef startNode fill:#000,stroke:#000,color:#fff;
    classDef endNode fill:#fff,stroke:#000,stroke-width:4px,color:#000;

    subgraph TacNhan [Người dùng]
        direction TB
        A[Truy cập trang Hồ sơ cá nhân]
    end

    subgraph HeThong [Hệ thống]
        direction TB
        Start((Bắt đầu)):::startNode
        B[Xác định người dùng qua phiên đăng nhập]
        C[Truy xuất thông tin cá nhân từ cơ sở dữ liệu]
        D[Hiển thị thông tin lên màn hình]
        End(((Kết thúc))):::endNode
    end

    Start --> A
    A --> B
    B --> C
    C --> D
    D --> End
```

---

## UC08 – Cập nhật thông tin cá nhân

```mermaid
flowchart TD
    classDef startNode fill:#000,stroke:#000,color:#fff;
    classDef endNode fill:#fff,stroke:#000,stroke-width:4px,color:#000;

    subgraph TacNhan [Người dùng]
        direction TB
        A[Chỉnh sửa thông tin trên biểu mẫu]
        B[Nhấn nút Cập nhật]
    end

    subgraph HeThong [Hệ thống]
        direction TB
        Start((Bắt đầu)):::startNode
        C[Hiển thị biểu mẫu thông tin hiện tại]
        D{Kiểm tra tính hợp lệ của thông tin mới}
        E[Hiển thị thông báo lỗi nhập liệu]
        F[Lưu thông tin mới vào cơ sở dữ liệu]
        G[Hiển thị thông báo cập nhật thành công]
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

## UC09 – Cập nhật ảnh đại diện

```mermaid
flowchart TD
    classDef startNode fill:#000,stroke:#000,color:#fff;
    classDef endNode fill:#fff,stroke:#000,stroke-width:4px,color:#000;

    subgraph TacNhan [Người dùng]
        direction TB
        A[Chọn tệp ảnh từ máy tính/điện thoại]
    end

    subgraph HeThong [Hệ thống]
        direction TB
        Start((Bắt đầu)):::startNode
        B[Hiển thị nút thay đổi ảnh đại diện]
        C{Kiểm tra định dạng và kích thước ảnh}
        D[Thông báo lỗi định dạng/kích thước]
        E[Tải ảnh lên máy chủ lưu trữ trực tuyến]
        F{Kiểm tra quá trình tải ảnh}
        G[Thông báo lỗi máy chủ tải ảnh]
        H[Nhận đường dẫn ảnh mới]
        I[Cập nhật đường dẫn ảnh vào tài khoản]
        J[Hiển thị ảnh mới lên màn hình]
        End(((Kết thúc))):::endNode
    end

    Start --> B
    B --> A
    A --> C
    C -- Không hợp lệ --> D
    D --> A
    
    C -- Hợp lệ --> E
    E --> F
    F -- Lỗi tải lên --> G
    G --> End
    
    F -- Thành công --> H
    H --> I
    I --> J
    J --> End
```

---

## UC10 – Yêu cầu khóa tài khoản

```mermaid
flowchart TD
    classDef startNode fill:#000,stroke:#000,color:#fff;
    classDef endNode fill:#fff,stroke:#000,stroke-width:4px,color:#000;

    subgraph TacNhan [Người dùng]
        direction TB
        A[Nhấn nút Xóa/Khóa tài khoản]
        B[Xác nhận quyết định khóa]
    end

    subgraph HeThong [Hệ thống]
        direction TB
        Start((Bắt đầu)):::startNode
        C[Hiển thị hộp thoại cảnh báo]
        D[Đóng hộp thoại]
        E[Cập nhật trạng thái tài khoản thành Đã khóa]
        F[Hủy phiên đăng nhập hiện tại]
        G[Chuyển hướng về trang chủ]
        End(((Kết thúc))):::endNode
    end

    Start --> A
    A --> C
    C --> B
    B -- Hủy thao tác --> D
    D --> End
    
    B -- Xác nhận --> E
    E --> F
    F --> G
    G --> End
```
