package com.example.taskmanagementapp.entities;

import static com.example.taskmanagementapp.constants.entitities.EntitiesConstants.LABELS;
import static com.example.taskmanagementapp.constants.entitities.EntitiesConstants.LABELS_TASKS;
import static com.example.taskmanagementapp.constants.entitities.EntitiesConstants.LABEL_ID;
import static com.example.taskmanagementapp.constants.entitities.EntitiesConstants.TASK_ID;
import static com.example.taskmanagementapp.constants.entitities.EntitiesConstants.USER_ID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.util.HashSet;
import java.util.Set;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = LABELS)
@Getter
@Setter
public class Label {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false)
    private String name;
    @Enumerated(EnumType.STRING)
    private Color color;
    @ManyToOne
    @JoinColumn(name = USER_ID)
    private User user;
    @ManyToMany
    @JoinTable(name = LABELS_TASKS,
                joinColumns = @JoinColumn (name = LABEL_ID),
                inverseJoinColumns = @JoinColumn(name = TASK_ID))
    private Set<Task> task = new HashSet<>();

    public enum Color {
        RED,
        ORANGE,
        YELLOW,
        GREEN
    }
}
