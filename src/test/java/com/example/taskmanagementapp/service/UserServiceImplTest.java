package com.example.taskmanagementapp.service;

import static com.example.taskmanagementapp.constant.security.SecurityConstants.CONFIRM_NEW_EMAIL_MESSAGE;
import static com.example.taskmanagementapp.constant.security.SecurityConstants.FORBIDDEN_STATUS_CHANGE;
import static com.example.taskmanagementapp.constant.security.SecurityConstants.STATUS_CANNOT_BE_NULL;
import static com.example.taskmanagementapp.constant.security.SecurityConstants.UPDATE_USER_ROLE_EXCEPTION;
import static com.example.taskmanagementapp.constant.security.SecurityConstants.URL_WAS_CHANGED;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.example.taskmanagementapp.dto.role.RoleNameDto;
import com.example.taskmanagementapp.dto.user.request.UpdateUserProfileRequest;
import com.example.taskmanagementapp.dto.user.request.UserAccountStatusDto;
import com.example.taskmanagementapp.dto.user.response.UpdateUserProfileResponse;
import com.example.taskmanagementapp.dto.user.response.UserProfileAdminResponse;
import com.example.taskmanagementapp.dto.user.response.UserProfileResponse;
import com.example.taskmanagementapp.entity.Role;
import com.example.taskmanagementapp.entity.User;
import com.example.taskmanagementapp.exception.EntityNotFoundException;
import com.example.taskmanagementapp.exception.ForbiddenException;
import com.example.taskmanagementapp.mapper.UserMapper;
import com.example.taskmanagementapp.repository.ActionTokenRepository;
import com.example.taskmanagementapp.repository.RoleRepository;
import com.example.taskmanagementapp.repository.UserRepository;
import com.example.taskmanagementapp.security.jwtutil.abstr.JwtAbstractUtil;
import com.example.taskmanagementapp.security.jwtutil.impl.JwtActionUtil;
import com.example.taskmanagementapp.security.jwtutil.strategy.JwtStrategy;
import com.example.taskmanagementapp.security.jwtutil.strategy.JwtType;
import com.example.taskmanagementapp.service.email.ChangeEmailService;
import com.example.taskmanagementapp.service.impl.UserServiceImpl;
import com.example.taskmanagementapp.service.utils.ParamFromHttpRequestUtil;
import jakarta.servlet.http.HttpServletRequest;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

@ExtendWith(MockitoExtension.class)
public class UserServiceImplTest {
    private static final String TEST_USERNAME = "JohnDoe";
    private static final String TEST_PASSWORD_ENCODED =
            "$2a$10$u4cOSEeePFyJlpvkPdtmhenMuPYhloQfrVS19DZU8/.5jtJNm7piW";
    private static final String TEST_EMAIL = "john_doe@mail.com";

    private static final String ANOTHER_TEST_USERNAME = "RichardRoe";
    private static final String ANOTHER_TEST_EMAIL = "richard_roe@mail.com";

    private static final String YET_ANOTHER_TEST_EMAIL = "jane_doe@mail.com";

    private static final String ROLE_USER = "ROLE_USER";

    private static final String FIRST_NAME = "John";
    private static final String LAST_NAME = "Doe";

    private static final String ANOTHER_FIRST_NAME = "Richard";
    private static final String ANOTHER_LAST_NAME = "Roe";

    private static final String YET_ANOTHER_FIRST_NAME = "Alice";
    private static final String YET_ANOTHER_LAST_NAME = "Bow";

    private static final long ACTION_EXPIRATION = 60000L;
    private static final String SECRET_KEY =
            "eZTQb1Um2KE0dukTWfyHZSq2R3R1SFyqfRFfiReAPn1NHMKUBiTDKc5tajfn";

    private static final long FIRST_USER_ID = 1L;
    private static final long ANOTHER_USER_ID = 2L;
    private static final long YET_ANOTHER_USER_ID = 3L;
    private static final long RANDOM_USER_ID = 1000L;

