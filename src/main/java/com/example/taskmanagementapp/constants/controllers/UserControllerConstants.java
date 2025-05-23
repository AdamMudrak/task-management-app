package com.example.taskmanagementapp.constants.controllers;

public class UserControllerConstants {
    public static final String USER_API_NAME = "User API";
    public static final String USER_API_DESCRIPTION = """
            Here you'll find a comprehensive overview
            of how to assign roles to users in this app
            having ADMIN role, how to get and update
            your profile info.
            """;

    public static final String GET_PROFILE_INFO_SUMMARY = "Retrieve profile info.";
    public static final String SUCCESSFULLY_RETRIEVED = "Successfully retrieved profile info.";

    public static final String UPDATE_PROFILE_INFO_SUMMARY = "Update profile info.";
    public static final String SUCCESSFULLY_UPDATED_PROFILE_INFO =
            "Successfully updated profile info.";

    public static final String UPDATE_USER_ROLE_SUMMARY = "Update user role.";
    public static final String SUCCESSFULLY_UPDATED_ROLE = "Successfully updated user role.";

    public static final String RETRIEVE_ALL_USERS = "Retrieve all users.";
    public static final String SUCCESSFULLY_RETRIEVE_ALL_USERS =
            "Successfully retrieved all users.";

    public static final String CHANGE_USER_ACCOUNT_STATUS = "Enable or disable user account.";
    public static final String SUCCESSFULLY_CHANGED_STATUS =
            "Successfully changed user account status.";

    public static final String PAGEABLE_EXAMPLE = """
            {"page": 0,
            "size": 5,
             "sort": "email,DESC"}""";
}
