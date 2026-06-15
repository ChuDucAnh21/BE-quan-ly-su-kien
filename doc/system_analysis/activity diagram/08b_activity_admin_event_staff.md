# BIỂU ĐỒ HOẠT ĐỘNG - PHẦN B: QUẢN LÝ SỰ KIỆN (UC09 – UC14)

---

## UC09 – Thêm mới sự kiện

```mermaid
%%{init: {"flowchart": {"curve": "linear"}}}%%
flowchart TD
    classDef default fill:#fff,stroke:#333,stroke-width:1.5px,color:#000,rx:10px,ry:10px;
    classDef startNode fill:#000,stroke:#000,color:#fff;
    classDef endNode fill:#fff,stroke:#000,stroke-width:4px,color:#000;
    classDef hidden fill:none,stroke:none,color:none;

    subgraph Organizer [Organizer]
        direction TB
        T_start(" "):::hidden
        A(Nhập thông tin sự kiện)
        B(Nhấn Lưu sự kiện)
        T_end(" "):::hidden
    end

    subgraph System [Hệ thống]
        direction TB
        H_start(" "):::hidden
        Start((Bắt đầu)):::startNode
        C{Mốc thời gian hợp lệ?}
        D(Báo lỗi từ chối lưu)
        E(Tạo sự kiện lưu dạng Bản nháp)
        F(Báo thành công)
        End(((Kết thúc))):::endNode
        H_end(" "):::hidden
    end

    T_start ~~~ H_start
    T_end ~~~ H_end
    Start --> A
    A --> B
    B --> C
    C -- Không --> D
    C -- Có --> E
    E --> F
    F --> End
    D --> End

    style Organizer fill:#fff,stroke:#333,stroke-width:2px
    style System fill:#fff,stroke:#333,stroke-width:2px
```

---

## UC10 – Cập nhật thông tin sự kiện

```mermaid
%%{init: {"flowchart": {"curve": "linear"}}}%%
flowchart TD
    classDef default fill:#fff,stroke:#333,stroke-width:1.5px,color:#000,rx:10px,ry:10px;
    classDef startNode fill:#000,stroke:#000,color:#fff;
    classDef endNode fill:#fff,stroke:#000,stroke-width:4px,color:#000;
    classDef hidden fill:none,stroke:none,color:none;

    subgraph Organizer [Organizer]
        direction TB
        T_start(" "):::hidden
        A(Chỉnh sửa thông tin)
        B(Nhấn Lưu thay đổi)
        T_end(" "):::hidden
    end

    subgraph System [Hệ thống]
        direction TB
        H_start(" "):::hidden
        Start((Bắt đầu)):::startNode
        C{Là chủ sự kiện?}
        D(Báo lỗi truy cập)
        E{Sức chứa < Số vé đã bán?}
        F(Lưu dữ liệu và báo thành công)
        G(Từ chối vì sức chứa không hợp lệ)
        End(((Kết thúc))):::endNode
        H_end(" "):::hidden
    end

    T_start ~~~ H_start
    T_end ~~~ H_end
    Start --> A
    A --> B
    B --> C
    C -- Không --> D
    C -- Có --> E
    E -- Có --> G
    E -- Không --> F
    F --> End
    D --> End
    G --> End

    style Organizer fill:#fff,stroke:#333,stroke-width:2px
    style System fill:#fff,stroke:#333,stroke-width:2px
```

---

## UC11 – Xóa / Hủy sự kiện

```mermaid
%%{init: {"flowchart": {"curve": "linear"}}}%%
flowchart TD
    classDef default fill:#fff,stroke:#333,stroke-width:1.5px,color:#000,rx:10px,ry:10px;
    classDef startNode fill:#000,stroke:#000,color:#fff;
    classDef endNode fill:#fff,stroke:#000,stroke-width:4px,color:#000;
    classDef hidden fill:none,stroke:none,color:none;

    subgraph Organizer [Organizer]
        direction TB
        T_start(" "):::hidden
        A(Nhấn Xóa sự kiện)
        T_end(" "):::hidden
    end

    subgraph System [Hệ thống]
        direction TB
        H_start(" "):::hidden
        Start((Bắt đầu)):::startNode
        B{Số vé đã bán?}
        C(Xóa vĩnh viễn)
        D(Đổi trạng thái thành Đã Hủy)
        E(Báo thành công)
        End(((Kết thúc))):::endNode
        H_end(" "):::hidden
    end

    T_start ~~~ H_start
    T_end ~~~ H_end
    Start --> A
    A --> B
    B -- Chưa có khách hàng --> C
    B -- Đã bán vé --> D
    C --> E
    D --> E
    E --> End

    style Organizer fill:#fff,stroke:#333,stroke-width:2px
    style System fill:#fff,stroke:#333,stroke-width:2px
```

