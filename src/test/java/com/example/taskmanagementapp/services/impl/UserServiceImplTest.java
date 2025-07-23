package com.example.taskmanagementapp.services.impl;

import static com.example.taskmanagementapp.constants.security.SecurityConstants.FORBIDDEN_STATUS_CHANGE;
import static com.example.taskmanagementapp.constants.security.SecurityConstants.UPDATE_USER_ROLE_EXCEPTION;
import static com.example.taskmanagementapp.constants.security.SecurityConstants.URL_WAS_CHANGED;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.example.taskmanagementapp.dtos.role.RoleNameDto;
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
import com.example.taskmanagementapp.security.jwtutils.impl.JwtActionUtil;
import com.example.taskmanagementapp.security.jwtutils.strategy.JwtStrategy;
import com.example.taskmanagementapp.security.jwtutils.strategy.JwtType;
import com.example.taskmanagementapp.services.email.ChangeEmailService;
import com.example.taskmanagementapp.services.utils.ParamFromHttpRequestUtil;
import com.example.taskmanagementapp.testutils.Constants;
import com.example.taskmanagementapp.testutils.ObjectFactory;
import jakarta.servlet.http.HttpServletRequest;
import java.util.Optional;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class UserServiceImplTest {
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
            Long authenticatedUserId = Constants.FIRST_USER_ID;
            Long chosenUserId = Constants.FIRST_USER_ID;
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
            Role role = ObjectFactory.getUserRole();
            User user = ObjectFactory.getUser2(role);
            user.setId(Constants.LAST_USER_ID);

            UserProfileResponse userProfileResponse = ObjectFactory.getUserProfileResponse(user);

            RoleNameDto roleNameDto = RoleNameDto.ROLE_ADMIN;
            Long chosenUserId = Constants.LAST_USER_ID;

            //when
            when(userRepository.findById(chosenUserId)).thenReturn(Optional.of(user));
            when(roleRepository.findByName(
                    Role.RoleName.valueOf(roleNameDto.name()))).thenReturn(role);
            when(userRepository.save(user)).thenReturn(user);
            when(userMapper.toUserProfileInfoDto(user)).thenReturn(userProfileResponse);

            Long authenticatedUserId = Constants.FIRST_USER_ID;
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
            Long authenticatedUserId = Constants.FIRST_USER_ID;
            Long chosenUserId = Constants.RANDOM_USER_ID;
            RoleNameDto roleNameDto = RoleNameDto.ROLE_ADMIN;

            //when
            when(userRepository.findById(Constants.RANDOM_USER_ID)).thenReturn(Optional.empty());

            //then
            EntityNotFoundException entityNotFoundException =
                    assertThrows(EntityNotFoundException.class, () -> userServiceImpl
                            .updateUserRole(authenticatedUserId, chosenUserId, roleNameDto));
            assertEquals("Employee with id " + Constants.RANDOM_USER_ID
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
            Long authenticatedUserId = Constants.FIRST_USER_ID;
            Role role = ObjectFactory.getUserRole();
            User user = ObjectFactory.getUser1(role);
            user.setId(authenticatedUserId);

            UserProfileResponse userProfileResponse = ObjectFactory.getUserProfileResponse(user);

            //when
            when(userRepository.findById(authenticatedUserId)).thenReturn(Optional.of(user));
            when(userMapper.toUserProfileInfoDto(user)).thenReturn(userProfileResponse);

            //then
            assertEquals(userProfileResponse, userServiceImpl.getProfileInfo(authenticatedUserId));

            //verify
            verify(userRepository, times(1)).findById(authenticatedUserId);
            verify(userMapper, times(1)).toUserProfileInfoDto(user);
        }

        @Test
        void givenUnrealUserId_whenGetProfileInfo_ThenFail() {
            //given
            Long authenticatedUserId = Constants.RANDOM_USER_ID;

            //when
            when(userRepository.findById(authenticatedUserId)).thenReturn(Optional.empty());

            //then
            EntityNotFoundException entityNotFoundException =
                    assertThrows(EntityNotFoundException.class, () -> userServiceImpl
                            .getProfileInfo(authenticatedUserId));
            assertEquals("Employee with id " + Constants.RANDOM_USER_ID
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
            Long authenticatedUserId = Constants.LAST_USER_ID;
            Role role = ObjectFactory.getUserRole();
            User user = ObjectFactory.getUser2(role);
            user.setId(authenticatedUserId);

            UpdateUserProfileResponse expectedUpdateUserProfileResponse =
                    ObjectFactory.getUpdateUserProfileResponse();

            //when
            when(userRepository.findById(authenticatedUserId)).thenReturn(Optional.of(user));
            when(userRepository.existsByEmail(Constants.EMAIL_1)).thenReturn(false);
            when(userRepository.save(user)).thenReturn(user);
            when(userMapper.toUpdateUserProfileInfoDto(user))
                    .thenReturn(expectedUpdateUserProfileResponse);

            //then
            assertEquals(expectedUpdateUserProfileResponse,
                    userServiceImpl.updateProfileInfo(authenticatedUserId,
                            ObjectFactory.getUpdateUserProfileRequest()));

            //verify
            verify(userRepository, times(1)).findById(authenticatedUserId);
            verify(userRepository, times(1)).existsByEmail(Constants.EMAIL_1);
            verify(userRepository, times(1)).save(user);
            verify(userMapper, times(1)).toUpdateUserProfileInfoDto(user);
        }

        @Test
        void givenUpdateDtoWithTakenEmail_whenUpdateProfileInfo_ThenFail() {
            //given
            Long authenticatedUserId = Constants.LAST_USER_ID;
            Role role = ObjectFactory.getUserRole();
            User user = ObjectFactory.getUser2(role);
            user.setId(authenticatedUserId);

            //when
            when(userRepository.findById(authenticatedUserId)).thenReturn(Optional.of(user));
            when(userRepository.existsByEmail(Constants.EMAIL_1)).thenReturn(true);

            //then
            IllegalArgumentException illegalArgumentException = assertThrows(
                    IllegalArgumentException.class, () -> userServiceImpl
                            .updateProfileInfo(authenticatedUserId,
                            ObjectFactory.getUpdateUserProfileRequest()));
            assertEquals("Email " + Constants.EMAIL_1
                    + " is already taken", illegalArgumentException.getMessage());

            //verify
            verify(userRepository, times(1)).findById(authenticatedUserId);
            verify(userRepository, times(1)).existsByEmail(Constants.EMAIL_1);
        }

        @Test
        void givenFakeAuthenticatedId_whenUpdateProfileInfo_ThenFail() {
            //given
            Long authenticatedUserId = Constants.RANDOM_USER_ID;

            //when
            when(userRepository.findById(authenticatedUserId)).thenReturn(Optional.empty());

            //then
            EntityNotFoundException entityNotFoundException = assertThrows(
                    EntityNotFoundException.class, () -> userServiceImpl
                            .updateProfileInfo(authenticatedUserId,
                                    ObjectFactory.getUpdateUserProfileRequest()));
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
            Long changedUserId = Constants.FIRST_USER_ID;

            //then
            ForbiddenException forbiddenException = assertThrows(ForbiddenException.class,
                    () -> userServiceImpl.changeStatus(
                            changedUserId, changedUserId, UserAccountStatusDto.LOCKED));
            assertEquals(FORBIDDEN_STATUS_CHANGE, forbiddenException.getMessage());
        }

        @Test
        void givenFakeChangedUserId_whenChangeStatus_thenFail() {
            //given
            Long changedUserId = Constants.RANDOM_USER_ID;
            Long adminUserId = Constants.FIRST_USER_ID;

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
            Long changedUserId = Constants.FIRST_USER_ID;
            User user = ObjectFactory.getUser1(ObjectFactory.getUserRole());
            user.setId(changedUserId);
            UserProfileAdminResponse userProfileAdminResponseLocked =
                    ObjectFactory.getUserProfileAdminResponseLocked(user);

            //when
            when(userRepository.findById(changedUserId)).thenReturn(Optional.of(user));
            when(userRepository.save(user)).thenReturn(user);
            when(userMapper.toUserProfileAdminInfoDto(user))
                    .thenReturn(userProfileAdminResponseLocked);

            //then
            assertEquals(userProfileAdminResponseLocked,
                    userServiceImpl.changeStatus(
                            Constants.LAST_USER_ID, changedUserId, UserAccountStatusDto.LOCKED));

            //verify
            verify(userRepository, times(1)).findById(changedUserId);
            verify(userRepository, times(1)).save(user);
            verify(userMapper, times(1)).toUserProfileAdminInfoDto(user);
        }

        @Test
        void givenValidUserAndChangedId_whenChangeStatusNonLocked_thenSuccess()
                throws ForbiddenException {
            //given
            Long changedUserId = Constants.FIRST_USER_ID;
            User user = ObjectFactory.getUser1(ObjectFactory.getUserRole());
            user.setId(changedUserId);
            UserProfileAdminResponse userProfileAdminResponseNonLocked =
                    ObjectFactory.getUserProfileAdminResponseNonLocked(user);

            //when
            when(userRepository.findById(changedUserId)).thenReturn(Optional.of(user));
            when(userRepository.save(user)).thenReturn(user);
            when(userMapper.toUserProfileAdminInfoDto(user))
                    .thenReturn(userProfileAdminResponseNonLocked);

            //then
            assertEquals(userProfileAdminResponseNonLocked,
                    userServiceImpl.changeStatus(Constants.LAST_USER_ID,
                            changedUserId, UserAccountStatusDto.NON_LOCKED));

            //verify
            verify(userRepository, times(1)).findById(changedUserId);
            verify(userRepository, times(1)).save(user);
            verify(userMapper, times(1)).toUserProfileAdminInfoDto(user);
        }

        @Test
        void givenNullStatusDto_whenChangeStatus_thenThrowException() {
            //given
            Long changedUserId = Constants.FIRST_USER_ID;
            User user = ObjectFactory.getUser1(ObjectFactory.getUserRole());
            user.setId(changedUserId);

            //when
            when(userRepository.findById(changedUserId)).thenReturn(Optional.of(user));

            //then
            IllegalArgumentException illegalArgumentException =
                    assertThrows(IllegalArgumentException.class,
                            () -> userServiceImpl.changeStatus(Constants.LAST_USER_ID,
                    changedUserId, null));
            assertEquals("accountStatusDto can't be null",
                    illegalArgumentException.getMessage());

            //verify
            verify(userRepository, times(1)).findById(changedUserId);
        }
    }

    @Nested
    class ConfirmEmailChange {
        @Test
        void givenValidToken_whenConfirmEmailChange_thenSuccess() {
            //given
            String oldEmail = Constants.EMAIL_1;
            String newEmail = Constants.EMAIL_2;
            JwtAbstractUtil jwtActionUtil = new JwtActionUtil(
                    Constants.SECRET_KEY, Constants.ACTION_EXPIRATION);
            String expectedToken = jwtActionUtil.generateToken(oldEmail);
            User user = ObjectFactory.getUser1(ObjectFactory.getUserRole());
            UserProfileResponse expectedUserProfileResponse =
                    ObjectFactory.getUserProfileResponse(user);
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
            String oldEmail = Constants.EMAIL_1;
            String newEmail = Constants.EMAIL_2;
            JwtAbstractUtil jwtActionUtil = new JwtActionUtil(
                    Constants.SECRET_KEY, Constants.ACTION_EXPIRATION);
            String expectedToken = jwtActionUtil.generateToken(oldEmail);
            HttpServletRequest httpServletRequest = Mockito.mock(HttpServletRequest.class);

            //when
            when(randomParamFromHttpRequestUtil.parseRandomParameterAndToken(httpServletRequest))
                    .thenReturn(expectedToken);
            when(jwtStrategy.getStrategy(JwtType.ACTION)).thenReturn(jwtActionUtil);
            when(randomParamFromHttpRequestUtil.getNamedParameter(httpServletRequest, "newEmail"))
                    .thenReturn(newEmail);
            when(actionTokenRepository.existsByActionToken(expectedToken + newEmail))
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
            verify(actionTokenRepository, times(1)).existsByActionToken(expectedToken + newEmail);
        }

        @Test
        void givenInvalidTokenWithFakeEmail_whenConfirmEmailChange_thenException() {
            //given
            String oldEmail = Constants.EMAIL_3;
            String newEmail = Constants.EMAIL_2;
            JwtAbstractUtil jwtActionUtil = new JwtActionUtil(
                    Constants.SECRET_KEY, Constants.ACTION_EXPIRATION);
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
}
