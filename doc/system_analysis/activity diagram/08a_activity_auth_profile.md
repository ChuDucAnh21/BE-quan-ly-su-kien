# BIỂU ĐỒ HOẠT ĐỘNG - PHẦN A: NỀN TẢNG HỆ THỐNG (UC01 – UC08)

---

## UC01 – Đăng ký tài khoản

```mermaid
%%{init: {"flowchart": {"curve": "linear"}}}%%
flowchart TD
    classDef default fill:#fff,stroke:#333,stroke-width:1.5px,color:#000,rx:10px,ry:10px;
    classDef startNode fill:#000,stroke:#000,color:#fff;
    classDef endNode fill:#fff,stroke:#000,stroke-width:4px,color:#000;
    classDef hidden fill:none,stroke:none,color:none;

    subgraph Guest [Guest]
        direction TB
        T_start(" "):::hidden
        A(Điền thông tin và nhấn Đăng ký)
        T_end(" "):::hidden
    end

    subgraph System [Hệ thống]
        direction TB
        H_start(" "):::hidden
        Start((Bắt đầu)):::startNode
        B{Định dạng hợp lệ?}
        C{Email đã tồn tại?}
        D(Thông báo lỗi)
        E(Mã hóa mật khẩu và Lưu tài khoản)
        F(Thông báo thành công và chuyển Đăng nhập)
        End(((Kết thúc))):::endNode
        H_end(" "):::hidden
    end

    T_start ~~~ H_start
    T_end ~~~ H_end
    Start --> A
    A --> B
    B -- Không --> D
    B -- Có --> C
    C -- Có --> D
    C -- Không --> E
    E --> F
    F --> End
    D --> End

    style Guest fill:#fff,stroke:#333,stroke-width:2px
    style System fill:#fff,stroke:#333,stroke-width:2px
```

---

## UC02 – Đăng nhập hệ thống

```mermaid
%%{init: {"flowchart": {"curve": "linear"}}}%%
flowchart TD
    classDef default fill:#fff,stroke:#333,stroke-width:1.5px,color:#000,rx:10px,ry:10px;
    classDef startNode fill:#000,stroke:#000,color:#fff;
    classDef endNode fill:#fff,stroke:#000,stroke-width:4px,color:#000;
    classDef hidden fill:none,stroke:none,color:none;

    subgraph Guest [Guest]
        direction TB
        T_start(" "):::hidden
        A{Chọn phương thức?}
        B(Nhập Email, Mật khẩu và Đăng nhập)
        C(Chọn Đăng nhập qua Google)
        T_end(" "):::hidden
    end

    subgraph System [Hệ thống]
        direction TB
        H_start(" "):::hidden
        Start((Bắt đầu)):::startNode
        D{Mật khẩu đúng?}
        E{Tài khoản bị khóa?}
        F(Nhận thông tin Google)
        G{Email đã có chưa?}
        H(Tạo tài khoản mới)
        I(Khởi tạo phiên làm việc và chuyển Trang chủ)
        J(Báo lỗi đăng nhập)
        End(((Kết thúc))):::endNode
        H_end(" "):::hidden
    end

    T_start ~~~ H_start
    T_end ~~~ H_end
    Start --> A
    A -- Email --> B
    B --> D
    D -- Không --> J
    D -- Có --> E
    E -- Có --> J
    E -- Không --> I
    A -- Google --> C
    C --> F
    F --> G
    G -- Chưa có --> H
    H --> I
    G -- Đã có --> E
    I --> End
    J --> End

    style Guest fill:#fff,stroke:#333,stroke-width:2px
    style System fill:#fff,stroke:#333,stroke-width:2px
```

---

## UC03 – Đăng xuất

