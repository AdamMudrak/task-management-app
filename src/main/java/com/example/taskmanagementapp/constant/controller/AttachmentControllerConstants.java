package com.example.taskmanagementapp.constant.controller;

public class AttachmentControllerConstants {
    public static final String ATTACHMENTS_API_NAME = "Attachments API";
    public static final String ATTACHMENTS_API_DESCRIPTION = """
            Here you'll find a comprehensive overview
            of how to add, get and delete attachments.
            """;

    public static final String UPLOAD_ATTACHMENT_SUMMARY = "Upload attachments. Important condition"
            + ": the task you are adding attachments to is from a project you are participant of.";
    public static final String SUCCESSFULLY_UPLOADED_ATTACHMENTS =
            "Successfully uploaded attachments.";

    public static final String GET_ATTACHMENT_SUMMARY = "Get attachments. Important condition:"
            + " the task you are getting attachments for is from a project you are participant of.";
    public static final String SUCCESSFULLY_GOT_ATTACHMENTS =
            "Successfully got attachments.";

    public static final String DELETE_ATTACHMENT_SUMMARY = "Delete attachments. Important condition"
            + ": the task you are deleting attachments"
            + " from is from a project you are participant of.";
    public static final String SUCCESSFULLY_DELETED_ATTACHMENTS =
            "Successfully deleted attachments.";
}
