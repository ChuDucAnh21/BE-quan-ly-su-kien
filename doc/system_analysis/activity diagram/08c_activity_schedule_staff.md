# BIỂU ĐỒ HOẠT ĐỘNG - PHẦN C: LỊCH TRÌNH & PHÂN CÔNG NHÂN SỰ
**Hệ thống Quản lý Sự kiện & Check-in QR**

---

Tài liệu này chứa các Biểu đồ Hoạt động (Activity Diagrams) mô tả thao tác xây dựng dòng thời gian (Timeline) của sự kiện và quản lý đội ngũ nhân viên soát vé.

---

## UC20 – Xem lịch trình sự kiện

```mermaid
flowchart TD
    classDef startNode fill:#000,stroke:#000,color:#fff;
    classDef endNode fill:#fff,stroke:#000,stroke-width:4px,color:#000;

    subgraph TacNhan [Người dùng / Khách]
        direction TB
        A[Truy cập trang chi tiết Sự kiện]
        B[Cuộn xuống phần Lịch trình hoạt động]
    end

    subgraph HeThong [Hệ thống]
        direction TB
        Start((Bắt đầu)):::startNode
        C[Tìm kiếm dữ liệu lịch trình của sự kiện này]
        D[Sắp xếp các hoạt động theo trình tự thời gian]
        E[Hiển thị danh sách hoạt động lên màn hình]
        End(((Kết thúc))):::endNode
    end

    Start --> A
    A --> B
    B --> C
    C --> D
    D --> E
    E --> End
```

---

## UC21 – Tạo lịch trình con (Hàng loạt)

```mermaid
flowchart TD
    classDef startNode fill:#000,stroke:#000,color:#fff;
    classDef endNode fill:#fff,stroke:#000,stroke-width:4px,color:#000;

    subgraph TacNhan [Ban tổ chức]
        direction TB
        A[Nhập danh sách các hoạt động và thời gian]
        B[Nhấn nút Lưu lịch trình]
    end

    subgraph HeThong [Hệ thống]
        direction TB
        Start((Bắt đầu)):::startNode
        C[Hiển thị bảng nhập liệu lịch trình]
        D{Kiểm tra thời gian có nằm trong thời gian sự kiện không}
        E[Thông báo lỗi khoảng thời gian không hợp lệ]
        F[Lưu tất cả các hoạt động vào hệ thống]
        G[Thông báo tạo lịch trình thành công]
        End(((Kết thúc))):::endNode
    end

    Start --> C
    C --> A
    A --> B
    B --> D
    
    D -- Nằm ngoài khoảng --> E
    E --> A
    
    D -- Hợp lệ --> F
    F --> G
    G --> End
```

---

## UC22 – Cập nhật lịch trình con (Hàng loạt)

```mermaid
flowchart TD
    classDef startNode fill:#000,stroke:#000,color:#fff;
    classDef endNode fill:#fff,stroke:#000,stroke-width:4px,color:#000;

    subgraph TacNhan [Ban tổ chức]
        direction TB
        A[Chỉnh sửa thông tin các hoạt động trên bảng]
        B[Nhấn nút Cập nhật lịch trình]
    end

    subgraph HeThong [Hệ thống]
        direction TB
        Start((Bắt đầu)):::startNode
        C[Hiển thị danh sách hoạt động hiện tại]
        D{Kiểm tra thời gian thay đổi có hợp lệ không}
        E[Thông báo lỗi khoảng thời gian không hợp lệ]
        F[Cập nhật dữ liệu mới vào hệ thống]
        G[Thông báo chỉnh sửa thành công]
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

## UC23 – Xóa toàn bộ lịch trình

```mermaid
flowchart TD
    classDef startNode fill:#000,stroke:#000,color:#fff;
    classDef endNode fill:#fff,stroke:#000,stroke-width:4px,color:#000;

    subgraph TacNhan [Ban tổ chức]
        direction TB
        A[Nhấn nút Xóa toàn bộ lịch trình]
        B[Xác nhận đồng ý xóa]
    end

    subgraph HeThong [Hệ thống]
        direction TB
        Start((Bắt đầu)):::startNode
        C[Hiển thị hộp thoại xác nhận]
        D[Hủy bỏ lệnh xóa]
        E[Xóa toàn bộ các hoạt động khỏi dữ liệu sự kiện]
        F[Thông báo làm trống lịch trình thành công]
        End(((Kết thúc))):::endNode
    end

    Start --> A
    A --> C
    C --> B
    
    B -- Không đồng ý --> D
    D --> End
    
    B -- Đồng ý --> E
    E --> F
    F --> End
