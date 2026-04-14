package com.avijeet.jbank.controllers;

import com.avijeet.jbank.dtos.AccountResponse;
import com.avijeet.jbank.dtos.CreateAccountRequest;
import com.avijeet.jbank.services.AccountService;
import com.avijeet.jbank.utils.ApiResponse;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;

@RestController
@RequestMapping("/api/accounts")
@PreAuthorize("hasAnyRole('USER','ADMIN')")
public class AccountController {

    private final AccountService accountService;

    public AccountController(AccountService accountService) {
        this.accountService = accountService;
    }

    @PostMapping
    public ResponseEntity<ApiResponse<AccountResponse>> createAccount(
            @Valid @RequestBody CreateAccountRequest request,
            Authentication authentication
    ) {
        return ResponseEntity.ok(ApiResponse.success("account created", accountService.createAccountForCurrentUser(request, authentication)));
    }

    @GetMapping("/me")
    public ResponseEntity<ApiResponse<AccountResponse>> getMyAccount(Authentication authentication) {
        return ResponseEntity.ok(ApiResponse.success("account details", accountService.getCurrentUserAccount(authentication)));
    }

    @GetMapping("/balance")
    public ResponseEntity<ApiResponse<BigDecimal>> getBalance(Authentication authentication) {
        return ResponseEntity.ok(ApiResponse.success("balance", accountService.getCurrentUserBalance(authentication)));
    }

    @PatchMapping("/close")
    public ResponseEntity<ApiResponse<AccountResponse>> close(Authentication authentication) {
        return ResponseEntity.ok(ApiResponse.success("account closed", accountService.closeAccount(authentication)));
    }
}

