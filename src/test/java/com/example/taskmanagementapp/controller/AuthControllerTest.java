package com.example.taskmanagementapp.controller;

import static com.example.taskmanagementapp.constant.security.SecurityConstants.CHECK_YOUR_EMAIL;
import static com.example.taskmanagementapp.constant.security.SecurityConstants.RANDOM_LINK_STRENGTH;
import static com.example.taskmanagementapp.constant.security.SecurityConstants.REGISTERED;
import static com.example.taskmanagementapp.constant.security.SecurityConstants.REGISTERED_BUT_NOT_ACTIVATED;
import static com.example.taskmanagementapp.constant.security.SecurityConstants.REGISTRATION_CONFIRMED;
import static com.example.taskmanagementapp.constant.security.SecurityConstants.SEND_LINK_TO_RESET_PASSWORD;
import static com.example.taskmanagementapp.constant.security.SecurityConstants.STRENGTH;
import static com.example.taskmanagementapp.constant.validation.ValidationConstants.NEW_PASSWORD_MISMATCH;
import static com.example.taskmanagementapp.constant.validation.ValidationConstants.PASSWORD_COLLISION;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.example.taskmanagementapp.dto.authentication.request.LoginRequest;
import com.example.taskmanagementapp.dto.authentication.request.PasswordChangeRequest;
import com.example.taskmanagementapp.dto.authentication.request.PasswordResetLinkRequest;
import com.example.taskmanagementapp.dto.authentication.request.RegistrationRequest;
import com.example.taskmanagementapp.entity.ActionToken;
import com.example.taskmanagementapp.entity.Role;
import com.example.taskmanagementapp.entity.User;
import com.example.taskmanagementapp.exception.EntityNotFoundException;
import com.example.taskmanagementapp.repository.ActionTokenRepository;
import com.example.taskmanagementapp.repository.RoleRepository;
import com.example.taskmanagementapp.repository.UserRepository;
import com.example.taskmanagementapp.security.jwtutil.strategy.JwtStrategy;
import com.example.taskmanagementapp.security.jwtutil.strategy.JwtType;
import com.example.taskmanagementapp.service.utils.RandomStringUtil;
import com.example.taskmanagementapp.service.utils.TestCaptureService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.Cookie;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@Transactional
public class AuthControllerTest {
    private static final String VALID_USERNAME = "JohnDoe";
    private static final String VALID_EMAIL = "john_doe@mail.com";
    private static final String FIRST_TEST_PASSWORD = "Best_Password1@3$";
    private static final String FIRST_TEST_PASWORD_ENCODED =
            "$2a$10$u4cOSEeePFyJlpvkPdtmhenMuPYhloQfrVS19DZU8/.5jtJNm7piW";


    private static final String DISABLED_USERNAME = "RichardRoe";
    private static final String DISABLED_EMAIL = "richard_roe@mail.com";
    private static final String SECOND_TEST_PASSWORD = "newPassword1@";

    private static final String NOT_ACTIVATED_USERNAME = "JaneDoe";
    private static final String NOT_ACTIVATED_EMAIL = "jane_doe@mail.com";

    private static final String CONFIRM_REGISTER_SUCCESS_USERNAME = "TheBestJohnDoe";
    private static final String CONFIRM_REGISTER_SUCCESS_EMAIL = "bestjohndoe@mail.com";

    private static final String REGISTER_SUCCESS_USERNAME = "TheNewJohnDoe";
    private static final String REGISTER_SUCCESS_EMAIL = "newjohndoe@mail.com";

    private static final String EXISTS_BY_EMAIL_USERNAME = "YetAnotherJohnDoe";
    private static final String EXISTS_BY_USERNAME_EMAIL = "yetanothertestjohndoe@mail.com";

    private static final String INVALID_USERNAME = "username@likemail.com";
    private static final String INVALID_EMAIL = "invalidmail.com";
    private static final String INVALID_PASSWORD = "password";
    private static final String ANOTHER_INVALID_PASSWORD = "new_password";
    private static final String EMPTY = "";

