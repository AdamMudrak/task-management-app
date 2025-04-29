package com.example.taskmanagementapp.services;

import com.example.taskmanagementapp.entities.Project;
import com.example.taskmanagementapp.entities.Role;
import com.example.taskmanagementapp.entities.User;
import org.springframework.stereotype.Component;

@Component
class CheckUserAccessLevelUtil {
    boolean isUserSupervisor(User user) {
        return user.getRole().getName().equals(Role.RoleName.ROLE_SUPERVISOR);
    }

    boolean isUserOwner(User user, Project project) {
        return user.getId().equals(project.getOwner().getId());
    }

    boolean isUserAssignee(User user, Project project) {
        return project.getEmployees().stream()
                .map(User::getId)
                .anyMatch(id -> user.getId().equals(id));
    }

    boolean hasAnyAccess(User user, Project project) {
        return isUserSupervisor(user)
                || isUserOwner(user, project)
                || isUserAssignee(user, project);
    }

    boolean hasAdminAccess(User user, Project project) {
        return isUserSupervisor(user)
                || isUserOwner(user, project);
    }
}
