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

    public static final String CREATE_PROJECT =
            "Create a new project. Having MANAGER or SUPERVISOR role required";
    public static final String SUCCESSFULLY_CREATED_PROJECT = "Successfully created project";

    public static final String GET_ALL_PROJECTS =
            "Get all projects. User with role EMPLOYEE projects only if they are assignees, "
                    + "MANAGER gets projects if they are owners, SUPERVISOR sees everything";
    public static final String SUCCESSFULLY_GET_ALL_PROJECTS =
            "Successfully retrieved all projects";

    public static final String GET_ALL_DELETED_PROJECTS =
            "Get all deleted projects. Only for SUPERVISOR";
    public static final String SUCCESSFULLY_GET_ALL_DELETED_PROJECTS =
            "Successfully retrieved all deleted projects";

    public static final String GET_PROJECT_BY_ID =
            "Get project by id. EMPLOYEE gets to see the project if they are assignees, "
                    + "MANAGER sees projects if they are owners, SUPERVISOR sees everything";
    public static final String SUCCESSFULLY_GET_PROJECT_BY_ID =
            "Successfully retrieved project by id";

    public static final String UPDATE_PROJECT_BY_ID =
            "Update project by id. MANAGER can update only projects they own, "
                    + "SUPERVISOR can update everything";
    public static final String SUCCESSFULLY_UPDATED_PROJECT_BY_ID =
            "Successfully updated project by id";

    public static final String DELETE_PROJECT_BY_ID =
            "Delete project by id. MANAGER can delete only projects they own, "
                    + "SUPERVISOR can delete everything";
    public static final String SUCCESSFULLY_DELETED_PROJECT_BY_ID =
            "Successfully deleted project by id";

    public static final String ADD_EMPLOYEE_TO_PROJECT = "Add employee to project. MANAGER can"
            + " add employees only to projects they own, SUPERVISOR can add everybody everywhere";
    public static final String SUCCESSFULLY_ADDED_EMPLOYEE_TO_PROJECT =
            "Successfully added employee to project";

    public static final String DELETE_EMPLOYEE_FROM_PROJECT = "Delete employee from project. "
            + "MANAGER can add employees only to projects they own, "
            + "SUPERVISOR can add everybody everywhere";
    public static final String SUCCESSFULLY_DELETED_EMPLOYEE_FROM_PROJECT =
            "Successfully deleted employee from project";
    public static final String PAGEABLE_EXAMPLE = """
            {"page": 0,
            "size": 5,
             "sort": "name,ASC"}""";
}
