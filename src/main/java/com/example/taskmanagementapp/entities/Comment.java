package com.example.taskmanagementapp.entities;

import static com.example.taskmanagementapp.constants.entitities.EntitiesConstants.COMMENTS;
import static com.example.taskmanagementapp.constants.entitities.EntitiesConstants.TASK_ID;
import static com.example.taskmanagementapp.constants.entitities.EntitiesConstants.USER_ID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = COMMENTS)
@Getter
@Setter
public class Comment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne
    @JoinColumn(nullable = false, name = TASK_ID)
    private Task task;
    @ManyToOne
    @JoinColumn(nullable = false, name = USER_ID)
    private User user;
    @Column(nullable = false)
    private String text;
    @Column(nullable = false)
    private LocalDateTime timestamp;
}
