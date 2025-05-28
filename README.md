# Facio TaskManagementApp

## 📌 Introduction
Facio TaskManagementApp is a robust task and project management platform designed for teams.
With secure role-based access, seamless user authentication, and deep integration with Dropbox,
Facio streamlines collaboration through task assignment, commenting, file attachments, labeling,
and real-time notifications — all built to help users stay organized and productive.

## 🚀 Features

## Authentication API Controller
Here you'll find a comprehensive overview of how to register and login in this app. Also, you can
use this API to reset password, get a new random password instead, and set a new password afterward.

- **POST**: `/auth/register` - Register a new user in the app.
- **POST**: `/auth/login` - Log in using existing email account.
- **POST**: `/auth/change-password` - Change password while being logged in.
- **POST**: `/auth/forgot-password` - Initiate password reset via a link sent to your email.
- [AuthenticationController](src/main/java/com/example/taskmanagementapp/controllers/AuthController.java)

## User API Controller
Here you'll find a comprehensive overview of how to assign roles to users in this app having
ADMIN role or to block them, and also how to get and update your profile info.

**Admin functionality:**
- **POST**: `/users/change-user-account-status/{userId}` - Enable or disable user account.
- **PUT**: `/users/{employeeId}/role` - Update user role.
- **GET**: `/users` - Retrieve all users.

**User functionality:**
- **GET**: `/users/me` - Retrieve profile info.
- **PUT**: `/users/me` - Update profile info.
- [UserController](src/main/java/com/example/taskmanagementapp/controllers/UserController.java)

## Project API Controller
Here you'll find a comprehensive overview of how to create, read, update, delete, projects,
and also how to read deleted projects and assign employees to or remove from projects.

- **POST**: `/projects` - Create a new project.
- **GET**: `/projects/{projectId}` - Get project by id if you are employee or owner.
- **GET**: `/projects/deleted` - Get all deleted projects which you created.
- **GET**: `/projects/created` - Get all projects which you created.
- **GET**: `/projects/assigned` - Get all projects in which you are assigned as employee.
- **PUT**: `/projects/{projectId}` - Update project by id. Only for owners and managers.
- **DELETE**: `/projects/{projectId}` - Delete project by id. Only for owners.
- **POST**: `/projects/assign-employee/{projectId}/{employeeId}` - Add employee to project, optionally give them managerial role. Only for owners and managers.
- **POST**: `/projects/remove-employee/{projectId}/{employeeId}` - Delete employee from project. Only for owners and managers.
- [ProjectController](src/main/java/com/example/taskmanagementapp/controllers/ProjectController.java)

## Tasks API Controller
Here you'll find a comprehensive overview of how to create, read, update, delete tasks.

- **POST**: `/tasks` - Create task. For this, you should be owner or manager of project.
- **GET**: `/tasks/{taskId}` - Retrieve task by id. Only possible for projects you participate in.
- **GET**: `/tasks/with-label/{labelId}` - Retrieve tasks by label id.
- **GET**: `/tasks/all-project-tasks/{projectId}` - Retrieve tasks for project.
- **PUT**: `/tasks/{taskId}` - Update task by id. Only owners and managers of project can update tasks.
- **DELETE**: `/tasks/{taskId}` - Delete task by id. Only owners and managers of project can delete tasks.
- [TaskController](src/main/java/com/example/taskmanagementapp/controllers/TaskController.java)

## Labels API
Here you'll find a comprehensive overview of how to create, update, get and delete labels.
It is also possible to attach/detach label to/from a task on condition you are this task assignee.

- **POST**: `/labels` - Create labels for tasks.
- **GET**: `/labels/{labelId}` - Get your label by id.
- **GET**: `/labels` - Get your labels.
- **PUT**: `/labels/{labelId}` - Update your label.
- **PUT**: `/labels/{taskId}/{labelId}/attach` - Attach your label to your task.
- **PUT**: `/labels/{taskId}/{labelId}/detach` - Detach your label from your task.
- **DELETE**: `/labels/{labelId}` - Delete your label by id.
- [LabelController](src/main/java/com/example/taskmanagementapp/controllers/LabelController.java)

