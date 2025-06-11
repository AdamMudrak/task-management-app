package com.example.taskmanagementapp.services.email;

import static com.example.taskmanagementapp.constants.Constants.FIRST_POSITION;
import static com.example.taskmanagementapp.constants.Constants.SECOND_POSITION;
import static com.example.taskmanagementapp.constants.Constants.SPACE;
import static com.example.taskmanagementapp.constants.Constants.SPLITERATOR;
import static com.example.taskmanagementapp.constants.security.SecurityConstants.ACCEPT_PROJECT_INVITATION_BODY_1;
import static com.example.taskmanagementapp.constants.security.SecurityConstants.ACCEPT_PROJECT_INVITATION_BODY_2;
import static com.example.taskmanagementapp.constants.security.SecurityConstants.ACCEPT_PROJECT_INVITATION_BODY_3;
import static com.example.taskmanagementapp.constants.security.SecurityConstants.ACCEPT_PROJECT_INVITATION_SUBJECT;

import com.example.taskmanagementapp.services.utils.EmailLinkParameterProvider;
import com.example.taskmanagementapp.services.utils.TestCaptureService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class AssignmentToProjectEmailService extends EmailService {
    @Value("${server.path}")
    private String serverPath;

    public AssignmentToProjectEmailService(EmailLinkParameterProvider emailLinkParameterProvider) {
        super(emailLinkParameterProvider);
    }

    public void sendAssignmentEmail(String sender, String receiver,
                                    String projectName, String token) {
        String[] paramTokenPair = getEmailLinkParameterProvider()
                .formRandomParamTokenPair(receiver);

        sendMessage(receiver, ACCEPT_PROJECT_INVITATION_SUBJECT,
                ACCEPT_PROJECT_INVITATION_BODY_1 + SPACE + sender + SPACE
                        + ACCEPT_PROJECT_INVITATION_BODY_2
                        + SPACE + projectName + SPACE + ACCEPT_PROJECT_INVITATION_BODY_3
                        + serverPath + "/projects/accept-invite?"
                        + paramTokenPair[FIRST_POSITION]
                        + SPLITERATOR + paramTokenPair[SECOND_POSITION]
                        + "&actionToken" + SPLITERATOR + token);

        TestCaptureService.capture(paramTokenPair);
    }
}
