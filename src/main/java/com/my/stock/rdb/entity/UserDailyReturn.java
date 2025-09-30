package com.my.stock.rdb.entity;

import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.LocalDate;

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

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public LocalDate getDate() { return date; }
    public void setDate(LocalDate date) { this.date = date; }
    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }
    public BigDecimal getNavBegin() { return navBegin; }
    public void setNavBegin(BigDecimal navBegin) { this.navBegin = navBegin; }
    public BigDecimal getNavEnd() { return navEnd; }
    public void setNavEnd(BigDecimal navEnd) { this.navEnd = navEnd; }
    public BigDecimal getNetFlow() { return netFlow; }
    public void setNetFlow(BigDecimal netFlow) { this.netFlow = netFlow; }
    public BigDecimal getDailyTwr() { return dailyTwr; }
    public void setDailyTwr(BigDecimal dailyTwr) { this.dailyTwr = dailyTwr; }
    public BigDecimal getCumIndex() { return cumIndex; }
    public void setCumIndex(BigDecimal cumIndex) { this.cumIndex = cumIndex; }
}


