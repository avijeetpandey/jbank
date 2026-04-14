package com.avijeet.jbank.repositories;

import com.avijeet.jbank.entities.BankAccount;
import com.avijeet.jbank.entities.FixedDeposit;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface FixedDepositRepository extends JpaRepository<FixedDeposit, Long> {
    List<FixedDeposit> findByAccountOrderByCreatedAtDesc(BankAccount account);
}