    private static final String FIRST_NAME = "John";
    private static final String LAST_NAME = "Doe";

    private static final String MOCK_EMAIL = "mock@mail.com";
    private static final String MOCK_USERNAME = "mockUsername";
    private static final String MOCK_PASSWORD = "Mock_Password1";

    private static final List<String> EXPECTED_ERRORS_ON_REGISTER = List.of(
            "firstName must not be blank.",
            "lastName must not be blank.",
            "password and repeatPassword don't match. Try again.",
            "password  should contain 1 lowercase letter, 1 uppercase letter, 1 digit, "
                    + "1 special character and be from 8 to 32 characters long.",
            "repeatPassword  should contain 1 lowercase letter, 1 uppercase letter, 1 digit, "
                    + "1 special character and be from 8 to 32 characters long.",
            "email : invalid email. Try again.",
            "username : invalid username. Can't be like email.");

    private static final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder(STRENGTH);
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private RoleRepository roleRepository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private ActionTokenRepository actionTokenRepository;
    @Autowired
    private JwtStrategy jwtStrategy;
    private User user;
    private User disabledUser;
    private User notActivatedUser;

    @BeforeAll
    void setUpBeforeAll(@Autowired WebApplicationContext webApplicationContext) {
        mockMvc = MockMvcBuilders
                .webAppContextSetup(webApplicationContext)
                .apply(SecurityMockMvcConfigurers.springSecurity())
                .build();
        Role savedRole = roleRepository.save(
                Role.builder().name(Role.RoleName.ROLE_USER).build());

        user = userRepository.save(
                User.builder()
                        .username(VALID_USERNAME)
                        .password(FIRST_TEST_PASWORD_ENCODED)
                        .email(VALID_EMAIL)
                        .firstName(FIRST_NAME)
                        .lastName(LAST_NAME)
                        .role(savedRole)
                        .isEnabled(true)
                        .isAccountNonLocked(true)
                        .build());

        disabledUser = userRepository.save(
                User.builder()
                        .username(DISABLED_USERNAME)
                        .password(FIRST_TEST_PASWORD_ENCODED)
                        .email(DISABLED_EMAIL)
                        .firstName(FIRST_NAME)
                        .lastName(LAST_NAME)
                        .role(savedRole)
                        .isEnabled(false)
                        .isAccountNonLocked(false)
                        .build());

        notActivatedUser = userRepository.save(
                User.builder()
                        .username(NOT_ACTIVATED_USERNAME)
                        .password(FIRST_TEST_PASWORD_ENCODED)
                        .email(NOT_ACTIVATED_EMAIL)
                        .firstName(FIRST_NAME)
                        .lastName(LAST_NAME)
                        .role(savedRole)
                        .isEnabled(false)
                        .isAccountNonLocked(true)
                        .build());
    }

    @AfterAll
    void tearDownAfterAll() {
        actionTokenRepository.deleteAll();
        userRepository.deleteAll();
        roleRepository.deleteAll();
    }

