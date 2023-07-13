package com.my.stock.rdb.repository;

import com.my.stock.rdb.entity.Stocks;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface StocksRepository extends JpaRepository<Stocks, String> {

	@Query(value = "SELECT code FROM stocks WHERE national = :national GROUP BY code", nativeQuery = true)
	List<String> findCodeByNationalGroupByCode(@Param("national")String national);

	List<Stocks> findAllByCode(String code);
}