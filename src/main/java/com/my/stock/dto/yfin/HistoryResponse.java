package com.my.stock.dto.yfin;

import java.util.List;

public class HistoryResponse {
    private String ticker;
    private String range;
    private String interval;
    private List<HistoryRow> rows;

    public String getTicker() { return ticker; }
    public void setTicker(String ticker) { this.ticker = ticker; }
    public String getRange() { return range; }
    public void setRange(String range) { this.range = range; }
    public String getInterval() { return interval; }
    public void setInterval(String interval) { this.interval = interval; }
    public List<HistoryRow> getRows() { return rows; }
    public void setRows(List<HistoryRow> rows) { this.rows = rows; }

    public static class HistoryRow {
        private String time; // ISO-8601
        private Double open;
        private Double high;
        private Double low;
        private Double close;
        private Long volume;

        public String getTime() { return time; }
        public void setTime(String time) { this.time = time; }
        public Double getOpen() { return open; }
        public void setOpen(Double open) { this.open = open; }
        public Double getHigh() { return high; }
        public void setHigh(Double high) { this.high = high; }
        public Double getLow() { return low; }
        public void setLow(Double low) { this.low = low; }
        public Double getClose() { return close; }
        public void setClose(Double close) { this.close = close; }
        public Long getVolume() { return volume; }
        public void setVolume(Long volume) { this.volume = volume; }
    }
}


