package com.example.taskmanagementapp.services.impl;

import static com.example.taskmanagementapp.constants.security.SecurityConstants.ACCOUNT_IS_LOCKED;
import static com.example.taskmanagementapp.constants.security.SecurityConstants.CHECK_YOUR_EMAIL;
import static com.example.taskmanagementapp.constants.security.SecurityConstants.LINK_EXPIRED;
import static com.example.taskmanagementapp.constants.security.SecurityConstants.LOGIN_SUCCESS;
import static com.example.taskmanagementapp.constants.security.SecurityConstants.PASSWORD_MISMATCH;
import static com.example.taskmanagementapp.constants.security.SecurityConstants.PASSWORD_SET_SUCCESSFULLY;
import static com.example.taskmanagementapp.constants.security.SecurityConstants.REGISTERED;
import static com.example.taskmanagementapp.constants.security.SecurityConstants.REGISTERED_BUT_NOT_ACTIVATED;
import static com.example.taskmanagementapp.constants.security.SecurityConstants.REGISTRATION_CONFIRMED;
import static com.example.taskmanagementapp.constants.security.SecurityConstants.SEND_LINK_TO_RESET_PASSWORD;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.example.taskmanagementapp.dtos.authentication.request.LoginRequest;
import com.example.taskmanagementapp.dtos.authentication.request.PasswordChangeRequest;
import com.example.taskmanagementapp.dtos.authentication.request.RegistrationRequest;
import com.example.taskmanagementapp.dtos.authentication.response.LoginResponse;
import com.example.taskmanagementapp.dtos.authentication.response.PasswordChangeResponse;
import com.example.taskmanagementapp.dtos.authentication.response.PasswordResetLinkResponse;
import com.example.taskmanagementapp.dtos.authentication.response.RegistrationConfirmationResponse;
import com.example.taskmanagementapp.dtos.authentication.response.RegistrationResponse;
import com.example.taskmanagementapp.dtos.authentication.response.ResetLinkSentResponse;
import com.example.taskmanagementapp.entities.Role;
import com.example.taskmanagementapp.entities.User;
import com.example.taskmanagementapp.exceptions.EntityNotFoundException;
import com.example.taskmanagementapp.exceptions.LinkExpiredException;
import com.example.taskmanagementapp.exceptions.LoginException;
import com.example.taskmanagementapp.exceptions.PasswordMismatchException;
import com.example.taskmanagementapp.exceptions.RegistrationException;
import com.example.taskmanagementapp.mappers.UserMapper;
import com.example.taskmanagementapp.repositories.RoleRepository;
import com.example.taskmanagementapp.repositories.UserRepository;
import com.example.taskmanagementapp.security.jwtutils.abstr.JwtAbstractUtil;
import com.example.taskmanagementapp.security.jwtutils.impl.JwtAccessUtil;
import com.example.taskmanagementapp.security.jwtutils.impl.JwtActionUtil;
import com.example.taskmanagementapp.security.jwtutils.impl.JwtRefreshUtil;
import com.example.taskmanagementapp.security.jwtutils.strategy.JwtStrategy;
import com.example.taskmanagementapp.security.jwtutils.strategy.JwtType;
import com.example.taskmanagementapp.services.email.PasswordEmailService;
import com.example.taskmanagementapp.services.email.RegisterConfirmEmailService;
import com.example.taskmanagementapp.services.utils.ParamFromHttpRequestUtil;
import com.example.taskmanagementapp.testutils.Constants;
import com.example.taskmanagementapp.testutils.ObjectFactory;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.crypto.password.PasswordEncoder;

@ExtendWith(MockitoExtension.class)
public class AuthenticationServiceImplTest {
    private static final Role role = ObjectFactory.getUserRole();
    private static final User user = ObjectFactory.getUser1(role);
    private static final User disabledUser = ObjectFactory.getDisabledUser(role);
    private static final User notActiveUser = ObjectFactory.getNotActiveUser(role);
    private static final List<GrantedAuthority> grantedAuthorities = List.of(
            new SimpleGrantedAuthority(role.getName().name()));

