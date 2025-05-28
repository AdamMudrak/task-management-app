package com.example.taskmanagementapp.services.email;

import static com.example.taskmanagementapp.constants.Constants.SPLITERATOR;
import static com.example.taskmanagementapp.constants.security.SecurityConstants.CONFIRM_REGISTRATION_BODY;
import static com.example.taskmanagementapp.constants.security.SecurityConstants.CONFIRM_REGISTRATION_SUBJECT;
import static com.example.taskmanagementapp.constants.security.SecurityConstants.INITIATE_RANDOM_PASSWORD_BODY;
import static com.example.taskmanagementapp.constants.security.SecurityConstants.INITIATE_RANDOM_PASSWORD_SUBJECT;
import static com.example.taskmanagementapp.constants.security.SecurityConstants.RANDOM_PASSWORD_BODY;
import static com.example.taskmanagementapp.constants.security.SecurityConstants.RANDOM_PASSWORD_BODY_2;
import static com.example.taskmanagementapp.constants.security.SecurityConstants.RANDOM_PASSWORD_SUBJECT;

import com.example.taskmanagementapp.exceptions.ActionNotFoundException;
import com.example.taskmanagementapp.security.RequestType;
import com.example.taskmanagementapp.services.utils.EmailLinkParameterProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class PasswordEmailService extends EmailService {
    private final EmailLinkParameterProvider emailLinkParameterProvider;
    @Value("${server.path}")
    private String serverPath;

    public PasswordEmailService(EmailLinkParameterProvider emailLinkParameterProvider) {
        this.emailLinkParameterProvider = emailLinkParameterProvider;
    }

    public void sendActionMessage(String toEmail, RequestType requestType) {
        switch (requestType) {
            case PASSWORD_RESET -> this.sendMessage(toEmail, INITIATE_RANDOM_PASSWORD_SUBJECT,
                    formTextForAction(toEmail, INITIATE_RANDOM_PASSWORD_BODY,
                            serverPath + "/auth/reset-password?"));
            case REGISTRATION_CONFIRMATION ->
                    this.sendMessage(toEmail, CONFIRM_REGISTRATION_SUBJECT,
                        formTextForAction(toEmail, CONFIRM_REGISTRATION_BODY,
                                serverPath + "/auth/register-success?"));
            default -> throw new ActionNotFoundException("Unknown request type " + requestType);
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
