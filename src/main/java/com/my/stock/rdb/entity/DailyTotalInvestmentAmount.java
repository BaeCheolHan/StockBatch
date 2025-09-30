package com.my.stock.rdb.entity;

import com.my.stock.base.entity.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@EqualsAndHashCode(callSuper = true)
@Builder
@Entity
@Table(name = "daily_total_investment_amount",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_member_date", columnNames = {"member_id", "date"})
        },
        indexes = {
                @Index(name = "idx_member_date", columnList = "member_id,date"),
                @Index(name = "idx_date_member", columnList = "date,member_id")
        }
)
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class DailyTotalInvestmentAmount extends BaseTimeEntity {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "member_id")
	private Member member;

	private LocalDate date;
	private BigDecimal totalInvestmentAmount;
	private BigDecimal evaluationAmount;
}
