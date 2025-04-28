package com.example.taskmanagementapp.constants.config;

public class ConfigConstants {
    public static final String COMPONENT_MODEL = "spring";

    public static final String ALLOWED_ORIGINS = "${origins.allowed}";
    public static final String ALLOWED_METHODS = "*";
    public static final String ALLOWED_HEADERS = "*";

    public static final String AUTH_MATCHER = "/auth/**";
    public static final String USERS_CONFIRM_EMAIL_CHANGE_MATCHER = "/users/change-email-success";
    public static final String SWAGGER_MATCHER = "/swagger-ui/**";
    public static final String SWAGGER_DOCS_MATCHER = "/v3/api-docs/**";
    public static final String ERRORS_MATCHER = "/errors";
}
