package com.my.stock.dto.yfin;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class QuoteDto {
    private String symbol;
    private String shortName;
    private String currency;
    private Double regularMarketPrice;
    private Double regularMarketChange;
    private Double regularMarketChangePercent;
    private Long regularMarketVolume;
    private Double previousClose;
    private Double dayHigh;
    private Double dayLow;
    private Double trailingAnnualDividendRate;
    private Double trailingAnnualDividendYield;
    private Double forwardDividendRate;
    private Double forwardDividendYield;
    private Double forwardDividendYieldPct;
}


