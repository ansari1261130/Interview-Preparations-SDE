package com.lld_practice.LLD.BankingDepositWithdraw.repository;

import com.lld_practice.LLD.BankingDepositWithdraw.entity.BankAccount;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface BankAccountRepository extends JpaRepository<BankAccount, Long> {

	@Lock(LockModeType.PESSIMISTIC_WRITE)
	@Query("select account from BankAccount account where account.bankAccountId = :accountId")
	Optional<BankAccount> findByIdForUpdate(@Param("accountId") Long accountId);
}
