package com.example.taskmanagementapp.mappers;

import com.example.taskmanagementapp.config.MapperConfig;
import com.example.taskmanagementapp.dtos.authentication.request.UserRegistrationRequestDto;
import com.example.taskmanagementapp.dtos.user.response.UserProfileInfoDto;
import com.example.taskmanagementapp.entities.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(config = MapperConfig.class)
public interface UserMapper {
    User toUser(UserRegistrationRequestDto requestDto);

    @Mapping(target = "role", source = "role.name")
    UserProfileInfoDto toUserProfileInfoDto(User user);
}
