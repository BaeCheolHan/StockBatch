package com.my.stock.dto.yfin;

import java.util.List;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class HistoryResponse {
    private String ticker;
    private String range;
    private String interval;
    private List<HistoryRow> rows;

    @Setter
    @Getter
    public static class HistoryRow {
        private String time; // ISO-8601
        private Double open;
        private Double high;
        private Double low;
        private Double close;
        private Long volume;

    }
}