    @Mock
    private ChangeEmailService changeEmailService;
    @Mock
    private ParamFromHttpRequestUtil randomParamFromHttpRequestUtil;
    @Mock
    private JwtStrategy jwtStrategy;
    @Mock
    private UserRepository userRepository;
    @Mock
    private RoleRepository roleRepository;
    @Mock
    private UserMapper userMapper;
    @Mock
    private ActionTokenRepository actionTokenRepository;
    @InjectMocks
    private UserServiceImpl userServiceImpl;

    @Nested
    class UpdateUserRole {
        @Test
        void givenSameAuthenticatedAndChosenUserId_whenUpdateUserRole_ThenFail() {
            //given
            Long authenticatedUserId = FIRST_USER_ID;
            Long chosenUserId = FIRST_USER_ID;
            RoleNameDto roleNameDto = RoleNameDto.ROLE_ADMIN;
            //then
            IllegalArgumentException illegalArgumentException =
                    assertThrows(IllegalArgumentException.class,
                            () -> userServiceImpl.updateUserRole(
                            authenticatedUserId, chosenUserId, roleNameDto));
            assertEquals(UPDATE_USER_ROLE_EXCEPTION, illegalArgumentException.getMessage());
        }

        @Test
        void givenDifferentAuthenticatedAndChosenUserId_whenUpdateUserRole_ThenSuccess() {
            //given
            Role role = Role.builder().name(Role.RoleName.ROLE_USER).build();
            User user = User.builder()
                    .id(ANOTHER_USER_ID)
                    .username(TEST_USERNAME)
                    .password(TEST_PASSWORD_ENCODED)
                    .email(TEST_EMAIL)
                    .firstName(FIRST_NAME)
                    .lastName(LAST_NAME)
                    .role(role)
                    .isEnabled(true)
                    .isAccountNonLocked(true)
                    .build();

            UserProfileResponse userProfileResponse = UserProfileResponse.builder()
                    .id(ANOTHER_USER_ID)
                    .username(TEST_USERNAME)
                    .email(TEST_EMAIL)
                    .firstName(FIRST_NAME)
                    .lastName(LAST_NAME)
                    .role(ROLE_USER)
                    .build();

            RoleNameDto roleNameDto = RoleNameDto.ROLE_ADMIN;
            Long chosenUserId = ANOTHER_USER_ID;

            //when
            when(userRepository.findById(chosenUserId)).thenReturn(Optional.of(user));
            when(roleRepository.findByName(
                    Role.RoleName.valueOf(roleNameDto.name()))).thenReturn(role);
            when(userRepository.save(user)).thenReturn(user);
            when(userMapper.toUserProfileInfoDto(user)).thenReturn(userProfileResponse);

            Long authenticatedUserId = FIRST_USER_ID;
            //then
            assertEquals(userProfileResponse,
                    userServiceImpl.updateUserRole(authenticatedUserId, chosenUserId, roleNameDto));

            //verify
            verify(userRepository, times(1)).findById(chosenUserId);
            verify(roleRepository, times(1))
                    .findByName(Role.RoleName.valueOf(roleNameDto.name()));
            verify(userRepository, times(1)).save(user);
            verify(userMapper, times(1)).toUserProfileInfoDto(user);
        }

        @Test
        void givenRandomChosenId_whenUpdateUserRole_ThenEntityNotFound() {
            //given
            Long authenticatedUserId = FIRST_USER_ID;
            Long chosenUserId = RANDOM_USER_ID;
            RoleNameDto roleNameDto = RoleNameDto.ROLE_ADMIN;

            //when
            when(userRepository.findById(RANDOM_USER_ID)).thenReturn(Optional.empty());

            //then
            EntityNotFoundException entityNotFoundException =
                    assertThrows(EntityNotFoundException.class, () -> userServiceImpl
                            .updateUserRole(authenticatedUserId, chosenUserId, roleNameDto));
            assertEquals("Employee with id " + RANDOM_USER_ID
                    + " not found", entityNotFoundException.getMessage());

            //verify
            verify(userRepository, times(1)).findById(chosenUserId);
        }
    }

