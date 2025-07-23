package com.example.taskmanagementapp.services.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.example.taskmanagementapp.dtos.project.request.ProjectRequest;
import com.example.taskmanagementapp.dtos.project.response.ProjectResponse;
import com.example.taskmanagementapp.entities.Project;
import com.example.taskmanagementapp.entities.User;
import com.example.taskmanagementapp.mappers.ProjectMapper;
import com.example.taskmanagementapp.repositories.ActionTokenRepository;
import com.example.taskmanagementapp.repositories.CommentRepository;
import com.example.taskmanagementapp.repositories.ProjectRepository;
import com.example.taskmanagementapp.repositories.TaskRepository;
import com.example.taskmanagementapp.repositories.UserRepository;
import com.example.taskmanagementapp.security.jwtutils.strategy.JwtStrategy;
import com.example.taskmanagementapp.services.email.AssignmentToProjectEmailService;
import com.example.taskmanagementapp.services.utils.ParamFromHttpRequestUtil;
import com.example.taskmanagementapp.services.utils.ProjectAuthorityUtil;
import com.example.taskmanagementapp.testutils.Constants;
import com.example.taskmanagementapp.testutils.ObjectFactory;
import java.util.List;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

@ExtendWith(MockitoExtension.class)
public class ProjectServiceImplTest {
    @Mock
    private ProjectMapper projectMapper;
    @Mock
    private ProjectRepository projectRepository;
    @Mock
    private CommentRepository commentRepository;
    @Mock
    private TaskRepository taskRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private AssignmentToProjectEmailService emailService;
    @Mock
    private ParamFromHttpRequestUtil paramFromHttpRequestUtil;
    @Mock
    private JwtStrategy jwtStrategy;
    @Mock
    private ActionTokenRepository actionTokenRepository;
    @Mock
    private ProjectAuthorityUtil projectAuthorityUtil;

    @InjectMocks
    private ProjectServiceImpl projectServiceImpl;

    @Nested
    class CreateProject {
        @Test
        void givenValidUserAndDto_whenCreateProject_thenSuccess() {
            //given
            ProjectRequest projectRequest = ObjectFactory.getProjectRequest();
            User user = ObjectFactory.getUser1(ObjectFactory.getUserRole());
            user.setId(Constants.FIRST_USER_ID);
            Project project = ObjectFactory.getProjectWithOneEmployee(user);
            ProjectResponse projectResponse = ObjectFactory.getProjectResponse(project);

            //when
            when(projectMapper.toCreateProject(projectRequest, user)).thenReturn(project);
            when(projectRepository.save(project)).thenReturn(project);
            when(projectMapper.toProjectDto(project)).thenReturn(projectResponse);

            //then
            assertEquals(projectResponse, projectServiceImpl.createProject(user, projectRequest));
        }
    }

    @Nested
    class GetAssignedProjects {
        @Test
        void givenPageable_whenGetAssignedProjects_thenSuccess() {
            //given
            PageRequest pageRequest = PageRequest.of(0, 1);
            User user = ObjectFactory.getUser1(ObjectFactory.getUserRole());
            user.setId(Constants.FIRST_USER_ID);
            Project project = ObjectFactory.getProjectWithOneEmployee(user);
            ProjectResponse projectResponse = ObjectFactory.getProjectResponse(project);
            Page<Project> projects = new PageImpl<>(List.of(project));
            List<ProjectResponse> projectResponses = List.of(projectResponse);

            //when
            when(projectRepository.findAllByEmployeeId(user.getId(), pageRequest))
                    .thenReturn(projects);
            when(projectMapper.toProjectDtoList(projects.getContent()))
                    .thenReturn(projectResponses);

            //then
            assertEquals(projectResponses, projectServiceImpl
                    .getAssignedProjects(user.getId(), pageRequest));

            //verify
            verify(projectRepository, times(1)).findAllByEmployeeId(user.getId(), pageRequest);
            verify(projectMapper, times(1)).toProjectDtoList(projects.getContent());
        }
    }
}
