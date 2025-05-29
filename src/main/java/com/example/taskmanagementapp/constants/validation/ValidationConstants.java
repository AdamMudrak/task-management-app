package com.example.taskmanagementapp.constants.validation;

import java.util.regex.Pattern;

public class ValidationConstants {
    public static final String INVALID_DATE_FORMAT = ": invalid date format. Should be YYYY-MM-dd.";
    public static final String END_DATE_EARLIER_THAN_START_DATE = ": invalid date. "
            + "endDate can't be earlier than startDate";
    public static final Pattern COMPILED_DATE_PATTERN = Pattern.compile("^\\d{4}-\\d{2}-\\d{2}$");

    public static final String INVALID_EMAIL = ": invalid email. Try again.";
    public static final String INVALID_USERNAME = ": invalid username. Can't be like email.";
    public static final Pattern COMPILED_EMAIL_PATTERN = Pattern.compile("^(?=.{1,64}@)[A-Za-z0-9_-"
            + "]+(\\.[A-Za-z0-9_-]+)*@[^-][A-Za-z0-9-]+(\\.[A-Za-z0-9-]+)*(\\.[A-Za-z]{2,})$");

    public static final String PASSWORD_COLLISION =
            "currentPassword collides with newPassword. Try again.";
    public static final String PASSWORD_MISMATCH =
            "password and repeatPassword don't match. Try again.";
    public static final String NEW_PASSWORD_MISMATCH =
            "newPassword and repeatPassword don't match. Try again.";

    public static final String INVALID_PASSWORD = " should contain 1 lowercase letter, 1 uppercase "
            + "letter, 1 digit, 1 special character and be from 8 to 32 characters long";
    public static final Pattern COMPILED_PASSWORD_PATTERN = Pattern.compile("(?=^.*[A-Z])(?=^.*[a-z"
            + "])(?=^.*\\d)(?=^.*[" + Pattern.quote("^$*{}["
            + "]()|~`!@#%&-_=+;:'\"<>,./?") + "]).{8,32}$");
}
