package com.my.stock.rdb.entity;

import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "account_daily_return",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_account_date", columnNames = {"date", "account_id"})
        },
        indexes = {
                @Index(name = "idx_account_date", columnList = "account_id,date"),
                @Index(name = "idx_date_account", columnList = "date,account_id")
        }
)
public class AccountDailyReturn {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "date", nullable = false)
    private LocalDate date;

    @Column(name = "account_id", nullable = false)
    private Long accountId;

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
    public Long getAccountId() { return accountId; }
    public void setAccountId(Long accountId) { this.accountId = accountId; }
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


