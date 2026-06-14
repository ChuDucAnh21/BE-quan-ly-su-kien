# BIỂU ĐỒ HOẠT ĐỘNG - PHẦN B: QUẢN TRỊ & SỰ KIỆN

**Hệ thống Quản lý Sự kiện & Check-in QR**

---

Tài liệu này chứa các Biểu đồ Hoạt động (Activity Diagrams) mô tả các chức năng dành cho Quản trị viên (Admin) và Ban tổ chức sự kiện (Organizer).

---

## UC10 – Xem danh sách người dùng

```mermaid
%%{init: {"flowchart": {"curve": "linear"}}}%%
flowchart TD
    classDef default fill:#fff,stroke:#333,stroke-width:1.5px,color:#000,rx:10px,ry:10px;
    classDef startNode fill:#000,stroke:#000,color:#fff;
    classDef endNode fill:#fff,stroke:#000,stroke-width:4px,color:#000;
    classDef hidden fill:none,stroke:none,color:none;

    subgraph TacNhan [Quản trị viên]
        direction TB
        T_start(" "):::hidden
        A(Truy cập trang Quản lý người dùng)
        B(Nhập thông tin tìm kiếm hoặc chọn bộ lọc)
        T_end(" "):::hidden
    end

    subgraph HeThong [Hệ thống]
        direction TB
        H_start(" "):::hidden
        Start((Bắt đầu)):::startNode
        C{Kiểm tra quyền quản trị}
        D(Thông báo không có quyền truy cập)
        E(Tìm kiếm dữ liệu người dùng theo yêu cầu)
        F(Hiển thị danh sách người dùng)
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
    B --> C

    C -- Không có quyền --> D
    D --> End

    C -- Được phép --> E
    E --> F
    F --> End

    style TacNhan fill:#fff,stroke:#333,stroke-width:2px
    style HeThong fill:#fff,stroke:#333,stroke-width:2px
```

---

## UC11 – Khóa / Mở khóa tài khoản

```mermaid
%%{init: {"flowchart": {"curve": "linear"}}}%%
flowchart TD
    classDef default fill:#fff,stroke:#333,stroke-width:1.5px,color:#000,rx:10px,ry:10px;
    classDef startNode fill:#000,stroke:#000,color:#fff;
    classDef endNode fill:#fff,stroke:#000,stroke-width:4px,color:#000;
    classDef hidden fill:none,stroke:none,color:none;

    subgraph TacNhan [Quản trị viên]
        direction TB
        T_start(" "):::hidden
        A(Chọn một người dùng cụ thể)
        B(Nhấn nút Khóa hoặc Mở khóa)
        T_end(" "):::hidden
    end

    subgraph HeThong [Hệ thống]
        direction TB
        H_start(" "):::hidden
        Start((Bắt đầu)):::startNode
        C(Hiển thị danh sách người dùng)
        D{Kiểm tra tài khoản có phải là chính quản trị viên không}
        E(Thông báo không thể tự khóa bản thân)
        F(Cập nhật trạng thái mới cho tài khoản)
        G(Thông báo thay đổi thành công)
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
    D -- Là bản thân --> E
    E --> A

    D -- Không phải bản thân --> F
    F --> G
    G --> End

    style TacNhan fill:#fff,stroke:#333,stroke-width:2px
    style HeThong fill:#fff,stroke:#333,stroke-width:2px
```

---

## UC12 – Xóa tài khoản vĩnh viễn (Hybrid Delete)

```mermaid
%%{init: {"flowchart": {"curve": "linear"}}}%%
flowchart TD
    classDef default fill:#fff,stroke:#333,stroke-width:1.5px,color:#000,rx:10px,ry:10px;
    classDef startNode fill:#000,stroke:#000,color:#fff;
    classDef endNode fill:#fff,stroke:#000,stroke-width:4px,color:#000;
    classDef hidden fill:none,stroke:none,color:none;

    subgraph TacNhan [Quản trị viên Super Admin]
        direction TB
        T_start(" "):::hidden
        A(Chọn tài khoản cần xóa)
        B(Xác nhận xóa vĩnh viễn)
        T_end(" "):::hidden
    end

    subgraph HeThong [Hệ thống]
        direction TB
        H_start(" "):::hidden
        Start((Bắt đầu)):::startNode
        C(Hiển thị cảnh báo nguy hiểm)
        D{Thử xóa vật lý khỏi CSDL}
        E(Xóa thành công khỏi CSDL)
        F{Bắt lỗi Khóa ngoại}
        G(Ẩn danh hóa thông tin cá nhân)
        H(Chuyển trạng thái thành Đã Khóa)
        I(Thông báo hoàn tất thao tác dọn dẹp)
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

    D -- Không có dữ liệu liên quan --> E
    E --> I

    D -- Có dữ liệu vé/sự kiện --> F
    F --> G
    G --> H
    H --> I
    I --> End

    style TacNhan fill:#fff,stroke:#333,stroke-width:2px
    style HeThong fill:#fff,stroke:#333,stroke-width:2px
```