    private UserRepository userRepository;
    private UserMapper userMapper;
    private AuthenticationManager authenticationManager;
    private PasswordEncoder passwordEncoder;
    private JwtStrategy jwtStrategy;
    private ParamFromHttpRequestUtil paramFromHttpRequestUtil;
    private AuthenticationServiceImpl authenticationService;

    @BeforeEach
    void setUp() {
        userRepository = mock(UserRepository.class);
        userMapper = mock(UserMapper.class);
        authenticationManager = mock(AuthenticationManager.class);
        passwordEncoder = mock(PasswordEncoder.class);
        jwtStrategy = mock(JwtStrategy.class);
        paramFromHttpRequestUtil = mock(ParamFromHttpRequestUtil.class);

        PasswordEmailService passwordEmailService = mock(PasswordEmailService.class);
        RegisterConfirmEmailService registerConfirmEmailService =
                mock(RegisterConfirmEmailService.class);
        RoleRepository roleRepository = mock(RoleRepository.class);

        authenticationService = new AuthenticationServiceImpl(
                userRepository,
                userMapper,
                authenticationManager,
                passwordEncoder,
                jwtStrategy,
                passwordEmailService,
                registerConfirmEmailService,
                paramFromHttpRequestUtil,
                roleRepository,
                Constants.ACCESS_EXPIRATION,
                Constants.REFRESH_EXPIRATION);
    }

    @Nested
    class AuthenticateUser {
        @Test
        void givenValidLoginRequestDto_whenAuthenticateUserByEmail_thenSuccessfullyLogin()
                throws LoginException {
            //when
            when(userRepository.findByEmail(Constants.EMAIL_1)).thenReturn(Optional.of(user));
            when(authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            Constants.USERNAME_1, Constants.PASSWORD_1)))
                    .thenReturn(new UsernamePasswordAuthenticationToken(
                            Constants.USERNAME_1, Constants.PASSWORD_1_DB, grantedAuthorities));

            JwtAccessUtil jwtAccessUtil = mock(JwtAccessUtil.class);
            when(jwtStrategy.getStrategy(JwtType.ACCESS))
                    .thenReturn(jwtAccessUtil);

            JwtRefreshUtil jwtRefreshUtil = mock(JwtRefreshUtil.class);
            when(jwtStrategy.getStrategy(JwtType.REFRESHMENT))
                    .thenReturn(jwtRefreshUtil);

            //then
            LoginRequest validLoginRequest =
                    new LoginRequest(Constants.EMAIL_1, Constants.PASSWORD_1);
            HttpServletResponse httpServletResponse = mock(HttpServletResponse.class);
            LoginResponse loginResponseResult = authenticationService.authenticateUser(
                    validLoginRequest, httpServletResponse);
            assertEquals(new LoginResponse(LOGIN_SUCCESS), loginResponseResult);

