# BIỂU ĐỒ HOẠT ĐỘNG - PHẦN A: XÁC THỰC & HỒ SƠ CÁ NHÂN

**Hệ thống Quản lý Sự kiện & Check-in QR**

---

Tài liệu này chứa các Biểu đồ Hoạt động (Activity Diagrams) mô tả chi tiết từng bước thực hiện của các chức năng. Biểu đồ được chia thành 2 cột rõ ràng: **Tác nhân** (Người dùng thực hiện) và **Hệ thống** (Xử lý ngầm), sử dụng ngôn ngữ tự nhiên dễ hiểu.

---

## UC01 – Đăng ký tài khoản

```mermaid
%%{init: {"flowchart": {"curve": "linear"}}}%%
flowchart TD
    classDef default fill:#fff,stroke:#333,stroke-width:1.5px,color:#000,rx:10px,ry:10px;
    classDef startNode fill:#000,stroke:#000,color:#fff;
    classDef endNode fill:#fff,stroke:#000,stroke-width:4px,color:#000;
    classDef hidden fill:none,stroke:none,color:none;

    subgraph TacNhan [Người dùng]
        direction TB
        T_start(" "):::hidden
        A(Điền thông tin đăng ký lên biểu mẫu)
        B(Nhấn nút Đăng ký)
        T_end(" "):::hidden
    end

    subgraph HeThong [Hệ thống]
        direction TB
        H_start(" "):::hidden
        Start((Bắt đầu)):::startNode
        C(Hiển thị biểu mẫu đăng ký)
        D{Kiểm tra tính hợp lệ của thông tin}
        E(Hiển thị thông báo lỗi nhập liệu)
        F{Kiểm tra email đã được sử dụng chưa}
        G(Hiển thị thông báo email đã tồn tại)
        H(Mã hóa mật khẩu bảo mật)
        I(Lưu thông tin tài khoản mới)
        J(Hiển thị thông báo đăng ký thành công)
        End(((Kết thúc))):::endNode
        H_end(" "):::hidden
    end

    T_start ~~~ H_start
    T_end ~~~ H_end

    T_start ~~~ A
    B ~~~ T_end

    H_start ~~~ Start
    End ~~~ H_end

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

    style TacNhan fill:#fff,stroke:#333,stroke-width:2px
    style HeThong fill:#fff,stroke:#333,stroke-width:2px
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

    subgraph TacNhan [Người dùng]
        direction TB
        T_start(" "):::hidden
        A(Nhập Email và Mật khẩu)
        B(Nhấn nút Đăng nhập)
        T_end(" "):::hidden
    end

    subgraph HeThong [Hệ thống]
        direction TB
        H_start(" "):::hidden
        Start((Bắt đầu)):::startNode
        C(Hiển thị màn hình đăng nhập)
        D{Kiểm tra email có tồn tại không}
        E(Thông báo sai thông tin đăng nhập)
        F{Kiểm tra mật khẩu có đúng không}
        G{Kiểm tra trạng thái tài khoản}
        H(Thông báo tài khoản đang bị khóa)
        I(Tạo phiên đăng nhập an toàn)
        J(Chuyển hướng vào trang chủ)
        End(((Kết thúc))):::endNode
        H_end(" "):::hidden
    end

    T_start ~~~ H_start
    T_end ~~~ H_end

    T_start ~~~ A
    B ~~~ T_end

    H_start ~~~ Start
    End ~~~ H_end

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

    G -- Hoạt hoạt động --> I
    I --> J
    J --> End

    style TacNhan fill:#fff,stroke:#333,stroke-width:2px
    style HeThong fill:#fff,stroke:#333,stroke-width:2px
```

---

## UC03 – Đăng nhập bằng Google

