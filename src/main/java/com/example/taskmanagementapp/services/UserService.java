package com.example.taskmanagementapp.services;

import com.example.taskmanagementapp.dtos.role.RoleNameDto;
import com.example.taskmanagementapp.dtos.user.request.UpdateUserProfileDto;
import com.example.taskmanagementapp.dtos.user.request.UserAccountStatusDto;
import com.example.taskmanagementapp.dtos.user.response.UserProfileInfoDto;
import com.example.taskmanagementapp.dtos.user.response.UserProfileInfoDtoOnUpdate;
import com.example.taskmanagementapp.entities.User;
import com.example.taskmanagementapp.exceptions.forbidden.ForbiddenException;
import jakarta.servlet.http.HttpServletRequest;
import java.util.List;
import org.springframework.data.domain.Pageable;

public interface UserService {
    UserProfileInfoDto updateUserRole(Long authenticatedUserId,
                                      Long employeeId,
                                      RoleNameDto roleNameDto) throws ForbiddenException;

    UserProfileInfoDto getProfileInfo(Long authenticatedUserId);

    UserProfileInfoDtoOnUpdate updateProfileInfo(Long authenticatedUserId,
                                                 UpdateUserProfileDto updateUserProfileDto);

    void changeStatus(User user, Long disabledUserId, UserAccountStatusDto accountStatusDto)
            throws ForbiddenException;

    List<UserProfileInfoDto> getAllUsers(Pageable pageable);

    UserProfileInfoDto confirmEmailChange(HttpServletRequest httpServletRequest);
}