    @Nested
    class GetProfileInfo {
        @Test
        void givenRealUserId_whenGetProfileInfo_ThenSuccess() {
            //given
            Role role = Role.builder().name(Role.RoleName.ROLE_USER).build();
            User user = User.builder()
                    .id(FIRST_USER_ID)
                    .username(TEST_USERNAME)
                    .password(TEST_PASSWORD_ENCODED)
                    .email(TEST_EMAIL)
                    .firstName(FIRST_NAME)
                    .lastName(LAST_NAME)
                    .role(role)
                    .isEnabled(true)
                    .isAccountNonLocked(true)
                    .build();

            UserProfileResponse userProfileResponse = UserProfileResponse.builder()
                    .id(FIRST_USER_ID)
                    .username(TEST_USERNAME)
                    .email(TEST_EMAIL)
                    .firstName(FIRST_NAME)
                    .lastName(LAST_NAME)
                    .role(ROLE_USER)
                    .build();

            //when
            when(userRepository.findById(FIRST_USER_ID)).thenReturn(Optional.of(user));
            when(userMapper.toUserProfileInfoDto(user)).thenReturn(userProfileResponse);

            //then
            assertEquals(userProfileResponse, userServiceImpl.getProfileInfo(FIRST_USER_ID));

            //verify
            verify(userRepository, times(1)).findById(FIRST_USER_ID);
            verify(userMapper, times(1)).toUserProfileInfoDto(user);
        }

        @Test
        void givenUnrealUserId_whenGetProfileInfo_ThenFail() {
            //given
            Long authenticatedUserId = RANDOM_USER_ID;

            //when
            when(userRepository.findById(authenticatedUserId)).thenReturn(Optional.empty());

            //then
            EntityNotFoundException entityNotFoundException =
                    assertThrows(EntityNotFoundException.class, () -> userServiceImpl
                            .getProfileInfo(authenticatedUserId));
            assertEquals("Employee with id " + RANDOM_USER_ID
                    + " not found", entityNotFoundException.getMessage());

            //verify
            verify(userRepository, times(1)).findById(authenticatedUserId);
        }
    }

    @Nested
    class UpdateProfileInfo {
        @Test
        void givenValidAuthenticatedUserAndValidUpdateDto_whenUpdateProfileInfo_ThenSuccess() {
            //given
            Role role = Role.builder().name(Role.RoleName.ROLE_USER).build();
            User user = User.builder()
                    .id(ANOTHER_USER_ID)
                    .username(ANOTHER_TEST_USERNAME)
                    .password(TEST_PASSWORD_ENCODED)
                    .email(ANOTHER_TEST_EMAIL)
                    .firstName(ANOTHER_FIRST_NAME)
                    .lastName(ANOTHER_LAST_NAME)
                    .role(role)
                    .isEnabled(true)
                    .isAccountNonLocked(true)
                    .build();

            UpdateUserProfileResponse expectedUpdateUserProfileResponse =
                    UpdateUserProfileResponse.builder()
                            .id(ANOTHER_USER_ID)
                            .username(ANOTHER_TEST_USERNAME)
                            .email(TEST_EMAIL)
                            .firstName(YET_ANOTHER_FIRST_NAME)
                            .lastName(YET_ANOTHER_LAST_NAME)
                            .role(ROLE_USER)
                            .message(CONFIRM_NEW_EMAIL_MESSAGE).build();

            //when
            when(userRepository.findById(FIRST_USER_ID)).thenReturn(Optional.of(user));
            when(userRepository.existsByEmail(TEST_EMAIL)).thenReturn(false);
            when(userRepository.save(user)).thenReturn(user);
            when(userMapper.toUpdateUserProfileInfoDto(user))
                    .thenReturn(expectedUpdateUserProfileResponse);

            //then
            assertEquals(expectedUpdateUserProfileResponse,
                    userServiceImpl.updateProfileInfo(FIRST_USER_ID,
                            new UpdateUserProfileRequest(
                                    YET_ANOTHER_FIRST_NAME,
                                    YET_ANOTHER_LAST_NAME,
                                    TEST_EMAIL)));

            //verify
            verify(userRepository, times(1)).findById(FIRST_USER_ID);
            verify(userRepository, times(1)).existsByEmail(TEST_EMAIL);
            verify(userRepository, times(1)).save(user);
            verify(userMapper, times(1)).toUpdateUserProfileInfoDto(user);
        }