## Attachments API
Here you'll find a comprehensive overview of how to add, get and delete attachments. Under the hood,
attachments are saved on dropbox. Each file should not be more than 5MB
and all files attached at once should not be more than 25 MB.

- **POST**: `/attachments/{taskId}` - Upload attachments. Important condition: the task you are adding attachments to is from a project you are participant of.
- **GET**: `/attachments/{taskId}` - Get attachments. Important condition: the task you are getting attachments for is from a project you are participant of.
- **DELETE**: `/attachments/{taskId}/{attachmentId}` - Delete attachments. Important condition: the task you are deleting attachments from is from a project you are participant of.
- [AttachmentController](src/main/java/com/example/taskmanagementapp/controllers/AttachmentController.java)

## Comments API
Here you'll find a comprehensive overview of how to add, get, update and delete comments.

- **POST**: `/comments` - Add comment to a task if you are participant of the project the task belongs to.
- **GET**: `/comments/{taskId}` - Retrieve all comments for the task if you are participant of the project the task belongs to.
- **PUT**: `/comments/{commentId}` - Update your comment.
- **DELETE**: `/comments/{commentId}` - Delete your comment.
- [CommentController](src/main/java/com/example/taskmanagementapp/controllers/CommentController.java)

### ⚠️Nota bene!
#### All requests above, except for:
- **POST**: `/auth/register`
- **POST**: `/auth/login`
- **POST**: `/auth/forgot-password`
#### require user to be **AUTHENTICATED**. The auth tokens will be automatically saved to cookies when you use:
- **POST**: `/auth/login`

## 🛠️ Technologies Used

**Facio TaskManagementApp** is built with a modern, modular, and scalable technology stack, leveraging **Java 21** and the **Spring** ecosystem to support robust functionality, secure operations, and smooth deployment.

### 🧱 Core Language and Environment

- **Java 21** – The main programming language, offering cutting-edge features and enhanced performance.

### ⚙️ Backend Technologies

- **Spring Boot 3.4.4** – Core framework for building, testing, and deploying the application.
- **Spring Security** – Manages application security with JWT-based authentication.
- **Spring Data JPA** – Simplifies database interactions with powerful ORM capabilities.
- **Spring Validation (Hibernate Validator)** – Validates user input and enforces domain rules.
- **Liquibase 4.31.1** – Version-controlled database schema migrations.
- **JWT (JSON Web Token) 0.12.6** – Secure token-based authentication and authorization.
- **Resend Java SDK 4.3.0** – For sending transactional emails like verification and notifications.
- **Dropbox SDK 7.0.0** – File upload and integration with Dropbox cloud storage.
- **MySQL 8.0.33** – A robust and widely used relational database.
- **Log4j 2.24.3** – Provides advanced and customizable logging capabilities.

### 🧑‍💻 Developer Experience and Code Quality

- **MapStruct 1.6.3** – Automates mapping between DTOs and entities, minimizing boilerplate.
- **Lombok 1.18.36** – Reduces verbosity in Java code using annotations.
- **Lombok-MapStruct Binding 0.2.0** – Smooth integration between Lombok and MapStruct.
- **Maven Checkstyle Plugin 3.6.0** – Enforces consistent coding standards and formatting rules.

### 📄 Documentation

- **SpringDoc OpenAPI (Swagger UI) 2.8.6** – Generates interactive and testable API documentation.

### 🧪 Testing and Containers (YET IN DEVELOPMENT!!!)//TODO

- **Spring Boot Starter Test** – Standard utilities for unit and integration testing.
- **Testcontainers (JDBC & MySQL)** – Enables containerized integration tests with ephemeral databases.

---

> ✨ This stack ensures high performance, maintainability, and developer productivity, making Facio robust and production-ready.

