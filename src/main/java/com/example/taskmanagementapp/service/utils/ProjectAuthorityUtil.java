package com.example.taskmanagementapp.service.utils;

import com.example.taskmanagementapp.repository.ProjectRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Component
public class ProjectAuthorityUtil {
    private final ProjectRepository projectRepository;

    public boolean hasAnyAuthority(Long projectId, Long userId) {
        return projectRepository.isUserOwner(projectId, userId)
                || projectRepository.isUserEmployee(projectId, userId)
                || projectRepository.isUserManager(projectId, userId);
    }

    public boolean hasManagerialAuthority(Long projectId, Long userId) {
        return projectRepository.isUserManager(projectId, userId)
                || projectRepository.isUserOwner(projectId, userId);
    }
}
