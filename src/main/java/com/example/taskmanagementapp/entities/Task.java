package com.example.taskmanagementapp.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.LocalDate;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.SQLDelete;

@Getter
@Setter
@EqualsAndHashCode(exclude = {"project", "assignee", })
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "tasks")
@SQLDelete(sql = "UPDATE tasks SET is_deleted = TRUE WHERE id = ?")
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
    @Column(nullable = false, name = "due_date")
    private LocalDate dueDate;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(nullable = false, name = "project_id")
    private Project project;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(nullable = false, name = "assignee_id")
    private User assignee;
    @Column(nullable = false, name = "is_deleted", columnDefinition = "TINYINT(1)")
    private boolean isDeleted = false;

    public enum Status {
        NOT_STARTED, IN_PROGRESS, COMPLETED
    }

    public enum Priority {
        LOW, MEDIUM, HIGH }
}