        @Test
        void givenUpdateDtoWithTakenEmail_whenUpdateProfileInfo_ThenFail() {
            //given
            Role role = Role.builder().name(Role.RoleName.ROLE_USER).build();
            User user = User.builder()
                    .id(FIRST_USER_ID)
                    .username(TEST_USERNAME)
                    .password(TEST_PASSWORD_ENCODED)
                    .email(ANOTHER_TEST_EMAIL)
                    .firstName(FIRST_NAME)
                    .lastName(LAST_NAME)
                    .role(role)
                    .isEnabled(true)
                    .isAccountNonLocked(true)
                    .build();

            //when
            when(userRepository.findById(FIRST_USER_ID)).thenReturn(Optional.of(user));
            when(userRepository.existsByEmail(TEST_EMAIL)).thenReturn(true);

            //then
            IllegalArgumentException illegalArgumentException = assertThrows(
                    IllegalArgumentException.class, () -> userServiceImpl
                            .updateProfileInfo(FIRST_USER_ID,
                                    new UpdateUserProfileRequest(
                                            ANOTHER_FIRST_NAME,
                                            ANOTHER_LAST_NAME,
                                            TEST_EMAIL)));
            assertEquals("Email " + TEST_EMAIL
                    + " is already taken", illegalArgumentException.getMessage());

            //verify
            verify(userRepository, times(1)).findById(FIRST_USER_ID);
            verify(userRepository, times(1)).existsByEmail(TEST_EMAIL);
        }

        @Test
        void givenFakeAuthenticatedId_whenUpdateProfileInfo_ThenFail() {
            //given
            Long authenticatedUserId = RANDOM_USER_ID;

            //when
            when(userRepository.findById(authenticatedUserId)).thenReturn(Optional.empty());

            //then
            EntityNotFoundException entityNotFoundException = assertThrows(
                    EntityNotFoundException.class, () -> userServiceImpl
                            .updateProfileInfo(authenticatedUserId,
                                    new UpdateUserProfileRequest(
                                            ANOTHER_FIRST_NAME,
                                            ANOTHER_LAST_NAME,
                                            TEST_EMAIL)));
            assertEquals("User with id " + authenticatedUserId
                    + " not found", entityNotFoundException.getMessage());

            //verify
            verify(userRepository, times(1)).findById(authenticatedUserId);
        }
    }

    @Nested
    class ChangeStatus {
        @Test
        void givenTwoSameIds_whenChangeStatus_thenFail() {
            //given
            Long changedUserId = FIRST_USER_ID;

            //then
            ForbiddenException forbiddenException = assertThrows(ForbiddenException.class,
                    () -> userServiceImpl.changeStatus(
                            changedUserId, changedUserId, UserAccountStatusDto.LOCKED));
            assertEquals(FORBIDDEN_STATUS_CHANGE, forbiddenException.getMessage());
        }

        @Test
        void givenFakeChangedUserId_whenChangeStatus_thenFail() {
            //given
            Long changedUserId = RANDOM_USER_ID;
            Long adminUserId = FIRST_USER_ID;

            //when
            when(userRepository.findById(changedUserId)).thenReturn(Optional.empty());

            //then
            EntityNotFoundException entityNotFoundException =
                    assertThrows(EntityNotFoundException.class, () -> userServiceImpl.changeStatus(
                            adminUserId, changedUserId, UserAccountStatusDto.LOCKED));
            assertEquals("User with id " + changedUserId + " not found",
                    entityNotFoundException.getMessage());

            //verify
            verify(userRepository, times(1)).findById(changedUserId);
        }

