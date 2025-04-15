package com.example.taskmanagementapp.constants;

import java.util.regex.Pattern;

public class ValidationConstants {

    public static final String INVALID_EMAIL = ": invalid email";
    public static final String PATTERN_OF_EMAIL = "^(?=.{1,64}@)[A-Za-z0-9_-]+(\\.[A-Za-z0-9_-]+)*"
            + "@[^-][A-Za-z0-9-]+(\\.[A-Za-z0-9-]+)*(\\.[A-Za-z]{2,})$";

    public static final String PASSWORD_COLLISION =
            "currentPassword collides with newPassword. Try again.";
    public static final String PASSWORD_MISMATCH =
            "password and repeatPassword don't match. Try again.";
    public static final String NEW_PASSWORD_MISMATCH =
            "newPassword and repeatPassword don't match. Try again.";

    public static final String INVALID_PASSWORD =
            " should contain 1 lowercase letter, 1 uppercase letter, 1 digit, 1 special character.";
    public static final String ESCAPED_SPECIAL_CHARS =
            Pattern.quote("^$*{}[]()|~`!@#%&-_=+;:'\"<>,./?");
    public static final String PASSWORD_PATTERN = "(?=^.*[A-Z])(?=^.*[a-z])(?=^.*\\d)"
            + "(?=^.*[" + ESCAPED_SPECIAL_CHARS + "]).{8,32}$";
}
