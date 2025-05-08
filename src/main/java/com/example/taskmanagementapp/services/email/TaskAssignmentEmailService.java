package com.example.taskmanagementapp.services.email;

import static com.example.taskmanagementapp.constants.security.SecurityConstants.TASK_ASSIGNED_BODY_1;
import static com.example.taskmanagementapp.constants.security.SecurityConstants.TASK_ASSIGNED_BODY_2;
import static com.example.taskmanagementapp.constants.security.SecurityConstants.TASK_ASSIGNED_BODY_3;
import static com.example.taskmanagementapp.constants.security.SecurityConstants.TASK_ASSIGNED_SUBJECT;

import org.springframework.stereotype.Service;

@Service
public class TaskAssignmentEmailService extends EmailService {

    public void sendTaskAssignmentEmail(String assigneeEmail,
                                        String assignerEmail,
                                        String taskName,
                                        String projectName) {
        this.sendMessage(assigneeEmail,
                TASK_ASSIGNED_SUBJECT,
                prepareAssignmentEmail(assignerEmail, taskName, projectName));
    }

    private String prepareAssignmentEmail(String assignerEmail,
                                          String taskName,
                                          String projectName) {
        return TASK_ASSIGNED_BODY_1
                + assignerEmail
                + TASK_ASSIGNED_BODY_2
                + taskName
                + TASK_ASSIGNED_BODY_3
                + projectName;
    }
}
