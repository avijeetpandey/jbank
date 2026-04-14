package com.avijeet.jbank.repositories;

import com.avijeet.jbank.entities.AccountTransaction;
import com.avijeet.jbank.entities.BankAccount;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AccountTransactionRepository extends JpaRepository<AccountTransaction, Long> {
    Page<AccountTransaction> findByAccountOrderByCreatedAtDesc(BankAccount account, Pageable pageable);

    List<AccountTransaction> findTop5ByAccountOrderByCreatedAtDesc(BankAccount account);
}

