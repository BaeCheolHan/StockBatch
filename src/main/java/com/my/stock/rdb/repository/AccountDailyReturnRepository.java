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
    Optional<AccountDailyReturn> findByAccountIdAndDate(Long accountId, LocalDate date);

    default void upsert(AccountDailyReturn entity) {
        findByAccountIdAndDate(entity.getAccountId(), entity.getDate())
                .ifPresentOrElse(prev -> {
                    prev.setNavBegin(entity.getNavBegin());
                    prev.setNavEnd(entity.getNavEnd());
                    prev.setNetFlow(entity.getNetFlow());
                    prev.setDailyTwr(entity.getDailyTwr());
                    prev.setCumIndex(entity.getCumIndex());
                    save(prev);
                }, () -> save(entity));
    }
}


