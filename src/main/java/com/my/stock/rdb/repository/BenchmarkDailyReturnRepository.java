package com.my.stock.rdb.repository;

import com.my.stock.rdb.entity.BenchmarkDailyReturn;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface BenchmarkDailyReturnRepository extends JpaRepository<BenchmarkDailyReturn, Long> {
    Optional<BenchmarkDailyReturn> findTopBySymbolOrderByDateDesc(String symbol);
    Optional<BenchmarkDailyReturn> findTopBySymbolAndDateLessThanOrderByDateDesc(String symbol, LocalDate date);
    List<BenchmarkDailyReturn> findAllBySymbolAndDateBetweenOrderByDateAsc(String symbol, LocalDate from, LocalDate to);
    Optional<BenchmarkDailyReturn> findBySymbolAndDate(String symbol, LocalDate date);
}


