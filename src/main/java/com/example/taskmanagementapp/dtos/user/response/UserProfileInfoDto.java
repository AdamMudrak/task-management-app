package com.example.taskmanagementapp.dtos.user.response;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserProfileInfoDto {
    private Long id;
    private String username;
    private String email;
    private String firstName;
    private String lastName;
    private String role;
}
