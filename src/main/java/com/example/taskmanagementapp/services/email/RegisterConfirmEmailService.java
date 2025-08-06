package com.example.taskmanagementapp.services.email;

import static com.example.taskmanagementapp.constants.security.SecurityConstants.CONFIRM_REGISTRATION_BODY;
import static com.example.taskmanagementapp.constants.security.SecurityConstants.CONFIRM_REGISTRATION_SUBJECT;

import com.example.taskmanagementapp.services.utils.ActionTokenUtil;
import com.example.taskmanagementapp.services.utils.TestCaptureService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class RegisterConfirmEmailService {
    private final EmailService emailService;
    private final ActionTokenUtil actionTokenUtil;
    @Value("${server.path}")
    private String serverPath;

    public void sendRegisterConfirmEmail(String toEmail) {
        String token = actionTokenUtil.generateActionToken(toEmail);
        emailService.queueEmail(toEmail, CONFIRM_REGISTRATION_SUBJECT,
                CONFIRM_REGISTRATION_BODY + System.lineSeparator()
                        + serverPath + "/auth/register-success?token="
                        + actionTokenUtil.generateActionToken(toEmail));

        TestCaptureService.capture(new String[]{"token", token});
    }
}