            //verify
            verify(userRepository, times(1)).findByEmail(Constants.EMAIL_1);
            verify(authenticationManager, times(1)).authenticate(
                    new UsernamePasswordAuthenticationToken(
                            Constants.USERNAME_1, Constants.PASSWORD_1));
            verify(jwtStrategy, times(1)).getStrategy(JwtType.ACCESS);
            verify(jwtStrategy, times(1)).getStrategy(JwtType.REFRESHMENT);
        }

        @Test
        void givenNotExistingEmail_whenAuthenticateUserByEmail_thenThrowEntityNotFound() {
            //when
            when(userRepository.findByEmail(Constants.EMAIL_5))
                    .thenThrow(new EntityNotFoundException(
                            "No user with email " + Constants.EMAIL_5 + " found"));

            //then
            LoginRequest invalidLoginRequest =
                    new LoginRequest(Constants.EMAIL_5, Constants.PASSWORD_1);
            HttpServletResponse httpServletResponse = mock(HttpServletResponse.class);
            EntityNotFoundException userNotFoundException =
                    assertThrows(EntityNotFoundException.class, () ->
                            authenticationService
                                    .authenticateUser(invalidLoginRequest, httpServletResponse));
            assertEquals("No user with email "
                    + Constants.EMAIL_5 + " found", userNotFoundException.getMessage());

            //verify
            verify(userRepository, times(1)).findByEmail(Constants.EMAIL_5);
        }

        @Test
        void givenNotExistingUsername_whenAuthenticateUserByUsername_thenThrowEntityNotFound() {
            //when
            when(userRepository.findByUsername(Constants.USERNAME_5)).thenThrow(
                    new EntityNotFoundException("No user with username "
                    + Constants.USERNAME_5 + " found"));

            //then
            LoginRequest invalidLoginRequest =
                    new LoginRequest(Constants.USERNAME_5, Constants.PASSWORD_1);
            HttpServletResponse httpServletResponse = mock(HttpServletResponse.class);
            EntityNotFoundException userNotFoundException =
                    assertThrows(EntityNotFoundException.class, () ->
                            authenticationService
                                    .authenticateUser(invalidLoginRequest, httpServletResponse));
            assertEquals("No user with username "
                    + Constants.USERNAME_5 + " found", userNotFoundException.getMessage());

            //verify
            verify(userRepository, times(1)).findByUsername(Constants.USERNAME_5);
        }

        @Test
        void givenValidLoginRequestDto_whenAuthenticateUserByUsername_thenSuccessfullyLogin()
                throws LoginException {
            //given
            Role role = ObjectFactory.getUserRole();
            User user = ObjectFactory.getUser1(role);
            List<GrantedAuthority> grantedAuthorities = List.of(
                    new SimpleGrantedAuthority(role.getName().name()));

            //when
            when(userRepository.findByUsername(Constants.USERNAME_1)).thenReturn(Optional.of(user));
            when(authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            Constants.USERNAME_1, Constants.PASSWORD_1)))
                    .thenReturn(new UsernamePasswordAuthenticationToken(
                            Constants.USERNAME_1, Constants.PASSWORD_1_DB, grantedAuthorities));

            JwtAccessUtil jwtAccessUtil = mock(JwtAccessUtil.class);
            when(jwtStrategy.getStrategy(JwtType.ACCESS))
                    .thenReturn(jwtAccessUtil);

            JwtRefreshUtil jwtRefreshUtil = mock(JwtRefreshUtil.class);
            when(jwtStrategy.getStrategy(JwtType.REFRESHMENT))
                    .thenReturn(jwtRefreshUtil);

            //then
            LoginRequest validLoginRequest =
                    new LoginRequest(Constants.USERNAME_1, Constants.PASSWORD_1);
            HttpServletResponse httpServletResponse = mock(HttpServletResponse.class);
            LoginResponse loginResponseResult = authenticationService.authenticateUser(
                    validLoginRequest, httpServletResponse);
            assertEquals(new LoginResponse(LOGIN_SUCCESS), loginResponseResult);

            //verify
            verify(userRepository, times(1)).findByUsername(Constants.USERNAME_1);
            verify(authenticationManager, times(1)).authenticate(
                    new UsernamePasswordAuthenticationToken(
                            Constants.USERNAME_1, Constants.PASSWORD_1));
            verify(jwtStrategy, times(1)).getStrategy(JwtType.ACCESS);
            verify(jwtStrategy, times(1)).getStrategy(JwtType.REFRESHMENT);
        }

        @Test
        void givenLockedUser_whenAuthenticateUserByEmail_thenThrowLoginException() {
            //when
            when(userRepository.findByEmail(Constants.EMAIL_3))
                    .thenReturn(Optional.of(disabledUser));

            //then
            LoginRequest lockedUserLoginRequest =
                    new LoginRequest(Constants.EMAIL_3, Constants.PASSWORD_1);
            HttpServletResponse httpServletResponse = mock(HttpServletResponse.class);
            LoginException loginException = assertThrows(LoginException.class,
                    () -> authenticationService.authenticateUser(
                    lockedUserLoginRequest, httpServletResponse));

            assertEquals(ACCOUNT_IS_LOCKED, loginException.getMessage());

            //verify
            verify(userRepository, times(1)).findByEmail(Constants.EMAIL_3);
        }

        @Test
        void givenNonActivatedUser_whenAuthenticateUserByEmail_thenThrowLoginException() {
            //when
            when(userRepository.findByEmail(Constants.EMAIL_4))
                    .thenReturn(Optional.of(notActiveUser));

            //then
            LoginRequest notActiveUserRequest =
                    new LoginRequest(Constants.EMAIL_4, Constants.PASSWORD_1);
            HttpServletResponse httpServletResponse = mock(HttpServletResponse.class);
            LoginException loginException = assertThrows(LoginException.class,
                    () -> authenticationService.authenticateUser(
                            notActiveUserRequest, httpServletResponse));

            assertEquals(REGISTERED_BUT_NOT_ACTIVATED, loginException.getMessage());

            //verify
            verify(userRepository, times(1)).findByEmail(Constants.EMAIL_4);
        }

        @Test
        void givenInvalidPassword_whenAuthenticateUserByEmail_thenThrowLoginException() {
            //given
            Role role = ObjectFactory.getUserRole();
            User user = ObjectFactory.getUser1(role);

            //when
            when(userRepository.findByEmail(Constants.EMAIL_1)).thenReturn(Optional.of(user));
            when(authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            Constants.USERNAME_1, Constants.PASSWORD_2)))
                    .thenThrow(new BadCredentialsException("Bad credentials"));

            //then
            LoginRequest invalidLoginRequest =
                    new LoginRequest(Constants.EMAIL_1, Constants.PASSWORD_2);
            HttpServletResponse httpServletResponse = mock(HttpServletResponse.class);

            assertThrows(LoginException.class, () -> authenticationService.authenticateUser(
                    invalidLoginRequest, httpServletResponse));

            //verify
            verify(userRepository, times(1)).findByEmail(Constants.EMAIL_1);
            verify(authenticationManager, times(1)).authenticate(
                    new UsernamePasswordAuthenticationToken(
                            Constants.USERNAME_1, Constants.PASSWORD_2));
        }

        @Test
        void givenInvalidPassword_whenAuthenticateUserByUsername_thenThrowLoginException() {
            //given
            Role role = ObjectFactory.getUserRole();
            User user = ObjectFactory.getUser1(role);

            //when
            when(userRepository.findByUsername(Constants.USERNAME_1)).thenReturn(Optional.of(user));
            when(authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            Constants.USERNAME_1, Constants.PASSWORD_2)))
                    .thenThrow(new BadCredentialsException("Bad credentials"));

            //then
            LoginRequest invalidLoginRequest =
                    new LoginRequest(Constants.USERNAME_1, Constants.PASSWORD_2);
            HttpServletResponse httpServletResponse = mock(HttpServletResponse.class);

            assertThrows(LoginException.class, () -> authenticationService.authenticateUser(
                    invalidLoginRequest, httpServletResponse));

            //verify
            verify(userRepository, times(1)).findByUsername(Constants.USERNAME_1);
            verify(authenticationManager, times(1)).authenticate(
                    new UsernamePasswordAuthenticationToken(
                            Constants.USERNAME_1, Constants.PASSWORD_2));
        }
    }

    @Nested
    class SendPasswordResetLink {
        @Test
        void givenAnEmailOfEnabledUser_whenSendPasswordResetLink_thenSuccessfullySendLink()
                throws LoginException {
            //when
            when(userRepository.findByEmail(Constants.EMAIL_1)).thenReturn(Optional.of(user));

            //then
            assertEquals(new PasswordResetLinkResponse(SEND_LINK_TO_RESET_PASSWORD),
                    authenticationService.sendPasswordResetLink(Constants.EMAIL_1));

            //verify
            verify(userRepository, times(1)).findByEmail(Constants.EMAIL_1);
        }

        @Test
        void givenAUsernameOfEnabledUser_whenSendPasswordResetLink_thenSuccessfullySendLink()
                throws LoginException {
            //when
            when(userRepository.findByUsername(Constants.USERNAME_1)).thenReturn(Optional.of(user));

            //then
            assertEquals(new PasswordResetLinkResponse(SEND_LINK_TO_RESET_PASSWORD),
                    authenticationService.sendPasswordResetLink(Constants.USERNAME_1));

            //verify
            verify(userRepository, times(1)).findByUsername(Constants.USERNAME_1);
        }

        @Test
        void givenAnEmailOfDisabledUser_whenSendPasswordResetLink_thenThrowLoginException() {
            //when
            when(userRepository.findByEmail(Constants.EMAIL_3))
                    .thenReturn(Optional.of(disabledUser));

            //then
            LoginException loginException = assertThrows(LoginException.class,
                    () -> authenticationService.sendPasswordResetLink(Constants.EMAIL_3));
            assertEquals(ACCOUNT_IS_LOCKED, loginException.getMessage());

            //verify
            verify(userRepository, times(1)).findByEmail(Constants.EMAIL_3);
        }

        @Test
        void givenAnEmailOfNonActiveUser_whenSendPasswordResetLink_thenThrowLoginException() {
            //when
            when(userRepository.findByEmail(Constants.EMAIL_4))
                    .thenReturn(Optional.of(notActiveUser));

            //then
            LoginException loginException = assertThrows(LoginException.class,
                    () -> authenticationService.sendPasswordResetLink(Constants.EMAIL_4));
            assertEquals(REGISTERED_BUT_NOT_ACTIVATED, loginException.getMessage());

            //verify
            verify(userRepository, times(1)).findByEmail(Constants.EMAIL_4);
        }

        @Test
        void givenAnEmailOfNonExistingUser_whenSendPasswordResetLink_thenThrowEntityNotFound() {
            //when
            when(userRepository.findByEmail(Constants.EMAIL_5))
                    .thenThrow(new EntityNotFoundException(
                            "No user with email " + Constants.EMAIL_5 + " found"));

            //then
            EntityNotFoundException userNotFoundException =
                    assertThrows(EntityNotFoundException.class, () ->
                            authenticationService
                                    .sendPasswordResetLink(Constants.EMAIL_5));
            assertEquals("No user with email "
                    + Constants.EMAIL_5 + " found", userNotFoundException.getMessage());

            //verify
            verify(userRepository, times(1)).findByEmail(Constants.EMAIL_5);
        }

        @Test
        void givenAUsernameOfNonExistingUser_whenSendPasswordResetLink_thenThrowEntityNotFound() {
            //when
            when(userRepository.findByUsername(Constants.USERNAME_5))
                    .thenThrow(new EntityNotFoundException(
                            "No user with username " + Constants.USERNAME_5 + " found"));

            //then
            EntityNotFoundException userNotFoundException =
                    assertThrows(EntityNotFoundException.class, () ->
                            authenticationService
                                    .sendPasswordResetLink(Constants.USERNAME_5));
            assertEquals("No user with username "
                    + Constants.USERNAME_5 + " found", userNotFoundException.getMessage());

            //verify
            verify(userRepository, times(1)).findByUsername(Constants.USERNAME_5);
        }
    }

    @Nested
    class ConfirmResetPassword {
        @Test
        void givenGoodToken_whenConfirmResetPassword_thenSuccessfullyNewPassword() {
            //given
            JwtAbstractUtil jwtActionUtil =
                    new JwtActionUtil(Constants.SECRET_KEY, Constants.ACTION_EXPIRATION);
            String goodToken = jwtActionUtil.generateToken(Constants.EMAIL_1);
            HttpServletRequest request = mock(HttpServletRequest.class);

            //when
            when(paramFromHttpRequestUtil
                    .parseRandomParameterAndToken(request))
                        .thenReturn(goodToken);
            when(jwtStrategy.getStrategy(JwtType.ACTION)).thenReturn(jwtActionUtil);
            when(userRepository.findByEmail(Constants.EMAIL_1)).thenReturn(Optional.of(user));

            //then
            assertEquals(new ResetLinkSentResponse(CHECK_YOUR_EMAIL),
                    authenticationService.confirmResetPassword(request));

            //verify
            verify(paramFromHttpRequestUtil, times(1)).parseRandomParameterAndToken(request);
            verify(jwtStrategy, times(1)).getStrategy(JwtType.ACTION);
            verify(userRepository, times(1)).findByEmail(Constants.EMAIL_1);
        }

        @Test
        void givenBadToken_whenConfirmResetPassword_thenThrowLinkExpiredException() {
            //given
            JwtAbstractUtil jwtBadActionUtil =
                    new JwtActionUtil(Constants.SECRET_KEY, Constants.ULTRA_SHORT_EXPIRATION);
            String badToken = jwtBadActionUtil.generateToken(Constants.EMAIL_1);
            HttpServletRequest request = mock(HttpServletRequest.class);

            //when
            when(paramFromHttpRequestUtil
                    .parseRandomParameterAndToken(request))
                    .thenReturn(badToken);
            when(jwtStrategy.getStrategy(JwtType.ACTION)).thenReturn(jwtBadActionUtil);

            //then
            LinkExpiredException linkExpiredException = assertThrows(LinkExpiredException.class,
                    () -> authenticationService.confirmResetPassword(request));
            assertEquals(LINK_EXPIRED, linkExpiredException.getMessage());

            //verify
            verify(paramFromHttpRequestUtil, times(1)).parseRandomParameterAndToken(request);
            verify(jwtStrategy, times(1)).getStrategy(JwtType.ACTION);
        }
    }

    @Nested
    class ChangePassword {
        @Test
        void givenCorrectCurrentAndTwoSameNewPasswords_whenChangePassword_thenSuccess()
                throws PasswordMismatchException {
            //given
            User newUser = ObjectFactory.getUser1(role);
            PasswordChangeRequest passwordChangeRequest = new PasswordChangeRequest(
                    Constants.PASSWORD_1,
                    Constants.PASSWORD_2,
                    Constants.PASSWORD_2);

            //when
            when(passwordEncoder.matches(
                    Constants.PASSWORD_1, Constants.PASSWORD_1_DB)).thenReturn(true);

            //then
            assertEquals(new PasswordChangeResponse(PASSWORD_SET_SUCCESSFULLY),
                    authenticationService.changePassword(newUser, passwordChangeRequest));

            //verify
            verify(passwordEncoder, times(1))
                    .matches(Constants.PASSWORD_1, Constants.PASSWORD_1_DB);
        }

        @Test
        void givenIncorrectCurrentAndTwoSameNewPasswords_whenChangePassword_thenException() {
            //given
            User newUser = ObjectFactory.getUser1(role);
            PasswordChangeRequest passwordChangeRequest = new PasswordChangeRequest(
                    Constants.PASSWORD_3,
                    Constants.PASSWORD_2,
                    Constants.PASSWORD_2);

            //when
            when(passwordEncoder.matches(
                    Constants.PASSWORD_3, Constants.PASSWORD_1_DB)).thenReturn(false);

            //then
            PasswordMismatchException passwordMismatchException =
                    assertThrows(PasswordMismatchException.class,
                            () -> authenticationService
                                    .changePassword(newUser, passwordChangeRequest));

            assertEquals(PASSWORD_MISMATCH, passwordMismatchException.getMessage());

            //verify
            verify(passwordEncoder, times(1))
                    .matches(Constants.PASSWORD_3, Constants.PASSWORD_1_DB);
        }
    }

    @Nested
    class Register {
        @Test
        void givenValidRegistrationDto_whenRegister_thenSuccess() throws RegistrationException {
            //given
            RegistrationRequest registrationRequest = new RegistrationRequest(
                    Constants.USERNAME_1,
                    Constants.PASSWORD_1,
                    Constants.PASSWORD_1,
                    Constants.EMAIL_1,
                    Constants.FIRST_NAME,
                    Constants.LAST_NAME);

            //when
            when(userMapper.toUser(registrationRequest)).thenReturn(user);

            //then
            assertEquals(new RegistrationResponse(REGISTERED),
                    authenticationService.register(registrationRequest));

            //verify
            verify(userMapper, times(1)).toUser(registrationRequest);
        }

        @Test
        void givenRegDtoWithExistingEmail_whenRegister_thenThrowsRegistrationException() {
            //given
            RegistrationRequest registrationRequest = new RegistrationRequest(
                    Constants.USERNAME_1,
                    Constants.PASSWORD_1,
                    Constants.PASSWORD_1,
                    Constants.EMAIL_1,
                    Constants.FIRST_NAME,
                    Constants.LAST_NAME);

            //when
            when(userRepository.existsByEmail(Constants.EMAIL_1)).thenReturn(true);

            //then
            RegistrationException registrationException = assertThrows(RegistrationException.class,
                    () -> authenticationService.register(registrationRequest));
            assertEquals("User with email "
                    + Constants.EMAIL_1 + " already exists", registrationException.getMessage());

            //verify
            verify(userRepository, times(1)).existsByEmail(Constants.EMAIL_1);
        }

        @Test
        void givenRegDtoWithExistingUsername_whenRegister_thenThrowRegistrationException() {
            //given
            RegistrationRequest registrationRequest = new RegistrationRequest(
                    Constants.USERNAME_1,
                    Constants.PASSWORD_1,
                    Constants.PASSWORD_1,
                    Constants.EMAIL_1,
                    Constants.FIRST_NAME,
                    Constants.LAST_NAME);

            //when
            when(userRepository.existsByUsername(Constants.USERNAME_1)).thenReturn(true);

            //then
            RegistrationException registrationException = assertThrows(RegistrationException.class,
                    () -> authenticationService.register(registrationRequest));
            assertEquals("User with username "
                    + Constants.USERNAME_1 + " already exists", registrationException.getMessage());

            //verify
            verify(userRepository, times(1)).existsByUsername(Constants.USERNAME_1);
        }
    }

    @Nested
    class ConfirmRegistration {
        @Test
        void givenToken_whenConfirmRegistration_thenSuccess() {
            //given
            JwtAbstractUtil jwtActionUtil =
                    new JwtActionUtil(Constants.SECRET_KEY, Constants.ACTION_EXPIRATION);
            HttpServletRequest request = Mockito.mock(HttpServletRequest.class);
            String token = jwtActionUtil.generateToken(Constants.EMAIL_1);

            //when
            when(paramFromHttpRequestUtil
                    .parseRandomParameterAndToken(request)).thenReturn(token);
            when(jwtStrategy.getStrategy(JwtType.ACTION)).thenReturn(jwtActionUtil);
            when(userRepository.findByEmail(Constants.EMAIL_1)).thenReturn(Optional.of(user));

            //then
            assertEquals(new RegistrationConfirmationResponse(REGISTRATION_CONFIRMED),
                    authenticationService.confirmRegistration(request));

            //verify
            verify(paramFromHttpRequestUtil, times(1)).parseRandomParameterAndToken(request);
            verify(jwtStrategy, times(1)).getStrategy(JwtType.ACTION);
            verify(userRepository, times(1)).findByEmail(Constants.EMAIL_1);
        }
    }
}
