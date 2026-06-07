package com.example.backend.dto.response.dashboard;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TopEventsChartResponse {
    // Top 5 sự kiện có doanh thu cao nhất (nhãn là tên sự kiện, count là doanh thu)
    private List<ChartPointDTO> topRevenueEvents;

    // Top 5 sự kiện bán được nhiều vé nhất (nhãn là tên sự kiện, count là số vé)
    private List<ChartPointDTO> topSoldEvents;
}
