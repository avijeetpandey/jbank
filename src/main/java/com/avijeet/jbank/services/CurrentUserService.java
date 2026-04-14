package com.avijeet.jbank.services;

import com.avijeet.jbank.entities.AppUser;
import com.avijeet.jbank.exceptions.ResourceNotFoundException;
import com.avijeet.jbank.repositories.UserRepository;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

@Service
public class CurrentUserService {

    private final UserRepository userRepository;

    public CurrentUserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public AppUser getCurrentUser(Authentication authentication) {
        return userRepository.findByUsername(authentication.getName())
                .orElseThrow(() -> new ResourceNotFoundException("user not found"));
    }
}

