package com.example.leaderboard.exception;

public class UserAlreadyExistsException extends RuntimeException {
    public UserAlreadyExistsException(String message) {
        super(message);
    }
}