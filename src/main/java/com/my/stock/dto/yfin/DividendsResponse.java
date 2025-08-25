package com.my.stock.dto.yfin;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class DividendsResponse {
    private String ticker;
    private String range;
    private List<DivRow> rows;

    @Getter
    @Setter
    public static class DivRow {
        private String date; // ISO-8601 문자열로 수신
        private Double amount;
    }
}


