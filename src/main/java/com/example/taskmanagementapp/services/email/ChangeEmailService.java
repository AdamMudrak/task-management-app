package com.example.taskmanagementapp.services.email;

import static com.example.taskmanagementapp.constants.Constants.SPLITERATOR;
import static com.example.taskmanagementapp.constants.security.SecurityConstants.CONFIRM_CHANGE_EMAIL_BODY;
import static com.example.taskmanagementapp.constants.security.SecurityConstants.CONFIRM_CHANGE_EMAIL_SUBJECT;

import com.example.taskmanagementapp.entities.ActionToken;
import com.example.taskmanagementapp.repositories.ActionTokenRepository;
import com.example.taskmanagementapp.services.utils.ActionTokenUtil;
import com.example.taskmanagementapp.services.utils.TestCaptureService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ChangeEmailService extends EmailService {
    private final ActionTokenUtil actionTokenUtil;
    private final ActionTokenRepository actionTokenRepository;
    @Value("${server.path}")
    private String serverPath;

    public void sendChangeEmail(String newEmail, String oldEmail) {
        String token = actionTokenUtil.generateActionToken(oldEmail);

        ActionToken actionToken = new ActionToken();
        actionToken.setActionToken(token + newEmail);
        actionTokenRepository.save(actionToken);

        this.queueEmail(newEmail, CONFIRM_CHANGE_EMAIL_SUBJECT,
                CONFIRM_CHANGE_EMAIL_BODY + System.lineSeparator()
                        + serverPath + "/users/change-email-success?token="
                        + token + "&newEmail" + SPLITERATOR + newEmail);

        TestCaptureService.capture(new String[]{"token", token});
    }
}
