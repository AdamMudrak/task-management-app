package com.example.taskmanagementapp.services.email;

import static com.example.taskmanagementapp.constants.Constants.SPLITERATOR;
import static com.example.taskmanagementapp.constants.security.SecurityConstants.CONFIRM_CHANGE_EMAIL_BODY;
import static com.example.taskmanagementapp.constants.security.SecurityConstants.CONFIRM_CHANGE_EMAIL_SUBJECT;

import com.example.taskmanagementapp.entities.ActionToken;
import com.example.taskmanagementapp.repositories.ActionTokenRepository;
import com.example.taskmanagementapp.services.utils.EmailLinkParameterProvider;
import com.example.taskmanagementapp.services.utils.TestParamTokenCaptureService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class ChangeEmailService extends EmailService {
    private final ActionTokenRepository actionTokenRepository;
    @Value("${server.path}")
    private String serverPath;

    public ChangeEmailService(EmailLinkParameterProvider emailLinkParameterProvider,
                              ActionTokenRepository actionTokenRepository) {
        super(emailLinkParameterProvider);
        this.actionTokenRepository = actionTokenRepository;
    }

    public void sendChangeEmail(String newEmail, String oldEmail) {
        String[] paramTokenPair = getEmailLinkParameterProvider()
                .formRandomParamTokenPair(oldEmail);

        ActionToken actionToken = new ActionToken();
        actionToken.setActionToken(paramTokenPair[1] + newEmail);
        actionTokenRepository.save(actionToken);

        this.sendMessage(newEmail, CONFIRM_CHANGE_EMAIL_SUBJECT,
                CONFIRM_CHANGE_EMAIL_BODY + System.lineSeparator()
                        + serverPath + "/users/change-email-success?"
                        + paramTokenPair[0]
                        + SPLITERATOR + paramTokenPair[1]
                        + "&newEmail" + SPLITERATOR + newEmail);

        TestParamTokenCaptureService.capture(paramTokenPair);
    }
}
