package com.example.taskmanagementapp.services.email;

import static com.example.taskmanagementapp.constants.Constants.SPLITERATOR;
import static com.example.taskmanagementapp.constants.security.SecurityConstants.CONFIRM_CHANGE_EMAIL_BODY;
import static com.example.taskmanagementapp.constants.security.SecurityConstants.CONFIRM_CHANGE_EMAIL_SUBJECT;

import com.example.taskmanagementapp.entities.ActionToken;
import com.example.taskmanagementapp.repositories.ActionTokenRepository;
import com.example.taskmanagementapp.services.utils.EmailLinkParameterProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class ChangeEmailService extends EmailService {
    private final EmailLinkParameterProvider emailLinkParameterProvider;
    private final ActionTokenRepository actionTokenRepository;
    @Value("${server.path}")
    private String serverPath;

    public ChangeEmailService(EmailLinkParameterProvider emailLinkParameterProvider,
                              @Autowired ActionTokenRepository actionTokenRepository) {
        this.emailLinkParameterProvider = emailLinkParameterProvider;
        this.actionTokenRepository = actionTokenRepository;
    }

    public void sendChangeEmail(String newEmail, String oldEmail) {
        this.sendMessage(newEmail, CONFIRM_CHANGE_EMAIL_SUBJECT,
                formTextForChangeEmail(oldEmail, newEmail));
    }

    private String formTextForChangeEmail(String oldEmail, String newEmail) {
        emailLinkParameterProvider.formRandomParamTokenPair(oldEmail);
        buildActionToken(newEmail);
        return CONFIRM_CHANGE_EMAIL_BODY + System.lineSeparator()
                + serverPath + "/users/change-email-success?"
                + emailLinkParameterProvider.getEmailLinkParameter()
                + SPLITERATOR + emailLinkParameterProvider.getToken()
                + "&newEmail" + SPLITERATOR + newEmail;
    }

    private void buildActionToken(String email) {
        ActionToken actionToken = new ActionToken();
        actionToken.setActionToken(emailLinkParameterProvider.getToken() + email);
        actionTokenRepository.save(actionToken);
    }
}
