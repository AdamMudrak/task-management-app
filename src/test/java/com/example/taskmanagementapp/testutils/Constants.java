package com.example.taskmanagementapp.testutils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public class Constants {
    public static final String USERNAME_1 = "JohnDoe";
    public static final String PASSWORD_1 = "Best_Password1@3$";
    public static final String PASSWORD_1_DB =
            "$2a$10$u4cOSEeePFyJlpvkPdtmhenMuPYhloQfrVS19DZU8/.5jtJNm7piW";
    public static final String EMAIL_1 = "john_doe@mail.com";

    public static final String USERNAME_2 = "RichardRoe";
    public static final String PASSWORD_2 = "newPassword1@";
    public static final String EMAIL_2 = "richard_roe@mail.com";

    public static final String USERNAME_3 = "JaneDoe";
    public static final String PASSWORD_3 = "newPassword2@";
    public static final String EMAIL_3 = "jane_doe@mail.com";

    public static final String USERNAME_4 = "RickyRoe";
    public static final String EMAIL_4 = "ricky_roe@mail.com";

    public static final String USERNAME_5 = "TheBestJohnDoe";
    public static final String EMAIL_5 = "bestjohndoe@mail.com";

    public static final String USERNAME_6 = "TheNewJohnDoe";
    public static final String EMAIL_6 = "newjohndoe@mail.com";

    public static final String USERNAME_7 = "YetAnotherJohnDoe";
    public static final String EMAIL_7 = "yetanothertestjohndoe@mail.com";

    public static final String INVALID_USERNAME = "username@likemail.com";
    public static final String INVALID_EMAIL = "invalidmail.com";
    public static final String INVALID_PASSWORD = "password";
    public static final String ANOTHER_INVALID_PASSWORD = "new_password";
    public static final String EMPTY = "";

    public static final String ROLE_USER = "ROLE_USER";

    public static final String FIRST_NAME = "John";
    public static final String LAST_NAME = "Doe";

    public static final String ACTION_TOKEN = "actionToken";
    public static final String NOT_EXISTING_ACTION_TOKEN = "blaBlaBlaActionToken";

    public static final String PROJECT_NAME = "projectName";
    public static final String PROJECT_DESCRIPTION = "projectDescription";
    public static final String ANOTHER_PROJECT_NAME = "anotherProjectName";
    public static final String ANOTHER_PROJECT_DESCRIPTION = "anotherProjectDescription";
    public static final LocalDate PROJECT_START_DATE = LocalDate.of(2025, 1, 1);
    public static final LocalDate PROJECT_END_DATE = LocalDate.of(2025, 12, 31);

    public static final String TASK_NAME_1 = "taskName";
    public static final String TASK_NAME_2 = "anotherTaskName";
    public static final String TASK_DESCRIPTION_1 = "taskDescription";
    public static final String TASK_DESCRIPTION_2 = "anotherTaskDescription";
    public static final LocalDate TASK_DUE_DATE = LocalDate.of(2025, 12, 31);

    public static final String LABEL_NAME_1 = "labelName";
    public static final String LABEL_NAME_2 = "anotherLabelName";

    public static final String FILE_ID_1 = "fileId1";
    public static final String FILE_NAME_1 = "fileName1";
    public static final String FILE_ID_2 = "fileId2";
    public static final String FILE_NAME_2 = "fileName2";
    public static final LocalDateTime UPLOADED_DATE = LocalDateTime.of(2025, 1, 6, 0, 0);

    public static final LocalDateTime TIME_STAMP = LocalDateTime.of(2025, 1, 6, 8, 30);
    public static final String COMMENT_TEXT_1 = "commentText1";
    public static final String COMMENT_TEXT_2 = "commentText2";

    public static final List<String> EXPECTED_ERRORS_ON_REGISTER = List.of(
            "firstName must not be blank.",
            "lastName must not be blank.",
            "password and repeatPassword don't match. Try again.",
            "password  should contain 1 lowercase letter, 1 uppercase letter, 1 digit, "
                    + "1 special character and be from 8 to 32 characters long.",
            "repeatPassword  should contain 1 lowercase letter, 1 uppercase letter, 1 digit, "
                    + "1 special character and be from 8 to 32 characters long.",
            "email : invalid email. Try again.",
            "username : invalid username. Can't be like email.");

    public static final long ULTRA_SHORT_EXPIRATION = 1L;
    public static final long ACTION_EXPIRATION = 60000L;
    public static final long ACCESS_EXPIRATION = 900000L;
    public static final long REFRESH_EXPIRATION = 604800000L;
    public static final String SECRET_KEY =
            "eZTQb1Um2KE0dukTWfyHZSq2R3R1SFyqfRFfiReAPn1NHMKUBiTDKc5tajfn";

}