        @Test
        void givenValidUserAndChangedId_whenChangeStatusLocked_thenSuccess()
                throws ForbiddenException {
            //given
            Long changedUserId = FIRST_USER_ID;
            Role role = Role.builder().name(Role.RoleName.ROLE_USER).build();
            User user = User.builder()
                    .id(changedUserId)
                    .username(TEST_USERNAME)
                    .password(TEST_PASSWORD_ENCODED)
                    .email(TEST_EMAIL)
                    .firstName(FIRST_NAME)
                    .lastName(LAST_NAME)
                    .role(role)
                    .isEnabled(true)
                    .isAccountNonLocked(true)
                    .build();

            UserProfileAdminResponse userProfileAdminResponseLocked =
                    UserProfileAdminResponse.builder()
                            .id(FIRST_USER_ID)
                            .username(TEST_USERNAME)
                            .email(TEST_EMAIL)
                            .firstName(FIRST_NAME)
                            .lastName(LAST_NAME)
                            .role(ROLE_USER)
                            .isEnabled(false)
                            .isAccountNonLocked(false)
                            .build();

            //when
            when(userRepository.findById(changedUserId)).thenReturn(Optional.of(user));
            when(userRepository.save(user)).thenReturn(user);
            when(userMapper.toUserProfileAdminInfoDto(user))
                    .thenReturn(userProfileAdminResponseLocked);

            //then
            assertEquals(userProfileAdminResponseLocked,
                    userServiceImpl.changeStatus(
                            ANOTHER_USER_ID, changedUserId, UserAccountStatusDto.LOCKED));

            //verify
            verify(userRepository, times(1)).findById(changedUserId);
            verify(userRepository, times(1)).save(user);
            verify(userMapper, times(1)).toUserProfileAdminInfoDto(user);
        }

        @Test
        void givenValidUserAndChangedId_whenChangeStatusNonLocked_thenSuccess()
                throws ForbiddenException {
            //given
            Long changedUserId = FIRST_USER_ID;
            Role role = Role.builder().name(Role.RoleName.ROLE_USER).build();
            User user = User.builder()
                    .id(changedUserId)
                    .username(TEST_USERNAME)
                    .password(TEST_PASSWORD_ENCODED)
                    .email(TEST_EMAIL)
                    .firstName(FIRST_NAME)
                    .lastName(LAST_NAME)
                    .role(role)
                    .isEnabled(true)
                    .isAccountNonLocked(true)
                    .build();

            UserProfileAdminResponse userProfileAdminResponseNonLocked =
                    UserProfileAdminResponse.builder()
                            .id(FIRST_USER_ID)
                            .username(TEST_USERNAME)
                            .email(TEST_EMAIL)
                            .firstName(FIRST_NAME)
                            .lastName(LAST_NAME)
                            .role(ROLE_USER)
                            .isEnabled(true)
                            .isAccountNonLocked(true)
                            .build();

            //when
            when(userRepository.findById(changedUserId)).thenReturn(Optional.of(user));
            when(userRepository.save(user)).thenReturn(user);
            when(userMapper.toUserProfileAdminInfoDto(user))
                    .thenReturn(userProfileAdminResponseNonLocked);

            //then
            assertEquals(userProfileAdminResponseNonLocked,
                    userServiceImpl.changeStatus(ANOTHER_USER_ID,
                            changedUserId, UserAccountStatusDto.NON_LOCKED));

            //verify
            verify(userRepository, times(1)).findById(changedUserId);
            verify(userRepository, times(1)).save(user);
            verify(userMapper, times(1)).toUserProfileAdminInfoDto(user);
        }

