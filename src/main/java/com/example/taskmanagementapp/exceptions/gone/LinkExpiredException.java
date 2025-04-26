package com.example.taskmanagementapp.exceptions.gone;

public class LinkExpiredException extends RuntimeException {
    public LinkExpiredException(String message) {
        super(message);
    }
}
