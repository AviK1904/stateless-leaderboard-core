package com.example.leaderboard.controller;

import com.example.leaderboard.model.AppUser;
import com.example.leaderboard.repository.AppUserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "http://localhost:5173") // Allows React to talk to this endpoint
public class AuthController {

    @Autowired
    private AppUserRepository appUserRepository;

    @Autowired
    private PasswordEncoder passwordEncoder; // <-- Grabbing our calculator from the Toolbox!

    @Autowired
    private org.springframework.security.authentication.AuthenticationManager authenticationManager;

    @Autowired
    private com.example.leaderboard.security.JwtUtil jwtUtil;

    @PostMapping("/register")
    public ResponseEntity<String> registerUser(@RequestBody AppUser newUser) {
        // 1. Check if username is already taken
        if (appUserRepository.findByUsername(newUser.getUsername()).isPresent()) {
            return ResponseEntity.badRequest().body("Error: Username is already taken!");
        }

        // 2. Hash the password BEFORE saving!
        String plainPassword = newUser.getPassword();
        String hashedPassword = passwordEncoder.encode(plainPassword);
        newUser.setPassword(hashedPassword);

        // 3. Give them the ADMIN role by default for our arcade
        newUser.setRole("ADMIN");

        // 4. Save to PostgreSQL
        appUserRepository.save(newUser);

        return ResponseEntity.ok("Admin registered successfully!");
    }

    // --- YOUR NEW LOGIN METHOD ---
    @PostMapping("/login")
    public org.springframework.http.ResponseEntity<String> login(@RequestBody java.util.Map<String, String> request) {
        String username = request.get("username");
        String password = request.get("password");

        // 1. Hand the credentials to the Boss Bouncer
        authenticationManager.authenticate(
                new org.springframework.security.authentication.UsernamePasswordAuthenticationToken(username, password)
        );

        // 2. Mint the wristband
        String token = jwtUtil.generateToken(username);

        // 3. Hand the wristband back to React
        return org.springframework.http.ResponseEntity.ok(token);
    }
}