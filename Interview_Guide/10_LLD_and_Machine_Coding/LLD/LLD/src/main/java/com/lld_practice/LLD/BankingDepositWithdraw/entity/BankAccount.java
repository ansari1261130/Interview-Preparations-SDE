package com.lld_practice.LLD.BankingDepositWithdraw.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.Objects;

@Entity
@Getter
@NoArgsConstructor(access = lombok.AccessLevel.PROTECTED)
@Table(name = "bank_accounts")
public class BankAccount {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "bank_account_id", nullable = false)
    private Long bankAccountId;

    @Column(name = "account_number", nullable = false, unique = true)
    private Long accountNumber;

    @Enumerated(EnumType.STRING)
    @Column(name = "account_type", nullable = false)
    private AccountType accountType;

    @Column(name = "balance", nullable = false)
    private BigDecimal balance = BigDecimal.ZERO;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_id", nullable = false)
    private Customer customer;

    public BankAccount(
            Long accountNumber,
            AccountType accountType,
            BigDecimal initialBalance,
            Customer customer) {
        if (accountNumber == null || accountNumber <= 0) {
            throw new IllegalArgumentException("accountNumber must be positive");
        }
        this.accountNumber = accountNumber;
        this.accountType = Objects.requireNonNull(accountType, "accountType cannot be null");
        this.balance = Objects.requireNonNull(initialBalance, "initialBalance cannot be null");
        if (initialBalance.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("initialBalance cannot be negative");
        }
        this.customer = Objects.requireNonNull(customer, "customer cannot be null");
    }

    public void deposit(BigDecimal amount) {
        validatePositiveAmount(amount);
        balance = currentBalance().add(amount);
    }

    public void withdraw(BigDecimal amount) {
        validatePositiveAmount(amount);
        BigDecimal currentBalance = currentBalance();
        if (currentBalance.compareTo(amount) < 0) {
            throw new IllegalArgumentException("Insufficient balance");
        }
        balance = currentBalance.subtract(amount);
    }

    private BigDecimal currentBalance() {
        return Objects.requireNonNull(balance, "balance cannot be null");
    }

    private static void validatePositiveAmount(BigDecimal amount) {
        Objects.requireNonNull(amount, "amount cannot be null");
        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Amount must be greater than zero");
        }
    }
}