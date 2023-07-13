package com.my.stock.rdb.entity;


import com.my.stock.base.entity.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.*;

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
	@Column(nullable = false)
	private double quantity;
	@Column(nullable = false)
	private double price;

	@ManyToOne
	private BankAccount bankAccount;

}