package com.example.taskmanagementapp.constants.controllers;

public class CommentControllerConstants {
    public static final String COMMENTS_API_NAME = "Comments API";
    public static final String COMMENTS_API_DESCRIPTION = """
            Here you'll find a comprehensive overview
            of how to add, get, update and delete comments.
            """;

    public static final String COMMENTS = "/comments";
    public static final String COMMENT_ID = "/{commentId}";
    public static final String TASK_ID = "/{taskId}";

    public static final String ADD_COMMENT_SUMMARY =
            "Add comment to a task if you are participant of the project the task belongs to.";
    public static final String SUCCESSFULLY_ADDED_COMMENT =
            "Successfully added comment to the task.";

    public static final String UPDATE_COMMENT_SUMMARY =
            "Update your comment.";
    public static final String SUCCESSFULLY_UPDATED_COMMENT =
            "Successfully updated comment.";

    public static final String GET_COMMENTS_SUMMARY =
            "Retrieve all comments from the task if you are participant of "
                    + "the project the task belongs to.";
    public static final String SUCCESSFULLY_GOT_COMMENTS =
            "Successfully retrieved comments.";

    public static final String DELETE_COMMENT_SUMMARY =
            "Delete your comment.";
    public static final String SUCCESSFULLY_DELETED_COMMENT =
            "Successfully deleted comment.";
}
