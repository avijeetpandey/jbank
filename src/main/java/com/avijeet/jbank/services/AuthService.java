package com.avijeet.jbank.services;

import com.avijeet.jbank.dtos.AuthResponse;
import com.avijeet.jbank.dtos.LoginRequest;
import com.avijeet.jbank.dtos.SignupRequest;
import com.avijeet.jbank.entities.AppUser;
import com.avijeet.jbank.entities.BankAccount;
import com.avijeet.jbank.enums.UserRole;
import com.avijeet.jbank.exceptions.ConflictException;
import com.avijeet.jbank.repositories.UserRepository;
import com.avijeet.jbank.utils.JwtUtils;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtUtils jwtUtils;
    private final AccountService accountService;

    public AuthService(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder,
            AuthenticationManager authenticationManager,
            JwtUtils jwtUtils,
            AccountService accountService
    ) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.authenticationManager = authenticationManager;
        this.jwtUtils = jwtUtils;
        this.accountService = accountService;
    }

    @Transactional
    public AuthResponse signup(SignupRequest request) {
        if (userRepository.existsByUsername(request.username())) {
            throw new ConflictException("username already exists");
        }
        if (userRepository.existsByEmail(request.email())) {
            throw new ConflictException("email already exists");
        }

        AppUser user = new AppUser();
        user.setUsername(request.username());
        user.setPassword(passwordEncoder.encode(request.password()));
        user.setFullName(request.fullName());
        user.setAddress(request.address());
        user.setEmail(request.email());
        user.setRole(UserRole.USER);
        user.setCreatedAt(LocalDateTime.now());
        user = userRepository.save(user);

        BankAccount account = accountService.createAccountForUser(user, request.accountType());

        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.username(), request.password())
        );
        String token = jwtUtils.generateToken(authentication);

        return new AuthResponse(token, "Bearer", user.getUsername(), account.getAccountNumber(), user.getRole().name());
    }

    public AuthResponse login(LoginRequest request) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.username(), request.password())
        );

        String token = jwtUtils.generateToken(authentication);
        AppUser user = userRepository.findByUsername(request.username())
                .orElseThrow();
        String accountNumber = user.getBankAccount() != null ? user.getBankAccount().getAccountNumber() : null;

        return new AuthResponse(token, "Bearer", request.username(), accountNumber, user.getRole().name());
    }
}

