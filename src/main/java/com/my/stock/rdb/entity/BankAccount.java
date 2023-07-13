package com.my.stock.rdb.entity;

import com.my.stock.base.entity.BaseTimeEntity;
import com.my.stock.constants.Bank;
import jakarta.persistence.*;
import lombok.*;

import java.util.List;

@EqualsAndHashCode
@Builder
@Entity
@Table
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class BankAccount extends BaseTimeEntity {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	private String memo;

	private String alias;

	@Column(nullable = false)
	@Enumerated(EnumType.STRING)
	private Bank bank;

	@ManyToOne
	private Member member;

	@OneToMany(mappedBy = "bankAccount", fetch=FetchType.LAZY)
	private List<Stock> stocks;

	@OneToMany(mappedBy = "bankAccount", fetch=FetchType.LAZY)
	private List<DepositWithdrawalHistory> depositWithdrawalHistories;
}