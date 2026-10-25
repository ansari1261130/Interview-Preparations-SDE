package com.lld_practice.LLD.BankingDepositWithdraw.repository;

import com.lld_practice.LLD.BankingDepositWithdraw.entity.Customer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CustomerRepository extends JpaRepository<Customer, Long> {
}
