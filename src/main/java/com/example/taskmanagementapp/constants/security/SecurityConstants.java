package com.example.taskmanagementapp.constants.security;

import java.util.List;

public class SecurityConstants {
    public static final int DIVIDER = 1000;
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
    public static final String RESET_PATH = "${get.random.password.path}";
    public static final String CONFIRMATION_PATH = "${registration.confirmation.path}";
    public static final String CHANGE_EMAIL_CONFIRMATION_PATH = "${change.email.confirmation.path}";
    public static final String ACCEPT_INVITATION_TO_PROJECT_PATH =
            "${accept.invitation.to.project.path}";

    public static final String DROPBOX_REFRESH_TOKEN = "${dropbox.refresh.token}";
    public static final String DROPBOX_KEY = "${dropbox.key}";
    public static final String DROPBOX_SECRET = "${dropbox.secret}";
    public static final String DROPBOX_CLIENT_IDENTIFIER = "dropbox/taskmanagementapp";

    public static final String SUPPORT_EMAIL = "${mail.address}";
    public static final String SEND_GRID_API_KEY = "${sendgrid.api.key}";

    public static final String RESET = "RESET";
    public static final String CONFIRMATION = "CONFIRMATION";
    public static final String ACCESS = "ACCESS";
    public static final String ACCESS_TOKEN = "accessToken";
    public static final String ACTION = "ACTION";
    public static final String REFRESH = "REFRESH";

    public static final String LOGIN_SUCCESS =
            "User has been logged in successfully. Proceed with further actions";

    public static final String CONFIRM_REGISTRATION_SUBJECT =
            "Finish registration in Facio";
    public static final String CONFIRM_REGISTRATION_BODY = """
            Good day! This email is here to help you confirm your registration
            of Facio account. Please, use this link to finish it:""";

    public static final String INITIATE_RANDOM_PASSWORD_SUBJECT =
            "Initiate password reset for Facio";
    public static final String INITIATE_RANDOM_PASSWORD_BODY = """
            Good day! This email is here to help you reset
            password for your Facio account. After confirmation,
            you will receive a follow-up email with a new temporary
            random password. Do not hesitate to change it after to
            something you will remember! Please, use
            this link to confirm your request of password reset:""";

    public static final String RANDOM_PASSWORD_SUBJECT = "New password for Facio";
    public static final String RANDOM_PASSWORD_BODY = "Your new random password:";
    public static final String RANDOM_PASSWORD_BODY_2 =
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
    public static final String SEND_LINK_TO_RESET_PASSWORD =
            "An email with reset link has been sent.";
    public static final String CHECK_YOUR_EMAIL =
            "Password was reset successfully. Check your email.";

    public static final String REFRESH_TOKEN = "refreshToken";

    public static final String CONFIRM_CHANGE_EMAIL_SUBJECT =
            "Confirm email change in Facio";
    public static final String CONFIRM_CHANGE_EMAIL_BODY = """
            Good day! This email is here to help you confirm your
            request of email change for your Facio account. Please,
            use this link to confirm:""";
    public static final String NEW_EMAIL_PARAMETER = "&newEmail";
    public static final String PROJECT_ID_PARAMETER = "&projectId";
    public static final String ASSIGNEE_ID_PARAMETER = "&assigneeId";
    public static final String IS_NEW_MANAGER_PARAMETER = "&isNewManager";
    public static final String NEW_EMAIL = "newEmail";
    public static final String PROJECT_ID = "projectId";
    public static final String ASSIGNEE_ID = "assigneeId";
    public static final String IS_NEW_MANAGER = "isNewManager";
    public static final List<String> SKIPPED_PARAMS =
            List.of("newEmail", "projectId", "assigneeId", "isNewManager");

    public static final String CONFIRM_NEW_EMAIL_MESSAGE =
            "In case of email change, please click the link sent to your new email to confirm.";

    public static final String ACCEPT_PROJECT_INVITATION_SUBJECT =
            "Accept invitation to a project in Facio";
    public static final String ACCEPT_PROJECT_INVITATION_BODY_1 = """
            Good day! This email was sent to you by""";
    public static final String ACCEPT_PROJECT_INVITATION_BODY_2 = """
             to invite to a project called""";
    public static final String ACCEPT_PROJECT_INVITATION_BODY_3 = """
            . Please,use this link to confirm:""";
}
