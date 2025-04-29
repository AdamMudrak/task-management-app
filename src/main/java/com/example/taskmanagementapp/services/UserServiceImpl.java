package com.example.taskmanagementapp.services;

import static com.example.taskmanagementapp.constants.security.SecurityConstants.ACTION;

import com.example.taskmanagementapp.dtos.role.RoleNameDto;
import com.example.taskmanagementapp.dtos.user.request.UpdateUserProfileDto;
import com.example.taskmanagementapp.dtos.user.response.UserProfileInfoDto;
import com.example.taskmanagementapp.dtos.user.response.UserProfileInfoDtoOnUpdate;
import com.example.taskmanagementapp.entities.Role;
import com.example.taskmanagementapp.entities.User;
import com.example.taskmanagementapp.entities.tokens.ParamToken;
import com.example.taskmanagementapp.exceptions.notfoundexceptions.EntityNotFoundException;
import com.example.taskmanagementapp.mappers.UserMapper;
import com.example.taskmanagementapp.repositories.paramtoken.ParamTokenRepository;
import com.example.taskmanagementapp.repositories.role.RoleRepository;
import com.example.taskmanagementapp.repositories.user.UserRepository;
import com.example.taskmanagementapp.security.email.ChangeEmailService;
import com.example.taskmanagementapp.security.jwtutils.abstr.JwtAbstractUtil;
import com.example.taskmanagementapp.security.jwtutils.strategy.JwtStrategy;
import com.example.taskmanagementapp.security.utils.RandomParamFromHttpRequestUtil;
import jakarta.servlet.http.HttpServletRequest;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {
    private final ChangeEmailService changeEmailService;
    private final RandomParamFromHttpRequestUtil randomParamFromHttpRequestUtil;
    private final ParamTokenRepository paramTokenRepository;
    private final JwtStrategy jwtStrategy;
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final UserMapper userMapper;

    @Override
    public UserProfileInfoDto updateUserRole(Long authenticatedUserId,
                                             Long employeeId,
                                             RoleNameDto roleNameDto) {
        if (authenticatedUserId.equals(employeeId)) {
            throw new IllegalArgumentException(
                    "To prevent unwanted damage, self-assigning of roles is restricted. "
                            + "Your own role shall be changed only manually via MySql "
                            + "Workbench or any other MySql compatible instrument");
        }
        User employee = userRepository.findById(employeeId).orElseThrow(
                () -> new EntityNotFoundException("Employee with id " + employeeId + " not found"));
        Role role = roleRepository.findByName(Role.RoleName.valueOf(roleNameDto.name()));
        employee.setRole(role);
        return userMapper.toUserProfileInfoDto(userRepository.save(employee));
    }

    @Override
    public UserProfileInfoDto getProfileInfo(Long authenticatedUserId) {
        return userMapper.toUserProfileInfoDto(userRepository.findById(authenticatedUserId)
                .orElseThrow(() -> new EntityNotFoundException(
                        "Employee with id " + authenticatedUserId + " not found")));
    }

    @Override
    public UserProfileInfoDtoOnUpdate updateProfileInfo(Long authenticatedUserId,
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
            changeEmailService.sendChangeEmail(updateUserProfileDto.email(), user.getEmail());
        }
        return userMapper.toUpdateUserProfileInfoDto(userRepository.save(user));
    }

    @Override
    public List<UserProfileInfoDto> getAllUsers(Pageable pageable) {
        return userRepository.findAll(pageable).map(userMapper::toUserProfileInfoDto).getContent();
    }

    @Override
    @Transactional
    public UserProfileInfoDto confirmEmailChange(HttpServletRequest httpServletRequest) {
        randomParamFromHttpRequestUtil.parseRandomParameterAndToken(httpServletRequest);
        String token = randomParamFromHttpRequestUtil.getTokenFromRepo(
                randomParamFromHttpRequestUtil.getRandomParameter(),
                randomParamFromHttpRequestUtil.getToken());
        JwtAbstractUtil jwtActionUtil = jwtStrategy.getStrategy(ACTION);
        jwtActionUtil.isValidToken(token);

        String email = jwtActionUtil.getUsername(token);
        User user = userRepository.findByEmail(email).orElseThrow(
                () -> new EntityNotFoundException("User with email "
                        + email + " was not found"));
        user.setEmail(randomParamFromHttpRequestUtil.getNewEmail(httpServletRequest));

        ParamToken paramToken = paramTokenRepository.findByActionToken(token).orElseThrow(()
                -> new EntityNotFoundException("No such request"));
        paramTokenRepository.deleteById(paramToken.getId());

        return userMapper.toUserProfileInfoDto(userRepository.save(user));
    }
}
