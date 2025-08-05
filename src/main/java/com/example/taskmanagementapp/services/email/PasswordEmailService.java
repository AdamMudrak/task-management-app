package com.example.taskmanagementapp.services.email;

import static com.example.taskmanagementapp.constants.security.SecurityConstants.INITIATE_RANDOM_PASSWORD_BODY;
import static com.example.taskmanagementapp.constants.security.SecurityConstants.INITIATE_RANDOM_PASSWORD_SUBJECT;
import static com.example.taskmanagementapp.constants.security.SecurityConstants.RANDOM_PASSWORD_BODY;
import static com.example.taskmanagementapp.constants.security.SecurityConstants.RANDOM_PASSWORD_BODY_2;
import static com.example.taskmanagementapp.constants.security.SecurityConstants.RANDOM_PASSWORD_SUBJECT;

import com.example.taskmanagementapp.services.utils.ActionTokenUtil;
import com.example.taskmanagementapp.services.utils.TestCaptureService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class PasswordEmailService extends EmailService {
    private final ActionTokenUtil actionTokenUtil;
    @Value("${server.path}")
    private String serverPath;

    public void sendInitiatePasswordReset(String toEmail) {
        String token = actionTokenUtil.generateActionToken(toEmail);
        queueEmail(toEmail, INITIATE_RANDOM_PASSWORD_SUBJECT,
                INITIATE_RANDOM_PASSWORD_BODY + System.lineSeparator()
                        + serverPath + "/auth/reset-password?token="
                        + token);

        TestCaptureService.capture(new String[]{"token", token});
    }

    public void sendResetPassword(String toEmail, String randomPassword) {
        queueEmail(toEmail, RANDOM_PASSWORD_SUBJECT,
                RANDOM_PASSWORD_BODY
                        + System.lineSeparator()
                        + randomPassword
                        + System.lineSeparator()
                        + RANDOM_PASSWORD_BODY_2);

        TestCaptureService.capture(new String[]{randomPassword});
    }
}
