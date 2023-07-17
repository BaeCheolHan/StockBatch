package com.my.stock.rdb.entity;


import com.my.stock.base.entity.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@EqualsAndHashCode(callSuper = false)
@Builder
@Entity
@Table
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Stock extends BaseTimeEntity {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	@Column(nullable = false)
	private String symbol;
	@Column(nullable = false, precision = 10, scale = 6)
	private BigDecimal quantity;
	@Column(nullable = false, precision = 10, scale = 6)
	private BigDecimal price;

	@ManyToOne
	private BankAccount bankAccount;

}
