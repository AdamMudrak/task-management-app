package com.example.taskmanagementapp.dtos.user.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

@Getter
@Setter
@SuperBuilder
@AllArgsConstructor
@NoArgsConstructor
public class UserProfileAdminResponse extends UserProfileResponse {
    private boolean isEnabled;
    private boolean isAccountNonLocked;
}
