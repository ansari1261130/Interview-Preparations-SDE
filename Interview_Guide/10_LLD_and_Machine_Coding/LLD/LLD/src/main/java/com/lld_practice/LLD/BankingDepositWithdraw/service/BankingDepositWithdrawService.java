package com.lld_practice.LLD.BankingDepositWithdraw.service;

import com.lld_practice.LLD.BankingDepositWithdraw.dto.TransactionRequest;
import com.lld_practice.LLD.BankingDepositWithdraw.entity.BankAccount;
import com.lld_practice.LLD.BankingDepositWithdraw.repository.BankAccountRepository;
import jakarta.persistence.EntityNotFoundException;
import java.util.Objects;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Service
public class BankingDepositWithdrawService {

    private final BankAccountRepository bankAccountRepository;

    public BankingDepositWithdrawService(BankAccountRepository bankAccountRepository) {
        this.bankAccountRepository = Objects.requireNonNull(
                bankAccountRepository, "bankAccountRepository cannot be null");
    }

    private BankAccount getAccountForUpdate(Long accountId) {
        if (accountId == null || accountId <= 0) {
            throw new IllegalArgumentException("accountId must be positive");
        }

        return bankAccountRepository.findByIdForUpdate(accountId)
                .orElseThrow(() ->
                        new EntityNotFoundException("Bank account not found: " + accountId));
    }

    @Transactional
    public void deposit(TransactionRequest request) {
        Objects.requireNonNull(request, "request cannot be null");
        BankAccount account = getAccountForUpdate(request.getAccountId());
        account.deposit(request.getAmount());
    }

    @Transactional
    public void withdraw(TransactionRequest request) {
        Objects.requireNonNull(request, "request cannot be null");
        BankAccount account = getAccountForUpdate(request.getAccountId());
        account.withdraw(request.getAmount());
    }

    @Transactional(readOnly = true)
    public BigDecimal getBalance(Long accountId) {
        if (accountId == null || accountId <= 0) {
            throw new IllegalArgumentException("accountId must be positive");
        }
        return bankAccountRepository.findById(accountId)
                .orElseThrow(() ->
                        new EntityNotFoundException("Bank account not found: " + accountId))
                .getBalance();
    }
}
