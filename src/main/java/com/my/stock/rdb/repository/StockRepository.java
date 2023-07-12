package com.my.stock.rdb.repository;

import com.my.stock.dto.SymbolAndCode;
import com.my.stock.dto.SymbolAndCodeInterface;
import com.my.stock.rdb.entity.Stock;
import com.my.stock.rdb.repository.custom.StockRepositoryCustom;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface StockRepository extends JpaRepository<Stock, Long>, StockRepositoryCustom {
	@Query(value = "SELECT stock.symbol FROM stock INNER JOIN stocks on stock.symbol = stocks.symbol WHERE stocks.national=:national GROUP BY stock.symbol", nativeQuery = true)
	List<String> findSymbolByNationalGroupBySymbol(@Param("national")String national);

	@Query(value = "SELECT stock.symbol, stocks.code FROM stock INNER JOIN stocks on stock.symbol = stocks.symbol WHERE stocks.national !=:national GROUP BY stock.symbol", nativeQuery = true)
	List<SymbolAndCodeInterface> findSymbolAndCodeNotNationalGroupBySymbol(@Param("national")String national);
}
