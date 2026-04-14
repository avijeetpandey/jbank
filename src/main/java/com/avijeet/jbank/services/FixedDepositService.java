package com.avijeet.jbank.services;

import com.avijeet.jbank.constants.AppConstants;
import com.avijeet.jbank.dtos.FixedDepositRequest;
import com.avijeet.jbank.dtos.FixedDepositResponse;
import com.avijeet.jbank.entities.AccountTransaction;
import com.avijeet.jbank.entities.BankAccount;
import com.avijeet.jbank.entities.FixedDeposit;
import com.avijeet.jbank.enums.FdStatus;
import com.avijeet.jbank.enums.TransactionType;
import com.avijeet.jbank.exceptions.BadRequestException;
import com.avijeet.jbank.repositories.AccountTransactionRepository;
import com.avijeet.jbank.repositories.FixedDepositRepository;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
public class FixedDepositService {

    private final AccountService accountService;
    private final FixedDepositRepository fixedDepositRepository;
    private final AccountTransactionRepository accountTransactionRepository;

    public FixedDepositService(
            AccountService accountService,
            FixedDepositRepository fixedDepositRepository,
            AccountTransactionRepository accountTransactionRepository
    ) {
        this.accountService = accountService;
        this.fixedDepositRepository = fixedDepositRepository;
        this.accountTransactionRepository = accountTransactionRepository;
    }

    @Transactional
    public FixedDepositResponse createFd(Authentication authentication, FixedDepositRequest request) {
        BankAccount account = accountService.getCurrentUserActiveAccount(authentication);

        if (request.amount().compareTo(AppConstants.MIN_FD_AMOUNT) < 0) {
            throw new BadRequestException("minimum FD amount is " + AppConstants.MIN_FD_AMOUNT);
        }

        BigDecimal maxUsable = account.getBalance().subtract(account.getMinimumBalance());
        if (maxUsable.compareTo(request.amount()) < 0) {
            throw new BadRequestException("insufficient balance for FD creation");
        }

        BigDecimal yearlyRate = BigDecimal.valueOf(AppConstants.FD_INTEREST_RATE);
        BigDecimal tenureFactor = BigDecimal.valueOf((double) request.tenure().getMonths() / 12.0);
        BigDecimal interest = request.amount().multiply(yearlyRate).multiply(tenureFactor)
                .setScale(2, RoundingMode.HALF_UP);
        BigDecimal maturity = request.amount().add(interest);

        FixedDeposit fd = new FixedDeposit();
        fd.setAccount(account);
        fd.setPrincipalAmount(request.amount());
        fd.setTenure(request.tenure());
        fd.setInterestRate(yearlyRate);
        fd.setMaturityAmount(maturity);
        fd.setStatus(FdStatus.ACTIVE);
        fd.setStartDate(LocalDate.now());
        fd.setMaturityDate(LocalDate.now().plusMonths(request.tenure().getMonths()));
        fd.setCreatedAt(LocalDateTime.now());
        fd = fixedDepositRepository.save(fd);

        BigDecimal updatedBalance = account.getBalance().subtract(request.amount());
        accountService.updateBalance(account, updatedBalance);

        AccountTransaction transaction = new AccountTransaction();
        transaction.setAccount(account);
        transaction.setReferenceId(UUID.randomUUID().toString());
        transaction.setTransactionType(TransactionType.FD_CREATION);
        transaction.setAmount(request.amount());
        transaction.setFee(BigDecimal.ZERO);
        transaction.setBalanceAfter(updatedBalance);
        transaction.setRemark("FD created for " + request.tenure().name());
        transaction.setCreatedAt(LocalDateTime.now());
        accountTransactionRepository.save(transaction);

        return toResponse(fd);
    }

    @Transactional(readOnly = true)
    public List<FixedDepositResponse> getFds(Authentication authentication) {
        BankAccount account = accountService.getCurrentUserActiveAccount(authentication);
        return fixedDepositRepository.findByAccountOrderByCreatedAtDesc(account)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    private FixedDepositResponse toResponse(FixedDeposit fd) {
        return new FixedDepositResponse(
                fd.getId(),
                fd.getPrincipalAmount(),
                fd.getTenure(),
                fd.getInterestRate(),
                fd.getMaturityAmount(),
                fd.getStatus(),
                fd.getStartDate(),
                fd.getMaturityDate()
        );
    }
}

