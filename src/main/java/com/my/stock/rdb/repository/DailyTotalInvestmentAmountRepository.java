package com.my.stock.rdb.repository;

import com.my.stock.rdb.entity.DailyTotalInvestmentAmount;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.Optional;

@Repository
public interface DailyTotalInvestmentAmountRepository extends JpaRepository<DailyTotalInvestmentAmount, Long> {
    Optional<DailyTotalInvestmentAmount> findByMember_IdAndDate(String memberId, LocalDate date);

    default void upsert(DailyTotalInvestmentAmount entity) {
        String memberId = entity.getMember().getId();
        findByMember_IdAndDate(memberId, entity.getDate())
                .ifPresentOrElse(prev -> {
                    prev.setTotalInvestmentAmount(entity.getTotalInvestmentAmount());
                    prev.setEvaluationAmount(entity.getEvaluationAmount());
                    save(prev);
                }, () -> save(entity));
    }
}
