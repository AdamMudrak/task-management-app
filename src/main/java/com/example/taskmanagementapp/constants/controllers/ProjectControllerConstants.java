package com.example.taskmanagementapp.constants.controllers;

public class ProjectControllerConstants {
    public static final String PROJECT_API_NAME = "Project API";
    public static final String PROJECT_API_DESCRIPTION = """
            Here you'll find a comprehensive overview
            of how to create, read, update, delete, projects,
            and also how to read deleted projects and
            assign employees to or remove from projects
            """;
    public static final String PROJECTS = "/projects";
    public static final String DELETED = "/deleted";
    public static final String PROJECT_ID = "/{projectId}";
    public static final String ASSIGN_EMPLOYEE = "assign-employee/{projectId}/{employeeId}";
    public static final String REMOVE_EMPLOYEE = "remove-employee/{projectId}/{employeeId}";
}
