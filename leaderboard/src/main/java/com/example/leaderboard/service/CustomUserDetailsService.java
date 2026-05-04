package com.example.leaderboard.service;

import com.example.leaderboard.model.AppUser;
import com.example.leaderboard.repository.AppUserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    @Autowired
    private AppUserRepository appUserRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        // 1. Go to PostgreSQL and find the user
        AppUser dbUser = appUserRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found in DB!"));

        // 2. Translate our AppUser into the official Spring Security User object
        return User.builder()
                .username(dbUser.getUsername())
                .password(dbUser.getPassword())
                .roles(dbUser.getRole())
                .build();
    }
}