```

---

## UC24 – Xóa một hoạt động trong lịch trình

```mermaid
flowchart TD
    classDef startNode fill:#000,stroke:#000,color:#fff;
    classDef endNode fill:#fff,stroke:#000,stroke-width:4px,color:#000;

    subgraph TacNhan [Ban tổ chức]
        direction TB
        A[Nhấn biểu tượng Thùng rác bên cạnh một hoạt động]
        B[Xác nhận xóa]
    end

    subgraph HeThong [Hệ thống]
        direction TB
        Start((Bắt đầu)):::startNode
        C[Hiển thị hộp thoại xác nhận]
        D[Hủy thao tác]
        E[Xóa hoạt động này khỏi cơ sở dữ liệu]
        F[Cập nhật lại giao diện và báo thành công]
        End(((Kết thúc))):::endNode
    end

    Start --> A
    A --> C
    C --> B
    
    B -- Hủy --> D
    D --> End
    
    B -- Xác nhận --> E
    E --> F
    F --> End
```

---

## UC25 – Xem danh sách nhân viên hệ thống

```mermaid
flowchart TD
    classDef startNode fill:#000,stroke:#000,color:#fff;
    classDef endNode fill:#fff,stroke:#000,stroke-width:4px,color:#000;

    subgraph TacNhan [Ban tổ chức / Quản trị viên]
        direction TB
        A[Truy cập chức năng Phân công nhân sự]
    end

    subgraph HeThong [Hệ thống]
        direction TB
        Start((Bắt đầu)):::startNode
        B[Tìm kiếm danh sách tài khoản có vai trò là Nhân viên]
        C[Lọc các nhân viên đang hoạt động]
        D[Hiển thị danh sách tổng hợp lên màn hình]
        End(((Kết thúc))):::endNode
    end

    Start --> A
    A --> B
    B --> C
    C --> D
    D --> End
```

---

## UC26 – Xem danh sách nhân viên của sự kiện

```mermaid
flowchart TD
    classDef startNode fill:#000,stroke:#000,color:#fff;
    classDef endNode fill:#fff,stroke:#000,stroke-width:4px,color:#000;

    subgraph TacNhan [Ban tổ chức]
        direction TB
        A[Truy cập tab Danh sách nhân viên sự kiện]
    end

    subgraph HeThong [Hệ thống]
        direction TB
        Start((Bắt đầu)):::startNode
        B[Tìm kiếm dữ liệu phân công của sự kiện này]
        C[Trích xuất danh sách nhân viên được giao việc]
        D[Hiển thị danh sách lên màn hình]
        End(((Kết thúc))):::endNode
    end

    Start --> A
    A --> B
    B --> C
    C --> D
    D --> End
```

---

## UC27 – Thay đổi phân công nhân viên (Thêm / Bớt)

```mermaid
flowchart TD
    classDef startNode fill:#000,stroke:#000,color:#fff;
    classDef endNode fill:#fff,stroke:#000,stroke-width:4px,color:#000;

    subgraph TacNhan [Ban tổ chức]
        direction TB
        A[Đánh dấu chọn hoặc bỏ chọn nhân viên trong danh sách]
        B[Nhấn nút Lưu danh sách phân công]
    end

    subgraph HeThong [Hệ thống]
        direction TB
        Start((Bắt đầu)):::startNode
        C[Hiển thị danh sách nhân viên có thể chọn]
        D{Kiểm tra tính hợp lệ của các nhân viên được chọn}
        E[Thông báo lỗi chọn sai người]
        F[So sánh danh sách cũ và mới]
        G[Gỡ bỏ những người không còn được chọn]
        H[Thêm những người mới được chọn]
        I[Lưu kết quả phân công]
        J[Thông báo cập nhật thành công]
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
    F --> H
    G --> I
    H --> I
    I --> J
    J --> End
```

---

## UC28 – Xóa nhân viên khỏi sự kiện

```mermaid
flowchart TD
    classDef startNode fill:#000,stroke:#000,color:#fff;
    classDef endNode fill:#fff,stroke:#000,stroke-width:4px,color:#000;

    subgraph TacNhan [Ban tổ chức]
        direction TB
        A[Nhấn nút Xóa tại dòng của nhân viên cụ thể]
        B[Xác nhận loại bỏ nhân viên]
    end

    subgraph HeThong [Hệ thống]
        direction TB
        Start((Bắt đầu)):::startNode
        C[Hiển thị hộp thoại yêu cầu xác nhận]
        D[Đóng hộp thoại]
        E[Hủy bỏ quyền kiểm soát vé của nhân viên này]
        F[Xóa tên khỏi danh sách nhân viên sự kiện]
        G[Thông báo gỡ nhân viên thành công]
        End(((Kết thúc))):::endNode
    end

    Start --> A
    A --> C
    C --> B
    
    B -- Hủy thao tác --> D
    D --> End
    
    B -- Đồng ý --> E
    E --> F
    F --> G
    G --> End
```
