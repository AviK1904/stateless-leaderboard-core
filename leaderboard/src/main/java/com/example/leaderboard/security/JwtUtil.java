package com.example.leaderboard.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.function.Function;

@Component
public class JwtUtil {

    // 1. Generate a secure Cryptographic Key (Updated syntax for 0.12.x)
    private static final SecretKey SECRET_KEY = Jwts.SIG.HS256.key().build();

    // 2. How long until the wristband expires? (10 hours)
    private static final long EXPIRATION_TIME = 1000 * 60 * 60 * 10;

    // --- MINTING A NEW TOKEN ---
    public String generateToken(String username) {
        return Jwts.builder()
                .subject(username) // The payload (who is this for?)
                .issuedAt(new Date(System.currentTimeMillis())) // Timestamp
                .expiration(new Date(System.currentTimeMillis() + EXPIRATION_TIME)) // Expiration
                .signWith(SECRET_KEY) // The cryptographic signature!
                .compact();
    }

    // --- READING AN EXISTING TOKEN ---
    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    public boolean isTokenValid(String token, String username) {
        final String extractedUsername = extractUsername(token);
        return (extractedUsername.equals(username) && !isTokenExpired(token));
    }

    private boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    private Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    private <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = Jwts.parser()
                .verifyWith(SECRET_KEY) // We use our secret key to verify nobody tampered with the token
                .build()
                .parseSignedClaims(token) // This line cracks open the token mathematically
                .getPayload(); // Grabs the JSON data inside
        return claimsResolver.apply(claims);
    }
}

/* After JWT tokens (The Stateless Method) */

// --- PHASE 1: GETTING THE WRISTBAND (Login) ---
// React sends the username and password to our custom /api/auth/login endpoint.
// The Boss (AuthenticationManager) checks the database to verify the password, just like the old way.
// BUT, once verified, the server does NOT keep the user logged in in its memory.
// Instead, the server uses JwtUtil (The Printer) to mint a "wristband" (JWT string).
// This wristband has the username, an expiration time, and an unforgeable math Signature.
// The server hands this string to React, and React saves it in LocalStorage (The Glovebox).


// --- PHASE 2: USING THE WRISTBAND (Protected API Calls) ---
// When React calls an API that needs authorization (like deleting a player)...
// React grabs the token from the glovebox and pins it to the HTTP Header ("Authorization: Bearer <token>").
// The request hits our new JwtFilter (The Ticket Inspector) BEFORE the main Bouncer.
// The Inspector chops off the word "Bearer ", and hands the token to JwtUtil (The Scanner).
// The Scanner reads the data, and uses the Server's Secret Key to re-calculate the math.
// If the calculated math matches the Signature on the token, and the time hasn't expired...
// The Inspector says "Math is good!" and lets the request pass straight to the database.
// THE BIG DIFFERENCE: In Phase 2, the server NEVER had to look up the user in PostgreSQL!