package com.example.taskmanagementapp.services.email;

import static com.example.taskmanagementapp.constants.Constants.SPACE;
import static com.example.taskmanagementapp.constants.Constants.SPLITERATOR;
import static com.example.taskmanagementapp.constants.security.SecurityConstants.ACCEPT_INVITATION_TO_PROJECT_PATH;
import static com.example.taskmanagementapp.constants.security.SecurityConstants.ACCEPT_PROJECT_INVITATION_BODY_1;
import static com.example.taskmanagementapp.constants.security.SecurityConstants.ACCEPT_PROJECT_INVITATION_BODY_2;
import static com.example.taskmanagementapp.constants.security.SecurityConstants.ACCEPT_PROJECT_INVITATION_BODY_3;
import static com.example.taskmanagementapp.constants.security.SecurityConstants.ACCEPT_PROJECT_INVITATION_SUBJECT;
import static com.example.taskmanagementapp.constants.security.SecurityConstants.ACTION_TOKEN_PARAMETER;

import com.example.taskmanagementapp.services.utils.EmailLinkParameterProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class AssignmentToProjectEmailService extends EmailService {
    @Value(ACCEPT_INVITATION_TO_PROJECT_PATH)
    private String acceptInvitationToProjectPath;
    private final EmailLinkParameterProvider emailLinkParameterProvider;

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
                + acceptInvitationToProjectPath
                + emailLinkParameterProvider.getEmailLinkParameter()
                + SPLITERATOR + emailLinkParameterProvider.getToken()
                + ACTION_TOKEN_PARAMETER + SPLITERATOR + token;
    }
}
