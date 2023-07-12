package com.my.stock.rdb.repository;

import com.my.stock.rdb.entity.Stock;
import com.my.stock.rdb.repository.custom.StockRepositoryCustom;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface StockRepository extends JpaRepository<Stock, Long>, StockRepositoryCustom {
	@Query(value = "SELECT symbol FROM stock GROUP BY symbol", nativeQuery = true)
	List<String> findSymbolGroupBySymbol();
}
