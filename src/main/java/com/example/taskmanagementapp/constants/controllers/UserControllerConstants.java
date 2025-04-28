package com.example.taskmanagementapp.constants.controllers;

public class UserControllerConstants {
    public static final String USER_API_NAME = "User API";
    public static final String USER_API_DESCRIPTION = """
            Here you'll find a comprehensive overview
            of how to assign roles to users in this app
            having SUPERVISOR role, how to get and update
            your profile info
            """;
    public static final String USERS = "/users";
    public static final String GET_PROFILE_INFO = "/me";
    public static final String UPDATE_PROFILE_INFO = "/me";
    public static final String UPDATE_USER_ROLE = "/{employeeId}/role";
    public static final String CHANGE_EMAIL_SUCCESS = "/change-email-success";

    public static final String GET_PROFILE_INFO_SUMMARY = "Retrieve profile info";
    public static final String SUCCESSFULLY_RETRIEVED = "Successfully retrieved profile info";

    public static final String UPDATE_PROFILE_INFO_SUMMARY = "Update profile info";
    public static final String SUCCESSFULLY_UPDATED_PROFILE_INFO =
            "Successfully updated profile info";

    public static final String UPDATE_USER_ROLE_SUMMARY = "Update user role";
    public static final String SUCCESSFULLY_UPDATED_ROLE = "Successfully updated user role";

    public static final String CHANGE_EMAIL_CONFIRMATION = "Confirm email change";
    public static final String SUCCESSFULLY_CHANGED_EMAIL = "Successfully changed email";

    public static final String PAGEABLE_EXAMPLE = """
            {"page": 0,
            "size": 5,
             "sort": "email,DESC"}""";
}
