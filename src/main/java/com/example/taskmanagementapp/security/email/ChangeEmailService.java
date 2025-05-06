package com.example.taskmanagementapp.security.email;

import static com.example.taskmanagementapp.constants.Constants.SPLITERATOR;
import static com.example.taskmanagementapp.constants.security.SecurityConstants.CHANGE_EMAIL_CONFIRMATION_PATH;
import static com.example.taskmanagementapp.constants.security.SecurityConstants.CONFIRM_CHANGE_EMAIL_BODY;
import static com.example.taskmanagementapp.constants.security.SecurityConstants.CONFIRM_CHANGE_EMAIL_SUBJECT;
import static com.example.taskmanagementapp.constants.security.SecurityConstants.NEW_EMAIL_PARAMETER;

import com.example.taskmanagementapp.security.utils.EmailLinkParameterProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class ChangeEmailService extends EmailService {
    private final EmailLinkParameterProvider emailLinkParameterProvider;
    @Value(CHANGE_EMAIL_CONFIRMATION_PATH)
    private String changeEmailConfirmationPath;

    public ChangeEmailService(EmailLinkParameterProvider emailLinkParameterProvider) {
        this.emailLinkParameterProvider = emailLinkParameterProvider;
    }

    public void sendChangeEmail(String newEmail, String oldEmail) {
        this.sendMessage(newEmail, CONFIRM_CHANGE_EMAIL_SUBJECT,
                formTextForChangeEmail(oldEmail, newEmail));
    }

    private String formTextForChangeEmail(String oldEmail, String newEmail) {
        emailLinkParameterProvider.formRandomParamTokenPair(oldEmail);
        return CONFIRM_CHANGE_EMAIL_BODY + System.lineSeparator() + changeEmailConfirmationPath
                + emailLinkParameterProvider.getEmailLinkParameter()
                + SPLITERATOR + emailLinkParameterProvider.getToken()
                + NEW_EMAIL_PARAMETER + SPLITERATOR + newEmail;
    }
}