```mermaid
%%{init: {"flowchart": {"curve": "linear"}}}%%
flowchart TD
    classDef default fill:#fff,stroke:#333,stroke-width:1.5px,color:#000,rx:10px,ry:10px;
    classDef startNode fill:#000,stroke:#000,color:#fff;
    classDef endNode fill:#fff,stroke:#000,stroke-width:4px,color:#000;
    classDef hidden fill:none,stroke:none,color:none;

    subgraph Actor [Actor đang đăng nhập]
        direction TB
        T_start(" "):::hidden
        A(Nhấn nút Đăng xuất)
        T_end(" "):::hidden
    end

    subgraph System [Hệ thống]
        direction TB
        H_start(" "):::hidden
        Start((Bắt đầu)):::startNode
        B(Xóa phiên làm việc hiện tại)
        C(Xóa Cookie trên trình duyệt)
        D(Chuyển hướng về trang Đăng nhập)
        End(((Kết thúc))):::endNode
        H_end(" "):::hidden
    end

    T_start ~~~ H_start
    T_end ~~~ H_end
    Start --> A
    A --> B
    B --> C
    C --> D
    D --> End

    style Actor fill:#fff,stroke:#333,stroke-width:2px
    style System fill:#fff,stroke:#333,stroke-width:2px
```

---

## UC04 – Quên mật khẩu

```mermaid
%%{init: {"flowchart": {"curve": "linear"}}}%%
flowchart TD
    classDef default fill:#fff,stroke:#333,stroke-width:1.5px,color:#000,rx:10px,ry:10px;
    classDef startNode fill:#000,stroke:#000,color:#fff;
    classDef endNode fill:#fff,stroke:#000,stroke-width:4px,color:#000;
    classDef hidden fill:none,stroke:none,color:none;

    subgraph Guest [Guest]
        direction TB
        T_start(" "):::hidden
        A(Nhập Email yêu cầu khôi phục)
        B(Nhập mã OTP và Mật khẩu mới)
        T_end(" "):::hidden
    end

    subgraph System [Hệ thống]
        direction TB
        H_start(" "):::hidden
        Start((Bắt đầu)):::startNode
        C(Gửi mã OTP qua Email)
        D{Mã OTP hợp lệ và còn hạn?}
        E(Thông báo lỗi mã OTP)
        F(Mã hóa và cập nhật mật khẩu)
        End(((Kết thúc))):::endNode
        H_end(" "):::hidden
    end

    T_start ~~~ H_start
    T_end ~~~ H_end
    Start --> A
    A --> C
    C --> B
    B --> D
    D -- Không --> E
    D -- Có --> F
    F --> End
    E --> End

    style Guest fill:#fff,stroke:#333,stroke-width:2px
    style System fill:#fff,stroke:#333,stroke-width:2px
```

---

## UC05 – Đổi mật khẩu

```mermaid
%%{init: {"flowchart": {"curve": "linear"}}}%%
flowchart TD
    classDef default fill:#fff,stroke:#333,stroke-width:1.5px,color:#000,rx:10px,ry:10px;
    classDef startNode fill:#000,stroke:#000,color:#fff;
    classDef endNode fill:#fff,stroke:#000,stroke-width:4px,color:#000;
    classDef hidden fill:none,stroke:none,color:none;

    subgraph Actor [Actor đang đăng nhập]
        direction TB
        T_start(" "):::hidden
        A(Nhập mật khẩu cũ và mới)
        T_end(" "):::hidden
    end

    subgraph System [Hệ thống]
        direction TB
        H_start(" "):::hidden
        Start((Bắt đầu)):::startNode
        B{Mật khẩu cũ đúng?}
        C(Thông báo sai mật khẩu)
        D(Cập nhật và lưu mật khẩu mới)
        End(((Kết thúc))):::endNode
        H_end(" "):::hidden
    end

    T_start ~~~ H_start
    T_end ~~~ H_end
    Start --> A
    A --> B
    B -- Không --> C
    B -- Có --> D
    D --> End
    C --> End

    style Actor fill:#fff,stroke:#333,stroke-width:2px
    style System fill:#fff,stroke:#333,stroke-width:2px
```

---

## UC06 – Cập nhật hồ sơ cá nhân

