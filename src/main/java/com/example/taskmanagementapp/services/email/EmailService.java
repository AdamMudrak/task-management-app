package com.example.taskmanagementapp.services.email;

import static com.example.taskmanagementapp.constants.security.SecurityConstants.MAILER_SEND_API_KEY;
import static com.example.taskmanagementapp.constants.security.SecurityConstants.SENDER_EMAIL;

import com.mailersend.sdk.MailerSend;
import com.mailersend.sdk.MailerSendResponse;
import com.mailersend.sdk.emails.Email;
import com.mailersend.sdk.exceptions.MailerSendException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class EmailService {
    private static final Logger logger = LogManager.getLogger(EmailService.class);
    @Value(SENDER_EMAIL)
    protected String supportEmail;
    @Value(MAILER_SEND_API_KEY)
    private String mailerSendApiKey;

    public void sendMessage(String toEmail, String subject, String body) {
        Email email = new Email();

        email.setFrom(supportEmail, supportEmail);
        email.addRecipient(toEmail, toEmail);
        email.setSubject(subject);
        email.setPlain(body);
        MailerSend ms = new MailerSend();
        ms.setToken(mailerSendApiKey);

        try {
            MailerSendResponse response = ms.emails().send(email);
            logger.info("Email sent with code: {}",
                    response.responseStatusCode);
        } catch (MailerSendException e) {
            logger.error(e.getMessage());
        }
    }
}
