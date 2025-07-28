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
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.SQLDelete;

@Getter
@Setter
@EqualsAndHashCode
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "projects")
@SQLDelete(sql = "UPDATE projects SET is_deleted = TRUE WHERE id = ?")
public class Project {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false)
    private String name;
    private String description;
    @Column(nullable = false, name = "start_date")
    private LocalDate startDate;
    @Column(nullable = false, name = "end_date")
    private LocalDate endDate;
    @Enumerated(EnumType.STRING)
    private Status status;
    @Column(nullable = false, name = "is_deleted", columnDefinition = "TINYINT(1)")
    private boolean isDeleted = false;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(nullable = false, name = "owner_id")
    private User owner;
    @ManyToMany
    @JoinTable(
            name = "project_managers",
            joinColumns = @JoinColumn(name = "project_id"),
            inverseJoinColumns = @JoinColumn(name = "manager_id"))
    private Set<User> managers = new HashSet<>();
    @ManyToMany
    @JoinTable(
            name = "project_employees",
            joinColumns = @JoinColumn(name = "project_id"),
            inverseJoinColumns = @JoinColumn(name = "employee_id"))
    private Set<User> employees = new HashSet<>();

    public enum Status {
        INITIATED,
        IN_PROGRESS,
        COMPLETED
    }
}
