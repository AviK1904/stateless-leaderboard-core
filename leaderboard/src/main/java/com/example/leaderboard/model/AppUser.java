package com.example.leaderboard.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
public class AppUser {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true)
    private String username;
    private String password; // We will store encrypted passwords here!
    private String role; // E.g., "ADMIN"

    // Add standard Getters and Setters here...

}