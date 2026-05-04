package com.example.leaderboard.repository;

import com.example.leaderboard.model.Player;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List; // Make sure to import this!

@Repository
public interface PlayerRepository extends JpaRepository<Player, Long> {

    Player findByUsername(String username);

    // Spring Boot translates this exact sentence into a complex SQL sorting command!
    List<Player> findTop10ByOrderByScoreDesc();
}