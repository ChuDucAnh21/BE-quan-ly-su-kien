# BIỂU ĐỒ HOẠT ĐỘNG - PHẦN D: LỊCH TRÌNH & PHÂN CÔNG NHÂN SỰ

**Hệ thống Quản lý Sự kiện & Check-in QR**

---

Tài liệu này chứa các Biểu đồ Hoạt động (Activity Diagrams) mô tả thao tác quản lý đội ngũ nhân viên soát vé.

> **[LƯU Ý QUAN TRỌNG VỀ KIẾN TRÚC MỚI]**
> Kể từ phiên bản V2, các chức năng Tạo, Sửa, Xóa, Xem lịch trình con độc lập **đã bị loại bỏ**.
> Các thao tác này đã được tích hợp đồng bộ thành một mảng `schedules` và được xử lý chung trong luồng Tạo/Cập nhật/Xem Sự kiện (Xem phần B và C).

---

## UC21 – Xem danh sách nhân viên hệ thống

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
        A(Truy cập chức năng Phân công nhân sự)
        T_end(" "):::hidden
    end

    subgraph HeThong [Hệ thống]
        direction TB
        H_start(" "):::hidden
        Start((Bắt đầu)):::startNode
        B(Tìm kiếm danh sách tài khoản có vai trò là Nhân viên)
        C(Lọc các nhân viên đang hoạt động)
        D(Hiển thị danh sách tổng hợp lên màn hình)
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

## UC22 – Xem danh sách nhân viên của sự kiện

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
        A(Truy cập tab Danh sách nhân viên sự kiện)
        T_end(" "):::hidden
    end

    subgraph HeThong [Hệ thống]
        direction TB
        H_start(" "):::hidden
        Start((Bắt đầu)):::startNode
        B(Tìm kiếm dữ liệu phân công của sự kiện này)
        C(Trích xuất danh sách nhân viên được giao việc)
        D(Hiển thị danh sách lên màn hình)
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

## UC23 – Đồng bộ phân công Nhân viên

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
        A(Đánh dấu chọn hoặc bỏ chọn nhân viên trong danh sách)
        B(Nhấn nút Lưu danh sách phân công)
        T_end(" "):::hidden
    end

    subgraph HeThong [Hệ thống]
        direction TB
        H_start(" "):::hidden
        Start((Bắt đầu)):::startNode
        C(Hiển thị danh sách nhân viên có thể chọn)
        D{Kiểm tra tính hợp lệ của các nhân viên}
        E(Thông báo lỗi chọn sai người)
        F(So sánh danh sách cũ và mới)
        G(Gỡ bỏ những người không còn được chọn)
        H(Thêm những người mới được chọn)
        I(Lưu kết quả phân công)
        J(Thông báo cập nhật thành công)
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
    F --> H
    G --> I
    H --> I
    I --> J
    J --> End

    style TacNhan fill:#fff,stroke:#333,stroke-width:2px
    style HeThong fill:#fff,stroke:#333,stroke-width:2px
```

---

## UC24 – Xóa nhân viên khỏi sự kiện

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
        A(Nhấn nút Xóa tại dòng của nhân viên cụ thể)
        B(Xác nhận loại bỏ nhân viên)
        T_end(" "):::hidden
    end

    subgraph HeThong [Hệ thống]
        direction TB
        H_start(" "):::hidden
        Start((Bắt đầu)):::startNode
        C(Hiển thị hộp thoại yêu cầu xác nhận)
        D(Đóng hộp thoại)
        E(Hủy bỏ quyền kiểm soát vé của nhân viên này)
        F(Xóa tên khỏi danh sách nhân viên sự kiện)
        G(Thông báo gỡ nhân viên thành công)
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

    B -- Đồng ý --> E
    E --> F
    F --> G
    G --> End

    style TacNhan fill:#fff,stroke:#333,stroke-width:2px
    style HeThong fill:#fff,stroke:#333,stroke-width:2px
```
