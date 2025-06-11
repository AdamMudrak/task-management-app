package com.example.taskmanagementapp.services.email;

import static com.example.taskmanagementapp.constants.Constants.FIRST_POSITION;
import static com.example.taskmanagementapp.constants.Constants.SECOND_POSITION;
import static com.example.taskmanagementapp.constants.Constants.SPLITERATOR;
import static com.example.taskmanagementapp.constants.security.SecurityConstants.CONFIRM_REGISTRATION_BODY;
import static com.example.taskmanagementapp.constants.security.SecurityConstants.CONFIRM_REGISTRATION_SUBJECT;

import com.example.taskmanagementapp.services.utils.EmailLinkParameterProvider;
import com.example.taskmanagementapp.services.utils.TestCaptureService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class RegisterConfirmEmailService extends EmailService {
    @Value("${server.path}")
    private String serverPath;

    public RegisterConfirmEmailService(EmailLinkParameterProvider emailLinkParameterProvider) {
        super(emailLinkParameterProvider);
    }

    public void sendRegisterConfirmEmail(String toEmail) {
        String[] paramTokenPair = getEmailLinkParameterProvider().formRandomParamTokenPair(toEmail);
        sendMessage(toEmail, CONFIRM_REGISTRATION_SUBJECT,
                CONFIRM_REGISTRATION_BODY + System.lineSeparator()
                        + serverPath + "/auth/register-success?"
                        + paramTokenPair[FIRST_POSITION]
                        + SPLITERATOR
                        + paramTokenPair[SECOND_POSITION]);

        TestCaptureService.capture(paramTokenPair);
    }
}
