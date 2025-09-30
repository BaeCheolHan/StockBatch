package com.my.stock.rdb.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;

@Setter
@Getter
@Entity
@Table(name = "benchmark_daily_return",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_benchmark_date_symbol", columnNames = {"date", "symbol"})
        },
        indexes = {
                @Index(name = "idx_benchmark_symbol_date", columnList = "symbol,date"),
                @Index(name = "idx_benchmark_date_symbol", columnList = "date,symbol")
        }
)
public class BenchmarkDailyReturn {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private LocalDate date;

    @Column(name = "symbol")
    private String symbol;

    private BigDecimal close;

    private BigDecimal dailyReturn; // (close_t / close_{t-1} - 1)

    private BigDecimal cumIndex; // 초기 100, 이후 곱셈 누적

}