        @Test
        void givenNullStatusDto_whenChangeStatus_thenThrowException() {
            //given
            Role role = Role.builder().name(Role.RoleName.ROLE_USER).build();
            User user = User.builder()
                    .id(FIRST_USER_ID)
                    .username(TEST_USERNAME)
                    .password(TEST_PASSWORD_ENCODED)
                    .email(TEST_EMAIL)
                    .firstName(FIRST_NAME)
                    .lastName(LAST_NAME)
                    .role(role)
                    .isEnabled(true)
                    .isAccountNonLocked(true)
                    .build();

            //when
            when(userRepository.findById(FIRST_USER_ID)).thenReturn(Optional.of(user));

            //then
            IllegalArgumentException illegalArgumentException =
                    assertThrows(IllegalArgumentException.class,
                            () -> userServiceImpl.changeStatus(ANOTHER_USER_ID,
                                    FIRST_USER_ID, null));
            assertEquals(STATUS_CANNOT_BE_NULL,
                    illegalArgumentException.getMessage());

            //verify
            verify(userRepository, times(1)).findById(FIRST_USER_ID);
        }
    }

    @Nested
    class ConfirmEmailChange {
        @Test
        void givenValidToken_whenConfirmEmailChange_thenSuccess() {
            //given
            String oldEmail = TEST_EMAIL;
            String newEmail = ANOTHER_TEST_EMAIL;
            JwtAbstractUtil jwtActionUtil = new JwtActionUtil(
                    SECRET_KEY, ACTION_EXPIRATION);
            String expectedToken = jwtActionUtil.generateToken(oldEmail);
            Role role = Role.builder().name(Role.RoleName.ROLE_USER).build();
            User user = User.builder()
                    .id(FIRST_USER_ID)
                    .username(TEST_USERNAME)
                    .password(TEST_PASSWORD_ENCODED)
                    .email(TEST_EMAIL)
                    .firstName(FIRST_NAME)
                    .lastName(LAST_NAME)
                    .role(role)
                    .isEnabled(true)
                    .isAccountNonLocked(true)
                    .build();
            UpdateUserProfileResponse expectedUserProfileResponse =
                    UpdateUserProfileResponse.builder()
                            .id(FIRST_USER_ID)
                            .username(TEST_USERNAME)
                            .email(TEST_EMAIL)
                            .firstName(FIRST_NAME)
                            .lastName(LAST_NAME)
                            .role(ROLE_USER)
                            .message(CONFIRM_NEW_EMAIL_MESSAGE).build();
            HttpServletRequest httpServletRequest = Mockito.mock(HttpServletRequest.class);

            //when
            when(randomParamFromHttpRequestUtil.parseRandomParameterAndToken(httpServletRequest))
                    .thenReturn(expectedToken);
            when(jwtStrategy.getStrategy(JwtType.ACTION)).thenReturn(jwtActionUtil);
            when(randomParamFromHttpRequestUtil.getNamedParameter(httpServletRequest, "newEmail"))
                    .thenReturn(newEmail);
            when(actionTokenRepository.existsByActionToken(expectedToken + newEmail))
                    .thenReturn(true);
            when(userRepository.findByEmail(oldEmail)).thenReturn(Optional.of(user));
            when(userRepository.save(user)).thenReturn(user);
            when(userMapper.toUserProfileInfoDto(user)).thenReturn(expectedUserProfileResponse);

            //then
            assertEquals(expectedUserProfileResponse,
                    userServiceImpl.confirmEmailChange(httpServletRequest));

            //verify
            verify(randomParamFromHttpRequestUtil, times(1))
                    .parseRandomParameterAndToken(httpServletRequest);
            verify(jwtStrategy, times(1)).getStrategy(JwtType.ACTION);
            verify(randomParamFromHttpRequestUtil, times(1))
                    .getNamedParameter(httpServletRequest, "newEmail");
            verify(actionTokenRepository, times(1)).existsByActionToken(expectedToken + newEmail);
            verify(userRepository, times(1)).findByEmail(oldEmail);
            verify(userRepository, times(1)).save(user);
            verify(userMapper, times(1)).toUserProfileInfoDto(user);
        }

