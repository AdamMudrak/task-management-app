package com.example.taskmanagementapp.constants.security;

public class SecurityConstants {
    public static final String SPLITERATOR = "&";
    public static final int STRENGTH = 10;
    public static final int RANDOM_PASSWORD_STRENGTH = 28;
    public static final int RANDOM_LINK_STRENGTH = 128;

    public static final String RANDOM_STRING_BASE =
            "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
    public static final String RANDOM_PASSWORD_REQUIRED_CHARS =
            "Aa1!";

    public static final String JWT_ACCESS_EXPIRATION = "${jwt.access.expiration}";
    public static final String JWT_ACTION_EXPIRATION = "${jwt.action.expiration}";
    public static final String JWT_REFRESH_EXPIRATION = "${jwt.refresh.expiration}";
    public static final String JWT_SECRET = "${jwt.secret}";
    public static final String SERVER_PATH = "${server.path}";
    public static final String RESET_PATH = "${reset.path}";
    public static final String CONFIRMATION_PATH = "${confirmation.path}";

    public static final int BEGIN_INDEX = 7;

    public static final String RESET = "RESET";
    public static final String CONFIRMATION = "CONFIRMATION";
    public static final String ACCESS = "ACCESS";
    public static final String ACTION = "ACTION";
    public static final String REFRESH = "REFRESH";

    public static final String CONFIRM_REGISTRATION_SUBJECT =
            "Finish registration in Moneta";
    public static final String CONFIRM_REGISTRATION_BODY = """
            Good day! This email is here to help you confirm your registration
            of Moneta account. Please, use this link to finish it:""";

    public static final String INITIATE_RANDOM_PASSWORD_SUBJECT =
            "Initiate password reset for Moneta";
    public static final String INITIATE_RANDOM_PASSWORD_BODY = """
            Good day! This email is here to help you reset
            password for your Moneta account. After confirmation,
            you will receive a follow-up email with a new temporary
            random password. Do not hesitate to change it after to
            something you will remember! Please, use
            this link to confirm your request of password reset:""";

    public static final String RANDOM_PASSWORD_SUBJECT = "New password for Moneta";
    public static final String RANDOM_PASSWORD_BODY = "Your new random password:";
    public static final String RANDOM_PASSWORD_BODY_2 =
            "Click or tap the link and log in using new password:";
    public static final String RANDOM_PASSWORD_BODY_3 =
            "Feel free to change password to something you will remember!";

    public static final String PASSWORD_SET_SUCCESSFULLY =
            "New password has been set successfully.";
    public static final String REGISTERED_BUT_NOT_ACTIVATED =
            "User is registered but not enabled. "
                    + "Check your email to confirm registration. "
                    + "Your account will not be available until then.";
    public static final String REGISTERED =
            "User is registered successfully. "
                    + "Check your email to confirm registration. "
                    + "Your account will not be enabled until then.";
    public static final String REGISTRATION_CONFIRMED =
            "Registration has been confirmed successfully.";
    public static final String PASSWORD_RESET_SUCCESSFULLY =
            "An email with reset link has been sent.";

    public static final String REFRESH_TOKEN = "refreshToken";

    public static final String BEARER = "Bearer ";
}
