package com.example.taskmanagementapp.services.email;

import static com.example.taskmanagementapp.constants.Constants.SPACE;
import static com.example.taskmanagementapp.constants.Constants.SPLITERATOR;
import static com.example.taskmanagementapp.constants.security.SecurityConstants.ACCEPT_PROJECT_INVITATION_BODY_1;
import static com.example.taskmanagementapp.constants.security.SecurityConstants.ACCEPT_PROJECT_INVITATION_BODY_2;
import static com.example.taskmanagementapp.constants.security.SecurityConstants.ACCEPT_PROJECT_INVITATION_BODY_3;
import static com.example.taskmanagementapp.constants.security.SecurityConstants.ACCEPT_PROJECT_INVITATION_SUBJECT;

import com.example.taskmanagementapp.services.utils.ActionTokenUtil;
import com.example.taskmanagementapp.services.utils.TestCaptureService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AssignmentToProjectEmailService extends EmailService {
    private final ActionTokenUtil actionTokenUtil;
    @Value("${server.path}")
    private String serverPath;

    public void sendAssignmentEmail(String sender, String receiver,
                                    String projectName, String metaDateToken) {
        String shortToken = actionTokenUtil.generateActionToken(receiver);

        sendMessage(receiver, ACCEPT_PROJECT_INVITATION_SUBJECT,
                ACCEPT_PROJECT_INVITATION_BODY_1 + SPACE + sender + SPACE
                        + ACCEPT_PROJECT_INVITATION_BODY_2
                        + SPACE + projectName + SPACE + ACCEPT_PROJECT_INVITATION_BODY_3
                        + serverPath + "/projects/accept-invite?"
                        + "shortToken" + SPLITERATOR + shortToken
                        + "&actionToken" + SPLITERATOR + metaDateToken);

        TestCaptureService.capture(new String[]{"token", shortToken});
    }
}
