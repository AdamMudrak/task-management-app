package com.example.taskmanagementapp.testutils;

import com.example.taskmanagementapp.entities.ActionToken;
import com.example.taskmanagementapp.entities.Attachment;
import com.example.taskmanagementapp.entities.Label;
import com.example.taskmanagementapp.entities.ParamToken;
import com.example.taskmanagementapp.entities.Project;
import com.example.taskmanagementapp.entities.Role;
import com.example.taskmanagementapp.entities.Task;
import com.example.taskmanagementapp.entities.User;

public class EntityFactory {
    public static ActionToken getActionToken() {
        ActionToken actionToken = new ActionToken();
        actionToken.setActionToken(Constants.ACTION_TOKEN);
        return actionToken;
    }

    public static ParamToken getParamToken() {
        ParamToken paramToken = new ParamToken();
        paramToken.setParameter(Constants.PARAMETER);
        paramToken.setActionToken(Constants.ACTION_TOKEN);
        return paramToken;
    }

    public static Role getUserRole() {
        Role role = new Role();
        role.setName(Role.RoleName.ROLE_USER);
        return role;
    }

    public static Role getAdminRole() {
        Role adminRole = new Role();
        adminRole.setName(Role.RoleName.ROLE_ADMIN);
        return adminRole;
    }

    public static User getUser1(Role role) {
        User user = new User();
        user.setUsername(Constants.USERNAME);
        user.setPassword(Constants.PASSWORD);
        user.setEmail(Constants.EMAIL);
        user.setFirstName(Constants.FIRST_NAME);
        user.setLastName(Constants.LAST_NAME);
        user.setRole(role);
        user.setEnabled(true);
        user.setAccountNonLocked(true);
        return user;
    }

    public static User getUser2(Role role) {
        User user = new User();
        user.setUsername(Constants.ANOTHER_USERNAME);
        user.setPassword(Constants.PASSWORD);
        user.setEmail(Constants.ANOTHER_EMAIL);
        user.setFirstName(Constants.FIRST_NAME);
        user.setLastName(Constants.LAST_NAME);
        user.setRole(role);
        user.setEnabled(true);
        user.setAccountNonLocked(true);
        return user;
    }

    public static Project getProjectWithOneEmployee(User user) {
        Project project = new Project();
        project.setName(Constants.PROJECT_NAME);
        project.setDescription(Constants.PROJECT_DESCRIPTION);
        project.setStartDate(Constants.PROJECT_START_DATE);
        project.setEndDate(Constants.PROJECT_END_DATE);
        project.setStatus(Project.Status.INITIATED);
        project.setDeleted(false);
        project.setOwner(user);
        project.getManagers().add(user);
        project.getEmployees().add(user);
        return project;
    }

    public static Project getProjectWithTwoEmployees(User user1, User user2) {
        Project project = new Project();
        project.setName(Constants.PROJECT_NAME);
        project.setDescription(Constants.PROJECT_DESCRIPTION);
        project.setStartDate(Constants.PROJECT_START_DATE);
        project.setEndDate(Constants.PROJECT_END_DATE);
        project.setStatus(Project.Status.INITIATED);
        project.setDeleted(false);
        project.setOwner(user1);
        project.getManagers().add(user1);
        project.getEmployees().add(user1);
        project.getEmployees().add(user2);
        return project;
    }

    public static Project getDeletedProject(User user) {
        Project deletedProject = new Project();
        deletedProject.setName(Constants.ANOTHER_PROJECT_NAME);
        deletedProject.setDescription(Constants.ANOTHER_PROJECT_DESCRIPTION);
        deletedProject.setStartDate(Constants.PROJECT_START_DATE);
        deletedProject.setEndDate(Constants.PROJECT_END_DATE);
        deletedProject.setStatus(Project.Status.INITIATED);
        deletedProject.setDeleted(true);
        deletedProject.setOwner(user);
        deletedProject.getManagers().add(user);
        deletedProject.getEmployees().add(user);
        return deletedProject;
    }

    public static Task getTask1(Project project, User user) {
        Task task = new Task();
        task.setName(Constants.TASK_NAME);
        task.setDescription(Constants.TASK_DESCRIPTION);
        task.setPriority(Task.Priority.LOW);
        task.setStatus(Task.Status.NOT_STARTED);
        task.setDueDate(Constants.TASK_DUE_DATE);
        task.setProject(project);
        task.setAssignee(user);
        task.setDeleted(false);
        return task;
    }

    public static Task getTask2(Project project, User user) {
        Task task = new Task();
        task.setName(Constants.ANOTHER_TASK_NAME);
        task.setDescription(Constants.ANOTHER_TASK_DESCRIPTION);
        task.setPriority(Task.Priority.HIGH);
        task.setStatus(Task.Status.IN_PROGRESS);
        task.setDueDate(Constants.TASK_DUE_DATE);
        task.setProject(project);
        task.setAssignee(user);
        task.setDeleted(false);
        return task;
    }

    public static Label getLabel1(User user, Task task) {
        Label label = new Label();
        label.setName(Constants.LABEL_NAME);
        label.setUser(user);
        label.setColor(Label.Color.GREEN);
        label.getTasks().add(task);
        return label;
    }

    public static Label getLabel2(User user, Task task) {
        Label label = new Label();
        label.setName(Constants.ANOTHER_LABEL_NAME);
        label.setUser(user);
        label.setColor(Label.Color.RED);
        label.getTasks().add(task);
        return label;
    }

    public static Label getLabelWithNoTask(User user) {
        Label labelWithNotTask = new Label();
        labelWithNotTask.setName(Constants.ANOTHER_LABEL_NAME);
        labelWithNotTask.setUser(user);
        labelWithNotTask.setColor(Label.Color.RED);
        return labelWithNotTask;
    }

    public static Attachment getAttachment1(Task task) {
        Attachment attachment = new Attachment();
        attachment.setFileId(Constants.FILE_ID_1);
        attachment.setFileName(Constants.FILE_NAME_1);
        attachment.setTask(task);
        attachment.setUploadDate(Constants.UPLOADED_DATE);
        return attachment;
    }

    public static Attachment getAttachment2(Task task) {
        Attachment attachment = new Attachment();
        attachment.setFileId(Constants.FILE_ID_2);
        attachment.setFileName(Constants.FILE_NAME_2);
        attachment.setTask(task);
        attachment.setUploadDate(Constants.UPLOADED_DATE);
        return attachment;
    }
}
