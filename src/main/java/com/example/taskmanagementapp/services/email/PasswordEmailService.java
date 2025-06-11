package com.example.taskmanagementapp.services.email;

import static com.example.taskmanagementapp.constants.Constants.FIRST_POSITION;
import static com.example.taskmanagementapp.constants.Constants.SECOND_POSITION;
import static com.example.taskmanagementapp.constants.Constants.SPLITERATOR;
import static com.example.taskmanagementapp.constants.security.SecurityConstants.INITIATE_RANDOM_PASSWORD_BODY;
import static com.example.taskmanagementapp.constants.security.SecurityConstants.INITIATE_RANDOM_PASSWORD_SUBJECT;
import static com.example.taskmanagementapp.constants.security.SecurityConstants.RANDOM_PASSWORD_BODY;
import static com.example.taskmanagementapp.constants.security.SecurityConstants.RANDOM_PASSWORD_BODY_2;
import static com.example.taskmanagementapp.constants.security.SecurityConstants.RANDOM_PASSWORD_SUBJECT;

import com.example.taskmanagementapp.services.utils.EmailLinkParameterProvider;
import com.example.taskmanagementapp.services.utils.TestCaptureService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class PasswordEmailService extends EmailService {
    @Value("${server.path}")
    private String serverPath;

    public PasswordEmailService(EmailLinkParameterProvider emailLinkParameterProvider) {
        super(emailLinkParameterProvider);
    }

    public void sendInitiatePasswordReset(String toEmail) {
        String[] paramTokenPair = getEmailLinkParameterProvider().formRandomParamTokenPair(toEmail);

        sendMessage(toEmail, INITIATE_RANDOM_PASSWORD_SUBJECT,
                INITIATE_RANDOM_PASSWORD_BODY + System.lineSeparator()
                        + serverPath + "/auth/reset-password?"
                        + paramTokenPair[FIRST_POSITION]
                        + SPLITERATOR
                        + paramTokenPair[SECOND_POSITION]);

        TestCaptureService.capture(paramTokenPair);
    }

    public void sendResetPassword(String toEmail, String randomPassword) {
        sendMessage(toEmail, RANDOM_PASSWORD_SUBJECT,
                RANDOM_PASSWORD_BODY
                        + System.lineSeparator()
                        + randomPassword
                        + System.lineSeparator()
                        + RANDOM_PASSWORD_BODY_2);

        TestCaptureService.capture(new String[]{randomPassword});
    }
}
