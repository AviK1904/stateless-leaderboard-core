package com.example.leaderboard.config; // Check that this matches your project!

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Autowired
    private com.example.leaderboard.security.JwtRequestFilter jwtRequestFilter;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable()) // Disable CSRF protection for our simple API
                .cors(Customizer.withDefaults()) // Keep allowing React to talk to us

                .sessionManagement(session -> session.sessionCreationPolicy(org.springframework.security.config.http.SessionCreationPolicy.STATELESS))
                .addFilterBefore(jwtRequestFilter, org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter.class)

                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(HttpMethod.GET, "/api/players/**").permitAll() // PUBLIC: Anyone can view scores
                        .requestMatchers(HttpMethod.POST, "/api/auth/register").permitAll() // <-- NEW: Allow public registration!
                        .requestMatchers(HttpMethod.POST, "/api/auth/login").permitAll()
                        .requestMatchers("/ws/**").permitAll() // PUBLIC: Let the WebSockets flow freely
                        .anyRequest().authenticated() // LOCKED: Everything else (POST/DELETE) requires a password!
                )
                .httpBasic(basic -> basic.disable()); // Use basic HTTP authentication

        return http.build();
    }
    /* Before JWT tokens */
    // When React call an api which do not have the authorisation the bouncer gives the details of login to
    // authenticationManager(inbuilt spring function) then AuthenManager need to know if username exist and then it calls
    // UserDetailService and hand the username to it and than it check if username exist in database
    // then postgres return the row including scrambled password Now the bouncer has secret key from react and the
    // key from database and then it uses the encoder below on the key from react and compare
    // if it check out the request is authenticated

    @Bean
    public org.springframework.security.crypto.password.PasswordEncoder passwordEncoder() {
        return new org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder();
    }

    @Bean
    public org.springframework.security.authentication.AuthenticationManager authenticationManager(
            org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }


}