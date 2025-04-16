package com.example.taskmanagementapp.exceptions.notfoundexceptions;

public class ActionNotFoundException extends RuntimeException {
    public ActionNotFoundException(String message) {
        super(message);
    }
}
