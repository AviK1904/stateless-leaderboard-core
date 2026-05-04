package com.example.leaderboard.controller;


import com.example.leaderboard.model.Player;
import com.example.leaderboard.repository.PlayerRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@CrossOrigin(origins = "http://localhost:5173")
@RestController
@RequestMapping("/api/players")
public class PlayerController {
    @Autowired
    private org.springframework.messaging.simp.SimpMessagingTemplate messagingTemplate;
    private final PlayerRepository playerRepository;

    public PlayerController(PlayerRepository playerRepository) {
        this.playerRepository = playerRepository;
    }

    // See all players
    @GetMapping
    public List<Player> getAllPlayers() {
        return playerRepository.findAll();
    }

    // NEW: When a new score comes in, ERASE the "topPlayers" cache!
    @PostMapping
    @CacheEvict(value = "topPlayers", allEntries = true,beforeInvocation = true)
    public Player submitScore(@RequestBody Player newPlayer) {
        Player existingPlayer = playerRepository.findByUsername(newPlayer.getUsername());
        Player savedPlayer;

        // 1. Save the data to the database FIRST
        if (existingPlayer != null) {
            existingPlayer.setScore(newPlayer.getScore());
            savedPlayer = playerRepository.save(existingPlayer);
        } else {
            savedPlayer = playerRepository.save(newPlayer);
        }

        // 2. NOW shout to React that the database is officially updated!
        messagingTemplate.convertAndSend("/topic/leaderboard", "UPDATE_PLEASE");

        // 3. Finish the request
        return savedPlayer;
    }

    // 🟢 NEW: Memorize this result in Redis under the name "topPlayers"!
    @GetMapping("/top")
    @Cacheable(value = "topPlayers")
    public List<Player> getTopPlayers() {
        return playerRepository.findTop10ByOrderByScoreDesc();
    }

    // 🗑️ NEW DELETE ENDPOINT
    @DeleteMapping("/{id}")
    @CacheEvict(value = "topPlayers", allEntries = true,beforeInvocation = true) // <-- THIS IS CRITICAL // Wipes the Redis cache so it recalculates!
    public void deletePlayer(@PathVariable Long id) {
        playerRepository.deleteById(id);
        messagingTemplate.convertAndSend("/topic/leaderboard", "UPDATE_PLEASE");
    }
}