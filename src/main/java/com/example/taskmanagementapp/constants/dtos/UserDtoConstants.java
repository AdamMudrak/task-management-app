package com.example.taskmanagementapp.constants.dtos;

public class UserDtoConstants {
    public static final String USERNAME = "username";
    public static final String USERNAME_EXAMPLE = "ExampleUsername";

    public static final String FIRST_NAME = "firstName";
    public static final String FIRST_NAME_EXAMPLE = "John";

    public static final String LAST_NAME = "lastName";
    public static final String LAST_NAME_EXAMPLE = "Doe";

    public static final String EMAIL = "email";
    public static final String EMAIL_EXAMPLE = "example@gmail.com";

    public static final String EMAIL_OR_USERNAME = "email or username";
    public static final String EMAIL_OR_USERNAME_EXAMPLE = "example@gmail.com or ExampleUsername";

    public static final String PASSWORD = "password";
    public static final String PASSWORD_EXAMPLE = "Best_Password1@3$";
    public static final String PASSWORD_DESCRIPTION = """
                    Your password should contain:
                    1) at least one lowercase letter, like 'a';
                    2) at least one uppercase letter, like 'A';
                    3) at least one number, like '0';
                    4) at least one special character, like '?!@#$%^&*~';
                    5) from 8 to 32 characters.""";

    public static final String REPEAT_PASSWORD = "repeatPassword";
    public static final String REPEAT_PASSWORD_DESCRIPTION =
            "This field must be the same as password!";

    public static final String CURRENT_PASSWORD = "currentPassword";

    public static final String NEW_PASSWORD = "newPassword";

    public static final String REPEAT_NEW_PASSWORD = "repeatNewPassword";

    public static final int MIN_PASSWORD_SIZE = 8;
    public static final int MAX_PASSWORD_SIZE = 32;
}
