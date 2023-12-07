package com.my.stock.dto;

import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class StockDividendHistory {
	private String symbol;
	private String date;
	private BigDecimal dividend;
}
