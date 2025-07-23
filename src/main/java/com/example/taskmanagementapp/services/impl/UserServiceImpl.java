package com.example.taskmanagementapp.services.impl;

import static com.example.taskmanagementapp.constants.security.SecurityConstants.FORBIDDEN_STATUS_CHANGE;
import static com.example.taskmanagementapp.constants.security.SecurityConstants.UPDATE_USER_ROLE_EXCEPTION;
import static com.example.taskmanagementapp.constants.security.SecurityConstants.URL_WAS_CHANGED;
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
import com.example.taskmanagementapp.repositories.ActionTokenRepository;
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
    private final ActionTokenRepository actionTokenRepository;

    @Override
    public UserProfileResponse updateUserRole(Long authenticatedUserId,
                                              Long employeeId,
                                              RoleNameDto roleNameDto) {
        if (authenticatedUserId.equals(employeeId)) {
            throw new IllegalArgumentException(UPDATE_USER_ROLE_EXCEPTION);
        }
        User employee = userRepository.findById(employeeId).orElseThrow(
                () -> new EntityNotFoundException("Employee with id " + employeeId + " not found"));
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
            if (userRepository.existsByEmail(updateUserProfileDto.email())) {
                throw new IllegalArgumentException("Email " + updateUserProfileDto.email()
                        + " is already taken");
            }
            changeEmailService.sendChangeEmail(updateUserProfileDto.email(), user.getEmail());
        }
        return userMapper.toUpdateUserProfileInfoDto(userRepository.save(user));
    }

    @Override
    public UserProfileAdminResponse changeStatus(Long authenticatedUserId, Long changedUserId,
                                                 UserAccountStatusDto accountStatusDto)
            throws ForbiddenException {
        if (authenticatedUserId.equals(changedUserId)) {
            throw new ForbiddenException(FORBIDDEN_STATUS_CHANGE);
        }

        User thisUser = userRepository.findById(changedUserId).orElseThrow(
                () -> new EntityNotFoundException("User with id " + changedUserId + " not found"));

        if (accountStatusDto == null) {
            throw new IllegalArgumentException("accountStatusDto can't be null");
        }

        if (accountStatusDto.equals(UserAccountStatusDto.LOCKED)) {
            thisUser.setEnabled(false);
            thisUser.setAccountNonLocked(false);
        } else if (accountStatusDto.equals(UserAccountStatusDto.NON_LOCKED)) {
            thisUser.setEnabled(true);
            thisUser.setAccountNonLocked(true);
        } else {
            throw new IllegalArgumentException("Invalid account status " + accountStatusDto);
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
        String newEmail = randomParamFromHttpRequestUtil.getNamedParameter(httpServletRequest,
                "newEmail");
        if (!actionTokenRepository.existsByActionToken(token + newEmail)) {
            throw new EntityNotFoundException(URL_WAS_CHANGED);
        }
        actionTokenRepository.deleteByActionToken(token + newEmail);
        String email = jwtActionUtil.getUsername(token);
        User user = userRepository.findByEmail(email).orElseThrow(
                () -> new EntityNotFoundException("User with email "
                        + email + " was not found"));
        user.setEmail(newEmail);

        return userMapper.toUserProfileInfoDto(userRepository.save(user));
    }
}
