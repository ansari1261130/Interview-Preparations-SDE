package com.lld_practice.LLD.BankingDepositWithdraw.controller;

import com.lld_practice.LLD.BankingDepositWithdraw.dto.TransactionRequest;
import com.lld_practice.LLD.BankingDepositWithdraw.entity.BankAccount;
import com.lld_practice.LLD.BankingDepositWithdraw.service.BankingDepositWithdrawService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;

@RestController
@RequestMapping("/api/banking")
@RequiredArgsConstructor
public class BankingDepositWithdrawController {
    private final BankingDepositWithdrawService bankingService;

    @PostMapping("/deposit")
    public ResponseEntity<String> deposit(
            @RequestBody TransactionRequest request) {

        bankingService.deposit(request);

        return ResponseEntity.ok("Amount deposited successfully");
    }

    @PostMapping("/withdraw")
    public ResponseEntity<String> withdraw(
            @RequestBody TransactionRequest request) {

        bankingService.withdraw(request);

        return ResponseEntity.ok("Amount withdrawn successfully");
    }

    @GetMapping("/account/balance")
    public ResponseEntity<BigDecimal> getBalance(@RequestParam Long accountId) {
        BigDecimal balance = bankingService.getBalance(accountId);
        return ResponseEntity.ok(balance);
    }
}
