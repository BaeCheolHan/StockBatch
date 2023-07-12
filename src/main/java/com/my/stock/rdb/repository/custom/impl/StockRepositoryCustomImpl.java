package com.my.stock.rdb.repository.custom.impl;

import com.my.stock.rdb.repository.custom.StockRepositoryCustom;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class StockRepositoryCustomImpl implements StockRepositoryCustom {

	private final JPAQueryFactory queryFactory;

}
