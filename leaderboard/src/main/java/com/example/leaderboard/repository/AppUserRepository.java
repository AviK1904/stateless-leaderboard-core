package com.example.leaderboard.repository;

import com.example.leaderboard.model.AppUser;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface AppUserRepository extends JpaRepository<AppUser, Long> {
    // This magic method will search the database for the exact username React sends us
    Optional<AppUser> findByUsername(String username);
}