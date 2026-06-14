# BIỂU ĐỒ HOẠT ĐỘNG - PHẦN F: TRỢ LÝ ẢO AI CHATBOT & BÁO CÁO (UC37 – UC40)

**Hệ thống Quản lý Sự kiện & Check-in QR**

---

## UC38 – Trò chuyện với Trợ lý ảo AI (Chatbot)

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
        A(Mở cửa sổ Chatbot AI)
        B(Nhập câu hỏi hoặc yêu cầu)
        T_end(" "):::hidden
    end

    subgraph HeThong [Hệ thống]
        direction TB
        H_start(" "):::hidden
        Start((Bắt đầu)):::startNode
        C(Tiếp nhận câu hỏi)
        D(Phân tích ngữ nghĩa nội dung)
        E{Cần truy vấn dữ liệu không?}
        F(Gọi các công cụ tìm kiếm nội bộ: Sự kiện, Lịch trình, Vé)
        G(Sử dụng trí tuệ nhân tạo để tổng hợp câu trả lời)
        H(Gửi câu trả lời về màn hình chat)
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
    C --> D
    D --> E

    E -- Không cần --> G
    E -- Có cần --> F
    F --> G
    G --> H
    H --> End

    style TacNhan fill:#fff,stroke:#333,stroke-width:2px
    style HeThong fill:#fff,stroke:#333,stroke-width:2px
```

---

## UC39 – Xem danh sách sự kiện được phân công của 1 nhân viên

```mermaid
%%{init: {"flowchart": {"curve": "linear"}}}%%
flowchart TD
    classDef default fill:#fff,stroke:#333,stroke-width:1.5px,color:#000,rx:10px,ry:10px;
    classDef startNode fill:#000,stroke:#000,color:#fff;
    classDef endNode fill:#fff,stroke:#000,stroke-width:4px,color:#000;
    classDef hidden fill:none,stroke:none,color:none;

    subgraph TacNhan [Nhân viên soát vé]
        direction TB
        T_start(" "):::hidden
        A(Truy cập mục Công việc của tôi)
        T_end(" "):::hidden
    end

    subgraph HeThong [Hệ thống]
        direction TB
        H_start(" "):::hidden
        Start((Bắt đầu)):::startNode
        B(Xác định ID của nhân viên đang đăng nhập)
        C(Truy vấn các sự kiện mà nhân viên này được phân công)
        D(Lọc ra các sự kiện chưa kết thúc)
        E(Hiển thị danh sách sự kiện lên màn hình)
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
    D --> E
    E --> End

    style TacNhan fill:#fff,stroke:#333,stroke-width:2px
    style HeThong fill:#fff,stroke:#333,stroke-width:2px
```

---

## UC40 – Xem danh sách giao dịch thanh toán của sự kiện

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
        A(Truy cập tab Giao dịch thanh toán của sự kiện)
        B(Lọc giao dịch theo trạng thái: Thành công, Thất bại, Đang chờ)
        T_end(" "):::hidden
    end

    subgraph HeThong [Hệ thống]
        direction TB
        H_start(" "):::hidden
        Start((Bắt đầu)):::startNode
        C(Tìm kiếm danh sách đơn hàng thuộc sự kiện này)
        D(Trích xuất thông tin lịch sử giao dịch thanh toán)
        E(Áp dụng bộ lọc và tính toán tổng doanh thu)
        F(Hiển thị danh sách giao dịch lên màn hình)
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
    D --> E
    E --> F
    F --> End

    style TacNhan fill:#fff,stroke:#333,stroke-width:2px
    style HeThong fill:#fff,stroke:#333,stroke-width:2px
```

---

## UC41 – Xem lịch sử điểm danh (Check-in) của sự kiện

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
        A(Truy cập tab Lịch sử soát vé của sự kiện)
        T_end(" "):::hidden
    end

    subgraph HeThong [Hệ thống]
        direction TB
        H_start(" "):::hidden
        Start((Bắt đầu)):::startNode
        B(Truy xuất nhật ký soát vé của tất cả nhân viên)
        C(Tổng hợp thông tin: Thời gian, Nhân viên soát, Khách hàng)
        D(Sắp xếp theo thời gian mới nhất)
        E(Hiển thị danh sách lịch sử điểm danh lên màn hình)
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
    D --> E
    E --> End

    style TacNhan fill:#fff,stroke:#333,stroke-width:2px
    style HeThong fill:#fff,stroke:#333,stroke-width:2px
```