```mermaid
%%{init: {"flowchart": {"curve": "linear"}}}%%
flowchart TD
    classDef default fill:#fff,stroke:#333,stroke-width:1.5px,color:#000,rx:10px,ry:10px;
    classDef startNode fill:#000,stroke:#000,color:#fff;
    classDef endNode fill:#fff,stroke:#000,stroke-width:4px,color:#000;
    classDef hidden fill:none,stroke:none,color:none;

    subgraph TacNhan [Người dùng]
        direction TB
        T_start(" "):::hidden
        A(Nhấn nút Đăng nhập với Google)
        B(Chọn tài khoản và cho phép truy cập)
        T_end(" "):::hidden
    end

    subgraph HeThong [Hệ thống]
        direction TB
        H_start(" "):::hidden
        Start((Bắt đầu)):::startNode
        C(Chuyển hướng sang trang xác thực Google)
        D{Nhận và kiểm tra mã xác thực}
        E(Hiển thị thông báo lỗi xác thực)
        F{Kiểm tra email này đã có chưa}
        G(Tạo tài khoản mới tự động)
        H{Kiểm tra trạng thái tài khoản}
        I(Thông báo tài khoản đang bị khóa)
        J(Tạo phiên đăng nhập an toàn)
        K(Chuyển hướng vào trang chủ)
        End(((Kết thúc))):::endNode
        H_end(" "):::hidden
    end

    T_start ~~~ H_start
    T_end ~~~ H_end

    T_start ~~~ A
    B ~~~ T_end

    H_start ~~~ Start
    End ~~~ H_end

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

    style TacNhan fill:#fff,stroke:#333,stroke-width:2px
    style HeThong fill:#fff,stroke:#333,stroke-width:2px
```

---

## UC04 – Đăng xuất

```mermaid
%%{init: {"flowchart": {"curve": "linear"}}}%%
flowchart TD
    classDef default fill:#fff,stroke:#333,stroke-width:1.5px,color:#000,rx:10px,ry:10px;
    classDef startNode fill:#000,stroke:#000,color:#fff;
    classDef endNode fill:#fff,stroke:#000,stroke-width:4px,color:#000;
    classDef hidden fill:none,stroke:none,color:none;

    subgraph TacNhan [Người dùng]
        direction TB
        T_start(" "):::hidden
        A(Nhấn nút Đăng xuất)
        T_end(" "):::hidden
    end

    subgraph HeThong [Hệ thống]
        direction TB
        H_start(" "):::hidden
        Start((Bắt đầu)):::startNode
        B(Xóa dữ liệu phiên đăng nhập)
        C(Hiển thị thông báo đăng xuất thành công)
        D(Chuyển hướng về màn hình đăng nhập)
        End(((Kết thúc))):::endNode
        H_end(" "):::hidden
    end

    T_start ~~~ H_start
    T_end ~~~ H_end

    T_start ~~~ A
    A ~~~ T_end

    H_start ~~~ Start
    End ~~~ H_end

    Start --> A
    A --> B
    B --> C
    C --> D
    D --> End

    style TacNhan fill:#fff,stroke:#333,stroke-width:2px
    style HeThong fill:#fff,stroke:#333,stroke-width:2px
```

---

## UC05 – Quên mật khẩu & Đặt lại mật khẩu

```mermaid
%%{init: {"flowchart": {"curve": "linear"}}}%%
flowchart TD
    classDef default fill:#fff,stroke:#333,stroke-width:1.5px,color:#000,rx:10px,ry:10px;
    classDef startNode fill:#000,stroke:#000,color:#fff;
    classDef endNode fill:#fff,stroke:#000,stroke-width:4px,color:#000;
    classDef hidden fill:none,stroke:none,color:none;

    subgraph TacNhan [Người dùng]
        direction TB
        T_start(" "):::hidden
        A(Nhập email để yêu cầu khôi phục mật khẩu)
        B(Nhận email, nhập mã khôi phục và mật khẩu mới)
        C(Nhấn Xác nhận)
        T_end(" "):::hidden
    end

    subgraph HeThong [Hệ thống]
        direction TB
        H_start(" "):::hidden
        Start((Bắt đầu)):::startNode
        D(Tạo và lưu mã khôi phục, gửi email cho người dùng)
        E{Kiểm tra mã khôi phục hợp lệ và chưa hết hạn?}
        F(Thông báo mã lỗi hoặc hết hạn)
        G(Mã hóa mật khẩu mới và cập nhật vào hệ thống)
        H(Xóa mã khôi phục và thông báo thành công)
        End(((Kết thúc))):::endNode
        H_end(" "):::hidden
    end

    T_start ~~~ H_start
    T_end ~~~ H_end

    T_start ~~~ A
    C ~~~ T_end

    H_start ~~~ Start
    End ~~~ H_end

    Start --> A
    A --> D
    D --> B
    B --> C
    C --> E
    E -- Không hợp lệ --> F
    F --> B
    E -- Hợp lệ --> G
    G --> H
    H --> End

    style TacNhan fill:#fff,stroke:#333,stroke-width:2px
    style HeThong fill:#fff,stroke:#333,stroke-width:2px
```

