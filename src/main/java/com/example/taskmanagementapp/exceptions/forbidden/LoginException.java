package com.example.taskmanagementapp.exceptions.forbidden;

public class LoginException extends RuntimeException {
    public LoginException(String message) {
        super(message);
    }
}
