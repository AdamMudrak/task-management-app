package com.example.taskmanagementapp.constants.controllers;

public class AttachmentControllerConstants {
    public static final String ATTACHMENTS_API_NAME = "Attachments API";
    public static final String ATTACHMENTS_API_DESCRIPTION = """
            Here you'll find a comprehensive overview
            of how to add, get and delete attachments.
            """;

    public static final String ATTACHMENTS = "/attachments";
    public static final String TASK_ID = "/{taskId}";
    public static final String TASK_ID_ATTACHMENT_ID = "/{taskId}/{attachmentId}";

    public static final String UPLOAD_ATTACHMENT_SUMMARY = "Upload attachments. Important condition"
            + ": you are participant of the project of the task you are adding attachments to.";
    public static final String SUCCESSFULLY_UPLOADED_ATTACHMENTS =
            "Successfully uploaded attachments.";

    public static final String GET_ATTACHMENT_SUMMARY = "Get attachments. Important condition:"
            + " you are participant of the project of the task you getting attachments from.";
    public static final String SUCCESSFULLY_GOT_ATTACHMENTS =
            "Successfully got attachments.";

    public static final String DELETE_ATTACHMENT_SUMMARY = "Delete attachments. Important condition"
            + ": you are participant of the project of the task you deleting attachments from.";
    public static final String SUCCESSFULLY_DELETED_ATTACHMENTS =
            "Successfully deleted attachments.";
}