```mermaid
%%{init: {"flowchart": {"curve": "linear"}}}%%
flowchart TD
    classDef default fill:#fff,stroke:#333,stroke-width:1.5px,color:#000,rx:10px,ry:10px;
    classDef startNode fill:#000,stroke:#000,color:#fff;
    classDef endNode fill:#fff,stroke:#000,stroke-width:4px,color:#000;
    classDef hidden fill:none,stroke:none,color:none;

    subgraph Actor [Actor đang đăng nhập]
        direction TB
        T_start(" "):::hidden
        A(Đổi thông tin hoặc chọn ảnh mới)
        B(Nhấn Lưu)
        T_end(" "):::hidden
    end

    subgraph System [Hệ thống]
        direction TB
        H_start(" "):::hidden
        Start((Bắt đầu)):::startNode
        C{Có ảnh mới không?}
        D(Đẩy ảnh lên máy chủ)
        E(Lưu thông tin vào CSDL)
        F(Báo thành công)
        End(((Kết thúc))):::endNode
        H_end(" "):::hidden
    end

    T_start ~~~ H_start
    T_end ~~~ H_end
    Start --> A
    A --> B
    B --> C
    C -- Có --> D
    D --> E
    C -- Không --> E
    E --> F
    F --> End

    style Actor fill:#fff,stroke:#333,stroke-width:2px
    style System fill:#fff,stroke:#333,stroke-width:2px
```

---

## UC07 – Quản lý tài khoản người dùng

```mermaid
%%{init: {"flowchart": {"curve": "linear"}}}%%
flowchart TD
    classDef default fill:#fff,stroke:#333,stroke-width:1.5px,color:#000,rx:10px,ry:10px;
    classDef startNode fill:#000,stroke:#000,color:#fff;
    classDef endNode fill:#fff,stroke:#000,stroke-width:4px,color:#000;
    classDef hidden fill:none,stroke:none,color:none;

    subgraph Admin [Admin]
        direction TB
        T_start(" "):::hidden
        A(Chọn tài khoản và chỉ định thao tác)
        T_end(" "):::hidden
    end

    subgraph System [Hệ thống]
        direction TB
        H_start(" "):::hidden
        Start((Bắt đầu)):::startNode
        B{Thao tác lên chính mình?}
        C(Từ chối thao tác)
        D(Lưu trạng thái/quyền mới)
        E(Hiển thị thông báo thành công)
        End(((Kết thúc))):::endNode
        H_end(" "):::hidden
    end

    T_start ~~~ H_start
    T_end ~~~ H_end
    Start --> A
    A --> B
    B -- Có --> C
    B -- Không --> D
    D --> E
    E --> End
    C --> End

    style Admin fill:#fff,stroke:#333,stroke-width:2px
    style System fill:#fff,stroke:#333,stroke-width:2px
```

---

## UC08 – Xem báo cáo thống kê tổng quan (Dashboard)

```mermaid
%%{init: {"flowchart": {"curve": "linear"}}}%%
flowchart TD
    classDef default fill:#fff,stroke:#333,stroke-width:1.5px,color:#000,rx:10px,ry:10px;
    classDef startNode fill:#000,stroke:#000,color:#fff;
    classDef endNode fill:#fff,stroke:#000,stroke-width:4px,color:#000;
    classDef hidden fill:none,stroke:none,color:none;

    subgraph Actor [Admin / Organizer]
        direction TB
        T_start(" "):::hidden
        A(Truy cập trang Dashboard)
        T_end(" "):::hidden
    end

    subgraph System [Hệ thống]
        direction TB
        H_start(" "):::hidden
        Start((Bắt đầu)):::startNode
        B{Role là gì?}
        C(Tổng hợp dữ liệu nền tảng)
        D(Tổng hợp dữ liệu sự kiện cá nhân)
        E(Hiển thị biểu đồ trực quan)
        End(((Kết thúc))):::endNode
        H_end(" "):::hidden
    end

    T_start ~~~ H_start
    T_end ~~~ H_end
    Start --> A
    A --> B
    B -- Admin --> C
    B -- Organizer --> D
    C --> E
    D --> E
    E --> End

    style Actor fill:#fff,stroke:#333,stroke-width:2px
    style System fill:#fff,stroke:#333,stroke-width:2px
```
