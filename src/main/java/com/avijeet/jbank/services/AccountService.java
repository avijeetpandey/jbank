package com.avijeet.jbank.services;

import com.avijeet.jbank.constants.AppConstants;
import com.avijeet.jbank.dtos.AccountResponse;
import com.avijeet.jbank.dtos.CreateAccountRequest;
import com.avijeet.jbank.entities.AccountTransaction;
import com.avijeet.jbank.entities.AppUser;
import com.avijeet.jbank.entities.BankAccount;
import com.avijeet.jbank.enums.AccountStatus;
import com.avijeet.jbank.enums.AccountType;
import com.avijeet.jbank.enums.TransactionType;
import com.avijeet.jbank.exceptions.BadRequestException;
import com.avijeet.jbank.exceptions.ConflictException;
import com.avijeet.jbank.exceptions.ResourceNotFoundException;
import com.avijeet.jbank.repositories.AccountTransactionRepository;
import com.avijeet.jbank.repositories.BankAccountRepository;
import com.avijeet.jbank.repositories.FixedDepositRepository;
import com.avijeet.jbank.utils.AccountNumberGenerator;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Service
public class AccountService {

    private final BankAccountRepository bankAccountRepository;
    private final AccountTransactionRepository accountTransactionRepository;
    private final FixedDepositRepository fixedDepositRepository;
    private final CurrentUserService currentUserService;
    private final BalanceCacheService balanceCacheService;

    public AccountService(
            BankAccountRepository bankAccountRepository,
            AccountTransactionRepository accountTransactionRepository,
            FixedDepositRepository fixedDepositRepository,
            CurrentUserService currentUserService,
            BalanceCacheService balanceCacheService
    ) {
        this.bankAccountRepository = bankAccountRepository;
        this.accountTransactionRepository = accountTransactionRepository;
        this.fixedDepositRepository = fixedDepositRepository;
        this.currentUserService = currentUserService;
        this.balanceCacheService = balanceCacheService;
    }

    @Transactional
    public AccountResponse createAccountForCurrentUser(CreateAccountRequest request, Authentication authentication) {
        AppUser user = currentUserService.getCurrentUser(authentication);
        if (bankAccountRepository.findByUser(user).isPresent()) {
            throw new ConflictException("only one account is allowed per user");
        }

        BankAccount account = buildAccount(user, request.accountType());
        account = bankAccountRepository.save(account);

        createAuditTransaction(account, TransactionType.ACCOUNT_OPEN, account.getBalance(), BigDecimal.ZERO, "account opened");
        balanceCacheService.putBalance(account.getAccountNumber(), account.getBalance());

        return toResponse(account);
    }

    @Transactional
    public BankAccount createAccountForUser(AppUser user, AccountType accountType) {
        if (bankAccountRepository.findByUser(user).isPresent()) {
            throw new ConflictException("only one account is allowed per user");
        }

        BankAccount account = buildAccount(user, accountType);
        account = bankAccountRepository.save(account);

        createAuditTransaction(account, TransactionType.ACCOUNT_OPEN, account.getBalance(), BigDecimal.ZERO, "account opened at signup");
        balanceCacheService.putBalance(account.getAccountNumber(), account.getBalance());

        return account;
    }

    @Transactional(readOnly = true)
    public AccountResponse getCurrentUserAccount(Authentication authentication) {
        AppUser user = currentUserService.getCurrentUser(authentication);
        BankAccount account = bankAccountRepository.findByUser(user)
                .orElseThrow(() -> new ResourceNotFoundException("account not found"));
        return toResponse(account);
    }

    @Transactional(readOnly = true)
    public BigDecimal getCurrentUserBalance(Authentication authentication) {
        BankAccount account = getCurrentUserActiveAccount(authentication);
        return balanceCacheService.getBalance(account.getAccountNumber())
                .orElseGet(() -> {
                    balanceCacheService.putBalance(account.getAccountNumber(), account.getBalance());
                    return account.getBalance();
                });
    }

