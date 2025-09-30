package com.my.stock.rdb.repository;

import com.my.stock.rdb.entity.AccountDailyReturn;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface AccountDailyReturnRepository extends JpaRepository<AccountDailyReturn, Long> {
    Optional<AccountDailyReturn> findTopByAccountIdOrderByDateDesc(Long accountId);
    Optional<AccountDailyReturn> findTopByAccountIdAndDateLessThanOrderByDateDesc(Long accountId, LocalDate date);
    List<AccountDailyReturn> findAllByAccountIdAndDateBetweenOrderByDateAsc(Long accountId, LocalDate from, LocalDate to);
}


