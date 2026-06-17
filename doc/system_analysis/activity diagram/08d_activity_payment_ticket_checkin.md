# BIỂU ĐỒ HOẠT ĐỘNG - PHẦN D: BÁN VÉ, THANH TOÁN & CHECK-IN (UC16 – UC22)

---

## UC16 – Cấu hình cổng thanh toán (PayOS)

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
        A(Điền thông số API PayOS)
        B(Nhấn Lưu)
        T_end(" "):::hidden
    end

    subgraph System [Hệ thống]
        direction TB
        H_start(" "):::hidden
        Start((Bắt đầu)):::startNode
        C{Đủ thông số chưa?}
        D(Báo lỗi điền thiếu)
        E(Mã hóa thông số bảo mật)
        F(Lưu cấu hình và báo thành công)
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

## UC17 – Đặt mua vé sự kiện

```mermaid
%%{init: {"flowchart": {"curve": "linear"}}}%%
flowchart TD
    classDef default fill:#fff,stroke:#333,stroke-width:1.5px,color:#000,rx:10px,ry:10px;
    classDef startNode fill:#000,stroke:#000,color:#fff;
    classDef endNode fill:#fff,stroke:#000,stroke-width:4px,color:#000;
    classDef hidden fill:none,stroke:none,color:none;

    subgraph User [User]
        direction TB
        T_start(" "):::hidden
        A(Chọn loại vé, số lượng và nhấn Mua vé)
        T_end(" "):::hidden
    end

    subgraph System [Hệ thống]
        direction TB
        H_start(" "):::hidden
        Start((Bắt đầu)):::startNode
        B{Còn đủ vé?}
        C(Báo lỗi hết vé)
        D(Tạm giữ số lượng vé)
        E{Loại vé?}
        F(Cấp mã vé điện tử ngay)
        G(Tạo hóa đơn chờ và chuyển thanh toán)
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
    E -- Miễn phí --> F
    E -- Có phí --> G
    F --> End
    G --> End
    C --> End

    style User fill:#fff,stroke:#333,stroke-width:2px
    style System fill:#fff,stroke:#333,stroke-width:2px
```

---

## UC18 – Thanh toán vé sự kiện

```mermaid
%%{init: {"flowchart": {"curve": "linear"}}}%%
flowchart TD
    classDef default fill:#fff,stroke:#333,stroke-width:1.5px,color:#000,rx:10px,ry:10px;
    classDef startNode fill:#000,stroke:#000,color:#fff;
    classDef endNode fill:#fff,stroke:#000,stroke-width:4px,color:#000;
    classDef hidden fill:none,stroke:none,color:none;

    subgraph User [User / PayOS]
        direction TB
        T_start(" "):::hidden
        A(Quét QR chuyển khoản)
        B(Gửi tín hiệu Webhook thành công)
        T_end(" "):::hidden
    end

    subgraph System [Hệ thống]
        direction TB
        H_start(" "):::hidden
        Start((Bắt đầu)):::startNode
        C(Tiếp nhận tín hiệu Webhook)
        D{Chữ ký hợp lệ?}
        E(Từ chối giao dịch giả)
        F(Đổi trạng thái Đã Thanh Toán)
        G(Kích hoạt vé điện tử)
        H(Chuyển User sang trang Thành công)
        End(((Kết thúc))):::endNode
        H_end(" "):::hidden
    end

    T_start ~~~ H_start
    T_end ~~~ H_end
    Start --> A
    A --> B
    B --> C
    C --> D
    D -- Không --> E
    D -- Có --> F
    F --> G
    G --> H
    H --> End
    E --> End

    style User fill:#fff,stroke:#333,stroke-width:2px
    style System fill:#fff,stroke:#333,stroke-width:2px
```

---

## UC19 – Xem danh sách vé cá nhân

```mermaid
%%{init: {"flowchart": {"curve": "linear"}}}%%
flowchart TD
    classDef default fill:#fff,stroke:#333,stroke-width:1.5px,color:#000,rx:10px,ry:10px;
    classDef startNode fill:#000,stroke:#000,color:#fff;
    classDef endNode fill:#fff,stroke:#000,stroke-width:4px,color:#000;
    classDef hidden fill:none,stroke:none,color:none;

    subgraph User [User]
        direction TB
        T_start(" "):::hidden
        A(Truy cập Vé của tôi)
        T_end(" "):::hidden
    end

    subgraph System [Hệ thống]
        direction TB
        H_start(" "):::hidden
        Start((Bắt đầu)):::startNode
        B(Truy vấn các vé hợp lệ)
        C(Hiển thị danh sách kèm mã QR)
        End(((Kết thúc))):::endNode
        H_end(" "):::hidden
    end

    T_start ~~~ H_start
    T_end ~~~ H_end
    Start --> A
    A --> B
    B --> C
    C --> End

    style User fill:#fff,stroke:#333,stroke-width:2px
    style System fill:#fff,stroke:#333,stroke-width:2px
```

---

## UC20 – Hủy vé đã đặt

