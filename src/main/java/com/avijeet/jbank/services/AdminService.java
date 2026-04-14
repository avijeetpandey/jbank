package com.avijeet.jbank.services;

import com.avijeet.jbank.dtos.AdminUserResponse;
import com.avijeet.jbank.entities.AppUser;
import com.avijeet.jbank.enums.UserRole;
import com.avijeet.jbank.exceptions.ResourceNotFoundException;
import com.avijeet.jbank.repositories.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class AdminService {

    private final UserRepository userRepository;

    public AdminService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Transactional(readOnly = true)
    public List<AdminUserResponse> listUsers() {
        return userRepository.findAll().stream()
                .map(user -> new AdminUserResponse(user.getId(), user.getUsername(), user.getEmail(), user.getRole().name()))
                .toList();
    }

    @Transactional
    public AdminUserResponse updateUserRole(Long userId, UserRole role) {
        AppUser user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("user not found"));

        user.setRole(role);
        user = userRepository.save(user);

        return new AdminUserResponse(user.getId(), user.getUsername(), user.getEmail(), user.getRole().name());
    }
}

