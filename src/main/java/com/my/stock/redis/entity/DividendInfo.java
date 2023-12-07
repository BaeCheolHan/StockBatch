package com.my.stock.redis.entity;

import com.my.stock.dto.StockDividendHistory;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.redis.core.RedisHash;
import org.springframework.data.redis.core.TimeToLive;
import yahoofinance.histquotes2.HistoricalDividend;

import java.math.BigDecimal;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Getter
@Setter
@RedisHash("DividendInfo")
public class DividendInfo {
	@Id
	private String symbol;

	private BigDecimal annualDividend;

	private BigDecimal dividendRate;

	private List<StockDividendHistory> dividendHistories;

	@TimeToLive(unit = TimeUnit.HOURS)
	@Builder.Default
	private Long expiration = 25L;
}
