package com.example.taskmanagementapp.security.email;

import static com.example.taskmanagementapp.constants.Constants.SPLITERATOR;
import static com.example.taskmanagementapp.constants.security.SecurityConstants.CONFIRMATION;
import static com.example.taskmanagementapp.constants.security.SecurityConstants.CONFIRMATION_PATH;
import static com.example.taskmanagementapp.constants.security.SecurityConstants.CONFIRM_REGISTRATION_BODY;
import static com.example.taskmanagementapp.constants.security.SecurityConstants.CONFIRM_REGISTRATION_SUBJECT;
import static com.example.taskmanagementapp.constants.security.SecurityConstants.INITIATE_RANDOM_PASSWORD_BODY;
import static com.example.taskmanagementapp.constants.security.SecurityConstants.INITIATE_RANDOM_PASSWORD_SUBJECT;
import static com.example.taskmanagementapp.constants.security.SecurityConstants.RANDOM_PASSWORD_BODY;
import static com.example.taskmanagementapp.constants.security.SecurityConstants.RANDOM_PASSWORD_BODY_2;
import static com.example.taskmanagementapp.constants.security.SecurityConstants.RANDOM_PASSWORD_SUBJECT;
import static com.example.taskmanagementapp.constants.security.SecurityConstants.RESET;
import static com.example.taskmanagementapp.constants.security.SecurityConstants.RESET_PATH;

import com.example.taskmanagementapp.exceptions.notfoundexceptions.ActionNotFoundException;
import com.example.taskmanagementapp.security.utils.EmailLinkParameterProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class PasswordEmailService extends EmailService {
    private final EmailLinkParameterProvider emailLinkParameterProvider;
    @Value(RESET_PATH)
    private String resetPath;
    @Value(CONFIRMATION_PATH)
    private String confirmationPath;

    public PasswordEmailService(JavaMailSender mailSender,
                                EmailLinkParameterProvider emailLinkParameterProvider) {
        super(mailSender);
        this.emailLinkParameterProvider = emailLinkParameterProvider;
    }

    public void sendActionMessage(String toEmail, String action) {
        switch (action) {
            case RESET -> this.sendMessage(toEmail, INITIATE_RANDOM_PASSWORD_SUBJECT,
                    formTextForAction(toEmail, INITIATE_RANDOM_PASSWORD_BODY, resetPath));
            case CONFIRMATION -> this.sendMessage(toEmail, CONFIRM_REGISTRATION_SUBJECT,
                    formTextForAction(toEmail, CONFIRM_REGISTRATION_BODY, confirmationPath));
            default -> throw new ActionNotFoundException("Unknown action " + action);
        }
    }

    public void sendResetPassword(String toEmail, String randomPassword) {
        this.sendMessage(toEmail, RANDOM_PASSWORD_SUBJECT,
                RANDOM_PASSWORD_BODY
                        + System.lineSeparator()
                        + randomPassword
                        + System.lineSeparator()
                        + RANDOM_PASSWORD_BODY_2);
    }

    private String formTextForAction(String toEmail, String body, String actionPath) {
        emailLinkParameterProvider.formRandomParamTokenPair(toEmail);
        return body + System.lineSeparator() + actionPath
                + emailLinkParameterProvider.getEmailLinkParameter()
                + SPLITERATOR + emailLinkParameterProvider.getToken();
    }
}
