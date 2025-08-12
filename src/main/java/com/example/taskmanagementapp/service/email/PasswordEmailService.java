package com.example.taskmanagementapp.service.email;

import static com.example.taskmanagementapp.constant.security.SecurityConstants.INITIATE_RANDOM_PASSWORD_BODY;
import static com.example.taskmanagementapp.constant.security.SecurityConstants.INITIATE_RANDOM_PASSWORD_SUBJECT;
import static com.example.taskmanagementapp.constant.security.SecurityConstants.RANDOM_PASSWORD_BODY;
import static com.example.taskmanagementapp.constant.security.SecurityConstants.RANDOM_PASSWORD_BODY_2;
import static com.example.taskmanagementapp.constant.security.SecurityConstants.RANDOM_PASSWORD_SUBJECT;

import com.example.taskmanagementapp.service.utils.ActionTokenUtil;
import com.example.taskmanagementapp.service.utils.TestCaptureService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class PasswordEmailService {
    private final EmailService emailService;
    private final ActionTokenUtil actionTokenUtil;
    @Value("${server.path}")
    private String serverPath;

    public void sendInitiatePasswordReset(String toEmail) {
        String token = actionTokenUtil.generateActionToken(toEmail);
        emailService.queueEmail(toEmail, INITIATE_RANDOM_PASSWORD_SUBJECT,
                INITIATE_RANDOM_PASSWORD_BODY + System.lineSeparator()
                        + serverPath + "/auth/reset-password?token="
                        + token);

        TestCaptureService.capture(new String[]{"token", token});
    }

    public void sendResetPassword(String toEmail, String randomPassword) {
        emailService.queueEmail(toEmail, RANDOM_PASSWORD_SUBJECT,
                RANDOM_PASSWORD_BODY
                        + System.lineSeparator()
                        + randomPassword
                        + System.lineSeparator()
                        + RANDOM_PASSWORD_BODY_2);

        TestCaptureService.capture(new String[]{randomPassword});
    }
}