---

## UC13 – Phân quyền người dùng

```mermaid
%%{init: {"flowchart": {"curve": "linear"}}}%%
flowchart TD
    classDef default fill:#fff,stroke:#333,stroke-width:1.5px,color:#000,rx:10px,ry:10px;
    classDef startNode fill:#000,stroke:#000,color:#fff;
    classDef endNode fill:#fff,stroke:#000,stroke-width:4px,color:#000;
    classDef hidden fill:none,stroke:none,color:none;

    subgraph TacNhan [Quản trị viên]
        direction TB
        T_start(" "):::hidden
        A(Chọn người dùng cần thay đổi quyền)
        B(Chọn vai trò mới và Nhấn Xác nhận)
        T_end(" "):::hidden
    end

    subgraph HeThong [Hệ thống]
        direction TB
        H_start(" "):::hidden
        Start((Bắt đầu)):::startNode
        C(Hiển thị danh sách người dùng)
        D{Kiểm tra quyền thao tác hợp lệ}
        E(Thông báo thao tác không hợp lệ)
        F(Cập nhật vai trò mới vào hệ thống)
        G(Thông báo phân quyền thành công)
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
    D -- Không hợp lệ --> E
    E --> A

    D -- Hợp lệ --> F
    F --> G
    G --> End

    style TacNhan fill:#fff,stroke:#333,stroke-width:2px
    style HeThong fill:#fff,stroke:#333,stroke-width:2px
```

---

## UC14 – Xem thống kê tổng quan (Dashboard)

```mermaid
%%{init: {"flowchart": {"curve": "linear"}}}%%
flowchart TD
    classDef default fill:#fff,stroke:#333,stroke-width:1.5px,color:#000,rx:10px,ry:10px;
    classDef startNode fill:#000,stroke:#000,color:#fff;
    classDef endNode fill:#fff,stroke:#000,stroke-width:4px,color:#000;
    classDef hidden fill:none,stroke:none,color:none;

    subgraph TacNhan [Quản trị viên / Ban tổ chức]
        direction TB
        T_start(" "):::hidden
        A(Truy cập trang Bảng điều khiển)
        T_end(" "):::hidden
    end

    subgraph HeThong [Hệ thống]
        direction TB
        H_start(" "):::hidden
        Start((Bắt đầu)):::startNode
        B{Kiểm tra quyền hạn}
        C(Thông báo không có quyền truy cập)
        D(Tổng hợp dữ liệu: Số người dùng, doanh thu, sự kiện)
        E(Hiển thị biểu đồ và số liệu thống kê)
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

    B -- Không có quyền --> C
    C --> End

    B -- Được phép --> D
    D --> E
    E --> End

    style TacNhan fill:#fff,stroke:#333,stroke-width:2px
    style HeThong fill:#fff,stroke:#333,stroke-width:2px
```

---

## UC15 – Xem thống kê của một sự kiện

```mermaid
%%{init: {"flowchart": {"curve": "linear"}}}%%
flowchart TD
    classDef default fill:#fff,stroke:#333,stroke-width:1.5px,color:#000,rx:10px,ry:10px;
    classDef startNode fill:#000,stroke:#000,color:#fff;
    classDef endNode fill:#fff,stroke:#000,stroke-width:4px,color:#000;
    classDef hidden fill:none,stroke:none,color:none;

    subgraph TacNhan [Ban tổ chức]
        direction TB
        T_start(" "):::hidden
        A(Chọn xem thống kê của một sự kiện cụ thể)
        T_end(" "):::hidden
    end

    subgraph HeThong [Hệ thống]
        direction TB
        H_start(" "):::hidden
        Start((Bắt đầu)):::startNode
        B{Kiểm tra quyền sở hữu sự kiện}
        C(Thông báo không có quyền)
        D(Tổng hợp dữ liệu vé bán, check-in, doanh thu của sự kiện)
        E(Hiển thị biểu đồ chi tiết sự kiện)
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

    B -- Không có quyền --> C
    C --> End

    B -- Là chủ sự kiện --> D
    D --> E
    E --> End

    style TacNhan fill:#fff,stroke:#333,stroke-width:2px
    style HeThong fill:#fff,stroke:#333,stroke-width:2px
```