---

## UC12 – Thay đổi trạng thái sự kiện

```mermaid
%%{init: {"flowchart": {"curve": "linear"}}}%%
flowchart TD
    classDef default fill:#fff,stroke:#333,stroke-width:1.5px,color:#000,rx:10px,ry:10px;
    classDef startNode fill:#000,stroke:#000,color:#fff;
    classDef endNode fill:#fff,stroke:#000,stroke-width:4px,color:#000;
    classDef hidden fill:none,stroke:none,color:none;

    subgraph Organizer [Organizer]
        direction TB
        T_start(" "):::hidden
        A(Chọn trạng thái mới cho sự kiện)
        T_end(" "):::hidden
    end

    subgraph System [Hệ thống]
        direction TB
        H_start(" "):::hidden
        Start((Bắt đầu)):::startNode
        B{Trạng thái mới là Công bố?}
        C{Có vé thu phí?}
        D{Đã cấu hình PayOS?}
        E(Báo lỗi từ chối thao tác)
        F(Cập nhật CSDL và Đổi trạng thái)
        End(((Kết thúc))):::endNode
        H_end(" "):::hidden
    end

    T_start ~~~ H_start
    T_end ~~~ H_end
    Start --> A
    A --> B
    B -- Có --> C
    B -- Không --> F
    C -- Có --> D
    C -- Không --> F
    D -- Không --> E
    D -- Có --> F
    F --> End
    E --> End

    style Organizer fill:#fff,stroke:#333,stroke-width:2px
    style System fill:#fff,stroke:#333,stroke-width:2px
```

---

## UC13 – Tra cứu / Tìm kiếm sự kiện

```mermaid
%%{init: {"flowchart": {"curve": "linear"}}}%%
flowchart TD
    classDef default fill:#fff,stroke:#333,stroke-width:1.5px,color:#000,rx:10px,ry:10px;
    classDef startNode fill:#000,stroke:#000,color:#fff;
    classDef endNode fill:#fff,stroke:#000,stroke-width:4px,color:#000;
    classDef hidden fill:none,stroke:none,color:none;

    subgraph Guest [Guest / User]
        direction TB
        T_start(" "):::hidden
        A(Mở trang Khám phá sự kiện)
        B(Nhập từ khóa và bộ lọc)
        T_end(" "):::hidden
    end

    subgraph System [Hệ thống]
        direction TB
        H_start(" "):::hidden
        Start((Bắt đầu)):::startNode
        C(Truy vấn sự kiện Đã công bố)
        D(Trả về danh sách kết quả)
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

    style Guest fill:#fff,stroke:#333,stroke-width:2px
    style System fill:#fff,stroke:#333,stroke-width:2px
```

---

## UC14 – Xem chi tiết sự kiện

```mermaid
%%{init: {"flowchart": {"curve": "linear"}}}%%
flowchart TD
    classDef default fill:#fff,stroke:#333,stroke-width:1.5px,color:#000,rx:10px,ry:10px;
    classDef startNode fill:#000,stroke:#000,color:#fff;
    classDef endNode fill:#fff,stroke:#000,stroke-width:4px,color:#000;
    classDef hidden fill:none,stroke:none,color:none;

    subgraph Guest [Guest / User]
        direction TB
        T_start(" "):::hidden
        A(Nhấn vào một sự kiện)
        T_end(" "):::hidden
    end

    subgraph System [Hệ thống]
        direction TB
        H_start(" "):::hidden
        Start((Bắt đầu)):::startNode
        B{Sự kiện tồn tại?}
        C(Báo lỗi không tồn tại)
        D(Truy xuất dữ liệu chi tiết)
        E(Hiển thị toàn bộ thông tin)
        End(((Kết thúc))):::endNode
        H_end(" "):::hidden
    end

    T_start ~~~ H_start
    T_end ~~~ H_end
    Start --> A
    A --> B
    B -- Không --> C
    B -- Có --> D
    D --> E
    E --> End
    C --> End

    style Guest fill:#fff,stroke:#333,stroke-width:2px
    style System fill:#fff,stroke:#333,stroke-width:2px
```