```mermaid
%%{init: {"flowchart": {"curve": "linear"}}}%%
flowchart TD
    classDef default fill:#fff,stroke:#333,stroke-width:1.5px,color:#000,rx:10px,ry:10px;
    classDef startNode fill:#000,stroke:#000,color:#fff;
    classDef endNode fill:#fff,stroke:#000,stroke-width:4px,color:#000;
    classDef hidden fill:none,stroke:none,color:none;

    subgraph User [User]
        direction TB
        T_start(" "):::hidden
        A(Chọn vé và nhấn Hủy vé)
        T_end(" "):::hidden
    end

    subgraph System [Hệ thống]
        direction TB
        H_start(" "):::hidden
        Start((Bắt đầu)):::startNode
        B{Vé đã trả tiền chưa?}
        C(Yêu cầu liên hệ Organizer)
        D(Hủy vé và nhả chỗ trống)
        E(Báo thành công)
        End(((Kết thúc))):::endNode
        H_end(" "):::hidden
    end

    T_start ~~~ H_start
    T_end ~~~ H_end
    Start --> A
    A --> B
    B -- Đã trả tiền --> C
    B -- Miễn phí/Chưa trả --> D
    D --> E
    E --> End
    C --> End

    style User fill:#fff,stroke:#333,stroke-width:2px
    style System fill:#fff,stroke:#333,stroke-width:2px
```

---

## UC21 – Quét mã QR soát vé (Check-in Online / Offline)

```mermaid
%%{init: {"flowchart": {"curve": "linear"}}}%%
flowchart TD
    classDef default fill:#fff,stroke:#333,stroke-width:1.5px,color:#000,rx:10px,ry:10px;
    classDef startNode fill:#000,stroke:#000,color:#fff;
    classDef endNode fill:#fff,stroke:#000,stroke-width:4px,color:#000;
    classDef hidden fill:none,stroke:none,color:none;

    subgraph Staff [Staff]
        direction TB
        T_start(" "):::hidden
        A(Quét mã QR trên vé khách)
        T_end(" "):::hidden
    end

    subgraph System [Hệ thống / Ứng dụng]
        direction TB
        H_start(" "):::hidden
        Start((Bắt đầu)):::startNode
        Net{Có mạng Internet?}
        
        %% Nhánh Online
        B{Mã vé tồn tại trên máy chủ?}
        C{Đúng sự kiện?}
        D{Đã sử dụng chưa?}
        E(Báo màn hình đỏ từ chối)
        F(Cập nhật vé Đã sử dụng)
        G(Lưu lịch sử và hiện màn hình xanh kèm thông tin vé)
        
        %% Nhánh Offline
        O1(Mã hóa QR và so sánh với danh sách lưu trên máy)
        O2{Mã vé hợp lệ và chưa check-in?}
        O3(Lưu thông tin mã vé vào máy)
        O4(Hiện màn hình xanh - Sẽ đồng bộ khi có mạng)
        
        End(((Kết thúc))):::endNode
        H_end(" "):::hidden
    end

    T_start ~~~ H_start
    T_end ~~~ H_end
    Start --> A
    A --> Net
    
    %% Luồng Online
    Net -- Có --> B
    B -- Không --> E
    B -- Có --> C
    C -- Không --> E
    C -- Có --> D
    D -- Rồi --> E
    D -- Chưa --> F
    F --> G
    G --> End
    
    %% Luồng Offline
    Net -- Không --> O1
    O1 --> O2
    O2 -- Không --> E
    O2 -- Có --> O3
    O3 --> O4
    O4 --> End
    
    E --> End

    style Staff fill:#fff,stroke:#333,stroke-width:2px
    style System fill:#fff,stroke:#333,stroke-width:2px
```

---

## UC22 – Xem lịch sử Check-in

```mermaid
%%{init: {"flowchart": {"curve": "linear"}}}%%
flowchart TD
    classDef default fill:#fff,stroke:#333,stroke-width:1.5px,color:#000,rx:10px,ry:10px;
    classDef startNode fill:#000,stroke:#000,color:#fff;
    classDef endNode fill:#fff,stroke:#000,stroke-width:4px,color:#000;
    classDef hidden fill:none,stroke:none,color:none;

    subgraph Actor [Organizer / Admin]
        direction TB
        T_start(" "):::hidden
        A(Truy cập Nhật ký Check-in)
        T_end(" "):::hidden
    end

    subgraph System [Hệ thống]
        direction TB
        H_start(" "):::hidden
        Start((Bắt đầu)):::startNode
        B(Truy vấn dữ liệu soát vé)
        C(Hiển thị danh sách khách đã vào cổng)
        End(((Kết thúc))):::endNode
        H_end(" "):::hidden
    end

    T_start ~~~ H_start
    T_end ~~~ H_end
    Start --> A
    A --> B
    B --> C
    C --> End

    style Actor fill:#fff,stroke:#333,stroke-width:2px
    style System fill:#fff,stroke:#333,stroke-width:2px
```