        @Test
        void givenInvalidToken_whenConfirmEmailChange_thenException() {
            //given
            JwtAbstractUtil jwtActionUtil = new JwtActionUtil(
                    SECRET_KEY, ACTION_EXPIRATION);
            String expectedToken = jwtActionUtil.generateToken(TEST_EMAIL);
            HttpServletRequest httpServletRequest = Mockito.mock(HttpServletRequest.class);

            //when
            when(randomParamFromHttpRequestUtil.parseRandomParameterAndToken(httpServletRequest))
                    .thenReturn(expectedToken);
            when(jwtStrategy.getStrategy(JwtType.ACTION)).thenReturn(jwtActionUtil);
            when(randomParamFromHttpRequestUtil.getNamedParameter(httpServletRequest, "newEmail"))
                    .thenReturn(ANOTHER_TEST_EMAIL);
            when(actionTokenRepository.existsByActionToken(expectedToken + ANOTHER_TEST_EMAIL))
                    .thenReturn(false);

            //then
            EntityNotFoundException entityNotFoundException =
                    assertThrows(EntityNotFoundException.class,
                            () -> userServiceImpl.confirmEmailChange(httpServletRequest));
            assertEquals(URL_WAS_CHANGED, entityNotFoundException.getMessage());

            //verify
            verify(randomParamFromHttpRequestUtil, times(1))
                    .parseRandomParameterAndToken(httpServletRequest);
            verify(jwtStrategy, times(1)).getStrategy(JwtType.ACTION);
            verify(randomParamFromHttpRequestUtil, times(1))
                    .getNamedParameter(httpServletRequest, "newEmail");
            verify(actionTokenRepository, times(1)).existsByActionToken(expectedToken + ANOTHER_TEST_EMAIL);
        }

        @Test
        void givenInvalidTokenWithFakeEmail_whenConfirmEmailChange_thenException() {
            //given
            String oldEmail = YET_ANOTHER_TEST_EMAIL;
            String newEmail = ANOTHER_TEST_EMAIL;
            JwtAbstractUtil jwtActionUtil = new JwtActionUtil(
                    SECRET_KEY, ACTION_EXPIRATION);
            String expectedToken = jwtActionUtil.generateToken(oldEmail);
            HttpServletRequest httpServletRequest = Mockito.mock(HttpServletRequest.class);

            //when
            when(randomParamFromHttpRequestUtil.parseRandomParameterAndToken(httpServletRequest))
                    .thenReturn(expectedToken);
            when(jwtStrategy.getStrategy(JwtType.ACTION)).thenReturn(jwtActionUtil);
            when(randomParamFromHttpRequestUtil.getNamedParameter(httpServletRequest, "newEmail"))
                    .thenReturn(newEmail);
            when(actionTokenRepository.existsByActionToken(expectedToken + newEmail))
                    .thenReturn(true);
            when(userRepository.findByEmail(oldEmail)).thenReturn(Optional.empty());

            //then
            EntityNotFoundException entityNotFoundException = assertThrows(
                    EntityNotFoundException.class,
                    () -> userServiceImpl.confirmEmailChange(httpServletRequest));
            assertEquals("User with email "
                    + oldEmail + " was not found", entityNotFoundException.getMessage());

            //verify
            verify(randomParamFromHttpRequestUtil, times(1))
                    .parseRandomParameterAndToken(httpServletRequest);
            verify(jwtStrategy, times(1)).getStrategy(JwtType.ACTION);
            verify(randomParamFromHttpRequestUtil, times(1))
                    .getNamedParameter(httpServletRequest, "newEmail");
            verify(actionTokenRepository, times(1)).existsByActionToken(expectedToken + newEmail);
            verify(userRepository, times(1)).findByEmail(oldEmail);
        }
    }

