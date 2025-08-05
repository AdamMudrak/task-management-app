package com.example.taskmanagementapp.services.email;

import static com.example.taskmanagementapp.constants.services.email.TaskDeadlineNotifierConstants.BODY_1;
import static com.example.taskmanagementapp.constants.services.email.TaskDeadlineNotifierConstants.BODY_2;
import static com.example.taskmanagementapp.constants.services.email.TaskDeadlineNotifierConstants.BODY_3;
import static com.example.taskmanagementapp.constants.services.email.TaskDeadlineNotifierConstants.BODY_4;
import static com.example.taskmanagementapp.constants.services.email.TaskDeadlineNotifierConstants.BODY_5;
import static com.example.taskmanagementapp.constants.services.email.TaskDeadlineNotifierConstants.CRONOUNITS;
import static com.example.taskmanagementapp.constants.services.email.TaskDeadlineNotifierConstants.SUBJECT;

import com.example.taskmanagementapp.entities.Task;
import com.example.taskmanagementapp.entities.User;
import com.example.taskmanagementapp.repositories.TaskRepository;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class TaskDeadlineNotifier extends EmailService {
    private static final Logger logger = LogManager.getLogger(TaskDeadlineNotifier.class);
    private final TaskRepository taskRepository;

    @Async
    @Scheduled(cron = CRONOUNITS)
    public void taskDeadlineNotification() {
        Map<User, List<Task>> usersTasksMap = filterTasksWhereDueTomorrow();
        if (usersTasksMap.isEmpty()) {
            logger.info("No tasks due tomorrow found");
        } else {
            sendEmailWhereTaskDueTomorrow(usersTasksMap);
        }
    }

    private Map<User, List<Task>> filterTasksWhereDueTomorrow() {
        Predicate<Task> whereDueTomorrow = task -> task.getDueDate()
                .isEqual(LocalDate.now().plusDays(1));

        return taskRepository.findAllNonDeletedWithAssigneeAndProject().stream()
                .filter(whereDueTomorrow)
                .collect(Collectors.groupingBy(Task::getAssignee));
    }

    private void sendEmailWhereTaskDueTomorrow(Map<User, List<Task>> userTasks) {
        for (Map.Entry<User, List<Task>> entry : userTasks.entrySet()) {
            String toEmail = entry.getKey().getEmail();
            List<Task> tasks = entry.getValue();
            this.queueEmail(toEmail, SUBJECT, formBody(tasks));
        }
        logger.info("Task deadline notification sent");
    }

    private String formBody(List<Task> tasks) {
        StringBuilder result = new StringBuilder();
        result.append(BODY_1).append(System.lineSeparator());
        for (Task task : tasks) {
            result.append(BODY_2)
                    .append(task.getId())
                    .append(BODY_3)
                    .append(task.getName())
                    .append(BODY_4)
                    .append(task.getProject().getName())
                    .append(BODY_5)
                    .append(System.lineSeparator());
        }
        return result.toString();
    }
}
