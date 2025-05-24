package com.example.taskmanagementapp.mappers;

import static com.example.taskmanagementapp.constants.security.SecurityConstants.CONFIRM_NEW_EMAIL_MESSAGE;

import com.example.taskmanagementapp.config.MapperConfig;
import com.example.taskmanagementapp.dtos.authentication.request.RegistrationRequest;
import com.example.taskmanagementapp.dtos.user.response.UpdateUserProfileResponse;
import com.example.taskmanagementapp.dtos.user.response.UserProfileAdminResponse;
import com.example.taskmanagementapp.dtos.user.response.UserProfileResponse;
import com.example.taskmanagementapp.entities.User;
import org.mapstruct.AfterMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(config = MapperConfig.class)
public interface UserMapper {
    User toUser(RegistrationRequest requestDto);

    @Mapping(target = "role", source = "role.name")
    UserProfileResponse toUserProfileInfoDto(User user);

    @Mapping(target = "role", source = "role.name")
    UserProfileAdminResponse toUserProfileAdminInfoDto(User user);

    @Mapping(target = "role", source = "role.name")
    @Mapping(target = "message", ignore = true)
    UpdateUserProfileResponse toUpdateUserProfileInfoDto(User user);

    @AfterMapping
    default void setMessage(@MappingTarget UpdateUserProfileResponse userProfileInfoDto) {
        userProfileInfoDto.setMessage(CONFIRM_NEW_EMAIL_MESSAGE);
    }
}
