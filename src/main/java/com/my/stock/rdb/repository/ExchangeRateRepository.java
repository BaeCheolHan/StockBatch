package com.my.stock.rdb.repository;

import com.my.stock.rdb.entity.ExchangeRate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ExchangeRateRepository extends JpaRepository<ExchangeRate, Long> {
    ExchangeRate findFirstByOrderByIdDesc();
}
