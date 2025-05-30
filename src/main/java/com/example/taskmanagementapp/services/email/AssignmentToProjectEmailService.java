package com.example.taskmanagementapp.services.email;

import static com.example.taskmanagementapp.constants.Constants.SPACE;
import static com.example.taskmanagementapp.constants.Constants.SPLITERATOR;
import static com.example.taskmanagementapp.constants.security.SecurityConstants.ACCEPT_PROJECT_INVITATION_BODY_1;
import static com.example.taskmanagementapp.constants.security.SecurityConstants.ACCEPT_PROJECT_INVITATION_BODY_2;
import static com.example.taskmanagementapp.constants.security.SecurityConstants.ACCEPT_PROJECT_INVITATION_BODY_3;
import static com.example.taskmanagementapp.constants.security.SecurityConstants.ACCEPT_PROJECT_INVITATION_SUBJECT;

import com.example.taskmanagementapp.services.utils.EmailLinkParameterProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class AssignmentToProjectEmailService extends EmailService {
    private final EmailLinkParameterProvider emailLinkParameterProvider;
    @Value("${server.path}")
    private String serverPath;

    public AssignmentToProjectEmailService(
            EmailLinkParameterProvider emailLinkParameterProvider) {
        this.emailLinkParameterProvider = emailLinkParameterProvider;
    }

    public void sendChangeEmail(String sender, String receiver, String projectName, String token) {
        this.sendMessage(receiver, ACCEPT_PROJECT_INVITATION_SUBJECT,
                formTextForChangeEmail(sender, receiver, projectName, token));
    }

    private String formTextForChangeEmail(String sender, String receiver, String projectName,
                                          String token) {
        emailLinkParameterProvider.formRandomParamTokenPair(receiver);
        return ACCEPT_PROJECT_INVITATION_BODY_1 + SPACE + sender + SPACE
                + ACCEPT_PROJECT_INVITATION_BODY_2
                + SPACE + projectName + SPACE + ACCEPT_PROJECT_INVITATION_BODY_3
                + serverPath + "/projects/accept-invite?"
                + emailLinkParameterProvider.getEmailLinkParameter()
                + SPLITERATOR + emailLinkParameterProvider.getToken()
                + "&actionToken" + SPLITERATOR + token;
    }
}
