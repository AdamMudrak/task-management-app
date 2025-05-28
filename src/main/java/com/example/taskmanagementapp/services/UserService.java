package com.example.taskmanagementapp.services;

import com.example.taskmanagementapp.dtos.role.RoleNameDto;
import com.example.taskmanagementapp.dtos.user.request.UpdateUserProfileRequest;
import com.example.taskmanagementapp.dtos.user.request.UserAccountStatusDto;
import com.example.taskmanagementapp.dtos.user.response.UpdateUserProfileResponse;
import com.example.taskmanagementapp.dtos.user.response.UserProfileAdminResponse;
import com.example.taskmanagementapp.dtos.user.response.UserProfileResponse;
import com.example.taskmanagementapp.entities.User;
import com.example.taskmanagementapp.exceptions.ForbiddenException;
import jakarta.servlet.http.HttpServletRequest;
import java.util.List;
import org.springframework.data.domain.Pageable;

public interface UserService {
    UserProfileResponse updateUserRole(Long authenticatedUserId,
                                       Long employeeId,
                                       RoleNameDto roleNameDto) throws ForbiddenException;

    UserProfileResponse getProfileInfo(Long authenticatedUserId);

    UpdateUserProfileResponse updateProfileInfo(Long authenticatedUserId,
                                                UpdateUserProfileRequest updateUserProfileDto);

    UserProfileAdminResponse changeStatus(User user, Long disabledUserId,
                                          UserAccountStatusDto accountStatusDto)
            throws ForbiddenException;

    List<UserProfileResponse> getAllUsers(Pageable pageable);

    UserProfileResponse confirmEmailChange(HttpServletRequest httpServletRequest);
}
