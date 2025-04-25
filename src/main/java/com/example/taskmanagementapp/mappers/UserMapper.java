package com.example.taskmanagementapp.mappers;

import com.example.taskmanagementapp.config.MapperConfig;
import com.example.taskmanagementapp.dtos.authentication.request.UserRegistrationRequestDto;
import com.example.taskmanagementapp.dtos.user.response.UserProfileInfoDto;
import com.example.taskmanagementapp.entities.Role;
import com.example.taskmanagementapp.entities.User;
import java.util.stream.Collectors;
import org.mapstruct.AfterMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(config = MapperConfig.class)
public interface UserMapper {
    User toUser(UserRegistrationRequestDto requestDto);

    @Mapping(target = "roles", ignore = true)
    UserProfileInfoDto toUserProfileInfoDto(User user);

    @AfterMapping
    default void setRoles(@MappingTarget UserProfileInfoDto userProfileInfoDto, User user) {
        userProfileInfoDto.setRoles(user.getRoles().stream()
                .map(Role::getName)
                .map(Role.RoleName::toString)
                .collect(Collectors.toSet()));
    }
}
