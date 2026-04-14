package com.avijeet.jbank.controllers;

import com.avijeet.jbank.dtos.PaginatedResponse;
import com.avijeet.jbank.dtos.TransactionResponse;
import com.avijeet.jbank.dtos.TransferRequest;
import com.avijeet.jbank.dtos.WithdrawRequest;
import com.avijeet.jbank.services.TransactionService;
import com.avijeet.jbank.utils.ApiResponse;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/transactions")
@PreAuthorize("hasAnyRole('USER','ADMIN')")
public class TransactionController {

    private final TransactionService transactionService;

    public TransactionController(TransactionService transactionService) {
        this.transactionService = transactionService;
    }

    @PostMapping("/transfer")
    public ResponseEntity<ApiResponse<TransactionResponse>> transfer(
            @Valid @RequestBody TransferRequest request,
            Authentication authentication
    ) {
        return ResponseEntity.ok(ApiResponse.success("transfer successful", transactionService.transfer(authentication, request)));
    }

    @PostMapping("/withdraw")
    public ResponseEntity<ApiResponse<TransactionResponse>> withdraw(
            @Valid @RequestBody WithdrawRequest request,
            Authentication authentication
    ) {
        return ResponseEntity.ok(ApiResponse.success("withdraw successful", transactionService.withdraw(authentication, request)));
    }

    @GetMapping("/history")
    public ResponseEntity<ApiResponse<PaginatedResponse<TransactionResponse>>> history(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            Authentication authentication
    ) {
        return ResponseEntity.ok(ApiResponse.success("transaction history", transactionService.getHistory(authentication, page, size)));
    }

    @GetMapping("/mini-statement")
    public ResponseEntity<ApiResponse<List<TransactionResponse>>> miniStatement(Authentication authentication) {
        return ResponseEntity.ok(ApiResponse.success("mini statement", transactionService.miniStatement(authentication)));
    }
}

