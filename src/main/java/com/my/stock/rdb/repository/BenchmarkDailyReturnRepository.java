package com.my.stock.rdb.repository;

import java.time.LocalDate;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.my.stock.rdb.entity.BenchmarkDailyReturn;

@Repository
public interface BenchmarkDailyReturnRepository extends JpaRepository<BenchmarkDailyReturn, Long> {
    Optional<BenchmarkDailyReturn> findTopBySymbolOrderByDateDesc(String symbol);
    Optional<BenchmarkDailyReturn> findTopBySymbolAndDateLessThanOrderByDateDesc(String symbol, LocalDate date);
    Optional<BenchmarkDailyReturn> findBySymbolAndDate(String symbol, LocalDate date);

    default void upsert(BenchmarkDailyReturn entity) {
        findBySymbolAndDate(entity.getSymbol(), entity.getDate())
                .ifPresentOrElse(prev -> {
                    prev.setClose(entity.getClose());
                    prev.setDailyReturn(entity.getDailyReturn());
                    prev.setCumIndex(entity.getCumIndex());
                    save(prev);
                }, () -> save(entity));
    }
}


