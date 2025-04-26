package com.example.taskmanagementapp.exceptions.conflictexpections;

public class PasswordMismatchException extends Exception {
    public PasswordMismatchException(String message) {
        super(message);
    }
}
