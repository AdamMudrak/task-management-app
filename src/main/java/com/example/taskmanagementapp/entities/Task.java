package com.example.taskmanagementapp.entities;

import static com.example.taskmanagementapp.constants.entitities.EntitiesConstants.ASSIGNEE_ID;
import static com.example.taskmanagementapp.constants.entitities.EntitiesConstants.DUE_DATE;
import static com.example.taskmanagementapp.constants.entitities.EntitiesConstants.PROJECT_ID;
import static com.example.taskmanagementapp.constants.entitities.EntitiesConstants.TASKS;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.LocalDate;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = TASKS)
@Getter
@Setter
public class Task {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false)
    private String name;
    private String description;
    @Enumerated(EnumType.STRING)
    private Priority priority;
    @Enumerated(EnumType.STRING)
    private Status status;
    @Column(nullable = false, name = DUE_DATE)
    private LocalDate dueDate;
    @ManyToOne
    @JoinColumn(nullable = false, name = PROJECT_ID)
    private Project project;
    @ManyToOne
    @JoinColumn(nullable = false, name = ASSIGNEE_ID)
    private User assignee;

    public enum Status {
        NOT_STARTED, IN_PROGRESS, COMPLETED
    }

    public enum Priority {
        LOW, MEDIUM, HIGH }
}
