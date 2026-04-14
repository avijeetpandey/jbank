package com.avijeet.jbank.services;

import com.avijeet.jbank.constants.AppConstants;
import com.avijeet.jbank.dtos.PaginatedResponse;
import com.avijeet.jbank.dtos.TransactionResponse;
import com.avijeet.jbank.dtos.TransferRequest;
import com.avijeet.jbank.dtos.WithdrawRequest;
import com.avijeet.jbank.entities.AccountTransaction;
import com.avijeet.jbank.entities.BankAccount;
import com.avijeet.jbank.enums.TransactionType;
import com.avijeet.jbank.exceptions.BadRequestException;
import com.avijeet.jbank.repositories.AccountTransactionRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
public class TransactionService {

    private final AccountService accountService;
    private final AccountTransactionRepository accountTransactionRepository;

    public TransactionService(AccountService accountService, AccountTransactionRepository accountTransactionRepository) {
        this.accountService = accountService;
        this.accountTransactionRepository = accountTransactionRepository;
    }

    @Transactional
    public TransactionResponse transfer(Authentication authentication, TransferRequest request) {
        BankAccount source = accountService.getCurrentUserActiveAccount(authentication);
        BankAccount destination = accountService.getActiveAccountByNumber(request.toAccountNumber());

        if (source.getAccountNumber().equals(destination.getAccountNumber())) {
            throw new BadRequestException("cannot transfer to same account");
        }

        BigDecimal totalDebit = request.amount().add(AppConstants.TRANSACTION_FEE);
        validateDebit(source, totalDebit);

        BigDecimal sourceNewBalance = source.getBalance().subtract(totalDebit);
        BigDecimal destinationNewBalance = destination.getBalance().add(request.amount());

        accountService.updateBalance(source, sourceNewBalance);
        accountService.updateBalance(destination, destinationNewBalance);

        AccountTransaction debitTxn = buildTransaction(
                source,
                TransactionType.TRANSFER,
                request.amount(),
                AppConstants.TRANSACTION_FEE,
                sourceNewBalance,
                destination.getAccountNumber(),
                request.remark() == null ? "transfer" : request.remark()
        );
        accountTransactionRepository.save(debitTxn);

        AccountTransaction creditTxn = buildTransaction(
                destination,
                TransactionType.TRANSFER,
                request.amount(),
                BigDecimal.ZERO,
                destinationNewBalance,
                source.getAccountNumber(),
                "incoming transfer"
        );
        accountTransactionRepository.save(creditTxn);

        return toResponse(debitTxn);
    }

    @Transactional
    public TransactionResponse withdraw(Authentication authentication, WithdrawRequest request) {
        BankAccount account = accountService.getCurrentUserActiveAccount(authentication);
        BigDecimal totalDebit = request.amount().add(AppConstants.TRANSACTION_FEE);

        validateDebit(account, totalDebit);
        BigDecimal updatedBalance = account.getBalance().subtract(totalDebit);
        accountService.updateBalance(account, updatedBalance);

        AccountTransaction transaction = buildTransaction(
                account,
                TransactionType.WITHDRAWAL,
                request.amount(),
                AppConstants.TRANSACTION_FEE,
                updatedBalance,
                null,
                request.remark() == null ? "withdrawal" : request.remark()
        );

        accountTransactionRepository.save(transaction);
        return toResponse(transaction);
    }

    @Transactional(readOnly = true)
    public PaginatedResponse<TransactionResponse> getHistory(Authentication authentication, int page, int size) {
        BankAccount account = accountService.getCurrentUserActiveAccount(authentication);
        Pageable pageable = PageRequest.of(page, size);
        Page<AccountTransaction> transactionPage = accountTransactionRepository.findByAccountOrderByCreatedAtDesc(account, pageable);

        List<TransactionResponse> content = transactionPage.getContent().stream()
                .map(this::toResponse)
                .toList();

        return new PaginatedResponse<>(
                content,
                transactionPage.getNumber(),
                transactionPage.getSize(),
                transactionPage.getTotalElements(),
                transactionPage.getTotalPages(),
                transactionPage.isLast()
        );
    }

    @Transactional(readOnly = true)
    public List<TransactionResponse> miniStatement(Authentication authentication) {
        BankAccount account = accountService.getCurrentUserActiveAccount(authentication);
        return accountTransactionRepository.findTop5ByAccountOrderByCreatedAtDesc(account).stream()
                .map(this::toResponse)
                .toList();
    }

    private void validateDebit(BankAccount account, BigDecimal totalDebit) {
        BigDecimal minAfterDebit = account.getMinimumBalance();
        BigDecimal allowedToUse = account.getBalance().subtract(minAfterDebit);
        if (allowedToUse.compareTo(totalDebit) < 0) {
            throw new BadRequestException("insufficient balance after applying minimum balance and fees");
        }
    }

    private AccountTransaction buildTransaction(
            BankAccount account,
            TransactionType transactionType,
            BigDecimal amount,
            BigDecimal fee,
            BigDecimal balanceAfter,
            String counterpartyAccountNumber,
            String remark
    ) {
        AccountTransaction transaction = new AccountTransaction();
        transaction.setAccount(account);
        transaction.setReferenceId(UUID.randomUUID().toString());
        transaction.setTransactionType(transactionType);
        transaction.setAmount(amount);
        transaction.setFee(fee);
        transaction.setBalanceAfter(balanceAfter);
        transaction.setCounterpartyAccountNumber(counterpartyAccountNumber);
        transaction.setRemark(remark);
        transaction.setCreatedAt(LocalDateTime.now());
        return transaction;
    }

    private TransactionResponse toResponse(AccountTransaction transaction) {
        return new TransactionResponse(
                transaction.getReferenceId(),
                transaction.getTransactionType(),
                transaction.getAmount(),
                transaction.getFee(),
                transaction.getBalanceAfter(),
                transaction.getCounterpartyAccountNumber(),
                transaction.getRemark(),
                transaction.getCreatedAt()
        );
    }
}

