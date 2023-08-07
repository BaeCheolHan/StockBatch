package com.my.stock.rdb.repository;

import com.my.stock.rdb.entity.BankAccount;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BankAccountRepository extends JpaRepository<BankAccount, Long> {
	List<BankAccount> findAllByMemberId(Long memberId);
}