## 🔧 Setup and Installation
Not ready for setting up my application locally yet? Then explore [Landing](https://facio-landing.adammudrak.space/) first!<br>There, you will be able to explore Swagger documentation.

1. **Prerequisites:**
   - Software **required**:
     - Git
     - Maven
     - Docker
    ```sh
      #check if everything is installed
      #by checking version of software
      git -v
      mvn -v
      docker -v
    ``` 
    **Open git bash**
   ```sh
      #clone the repository
      git clone https://github.com/AdamMudrak/task-management-app.git
      #change to task-management-app root package
      cd task-management-app/
    ```
   ```sh
      #to change environment variables, you can now use
      nano .env.sample
   ```
   - If you want to use your **own** MySQL, update [application.properties](src/main/resources/application.properties) directly or [.env.sample](.env.sample) with your MySQL credentials.
       - If not, just proceed with the next step as follow-up commands are ready to start MySQL locally in docker container.
    - Having a resend API key **or** adjusting [EmailService](src/main/java/com/example/taskmanagementapp/services/email/EmailService.java) to use Google SMTP **is a must**.
        - Having a [Resend account](https://resend.com) **is a must** if using Google SMTP is unwanted;
          - [Get API key](https://apidog.com/blog/resend-api/#1-sign-up-and-create-an-api-key)
        - Having a spare domain for email address to use Resend **is highly recommended**;
          - [Verify your domain](https://apidog.com/blog/resend-api/#2-verify-your-domain)
        - After successful registration, domain verification and getting API token, in [.env.sample](.env.sample) replace values for:
          - RESEND_API_KEY=your_resend_api_key
          - MAIL=your_domained_email
   - Having a dropbox refresh token, key and secret **is required**. [Tutorial](DropboxTutorial.md). When you get them, replace placeholders with actual token, key and secret in [envrironment variables](.env.sample) for keys:
     - DROPBOX_REFRESH_TOKEN=your_dropbox_refresh_token
     - DROPBOX_KEY=your_dropbox_key
     - DROPBOX_SECRET=your_dropbox_refresh_secret
   - Recommended, but app will start without adjustment:
     - JWT_SECRET - secure your JWTs with something meaningful

2. **Run the application:**
    ```sh
      #build application archive
      mvn clean package
    ```
    ```sh
      #build application docker image
      docker build -t taskmanagementapp .
    ```
   ```sh
      #pull mysql docker image
      docker pull mysql:latest
    ```
    ```sh
      #run mysql docker container, save data in /var/lib/mysql
      docker run --name mysql-container \
        -e MYSQL_ROOT_PASSWORD=root \
        -e MYSQL_DATABASE=facio_db \
        -e MYSQL_USER=facio_user \
        -e MYSQL_PASSWORD=facio_pass \
        -p 3307:3306 -v mysql_data:/var/lib/mysql -d mysql
    ```
    ```sh
      #run application using .env.sample
      docker run -p 8080:8080 --env-file .env.sample taskmanagementapp
    ```

3. **Access the API documentation:**
    - Navigate to [Swagger UI](http://localhost:8080/swagger-ui/index.html#/) for API exploration.

### 🚧 Challenges Faced
- **Dropbox Integration for Attachments:** Integrating Dropbox to handle task-related file attachments was a completely new area for me. It involved mastering the Dropbox SDK, handling file uploads securely, and managing user access to shared files.
- **JWT in Cookies with Server-Side Management:** Unlike in my previous projects, where I returned just access token or access and refresh token in body of login response, and frontend needed to manage them, here, Servlet request manage AT and RT cookie automatically and seamlessly.
- **Scheduled Email Notifications for Task Deadlines:** Implementing a system to send out scheduled email reminders before task deadlines was a challenge. It required combining task tracking logic with scheduled jobs and email delivery via Resend, ensuring timely and reliable notifications without spamming users.

## 📜 License
Facio TaskManagementApp  is released under [Non-Commercial Use License Agreement](LICENSE.md).

---

🌟 **Enjoy seamless task management with Facio TaskManagementApp!**

Still have some questions? Don't hesitate to [reach out](https://www.linkedin.com/in/adam-mudrak-7813b3279/)!
