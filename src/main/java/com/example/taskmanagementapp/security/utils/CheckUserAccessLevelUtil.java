package com.example.taskmanagementapp.security.utils;

import com.example.taskmanagementapp.entities.Project;
import com.example.taskmanagementapp.entities.Role;
import com.example.taskmanagementapp.entities.User;
import org.springframework.stereotype.Component;

@Component
public class CheckUserAccessLevelUtil {
    public boolean isUserSupervisor(User user) {
        return user.getRole().getName().equals(Role.RoleName.ROLE_SUPERVISOR);
    }

    public boolean isUserOwner(User user, Project project) {
        return user.getId().equals(project.getOwner().getId());
    }

    public boolean isUserAssignee(User user, Project project) {
        return project.getEmployees().stream()
                .map(User::getId)
                .anyMatch(id -> user.getId().equals(id));
    }

    public boolean hasAnyAccess(User user, Project project) {
        return isUserSupervisor(user)
                || isUserOwner(user, project)
                || isUserAssignee(user, project);
    }

    public boolean hasAdminAccess(User user, Project project) {
        return isUserSupervisor(user)
                || isUserOwner(user, project);
    }
}
