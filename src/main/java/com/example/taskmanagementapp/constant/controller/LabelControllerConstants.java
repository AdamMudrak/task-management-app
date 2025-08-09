package com.example.taskmanagementapp.constant.controller;

public class LabelControllerConstants {
    public static final String LABELS_API_NAME = "Labels API";
    public static final String LABELS_API_DESCRIPTION = """
            Here you'll find a comprehensive overview
            of how to create, update, get and delete labels.
            It is also possible to attach/detach label to/from a task
            on condition you are this task assignee.
            """;

    public static final String CREATE_LABEL_SUMMARY = "Create labels for tasks.";
    public static final String SUCCESSFULLY_CREATED_LABEL =
            "Successfully created label.";

    public static final String UPDATE_LABEL_SUMMARY = "Update your label.";
    public static final String SUCCESSFULLY_UPDATED_LABEL =
            "Successfully updated label.";

    public static final String GET_LABEL_BY_ID_SUMMARY = "Get your label by id.";
    public static final String SUCCESSFULLY_GOT_LABEL =
            "Successfully retrieved label.";

    public static final String GET_LABELS_SUMMARY = "Get your labels.";
    public static final String SUCCESSFULLY_GOT_LABELS =
            "Successfully retrieved labels.";

    public static final String DELETE_LABEL_BY_ID_SUMMARY = "Delete your label by id.";
    public static final String SUCCESSFULLY_DELETED_LABEL =
            "Successfully deleted label.";

    public static final String ATTACH_LABEL_TO_TASK = "Attach your label to your task.";
    public static final String SUCCESSFULLY_ATTACHED_LABEL =
            "Successfully attached label.";

    public static final String DETACH_LABEL_TO_TASK = "Detach your label from your task.";
    public static final String SUCCESSFULLY_DETACHED_LABEL =
            "Successfully detached label.";

    public static final String PAGEABLE_EXAMPLE = """
            {"page": 0,
            "size": 5,
             "sort": "color,ASC"}""";
}
