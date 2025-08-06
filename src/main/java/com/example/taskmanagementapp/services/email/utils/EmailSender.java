package com.example.taskmanagementapp.services.email.utils;

import com.example.taskmanagementapp.services.email.EmailService;

public interface EmailSender {
    void send(EmailService.EmailRequest emailRequest);
}
