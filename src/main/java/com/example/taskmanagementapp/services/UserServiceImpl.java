package com.example.taskmanagementapp.services;

import com.example.taskmanagementapp.dtos.user.request.UpdateUserProfileDto;
import com.example.taskmanagementapp.dtos.user.response.UserProfileInfoDto;
import com.example.taskmanagementapp.entities.Role;
import com.example.taskmanagementapp.entities.User;
import com.example.taskmanagementapp.exceptions.notfoundexceptions.EntityNotFoundException;
import com.example.taskmanagementapp.mappers.UserMapper;
import com.example.taskmanagementapp.repositories.role.RoleRepository;
import com.example.taskmanagementapp.repositories.user.UserRepository;
import java.util.HashSet;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final UserMapper userMapper;

    @Override
    public UserProfileInfoDto updateUserRole(Long authenticatedUserId,
                                             Long employeeId,
                                             Role.RoleName roleName) {
        if (authenticatedUserId.equals(employeeId)) {
            throw new IllegalArgumentException(
                    "To prevent unwanted damage, self-assigning of roles is restricted. "
                            + "Your own role shall be changed only manually via MySql "
                            + "Workbench or any other MySql compatible instrument");
        }
        User employee = userRepository.findById(employeeId).orElseThrow(
                () -> new EntityNotFoundException("Employee with id " + employeeId + " not found"));
        Role role = roleRepository.findByName(roleName);
        employee.setRoles(new HashSet<>(List.of(role)));
        return userMapper.toUserProfileInfoDto(userRepository.save(employee));
    }

    @Override
    public UserProfileInfoDto getProfileInfo(Long authenticatedUserId) {
        return userMapper.toUserProfileInfoDto(userRepository.findById(authenticatedUserId)
                .orElseThrow(() -> new EntityNotFoundException(
                        "Employee with id " + authenticatedUserId + " not found")));
    }

    @Override
    public UserProfileInfoDto updateProfileInfo(Long authenticatedUserId,
                                                UpdateUserProfileDto updateUserProfileDto) {
        User user = userRepository.findById(authenticatedUserId).orElseThrow(
                () -> new EntityNotFoundException("User with id " + authenticatedUserId
                        + " not found"));
        if (updateUserProfileDto.firstName() != null
                && !updateUserProfileDto.firstName().isBlank()
                && !user.getFirstName().equals(updateUserProfileDto.firstName())) {
            user.setFirstName(updateUserProfileDto.firstName());
        }
        if (updateUserProfileDto.lastName() != null
                && !updateUserProfileDto.lastName().isBlank()
                && !user.getLastName().equals(updateUserProfileDto.lastName())) {
            user.setLastName(updateUserProfileDto.lastName());
        }
        if (updateUserProfileDto.email() != null
                && !updateUserProfileDto.email().isBlank()
                && !user.getEmail().equals(updateUserProfileDto.email())) {
            userRepository.findByEmail(updateUserProfileDto.email())
                    .ifPresent(existingUser -> {
                        throw new IllegalArgumentException("Email " + updateUserProfileDto.email()
                                + " is already taken");
                    });
            user.setEmail(updateUserProfileDto.email());
        }
        return userMapper.toUserProfileInfoDto(userRepository.save(user));
    }

    @Override
    public List<UserProfileInfoDto> getAllUsers(Pageable pageable) {
        return userRepository.findAll(pageable).map(userMapper::toUserProfileInfoDto).getContent();
    }
}
