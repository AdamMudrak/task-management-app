package com.example.taskmanagementapp.services.email;

import com.example.taskmanagementapp.entities.Task;
import com.example.taskmanagementapp.entities.User;
import com.example.taskmanagementapp.repositories.task.TaskRepository;
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
    @Scheduled(cron = "45 40 2 * * ?")
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

        return taskRepository.findAllNonDeleted().stream()
                .filter(whereDueTomorrow)
                .collect(Collectors.groupingBy(Task::getAssignee));
    }

    private void sendEmailWhereTaskDueTomorrow(Map<User, List<Task>> userTasks) {
        for (Map.Entry<User, List<Task>> entry : userTasks.entrySet()) {
            String toEmail = entry.getKey().getEmail();
            List<Task> tasks = entry.getValue();
            this.sendMessage(toEmail, "Tasks due tomorrow", formBody(tasks));
        }
        logger.info("Task deadline notification sent");
    }

    private String formBody(List<Task> tasks) {
        StringBuilder result = new StringBuilder();
        result.append("Good day! Following tasks are due tomorrow:").append(System.lineSeparator());
        for (Task task : tasks) {
            result.append("Task ")
                    .append(task.getId())
                    .append(" \"")
                    .append(task.getName())
                    .append("\"")
                    .append(", Project \"")
                    .append(task.getProject().getName())
                    .append("\"")
                    .append(System.lineSeparator());
        }
        return result.toString();
    }
}
//TODO constants
