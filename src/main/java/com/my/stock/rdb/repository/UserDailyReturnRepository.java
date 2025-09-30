package com.my.stock.rdb.repository;

import com.my.stock.rdb.entity.UserDailyReturn;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface UserDailyReturnRepository extends JpaRepository<UserDailyReturn, Long> {
    Optional<UserDailyReturn> findTopByUserIdOrderByDateDesc(String userId);
    Optional<UserDailyReturn> findTopByUserIdAndDateLessThanOrderByDateDesc(String userId, LocalDate date);
    List<UserDailyReturn> findAllByUserIdAndDateBetweenOrderByDateAsc(String userId, LocalDate from, LocalDate to);
    Optional<UserDailyReturn> findByUserIdAndDate(String userId, LocalDate date);

    default void upsert(UserDailyReturn entity) {
        findByUserIdAndDate(entity.getUserId(), entity.getDate())
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


