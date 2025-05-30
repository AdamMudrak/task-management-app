package com.example.taskmanagementapp.services.impl;

import static com.example.taskmanagementapp.services.utils.UpdateValueValidatorUtil.areStringsValid;

import com.example.taskmanagementapp.dtos.role.RoleNameDto;
import com.example.taskmanagementapp.dtos.user.request.UpdateUserProfileRequest;
import com.example.taskmanagementapp.dtos.user.request.UserAccountStatusDto;
import com.example.taskmanagementapp.dtos.user.response.UpdateUserProfileResponse;
import com.example.taskmanagementapp.dtos.user.response.UserProfileAdminResponse;
import com.example.taskmanagementapp.dtos.user.response.UserProfileResponse;
import com.example.taskmanagementapp.entities.Role;
import com.example.taskmanagementapp.entities.User;
import com.example.taskmanagementapp.exceptions.EntityNotFoundException;
import com.example.taskmanagementapp.exceptions.ForbiddenException;
import com.example.taskmanagementapp.mappers.UserMapper;
import com.example.taskmanagementapp.repositories.RoleRepository;
import com.example.taskmanagementapp.repositories.UserRepository;
import com.example.taskmanagementapp.security.jwtutils.abstr.JwtAbstractUtil;
import com.example.taskmanagementapp.security.jwtutils.strategy.JwtStrategy;
import com.example.taskmanagementapp.security.jwtutils.strategy.JwtType;
import com.example.taskmanagementapp.services.UserService;
import com.example.taskmanagementapp.services.email.ChangeEmailService;
import com.example.taskmanagementapp.services.utils.ParamFromHttpRequestUtil;
import jakarta.servlet.http.HttpServletRequest;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class UserServiceImpl implements UserService {
    private final ChangeEmailService changeEmailService;
    private final ParamFromHttpRequestUtil randomParamFromHttpRequestUtil;
    private final JwtStrategy jwtStrategy;
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final UserMapper userMapper;

    @Override
    public UserProfileResponse updateUserRole(Long authenticatedUserId,
                                              Long employeeId,
                                              RoleNameDto roleNameDto) throws ForbiddenException {
        if (authenticatedUserId.equals(employeeId)) {
            throw new IllegalArgumentException(
                    "To prevent unwanted damage, self-assigning of roles is restricted. "
                            + "Your own role shall be changed only manually via MySql "
                            + "Workbench or any other MySql compatible instrument");
        }
        User employee = userRepository.findById(employeeId).orElseThrow(
                () -> new EntityNotFoundException("Employee with id " + employeeId + " not found"));
        if (employee.getRole().getName().equals(Role.RoleName.ROLE_ADMIN)) {
            throw new ForbiddenException("SUPERVISOR role can be revoked only via SQL directly");
        }
        Role role = roleRepository.findByName(Role.RoleName.valueOf(roleNameDto.name()));
        employee.setRole(role);
        return userMapper.toUserProfileInfoDto(userRepository.save(employee));
    }

    @Override
    public UserProfileResponse getProfileInfo(Long authenticatedUserId) {
        return userMapper.toUserProfileInfoDto(userRepository.findById(authenticatedUserId)
                .orElseThrow(() -> new EntityNotFoundException(
                        "Employee with id " + authenticatedUserId + " not found")));
    }

    @Override
    public UpdateUserProfileResponse updateProfileInfo(Long authenticatedUserId,
                                                UpdateUserProfileRequest updateUserProfileDto) {
        User user = userRepository.findById(authenticatedUserId).orElseThrow(
                () -> new EntityNotFoundException("User with id " + authenticatedUserId
                        + " not found"));
        if (areStringsValid(updateUserProfileDto.firstName(), user.getFirstName())) {
            user.setFirstName(updateUserProfileDto.firstName());
        }
        if (areStringsValid(updateUserProfileDto.lastName(), user.getLastName())) {
            user.setLastName(updateUserProfileDto.lastName());
        }
        if (areStringsValid(updateUserProfileDto.email(), user.getEmail())) {
            userRepository.findByEmail(updateUserProfileDto.email())
                    .ifPresent(existingUser -> {
                        throw new IllegalArgumentException("Email " + updateUserProfileDto.email()
                                + " is already taken");
                    });
            changeEmailService.sendChangeEmail(updateUserProfileDto.email(), user.getEmail());
        }
        return userMapper.toUpdateUserProfileInfoDto(userRepository.save(user));
    }

    @Override
    public UserProfileAdminResponse changeStatus(User user, Long disabledUserId,
                                                 UserAccountStatusDto accountStatusDto)
            throws ForbiddenException {
        if (user.getId().equals(disabledUserId)) {
            throw new ForbiddenException("You can not change your own account status");
        }

        User thisUser = userRepository.findById(disabledUserId).orElseThrow(
                () -> new EntityNotFoundException("User with id " + disabledUserId + " not found"));

        switch (accountStatusDto) {
            case LOCKED -> {
                thisUser.setEnabled(false);
                thisUser.setAccountNonLocked(false);
            }
            case NON_LOCKED -> {
                thisUser.setEnabled(true);
                thisUser.setAccountNonLocked(true);
            }
            default -> throw new IllegalArgumentException(
                    "Invalid account status " + accountStatusDto);
        }
        return userMapper.toUserProfileAdminInfoDto(userRepository.save(thisUser));
    }

    @Override
    public List<UserProfileResponse> getAllUsers(Pageable pageable) {
        return userRepository.findAll(pageable).map(userMapper::toUserProfileInfoDto).getContent();
    }

    @Override
    public UserProfileResponse confirmEmailChange(HttpServletRequest httpServletRequest) {
        String token = randomParamFromHttpRequestUtil
                .parseRandomParameterAndToken(httpServletRequest);
        JwtAbstractUtil jwtActionUtil = jwtStrategy.getStrategy(JwtType.ACTION);
        jwtActionUtil.isValidToken(token);

        String email = jwtActionUtil.getUsername(token);
        User user = userRepository.findByEmail(email).orElseThrow(
                () -> new EntityNotFoundException("User with email "
                        + email + " was not found"));
        user.setEmail(randomParamFromHttpRequestUtil.getNamedParameter(httpServletRequest,
                "newEmail"));

        return userMapper.toUserProfileInfoDto(userRepository.save(user));
    }
}
