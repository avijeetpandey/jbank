package com.avijeet.jbank.repositories;

import com.avijeet.jbank.entities.AppUser;
import com.avijeet.jbank.entities.BankAccount;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface BankAccountRepository extends JpaRepository<BankAccount, Long> {
    Optional<BankAccount> findByAccountNumber(String accountNumber);

    Optional<BankAccount> findByUser(AppUser user);

    boolean existsByAccountNumber(String accountNumber);
}

