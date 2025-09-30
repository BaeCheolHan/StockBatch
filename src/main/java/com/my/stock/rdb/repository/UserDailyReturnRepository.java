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
}


