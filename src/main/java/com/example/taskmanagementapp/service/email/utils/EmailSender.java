package com.example.taskmanagementapp.service.email.utils;

import com.example.taskmanagementapp.service.email.EmailService;

public interface EmailSender {
    void send(EmailService.EmailRequest emailRequest);
}
