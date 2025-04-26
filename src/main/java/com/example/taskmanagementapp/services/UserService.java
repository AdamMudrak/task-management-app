package com.example.taskmanagementapp.services;

import com.example.taskmanagementapp.dtos.role.RoleNameDto;
import com.example.taskmanagementapp.dtos.user.request.UpdateUserProfileDto;
import com.example.taskmanagementapp.dtos.user.response.UserProfileInfoDto;
import java.util.List;
import org.springframework.data.domain.Pageable;

public interface UserService {
    UserProfileInfoDto updateUserRole(Long authenticatedUserId,
                                      Long employeeId,
                                      RoleNameDto roleNameDto);

    UserProfileInfoDto getProfileInfo(Long authenticatedUserId);

    UserProfileInfoDto updateProfileInfo(Long authenticatedUserId,
                                         UpdateUserProfileDto updateUserProfileDto);

    List<UserProfileInfoDto> getAllUsers(Pageable pageable);
}
