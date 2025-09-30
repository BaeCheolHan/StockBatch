package com.my.stock.rdb.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;

@Setter
@Getter
@Entity
@Table(name = "user_daily_return",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_user_date", columnNames = {"date", "user_id"})
        },
        indexes = {
                @Index(name = "idx_user_date", columnList = "user_id,date"),
                @Index(name = "idx_date_user", columnList = "date,user_id")
        }
)
public class UserDailyReturn {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "date", nullable = false)
    private LocalDate date;

    @Column(name = "user_id", nullable = false)
    private String userId;

    @Column(name = "nav_begin", precision = 19, scale = 6)
    private BigDecimal navBegin;

    @Column(name = "nav_end", precision = 19, scale = 6)
    private BigDecimal navEnd;

    @Column(name = "net_flow", precision = 19, scale = 6)
    private BigDecimal netFlow;

    @Column(name = "daily_twr", precision = 19, scale = 10)
    private BigDecimal dailyTwr;

    @Column(name = "cum_index", precision = 19, scale = 10)
    private BigDecimal cumIndex;

}


