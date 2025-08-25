package com.my.stock.redis.entity;

import com.my.stock.dto.StockDividendHistory;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.redis.core.RedisHash;

import java.math.BigDecimal;
import java.util.List;

@Getter
@Setter
@RedisHash("DividendInfo")
public class DividendInfo {
	@Id
	private String symbol;

	private BigDecimal annualDividend;

	private BigDecimal dividendRate;

	private List<StockDividendHistory> dividendHistories;
}