    @Nested
    class RegisterUser {
        @Test
        void givenInvalidRegistrationRequest_whenRegister_ThenThrowGroupOfExceptions()
                throws Exception {
            String jsonRequest = objectMapper.writeValueAsString(
                    new RegistrationRequest(
                            INVALID_USERNAME,
                            INVALID_PASSWORD,
                            ANOTHER_INVALID_PASSWORD,
                            INVALID_EMAIL,
                            EMPTY,
                            EMPTY));

            MvcResult result = mockMvc
                    .perform(MockMvcRequestBuilders.post("/auth/register")
                            .content(jsonRequest)
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(MockMvcResultMatchers.status().isBadRequest())
                    .andReturn();
            JsonNode jsonNode = objectMapper.readTree(result.getResponse().getContentAsString());
            assertEquals(EXPECTED_ERRORS_ON_REGISTER.size(),
                    jsonNode.get("errors").size());
            for (JsonNode node : jsonNode.get("errors")) {
                assertTrue(EXPECTED_ERRORS_ON_REGISTER
                        .contains(node.asText()));
            }
        }

        @Test
        void givenNewRegistrationRequest_whenRegister_thenReturnSuccess() throws Exception {
            successfulRegistration(new RegistrationRequest(
                    REGISTER_SUCCESS_USERNAME,
                    FIRST_TEST_PASSWORD,
                    FIRST_TEST_PASSWORD,
                    REGISTER_SUCCESS_EMAIL,
                    FIRST_NAME,
                    LAST_NAME));
        }

        @Test
        void givenRegistrationRequestWithExistingUsername_whenRegister_thenConflict()
                throws Exception {
            RegistrationRequest registrationRequest = new RegistrationRequest(
                    VALID_USERNAME,
                    FIRST_TEST_PASSWORD,
                    FIRST_TEST_PASSWORD,
                    EXISTS_BY_USERNAME_EMAIL,
                    FIRST_NAME,
                    LAST_NAME);
            String jsonRequest = objectMapper.writeValueAsString(registrationRequest);

            MvcResult result = mockMvc
                    .perform(MockMvcRequestBuilders.post("/auth/register")
                            .content(jsonRequest)
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(MockMvcResultMatchers.status().isConflict())
                    .andReturn();
            JsonNode jsonNode = objectMapper.readTree(result.getResponse().getContentAsString());

            assertEquals("CONFLICT", jsonNode.get("status").asText());
            assertEquals("User with username "
                            + registrationRequest.username() + " already exists.",
                    jsonNode.get("errors").asText());
        }

        @Test
        void givenRegistrationRequestWithExistingEmail_whenRegister_thenConflict()
                throws Exception {
            RegistrationRequest registrationRequest = new RegistrationRequest(
                    EXISTS_BY_EMAIL_USERNAME,
                    FIRST_TEST_PASSWORD,
                    FIRST_TEST_PASSWORD,
                    VALID_EMAIL,
                            FIRST_NAME,
                            LAST_NAME);
            String jsonRequest = objectMapper.writeValueAsString(registrationRequest);

            MvcResult result = mockMvc
                    .perform(MockMvcRequestBuilders.post("/auth/register")
                            .content(jsonRequest)
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(MockMvcResultMatchers.status().isConflict())
                    .andReturn();
            JsonNode jsonNode = objectMapper.readTree(result.getResponse().getContentAsString());

            assertEquals("User with email "
                            + registrationRequest.email() + " already exists.",
                    jsonNode.get("errors").asText());
        }
    }

    @Nested
    class ConfirmRegistration {
        @Test
        void givenSuccessfulRegistrationRequest_whenConfirmRegistration_thenSuccess()
                throws Exception {
            RegistrationRequest registrationRequest = new RegistrationRequest(
                    CONFIRM_REGISTER_SUCCESS_USERNAME,
                    FIRST_TEST_PASSWORD,
                    FIRST_TEST_PASSWORD,
                    CONFIRM_REGISTER_SUCCESS_EMAIL,
                    FIRST_NAME,
                    LAST_NAME);
            successfulRegistration(registrationRequest);
            String[] paramTokenPair = TestCaptureService.getLastValue();
            if (paramTokenPair == null) {
                throw new RuntimeException("Registration process proceeded with failures");
            }
            MvcResult result = mockMvc
                    .perform(MockMvcRequestBuilders.get("/auth/register-success")
                            .param(paramTokenPair[0], paramTokenPair[1]))
                    .andExpect(MockMvcResultMatchers.status().isOk())
                    .andReturn();
            JsonNode jsonNode = objectMapper.readTree(result.getResponse().getContentAsString());
            TestCaptureService.clear();
            assertEquals(REGISTRATION_CONFIRMED, jsonNode.get("response").asText());
        }

        @Test
        void givenRegistrationRequestForNotExistingUser_whenConfirmRegistration_thenNotFound()
                throws Exception {
            String token = jwtStrategy.getStrategy(JwtType.ACTION).generateToken(MOCK_EMAIL);
            ActionToken actionToken = new ActionToken();
            actionToken.setActionToken(token);
            actionTokenRepository.save(actionToken);

            MvcResult result = mockMvc
                    .perform(MockMvcRequestBuilders.get("/auth/register-success")
                            .param("token", token))
                    .andExpect(MockMvcResultMatchers.status().isNotFound())
                    .andReturn();
            JsonNode jsonNode = objectMapper.readTree(result.getResponse().getContentAsString());

            assertEquals("User with email " + MOCK_EMAIL + " was not found.",
                    jsonNode.get("errors").asText());
        }

        @Test
        void givenRegistrationRequestWithNotSavedParam_whenConfirmRegistration_thenNotFound()
                throws Exception {
            String randomParam = RandomStringUtil.generateRandomString(RANDOM_LINK_STRENGTH);
            String token = jwtStrategy.getStrategy(JwtType.ACTION).generateToken(MOCK_EMAIL);

            MvcResult result = mockMvc
                    .perform(MockMvcRequestBuilders.get("/auth/register-success")
                            .param(randomParam, token))
                    .andExpect(MockMvcResultMatchers.status().isNotFound())
                    .andReturn();
            JsonNode jsonNode = objectMapper.readTree(result.getResponse().getContentAsString());

            assertEquals("Wasn't able to parse link...Might be expired or forged.",
                    jsonNode.get("errors").asText());
        }

        @Test
        void givenRegistrationRequestWithNoParamAtAll_whenConfirmRegistration_thenNotFound()
                throws Exception {
            MvcResult result = mockMvc
                    .perform(MockMvcRequestBuilders.get("/auth/register-success"))
                    .andExpect(MockMvcResultMatchers.status().isNotFound())
                    .andReturn();
            JsonNode jsonNode = objectMapper.readTree(result.getResponse().getContentAsString());

            assertEquals("Wasn't able to parse link...Might be expired or forged.",
                    jsonNode.get("errors").asText());
        }
    }

    @Nested
    class Login {
        @Test
        void givenExistingUser_whenLoginWithEmail_thenGetResponseWithTokensInCookies()
                throws Exception {
            String jsonRequest = objectMapper.writeValueAsString(
                    new LoginRequest(VALID_EMAIL, FIRST_TEST_PASSWORD));
            List<String> expectedCookies = new ArrayList<>();
            expectedCookies.add("refreshToken");
            expectedCookies.add("accessToken");

            MvcResult result = mockMvc
                    .perform(MockMvcRequestBuilders.post("/auth/login")
                            .content(jsonRequest)
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(MockMvcResultMatchers.status().isOk())
                    .andReturn();

            Cookie[] cookies = result.getResponse().getCookies();
            assertEquals(expectedCookies.size(), cookies.length);
            for (Cookie cookie : cookies) {
                assertTrue(expectedCookies.contains(cookie.getName()));
            }
        }

        @Test
        void givenExistingUser_whenLoginWithUsername_thenGetResponseWithTokensInCookies()
                throws Exception {
            String jsonRequest = objectMapper.writeValueAsString(
                    new LoginRequest(VALID_USERNAME, FIRST_TEST_PASSWORD));
            List<String> expectedCookies = new ArrayList<>();
            expectedCookies.add("refreshToken");
            expectedCookies.add("accessToken");

            MvcResult result = mockMvc
                    .perform(MockMvcRequestBuilders.post("/auth/login")
                            .content(jsonRequest)
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(MockMvcResultMatchers.status().isOk())
                    .andReturn();

            Cookie[] cookies = result.getResponse().getCookies();
            assertEquals(expectedCookies.size(), cookies.length);
            for (Cookie cookie : cookies) {
                assertTrue(expectedCookies.contains(cookie.getName()));
            }
        }

        @Test
        void givenExistingUserWithWrongPassword_whenLoginWithUsername_thenLoginException()
                throws Exception {
            String jsonRequest = objectMapper.writeValueAsString(
                    new LoginRequest(VALID_USERNAME, MOCK_PASSWORD));

            MvcResult result = mockMvc
                    .perform(MockMvcRequestBuilders.post("/auth/login")
                            .content(jsonRequest)
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(MockMvcResultMatchers.status().isForbidden())
                    .andReturn();

            JsonNode jsonNode = objectMapper.readTree(result.getResponse().getContentAsString());
            assertEquals(
                    "Either login or password is invalid.", jsonNode.get("errors").asText());
        }

        @Test
        void givenExistingDisabledUser_whenLoginWithUsername_thenLoginException()
                throws Exception {
            String jsonRequest = objectMapper.writeValueAsString(
                    new LoginRequest(DISABLED_USERNAME, FIRST_TEST_PASSWORD));

            MvcResult result = mockMvc
                    .perform(MockMvcRequestBuilders.post("/auth/login")
                            .content(jsonRequest)
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(MockMvcResultMatchers.status().isForbidden())
                    .andReturn();

            JsonNode jsonNode = objectMapper.readTree(result.getResponse().getContentAsString());
            assertEquals(
                    "Your account is locked. Consider contacting support team.",
                    jsonNode.get("errors").asText());
        }

        @Test
        void givenExistingUserWithWrongPassword_whenLoginWithEmail_thenLoginException()
                throws Exception {
            String jsonRequest = objectMapper.writeValueAsString(
                    new LoginRequest(VALID_EMAIL, MOCK_PASSWORD));

            MvcResult result = mockMvc
                    .perform(MockMvcRequestBuilders.post("/auth/login")
                            .content(jsonRequest)
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(MockMvcResultMatchers.status().isForbidden())
                    .andReturn();

            JsonNode jsonNode = objectMapper.readTree(result.getResponse().getContentAsString());
            assertEquals(
                    "Either login or password is invalid.", jsonNode.get("errors").asText());
        }

        @Test
        void givenExistingUserWithWrongLogin_whenLoginWithEmail_thenLoginException()
                throws Exception {
            String jsonRequest = objectMapper.writeValueAsString(
                    new LoginRequest(MOCK_EMAIL, FIRST_TEST_PASSWORD));

            MvcResult result = mockMvc
                    .perform(MockMvcRequestBuilders.post("/auth/login")
                            .content(jsonRequest)
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(MockMvcResultMatchers.status().isForbidden())
                    .andReturn();

            JsonNode jsonNode = objectMapper.readTree(result.getResponse().getContentAsString());
            assertEquals(
                    "Either login or password is invalid.", jsonNode.get("errors").asText());
        }

        @Test
        void givenExistingUserWithWrongLogin_whenLoginWithUsername_thenLoginException()
                throws Exception {
            String jsonRequest = objectMapper.writeValueAsString(
                    new LoginRequest(MOCK_USERNAME, FIRST_TEST_PASSWORD));

            MvcResult result = mockMvc
                    .perform(MockMvcRequestBuilders.post("/auth/login")
                            .content(jsonRequest)
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(MockMvcResultMatchers.status().isForbidden())
                    .andReturn();

            JsonNode jsonNode = objectMapper.readTree(result.getResponse().getContentAsString());
            assertEquals(
                    "Either login or password is invalid.", jsonNode.get("errors").asText());
        }
    }

    @Nested
    class ChangePassword {
        @WithUserDetails(VALID_USERNAME)
        @Test
        void givenLoggedInUser_whenChangePassword_thenSuccess() throws Exception {
            String jsonRequest = objectMapper.writeValueAsString(new PasswordChangeRequest(
                    FIRST_TEST_PASSWORD,
                    SECOND_TEST_PASSWORD,
                    SECOND_TEST_PASSWORD));

            mockMvc.perform(MockMvcRequestBuilders.post("/auth/change-password")
                            .content(jsonRequest)
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(MockMvcResultMatchers.status().isOk());

            User user = userRepository.findByUsername(VALID_USERNAME)
                    .orElseThrow(() -> new EntityNotFoundException(
                            "No user with username " + VALID_USERNAME));
            assertTrue(encoder.matches(SECOND_TEST_PASSWORD, user.getPassword()));

            //reset this test
            user.setPassword(FIRST_TEST_PASWORD_ENCODED);
            userRepository.save(user);
        }

        @WithUserDetails(VALID_USERNAME)
        @Test
        void givenLoggedInUser_whenChangePassword_repeatPasswordDoesNotMatch_thenFail()
                throws Exception {
            String jsonRequest = objectMapper.writeValueAsString(new PasswordChangeRequest(
                    FIRST_TEST_PASSWORD,
                    SECOND_TEST_PASSWORD,
                    MOCK_PASSWORD));

            MvcResult result = mockMvc.perform(MockMvcRequestBuilders.post("/auth/change-password")
                            .content(jsonRequest)
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(MockMvcResultMatchers.status().isBadRequest())
                    .andReturn();
            JsonNode jsonNode = objectMapper.readTree(result.getResponse().getContentAsString());
            assertEquals(NEW_PASSWORD_MISMATCH, jsonNode.get("errors").get(0).asText());
        }

        @WithUserDetails(VALID_USERNAME)
        @Test
        void givenLoggedInUser_whenChangePassword_currentPasswordDoesNotMatch_thenFail()
                throws Exception {
            String jsonRequest = objectMapper.writeValueAsString(new PasswordChangeRequest(
                    MOCK_PASSWORD,
                    SECOND_TEST_PASSWORD,
                    SECOND_TEST_PASSWORD));

            MvcResult result = mockMvc.perform(MockMvcRequestBuilders.post("/auth/change-password")
                            .content(jsonRequest)
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(MockMvcResultMatchers.status().isConflict())
                    .andReturn();
            JsonNode jsonNode = objectMapper.readTree(result.getResponse().getContentAsString());
            assertEquals("Wrong password. Try resetting "
                    + "password and using a new random password.", jsonNode.get("errors").asText());
        }

        @WithUserDetails(VALID_USERNAME)
        @Test
        void givenLoggedInUser_whenChangePassword_currentPasswordCollidesWithNewPassword_thenFail()
                throws Exception {
            String jsonRequest = objectMapper.writeValueAsString(new PasswordChangeRequest(
                    FIRST_TEST_PASSWORD,
                    FIRST_TEST_PASSWORD,
                    FIRST_TEST_PASSWORD));

            MvcResult result = mockMvc.perform(MockMvcRequestBuilders.post("/auth/change-password")
                            .content(jsonRequest)
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(MockMvcResultMatchers.status().isBadRequest())
                    .andReturn();
            JsonNode jsonNode = objectMapper.readTree(result.getResponse().getContentAsString());
            assertEquals(PASSWORD_COLLISION, jsonNode.get("errors").get(0).asText());
        }
    }

    @Nested
    class InitiatePasswordReset {
        @Test
        void givenNonExistingUsername_whenInitiatePasswordReset_thenFail()
                throws Exception {
            String jsonRequest = objectMapper.writeValueAsString(
                    new PasswordResetLinkRequest(MOCK_USERNAME));

            MvcResult result = mockMvc
                    .perform(MockMvcRequestBuilders.post("/auth/forgot-password")
                            .content(jsonRequest)
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(MockMvcResultMatchers.status().isNotFound())
                    .andReturn();

            JsonNode jsonNode = objectMapper.readTree(result.getResponse().getContentAsString());

            assertEquals("No user with username " + MOCK_USERNAME + " found.",
                    jsonNode.get("errors").asText());
        }

        @Test
        void givenNonExistingEmail_whenInitiatePasswordReset_thenFail()
                throws Exception {
            String jsonRequest = objectMapper.writeValueAsString(
                    new PasswordResetLinkRequest(MOCK_EMAIL));

            MvcResult result = mockMvc
                    .perform(MockMvcRequestBuilders.post("/auth/forgot-password")
                            .content(jsonRequest)
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(MockMvcResultMatchers.status().isNotFound())
                    .andReturn();

            JsonNode jsonNode = objectMapper.readTree(result.getResponse().getContentAsString());

            assertEquals("No user with email " + MOCK_EMAIL + " found.",
                    jsonNode.get("errors").asText());
        }

        @Test
        void givenEmailOfDisabledUser_whenInitiatePasswordReset_thenFail()
                throws Exception {
            String jsonRequest = objectMapper.writeValueAsString(
                    new PasswordResetLinkRequest(disabledUser.getEmail()));

            MvcResult result = mockMvc
                    .perform(MockMvcRequestBuilders.post("/auth/forgot-password")
                            .content(jsonRequest)
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(MockMvcResultMatchers.status().isForbidden())
                    .andReturn();

            JsonNode jsonNode = objectMapper.readTree(result.getResponse().getContentAsString());

            assertEquals("Your account is locked. Consider contacting support team.",
                    jsonNode.get("errors").asText());
        }

        @Test
        void givenUsernameOfDisabledUser_whenInitiatePasswordReset_thenFail()
                throws Exception {
            String jsonRequest = objectMapper.writeValueAsString(
                    new PasswordResetLinkRequest(disabledUser.getUsername()));

            MvcResult result = mockMvc
                    .perform(MockMvcRequestBuilders.post("/auth/forgot-password")
                            .content(jsonRequest)
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(MockMvcResultMatchers.status().isForbidden())
                    .andReturn();

            JsonNode jsonNode = objectMapper.readTree(result.getResponse().getContentAsString());

            assertEquals("Your account is locked. Consider contacting support team.",
                    jsonNode.get("errors").asText());
        }

        @Test
        void givenUsernameOfNotActivatedUser_whenInitiatePasswordReset_thenFailButCanActivateAfter()
                throws Exception {
            String jsonRequest = objectMapper.writeValueAsString(
                    new PasswordResetLinkRequest(notActivatedUser.getUsername()));

            //provoke failure using not activated user
            MvcResult result = mockMvc
                    .perform(MockMvcRequestBuilders.post("/auth/forgot-password")
                            .content(jsonRequest)
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(MockMvcResultMatchers.status().isForbidden())
                    .andReturn();

            JsonNode jsonNode = objectMapper.readTree(result.getResponse().getContentAsString());

            assertEquals(REGISTERED_BUT_NOT_ACTIVATED,
                    jsonNode.get("errors").asText());

            //Check if you can activate after failure
            String[] paramTokenPair = TestCaptureService.getLastValue();
            if (paramTokenPair == null) {
                throw new RuntimeException("Registration process proceeded with failures");
            }
            result = mockMvc
                    .perform(MockMvcRequestBuilders.get("/auth/register-success")
                            .param(paramTokenPair[0], paramTokenPair[1]))
                    .andExpect(MockMvcResultMatchers.status().isOk())
                    .andReturn();
            jsonNode = objectMapper.readTree(result.getResponse().getContentAsString());
            TestCaptureService.clear();
            assertEquals(REGISTRATION_CONFIRMED, jsonNode.get("response").asText());
        }

        @Test
        void givenEmailOfNotActivatedUser_whenInitiatePasswordReset_thenFailButCanActivateAfter()
                throws Exception {
            String jsonRequest = objectMapper.writeValueAsString(
                    new PasswordResetLinkRequest(notActivatedUser.getEmail()));

            //provoke failure using not activated user
            MvcResult result = mockMvc
                    .perform(MockMvcRequestBuilders.post("/auth/forgot-password")
                            .content(jsonRequest)
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(MockMvcResultMatchers.status().isForbidden())
                    .andReturn();

            JsonNode jsonNode = objectMapper.readTree(result.getResponse().getContentAsString());

            assertEquals(REGISTERED_BUT_NOT_ACTIVATED,
                    jsonNode.get("errors").asText());

            //Check if you can activate after failure
            String[] paramTokenPair = TestCaptureService.getLastValue();
            if (paramTokenPair == null) {
                throw new RuntimeException("Registration process proceeded with failures");
            }
            result = mockMvc
                    .perform(MockMvcRequestBuilders.get("/auth/register-success")
                            .param(paramTokenPair[0], paramTokenPair[1]))
                    .andExpect(MockMvcResultMatchers.status().isOk())
                    .andReturn();
            jsonNode = objectMapper.readTree(result.getResponse().getContentAsString());
            TestCaptureService.clear();
            assertEquals(REGISTRATION_CONFIRMED, jsonNode.get("response").asText());
        }
    }

    /**
     * This test class will trigger both - initiatePasswordReset and resetPassword simultaneously
     * as it's the only logical way - these functions should work together exclusively.
     */
    @Nested
    class ResetPasswordFullFlow {

        @Test
        void givenExistingUser_whenInitiatePasswordResetAndResetPassword_thenSuccess()
                throws Exception {
            //initiate password reset
            String jsonRequest = objectMapper.writeValueAsString(
                    new PasswordResetLinkRequest(user.getEmail()));

            MvcResult result = mockMvc
                    .perform(MockMvcRequestBuilders.post("/auth/forgot-password")
                            .content(jsonRequest)
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(MockMvcResultMatchers.status().isOk())
                    .andReturn();

            JsonNode jsonNode = objectMapper.readTree(result.getResponse().getContentAsString());

            assertEquals(SEND_LINK_TO_RESET_PASSWORD, jsonNode.get("response").asText());

            //actually reset the password
            if (TestCaptureService.getLastValue() == null) {
                throw new RuntimeException("Initiate reset process proceeded with failures");
            }
            String[] paramTokenPair = TestCaptureService.getLastValue();
            TestCaptureService.clear();
            result = mockMvc
                    .perform(MockMvcRequestBuilders.get("/auth/reset-password")
                            .param(paramTokenPair[0], paramTokenPair[1]))
                    .andExpect(MockMvcResultMatchers.status().isOk())
                    .andReturn();

            jsonNode = objectMapper.readTree(result.getResponse().getContentAsString());

            assertEquals(CHECK_YOUR_EMAIL, jsonNode.get("response").asText());

            //verify that login with a new password works
            jsonRequest = objectMapper.writeValueAsString(new LoginRequest(VALID_EMAIL,
                    TestCaptureService.getLastValue()[0]));
            TestCaptureService.clear();
            List<String> expectedCookies = new ArrayList<>();
            expectedCookies.add("refreshToken");
            expectedCookies.add("accessToken");

            result = mockMvc
                    .perform(MockMvcRequestBuilders.post("/auth/login")
                            .content(jsonRequest)
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(MockMvcResultMatchers.status().isOk())
                    .andReturn();

            Cookie[] cookies = result.getResponse().getCookies();
            assertEquals(expectedCookies.size(), cookies.length);
            for (Cookie cookie : cookies) {
                assertTrue(expectedCookies.contains(cookie.getName()));
            }
        }

        @Test
        void givenForgedUri_whenResetPassword_thenThrowException() throws Exception {
            MvcResult result = mockMvc
                    .perform(MockMvcRequestBuilders.get("/auth/reset-password")
                            .param("pseudoParam", "pseudoToken"))
                    .andExpect(MockMvcResultMatchers.status().isNotFound())
                    .andReturn();

            JsonNode jsonNode = objectMapper.readTree(result.getResponse().getContentAsString());

            assertEquals("Wasn't able to parse link...Might be expired or forged.",
                    jsonNode.get("errors").asText());
        }

        @Test
        void givenEmptyUri_whenResetPassword_thenThrowException() throws Exception {
            MvcResult result = mockMvc
                    .perform(MockMvcRequestBuilders.get("/auth/reset-password"))
                    .andExpect(MockMvcResultMatchers.status().isNotFound())
                    .andReturn();

            JsonNode jsonNode = objectMapper.readTree(result.getResponse().getContentAsString());

            assertEquals("Wasn't able to parse link...Might be expired or forged.",
                    jsonNode.get("errors").asText());
        }
    }

    private void successfulRegistration(RegistrationRequest registrationRequest) throws Exception {
        String jsonRequest = objectMapper.writeValueAsString(registrationRequest);

        MvcResult result = mockMvc
                .perform(MockMvcRequestBuilders.post("/auth/register")
                        .content(jsonRequest)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isCreated())
                .andReturn();
        JsonNode jsonNode = objectMapper.readTree(result.getResponse().getContentAsString());

        assertEquals(REGISTERED, jsonNode.get("response").asText());
    }
}
