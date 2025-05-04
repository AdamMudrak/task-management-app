package com.example.taskmanagementapp.security.email;

import static com.example.taskmanagementapp.constants.Constants.SPLITERATOR;
import static com.example.taskmanagementapp.constants.security.SecurityConstants.ACCEPT_INVITATION_TO_PROJECT_PATH;
import static com.example.taskmanagementapp.constants.security.SecurityConstants.ACCEPT_PROJECT_INVITATION_BODY_1;
import static com.example.taskmanagementapp.constants.security.SecurityConstants.ACCEPT_PROJECT_INVITATION_BODY_2;
import static com.example.taskmanagementapp.constants.security.SecurityConstants.ACCEPT_PROJECT_INVITATION_BODY_3;
import static com.example.taskmanagementapp.constants.security.SecurityConstants.ACCEPT_PROJECT_INVITATION_SUBJECT;
import static com.example.taskmanagementapp.constants.security.SecurityConstants.ASSIGNEE_ID_PARAMETER;
import static com.example.taskmanagementapp.constants.security.SecurityConstants.IS_NEW_MANAGER_PARAMETER;
import static com.example.taskmanagementapp.constants.security.SecurityConstants.PROJECT_ID_PARAMETER;

import com.example.taskmanagementapp.security.utils.EmailLinkParameterProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class AcceptAssignmentToProjectEmailService extends EmailService {
    @Value(ACCEPT_INVITATION_TO_PROJECT_PATH)
    private String acceptInvitationToProjectPath;
    private final EmailLinkParameterProvider emailLinkParameterProvider;

    public AcceptAssignmentToProjectEmailService(JavaMailSender mailSender,
                                         EmailLinkParameterProvider emailLinkParameterProvider) {
        super(mailSender);
        this.emailLinkParameterProvider = emailLinkParameterProvider;
    }

    public void sendChangeEmail(String sender, String receiver, String projectName,
                                Long projectId, Long assigneeId, boolean isNewEmployeeManager) {
        this.sendMessage(receiver, ACCEPT_PROJECT_INVITATION_SUBJECT,
                formTextForChangeEmail(sender, receiver, projectName,
                        projectId, assigneeId, isNewEmployeeManager));
    }

    private String formTextForChangeEmail(String sender, String receiver, String projectName,
                                  Long projectId, Long assigneeId, boolean isNewEmployeeManager) {
        emailLinkParameterProvider.formRandomParamTokenPair(receiver);
        return ACCEPT_PROJECT_INVITATION_BODY_1 + " " + sender + " "
                + ACCEPT_PROJECT_INVITATION_BODY_2
                + " " + projectName + " " + ACCEPT_PROJECT_INVITATION_BODY_3
                + acceptInvitationToProjectPath
                + emailLinkParameterProvider.getEmailLinkParameter()
                + SPLITERATOR + emailLinkParameterProvider.getToken()
                + PROJECT_ID_PARAMETER + SPLITERATOR + projectId
                + ASSIGNEE_ID_PARAMETER + SPLITERATOR + assigneeId
                + IS_NEW_MANAGER_PARAMETER + SPLITERATOR + isNewEmployeeManager;
    }
}
