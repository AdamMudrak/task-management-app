package com.example.taskmanagementapp.repositories.task;

import com.example.taskmanagementapp.entities.Task;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TaskRepository extends JpaRepository<Task, Long> {
}