---

## UC06 – Đổi mật khẩu

```mermaid
%%{init: {"flowchart": {"curve": "linear"}}}%%
flowchart TD
    classDef default fill:#fff,stroke:#333,stroke-width:1.5px,color:#000,rx:10px,ry:10px;
    classDef startNode fill:#000,stroke:#000,color:#fff;
    classDef endNode fill:#fff,stroke:#000,stroke-width:4px,color:#000;
    classDef hidden fill:none,stroke:none,color:none;

    subgraph TacNhan [Người dùng]
        direction TB
        T_start(" "):::hidden
        A(Nhập mật khẩu hiện tại và mật khẩu mới)
        B(Nhấn Lưu thay đổi)
        T_end(" "):::hidden
    end

    subgraph HeThong [Hệ thống]
        direction TB
        H_start(" "):::hidden
        Start((Bắt đầu)):::startNode
        C(Hiển thị màn hình đổi mật khẩu)
        D{Kiểm tra mật khẩu hiện tại có đúng không}
        E(Thông báo mật khẩu hiện tại sai)
        F(Mã hóa mật khẩu mới)
        G(Cập nhật mật khẩu vào hệ thống)
        H(Thông báo đổi mật khẩu thành công)
        End(((Kết thúc))):::endNode
        H_end(" "):::hidden
    end

    T_start ~~~ H_start
    T_end ~~~ H_end

    T_start ~~~ A
    B ~~~ T_end

    H_start ~~~ Start
    End ~~~ H_end

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

    style TacNhan fill:#fff,stroke:#333,stroke-width:2px
    style HeThong fill:#fff,stroke:#333,stroke-width:2px
```

---

## UC07 – Xem thông tin cá nhân & Hồ sơ người dùng khác

```mermaid
%%{init: {"flowchart": {"curve": "linear"}}}%%
flowchart TD
    classDef default fill:#fff,stroke:#333,stroke-width:1.5px,color:#000,rx:10px,ry:10px;
    classDef startNode fill:#000,stroke:#000,color:#fff;
    classDef endNode fill:#fff,stroke:#000,stroke-width:4px,color:#000;
    classDef hidden fill:none,stroke:none,color:none;

    subgraph TacNhan [Người dùng]
        direction TB
        T_start(" "):::hidden
        A(Truy cập trang Hồ sơ cá nhân hoặc người khác)
        T_end(" "):::hidden
    end

    subgraph HeThong [Hệ thống]
        direction TB
        H_start(" "):::hidden
        Start((Bắt đầu)):::startNode
        B(Xác định yêu cầu xem hồ sơ cá nhân hay hồ sơ công khai)
        C{Hồ sơ của ai?}
        D(Lấy toàn bộ thông tin chi tiết từ CSDL)
        E(Lấy thông tin và ẩn các dữ liệu nhạy cảm)
        F(Hiển thị thông tin lên màn hình)
        End(((Kết thúc))):::endNode
        H_end(" "):::hidden
    end

    T_start ~~~ H_start
    T_end ~~~ H_end

    T_start ~~~ A
    A ~~~ T_end

    H_start ~~~ Start
    End ~~~ H_end

    Start --> A
    A --> B
    B --> C
    C -- Cá nhân --> D
    C -- Người khác --> E
    D --> F
    E --> F
    F --> End

    style TacNhan fill:#fff,stroke:#333,stroke-width:2px
    style HeThong fill:#fff,stroke:#333,stroke-width:2px
```

