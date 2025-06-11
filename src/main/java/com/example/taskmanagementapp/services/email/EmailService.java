package com.example.taskmanagementapp.services.email;

import com.example.taskmanagementapp.services.utils.EmailLinkParameterProvider;
import com.resend.Resend;
import com.resend.core.exception.ResendException;
import com.resend.services.emails.model.CreateEmailOptions;
import com.resend.services.emails.model.CreateEmailResponse;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class EmailService {
    private static final Logger logger = LogManager.getLogger(EmailService.class);
    @Getter
    private final EmailLinkParameterProvider emailLinkParameterProvider;
    @Value("${resend.api.key}")
    private String resendApiKey;
    @Value("${mail}")
    private String senderEmail;

    public void sendMessage(String toEmail, String subject, String body) {
        Resend resend = new Resend(resendApiKey);

        CreateEmailOptions params = CreateEmailOptions.builder()
                .from(senderEmail)
                .to(toEmail)
                .subject(subject)
                .text(body)
                .build();

        try {
            CreateEmailResponse data = resend.emails().send(params);
            logger.info("Response id: {}",
                    data.getId());
        } catch (ResendException e) {
            logger.error(e.getMessage());
        }
    }
}
