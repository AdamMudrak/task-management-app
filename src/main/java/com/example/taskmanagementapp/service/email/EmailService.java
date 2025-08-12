package com.example.taskmanagementapp.service.email;

import com.example.taskmanagementapp.service.email.utils.EmailQueueProcessor;
import org.springframework.stereotype.Service;

@Service
public class EmailService {
    private final EmailQueueProcessor emailQueueProcessor;

    public EmailService(EmailQueueProcessor emailQueueProcessor) {
        this.emailQueueProcessor = emailQueueProcessor;
    }

    public record EmailRequest(String toEmail, String subject, String body) {}

    public void queueEmail(String toEmail, String subject, String body) {
        emailQueueProcessor.queueEmail(new EmailRequest(toEmail, subject, body));
    }

}
