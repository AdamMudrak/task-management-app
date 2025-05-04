package com.example.taskmanagementapp.mappers;

import static com.example.taskmanagementapp.constants.security.SecurityConstants.CONFIRM_NEW_EMAIL_MESSAGE;

import com.example.taskmanagementapp.config.MapperConfig;
import com.example.taskmanagementapp.dtos.authentication.request.UserRegistrationRequestDto;
import com.example.taskmanagementapp.dtos.user.response.UserProfileAdminInfoDto;
import com.example.taskmanagementapp.dtos.user.response.UserProfileInfoDto;
import com.example.taskmanagementapp.dtos.user.response.UserProfileInfoDtoOnUpdate;
import com.example.taskmanagementapp.entities.User;
import org.mapstruct.AfterMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(config = MapperConfig.class)
public interface UserMapper {
    User toUser(UserRegistrationRequestDto requestDto);

    @Mapping(target = "role", source = "role.name")
    UserProfileInfoDto toUserProfileInfoDto(User user);

    @Mapping(target = "role", source = "role.name")
    UserProfileAdminInfoDto toUserProfileAdminInfoDto(User user);

    @Mapping(target = "role", source = "role.name")
    @Mapping(target = "message", ignore = true)
    UserProfileInfoDtoOnUpdate toUpdateUserProfileInfoDto(User user);

    @AfterMapping
    default void setMessage(@MappingTarget UserProfileInfoDtoOnUpdate userProfileInfoDto) {
        userProfileInfoDto.setMessage(CONFIRM_NEW_EMAIL_MESSAGE);
    }
}
