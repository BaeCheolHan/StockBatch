package com.my.stock.rdb.repository;

import com.my.stock.rdb.entity.DepositWithdrawalHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DepositWithdrawalHistoryRepository extends JpaRepository<DepositWithdrawalHistory, Long> {
}
