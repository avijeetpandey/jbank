package com.avijeet.jbank.controllers;

import com.avijeet.jbank.dtos.FixedDepositRequest;
import com.avijeet.jbank.dtos.FixedDepositResponse;
import com.avijeet.jbank.services.FixedDepositService;
import com.avijeet.jbank.utils.ApiResponse;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/fds")
@PreAuthorize("hasAnyRole('USER','ADMIN')")
public class FixedDepositController {

    private final FixedDepositService fixedDepositService;

    public FixedDepositController(FixedDepositService fixedDepositService) {
        this.fixedDepositService = fixedDepositService;
    }

    @PostMapping
    public ResponseEntity<ApiResponse<FixedDepositResponse>> createFd(
            @Valid @RequestBody FixedDepositRequest request,
            Authentication authentication
    ) {
        return ResponseEntity.ok(ApiResponse.success("fd created", fixedDepositService.createFd(authentication, request)));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<FixedDepositResponse>>> listFds(Authentication authentication) {
        return ResponseEntity.ok(ApiResponse.success("fd list", fixedDepositService.getFds(authentication)));
    }
}