---

## UC16 – Xem danh sách sự kiện

```mermaid
%%{init: {"flowchart": {"curve": "linear"}}}%%
flowchart TD
    classDef default fill:#fff,stroke:#333,stroke-width:1.5px,color:#000,rx:10px,ry:10px;
    classDef startNode fill:#000,stroke:#000,color:#fff;
    classDef endNode fill:#fff,stroke:#000,stroke-width:4px,color:#000;
    classDef hidden fill:none,stroke:none,color:none;

    subgraph TacNhan [Người dùng / Khách]
        direction TB
        T_start(" "):::hidden
        A(Truy cập trang Danh sách sự kiện)
        B(Nhập từ khóa tìm kiếm hoặc lọc chuyên mục)
        T_end(" "):::hidden
    end

    subgraph HeThong [Hệ thống]
        direction TB
        H_start(" "):::hidden
        Start((Bắt đầu)):::startNode
        C(Lọc các sự kiện đang được công khai)
        D(Hiển thị danh sách sự kiện)
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
    B --> C
    C --> D
    D --> End

    style TacNhan fill:#fff,stroke:#333,stroke-width:2px
    style HeThong fill:#fff,stroke:#333,stroke-width:2px
```

---

## UC17 – Xem chi tiết sự kiện

```mermaid
%%{init: {"flowchart": {"curve": "linear"}}}%%
flowchart TD
    classDef default fill:#fff,stroke:#333,stroke-width:1.5px,color:#000,rx:10px,ry:10px;
    classDef startNode fill:#000,stroke:#000,color:#fff;
    classDef endNode fill:#fff,stroke:#000,stroke-width:4px,color:#000;
    classDef hidden fill:none,stroke:none,color:none;

    subgraph TacNhan [Người dùng / Khách]
        direction TB
        T_start(" "):::hidden
        A(Nhấn chọn một sự kiện trong danh sách)
        T_end(" "):::hidden
    end

    subgraph HeThong [Hệ thống]
        direction TB
        H_start(" "):::hidden
        Start((Bắt đầu)):::startNode
        B{Kiểm tra sự kiện có tồn tại và đang mở không}
        C(Thông báo sự kiện không tồn tại hoặc đã ẩn)
        D(Tập hợp thông tin chi tiết và lịch trình của sự kiện)
        E(Hiển thị trang chi tiết sự kiện)
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
    B -- Đã ẩn/Xóa --> C
    C --> End

    B -- Hợp lệ --> D
    D --> E
    E --> End

    style TacNhan fill:#fff,stroke:#333,stroke-width:2px
    style HeThong fill:#fff,stroke:#333,stroke-width:2px
```

---

## UC18 – Tạo sự kiện mới

```mermaid
%%{init: {"flowchart": {"curve": "linear"}}}%%
flowchart TD
    classDef default fill:#fff,stroke:#333,stroke-width:1.5px,color:#000,rx:10px,ry:10px;
    classDef startNode fill:#000,stroke:#000,color:#fff;
    classDef endNode fill:#fff,stroke:#000,stroke-width:4px,color:#000;
    classDef hidden fill:none,stroke:none,color:none;

    subgraph TacNhan [Ban tổ chức]
        direction TB
        T_start(" "):::hidden
        A(Điền thông tin và lịch trình sự kiện lên biểu mẫu)
        B(Tải lên ảnh bìa cho sự kiện)
        C(Nhấn nút Tạo sự kiện)
        T_end(" "):::hidden
    end

    subgraph HeThong [Hệ thống]
        direction TB
        H_start(" "):::hidden
        Start((Bắt đầu)):::startNode
        D(Hiển thị biểu mẫu tạo sự kiện)
        E{Kiểm tra tính hợp lệ của thời gian và thông tin}
        F(Hiển thị lỗi nhập liệu)
        G{Kiểm tra có ảnh đính kèm không}
        H(Tải ảnh lên máy chủ lưu trữ trực tuyến)
        I(Lưu thông tin sự kiện ở trạng thái Nháp)
        J(Thông báo tạo sự kiện thành công)
        End(((Kết thúc))):::endNode
        H_end(" "):::hidden
    end

    T_start ~~~ H_start
    T_end ~~~ H_end

    T_start ~~~ A
    C ~~~ T_end

    H_start ~~~ Start
    End ~~~ H_end

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

    style TacNhan fill:#fff,stroke:#333,stroke-width:2px
    style HeThong fill:#fff,stroke:#333,stroke-width:2px
```