    @Nested
    class GetAllUsers {
        @Test
        void givenThreeUsers_whenGetAllUsers_thenGetThreeUsers() {
            //given
            PageRequest pageRequestForAllUsers = PageRequest.of(0, 3);

            Role role = Role.builder().name(Role.RoleName.ROLE_USER).build();
            User user = User.builder()
                    .id(FIRST_USER_ID)
                    .username(TEST_USERNAME)
                    .password(TEST_PASSWORD_ENCODED)
                    .email(TEST_EMAIL)
                    .firstName(FIRST_NAME)
                    .lastName(LAST_NAME)
                    .role(role)
                    .isEnabled(true)
                    .isAccountNonLocked(true)
                    .build();
            UpdateUserProfileResponse userProfileResponse = UpdateUserProfileResponse
                    .builder()
                            .id(FIRST_USER_ID)
                            .username(TEST_USERNAME)
                            .email(TEST_EMAIL)
                            .firstName(FIRST_NAME)
                            .lastName(LAST_NAME)
                            .role(ROLE_USER)
                            .message(CONFIRM_NEW_EMAIL_MESSAGE).build();

            User anotherUser = User.builder()
                    .id(ANOTHER_USER_ID)
                    .username(ANOTHER_TEST_USERNAME)
                    .password(TEST_PASSWORD_ENCODED)
                    .email(ANOTHER_TEST_EMAIL)
                    .firstName(ANOTHER_FIRST_NAME)
                    .lastName(ANOTHER_LAST_NAME)
                    .role(role)
                    .isEnabled(true)
                    .isAccountNonLocked(true)
                    .build();
            UpdateUserProfileResponse anotherUserProfileResponse = UpdateUserProfileResponse
                    .builder()
                    .id(ANOTHER_USER_ID)
                    .username(ANOTHER_TEST_USERNAME)
                    .email(ANOTHER_TEST_EMAIL)
                    .firstName(ANOTHER_FIRST_NAME)
                    .lastName(ANOTHER_LAST_NAME)
                    .role(ROLE_USER)
                    .message(CONFIRM_NEW_EMAIL_MESSAGE).build();

            User yetAnotherUser = User.builder()
                    .id(YET_ANOTHER_USER_ID)
                    .username(ANOTHER_TEST_USERNAME)
                    .password(TEST_PASSWORD_ENCODED)
                    .email(ANOTHER_TEST_EMAIL)
                    .firstName(YET_ANOTHER_FIRST_NAME)
                    .lastName(YET_ANOTHER_LAST_NAME)
                    .role(role)
                    .isEnabled(true)
                    .isAccountNonLocked(true)
                    .build();
            UpdateUserProfileResponse yetAnotherUserResponse = UpdateUserProfileResponse
                    .builder()
                    .id(YET_ANOTHER_USER_ID)
                    .username(ANOTHER_TEST_USERNAME)
                    .email(YET_ANOTHER_TEST_EMAIL)
                    .firstName(YET_ANOTHER_FIRST_NAME)
                    .lastName(YET_ANOTHER_LAST_NAME)
                    .role(ROLE_USER)
                    .message(CONFIRM_NEW_EMAIL_MESSAGE).build();

            List<User> users = Arrays.asList(user, anotherUser, yetAnotherUser);

            Page<User> usersPage = new PageImpl<>(users, pageRequestForAllUsers, users.size());

            //when
            when(userMapper.toUserProfileInfoDto(user)).thenReturn(userProfileResponse);
            when(userMapper.toUserProfileInfoDto(anotherUser)).thenReturn(anotherUserProfileResponse);
            when(userMapper.toUserProfileInfoDto(yetAnotherUser)).thenReturn(yetAnotherUserResponse);
            when(userRepository.findAll(pageRequestForAllUsers)).thenReturn(usersPage);

            //then
            List<UserProfileResponse> expectedUserProfileResponses =
                    List.of(userProfileResponse, anotherUserProfileResponse, yetAnotherUserResponse);
            assertEquals(expectedUserProfileResponses,
                    userServiceImpl.getAllUsers(pageRequestForAllUsers));
        }
    }
}
