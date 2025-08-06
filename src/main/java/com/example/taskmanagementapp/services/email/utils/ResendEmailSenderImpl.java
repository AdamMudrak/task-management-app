package com.example.taskmanagementapp.services.email.utils;

import com.example.taskmanagementapp.services.email.EmailService;
import com.resend.Resend;
import com.resend.core.exception.ResendException;
import com.resend.services.emails.model.CreateEmailOptions;
import com.resend.services.emails.model.CreateEmailResponse;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class ResendEmailSenderImpl implements EmailSender {
    private static final Logger logger = LogManager.getLogger(ResendEmailSenderImpl.class);

    private final Resend resendClient;
    private final String senderEmail;

    public ResendEmailSenderImpl(@Value("${resend.api.key}") String apiKey,
                                 @Value("${mail}") String senderEmail) {
        this.resendClient = new Resend(apiKey);
        this.senderEmail = senderEmail;
    }

    @Override
    public void send(EmailService.EmailRequest emailRequest) {
        CreateEmailOptions params = CreateEmailOptions.builder()
                .from(senderEmail)
                .to(emailRequest.toEmail())
                .subject(emailRequest.subject())
                .text(emailRequest.body())
                .build();
        try {
            CreateEmailResponse response = resendClient.emails().send(params);
            logger.info("Email sent to {} with ID: {}", emailRequest.toEmail(), response.getId());
        } catch (ResendException e) {
            logger.error("Failed to send email to {}: {}", emailRequest.toEmail(), e.getMessage());
        }
    }
}
