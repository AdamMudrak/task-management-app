package com.example.taskmanagementapp.service;

import com.example.taskmanagementapp.dto.role.RoleNameDto;
import com.example.taskmanagementapp.dto.user.request.UpdateUserProfileRequest;
import com.example.taskmanagementapp.dto.user.request.UserAccountStatusDto;
import com.example.taskmanagementapp.dto.user.response.UpdateUserProfileResponse;
import com.example.taskmanagementapp.dto.user.response.UserProfileAdminResponse;
import com.example.taskmanagementapp.dto.user.response.UserProfileResponse;
import com.example.taskmanagementapp.exception.ForbiddenException;
import jakarta.servlet.http.HttpServletRequest;
import java.util.List;
import org.springframework.data.domain.Pageable;

public interface UserService {
    UserProfileResponse updateUserRole(Long authenticatedUserId,
                                       Long employeeId,
                                       RoleNameDto roleNameDto);

    UserProfileResponse getProfileInfo(Long authenticatedUserId);

    UpdateUserProfileResponse updateProfileInfo(Long authenticatedUserId,
                                                UpdateUserProfileRequest updateUserProfileDto);

    UserProfileAdminResponse changeStatus(Long authenticatedUserId, Long changedUserId,
                                          UserAccountStatusDto accountStatusDto)
            throws ForbiddenException;

    List<UserProfileResponse> getAllUsers(Pageable pageable);

    UserProfileResponse confirmEmailChange(HttpServletRequest httpServletRequest);
}
