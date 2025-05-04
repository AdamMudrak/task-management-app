package com.example.taskmanagementapp.constants.controllers;

public class TaskControllerConstants {
    public static final String TASKS_API_NAME = "Tasks API";
    public static final String TASKS_API_DESCRIPTION = """
            Here you'll find a comprehensive overview
            of how to create, read, update, delete tasks.
            """;
    public static final String TASKS = "/tasks";
    public static final String GET_ALL_PROJECT_TASKS = "/all-project-tasks/{projectId}";
    public static final String TASK_BY_ID = "/{taskId}";

    public static final String CREATE_TASK =
            "Create task. For this, you should be owner or manager of project.";
    public static final String SUCCESSFULLY_CREATED_TASK = "Successfully created task";

    public static final String GET_TASKS_BY_PROJECT_ID =
            "Retrieve tasks for project.";
    public static final String SUCCESSFULLY_GET_TASKS_BY_PROJECT_ID =
            "Successfully retrieve tasks for project";

    public static final String GET_TASK_BY_ID =
            "Retrieve task by id. Only possible for projects you participate in.";
    public static final String SUCCESSFULLY_GET_TASK_BY_ID =
            "Successfully retrieve task by id";

    public static final String UPDATE_TASK_BY_ID =
            "Update task by id. Only owners and managers of project can update tasks";
    public static final String SUCCESSFULLY_UPDATED_TASK_BY_ID =
            "Successfully updated task by id";

    public static final String DELETE_TASK_BY_ID =
            "Delete task by id. Only owners and managers of project can delete tasks";
    public static final String SUCCESSFULLY_DELETED_TASK_BY_ID =
            "Successfully deleted task by id";
    public static final String PAGEABLE_EXAMPLE = """
            {"page": 0,
            "size": 5,
             "sort": "name,ASC"}""";
}