    @Transactional
    public AccountResponse closeAccount(Authentication authentication) {
        BankAccount account = getCurrentUserActiveAccount(authentication);
        if (account.getBalance().compareTo(BigDecimal.ZERO) != 0) {
            throw new BadRequestException("account can be closed only when balance is zero");
        }
        if (!fixedDepositRepository.findByAccountOrderByCreatedAtDesc(account).isEmpty()) {
            throw new BadRequestException("close all fixed deposits before closing account");
        }

        account.setStatus(AccountStatus.CLOSED);
        account.setClosedAt(LocalDateTime.now());
        bankAccountRepository.save(account);

        createAuditTransaction(account, TransactionType.ACCOUNT_CLOSE, BigDecimal.ZERO, BigDecimal.ZERO, "account closed");
        balanceCacheService.evictBalance(account.getAccountNumber());
        return toResponse(account);
    }

    @Transactional(readOnly = true)
    public BankAccount getCurrentUserActiveAccount(Authentication authentication) {
        AppUser user = currentUserService.getCurrentUser(authentication);
        BankAccount account = bankAccountRepository.findByUser(user)
                .orElseThrow(() -> new ResourceNotFoundException("account not found"));

        if (account.getStatus() != AccountStatus.ACTIVE) {
            throw new BadRequestException("account is closed");
        }
        return account;
    }

    @Transactional(readOnly = true)
    public BankAccount getActiveAccountByNumber(String accountNumber) {
        BankAccount account = bankAccountRepository.findByAccountNumber(accountNumber)
                .orElseThrow(() -> new ResourceNotFoundException("account not found"));

        if (account.getStatus() != AccountStatus.ACTIVE) {
            throw new BadRequestException("destination account is closed");
        }
        return account;
    }

    @Transactional
    public void updateBalance(BankAccount account, BigDecimal updatedBalance) {
        account.setBalance(updatedBalance);
        bankAccountRepository.save(account);
        balanceCacheService.putBalance(account.getAccountNumber(), updatedBalance);
    }

    public AccountResponse toResponse(BankAccount account) {
        return new AccountResponse(
                account.getAccountNumber(),
                account.getAccountType(),
                account.getStatus(),
                account.getBalance(),
                account.getMinimumBalance()
        );
    }

    private BankAccount buildAccount(AppUser user, AccountType accountType) {
        BankAccount account = new BankAccount();
        account.setUser(user);
        account.setAccountNumber(generateUniqueAccountNumber());
        account.setAccountType(accountType);
        account.setStatus(AccountStatus.ACTIVE);
        account.setOpenedAt(LocalDateTime.now());

        if (accountType == AccountType.MIN_BALANCE) {
            account.setBalance(AppConstants.SAVINGS_MIN_BALANCE);
            account.setMinimumBalance(AppConstants.SAVINGS_MIN_BALANCE);
        } else {
            account.setBalance(BigDecimal.ZERO);
            account.setMinimumBalance(BigDecimal.ZERO);
        }

        return account;
    }

    private String generateUniqueAccountNumber() {
        for (int i = 0; i < 10; i++) {
            String accountNumber = AccountNumberGenerator.generate();
            if (!bankAccountRepository.existsByAccountNumber(accountNumber)) {
                return accountNumber;
            }
        }
        throw new BadRequestException("unable to generate account number");
    }

    private void createAuditTransaction(
            BankAccount account,
            TransactionType transactionType,
            BigDecimal amount,
            BigDecimal fee,
            String remark
    ) {
        AccountTransaction transaction = new AccountTransaction();
        transaction.setAccount(account);
        transaction.setReferenceId(UUID.randomUUID().toString());
        transaction.setTransactionType(transactionType);
        transaction.setAmount(amount);
        transaction.setFee(fee);
        transaction.setBalanceAfter(account.getBalance());
        transaction.setRemark(remark);
        transaction.setCreatedAt(LocalDateTime.now());
        accountTransactionRepository.save(transaction);
    }
}

