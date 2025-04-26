package com.example.taskmanagementapp.entities;

import static com.example.taskmanagementapp.constants.entitities.EntitiesConstants.EMPLOYEE_ID;
import static com.example.taskmanagementapp.constants.entitities.EntitiesConstants.END_DATE;
import static com.example.taskmanagementapp.constants.entitities.EntitiesConstants.IS_DELETED;
import static com.example.taskmanagementapp.constants.entitities.EntitiesConstants.OWNER_ID;
import static com.example.taskmanagementapp.constants.entitities.EntitiesConstants.PROJECTS;
import static com.example.taskmanagementapp.constants.entitities.EntitiesConstants.PROJECT_EMPLOYEES;
import static com.example.taskmanagementapp.constants.entitities.EntitiesConstants.PROJECT_ID;
import static com.example.taskmanagementapp.constants.entitities.EntitiesConstants.START_DATE;

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
import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.SQLDelete;

@Getter
@Setter
@Entity
@Table(name = PROJECTS)
@SQLDelete(sql = "UPDATE orders SET is_deleted = TRUE WHERE id = ?")
public class Project {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false)
    private String name;
    private String description;
    @Column(nullable = false, name = START_DATE)
    private LocalDate startDate;
    @Column(nullable = false, name = END_DATE)
    private LocalDate endDate;
    @Enumerated(EnumType.STRING)
    private Status status;
    @Column(nullable = false, name = IS_DELETED)
    private boolean isDeleted = false;
    @ManyToOne
    @JoinColumn(nullable = false, name = OWNER_ID)
    private User owner;
    @ManyToMany
    @JoinTable(
            name = PROJECT_EMPLOYEES,
            joinColumns = @JoinColumn(name = PROJECT_ID),
            inverseJoinColumns = @JoinColumn(name = EMPLOYEE_ID))
    private Set<User> employees = new HashSet<>();

    public enum Status {
        INITIATED,
        IN_PROGRESS,
        COMPLETED
    }
}