---

## UC08 – Cập nhật thông tin cá nhân & Ảnh đại diện

```mermaid
%%{init: {"flowchart": {"curve": "linear"}}}%%
flowchart TD
    classDef default fill:#fff,stroke:#333,stroke-width:1.5px,color:#000,rx:10px,ry:10px;
    classDef startNode fill:#000,stroke:#000,color:#fff;
    classDef endNode fill:#fff,stroke:#000,stroke-width:4px,color:#000;
    classDef hidden fill:none,stroke:none,color:none;

    subgraph TacNhan [Người dùng]
        direction TB
        T_start(" "):::hidden
        A(Chỉnh sửa thông tin văn bản hoặc chọn tải lên ảnh mới)
        B(Nhấn Cập nhật / Lưu)
        T_end(" "):::hidden
    end

    subgraph HeThong [Hệ thống]
        direction TB
        H_start(" "):::hidden
        Start((Bắt đầu)):::startNode
        C{Loại dữ liệu cần cập nhật?}
        D(Kiểm tra tính hợp lệ của thông tin văn bản)
        E(Tải ảnh lên Cloudinary và lấy URL)
        F{Có lỗi xảy ra không?}
        G(Thông báo lỗi nhập liệu hoặc lỗi tải ảnh)
        H(Lưu thông tin/URL mới vào CSDL)
        I(Hiển thị thông tin đã cập nhật)
        End(((Kết thúc))):::endNode
        H_end(" "):::hidden
    end

    T_start ~~~ H_start
    T_end ~~~ H_end

    T_start ~~~ A
    B ~~~ T_end

    H_start ~~~ Start
    End ~~~ H_end

    Start --> A
    A --> B
    B --> C
    C -- Thông tin --> D
    C -- Ảnh đại diện --> E
    D --> F
    E --> F
    F -- Có lỗi --> G
    G --> A
    F -- Hợp lệ --> H
    H --> I
    I --> End

    style TacNhan fill:#fff,stroke:#333,stroke-width:2px
    style HeThong fill:#fff,stroke:#333,stroke-width:2px
```

---

## UC09 – Tự khóa / xóa tài khoản (Soft Delete)

```mermaid
%%{init: {"flowchart": {"curve": "linear"}}}%%
flowchart TD
    classDef default fill:#fff,stroke:#333,stroke-width:1.5px,color:#000,rx:10px,ry:10px;
    classDef startNode fill:#000,stroke:#000,color:#fff;
    classDef endNode fill:#fff,stroke:#000,stroke-width:4px,color:#000;
    classDef hidden fill:none,stroke:none,color:none;

    subgraph TacNhan [Người dùng]
        direction TB
        T_start(" "):::hidden
        A(Nhấn nút Khóa tài khoản)
        B(Xác nhận quyết định khóa)
        T_end(" "):::hidden
    end

    subgraph HeThong [Hệ thống]
        direction TB
        H_start(" "):::hidden
        Start((Bắt đầu)):::startNode
        C(Hiển thị hộp thoại cảnh báo)
        D(Đóng hộp thoại)
        E(Cập nhật trạng thái tài khoản thành Đã khóa)
        F(Hủy phiên đăng nhập hiện tại)
        G(Chuyển hướng về trang chủ)
        End(((Kết thúc))):::endNode
        H_end(" "):::hidden
    end

    T_start ~~~ H_start
    T_end ~~~ H_end

    T_start ~~~ A
    B ~~~ T_end

    H_start ~~~ Start
    End ~~~ H_end

    Start --> A
    A --> C
    C --> B
    B -- Hủy thao tác --> D
    D --> End

    B -- Xác nhận --> E
    E --> F
    F --> G
    G --> End

    style TacNhan fill:#fff,stroke:#333,stroke-width:2px
    style HeThong fill:#fff,stroke:#333,stroke-width:2px
```