---

## UC19 – Cập nhật thông tin và trạng thái sự kiện

```mermaid
%%{init: {"flowchart": {"curve": "linear"}}}%%
flowchart TD
    classDef default fill:#fff,stroke:#333,stroke-width:1.5px,color:#000,rx:10px,ry:10px;
    classDef startNode fill:#000,stroke:#000,color:#fff;
    classDef endNode fill:#fff,stroke:#000,stroke-width:4px,color:#000;
    classDef hidden fill:none,stroke:none,color:none;

    subgraph TacNhan [Ban tổ chức]
        direction TB
        T_start(" "):::hidden
        A(Nhấn nút Chỉnh sửa / Cập nhật thông tin sự kiện)
        B(Chọn trạng thái mới: Đăng tải / Dừng / Kết thúc)
        C(Nhấn nút Lưu thay đổi)
        T_end(" "):::hidden
    end

    subgraph HeThong [Hệ thống]
        direction TB
        H_start(" "):::hidden
        Start((Bắt đầu)):::startNode
        D(Hiển thị thông tin sự kiện hiện tại)
        E{Kiểm tra tính hợp lệ của thông tin thay đổi}
        F(Hiển thị lỗi nhập liệu)
        G{Kiểm tra điều kiện thay đổi trạng thái}
        H(Thông báo yêu cầu thiết lập thanh toán trước khi đăng tải)
        I(Cập nhật thông tin và trạng thái mới vào hệ thống)
        J(Thông báo thao tác thành công)
        End(((Kết thúc))):::endNode
        H_end(" "):::hidden
    end

    T_start ~~~ H_start
    T_end ~~~ H_end

    T_start ~~~ A
    C ~~~ T_end

    H_start ~~~ Start
    End ~~~ H_end

    Start --> D
    D --> A
    A --> B
    B --> C
    C --> E

    E -- Lỗi thông tin --> F
    F --> A

    E -- Hợp lệ --> G
    G -- Thiếu thiết lập thanh toán --> H
    G -- Hợp lệ --> I

    H --> End
    I --> J
    J --> End

    style TacNhan fill:#fff,stroke:#333,stroke-width:2px
    style HeThong fill:#fff,stroke:#333,stroke-width:2px
```

---

## UC20 – Xóa sự kiện

```mermaid
%%{init: {"flowchart": {"curve": "linear"}}}%%
flowchart TD
    classDef default fill:#fff,stroke:#333,stroke-width:1.5px,color:#000,rx:10px,ry:10px;
    classDef startNode fill:#000,stroke:#000,color:#fff;
    classDef endNode fill:#fff,stroke:#000,stroke-width:4px,color:#000;
    classDef hidden fill:none,stroke:none,color:none;

    subgraph TacNhan [Ban tổ chức / Quản trị viên]
        direction TB
        T_start(" "):::hidden
        A(Nhấn nút Xóa sự kiện)
        B(Xác nhận đồng ý xóa)
        T_end(" "):::hidden
    end

    subgraph HeThong [Hệ thống]
        direction TB
        H_start(" "):::hidden
        Start((Bắt đầu)):::startNode
        C(Hiển thị hộp thoại cảnh báo)
        D(Đóng hộp thoại)
        E{Kiểm tra sự kiện đã có người đăng ký chưa}
        F(Xóa hoàn toàn sự kiện khỏi dữ liệu)
        G(Chuyển trạng thái sự kiện thành Đã hủy)
        H(Thông báo thao tác thành công)
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
    B -- Hủy bỏ --> D
    D --> End

    B -- Xác nhận --> E
    E -- Chưa ai đăng ký --> F
    E -- Đã có người đăng ký --> G

    F --> H
    G --> H
    H --> End

    style TacNhan fill:#fff,stroke:#333,stroke-width:2px
    style HeThong fill:#fff,stroke:#333,stroke-width:2px
```
