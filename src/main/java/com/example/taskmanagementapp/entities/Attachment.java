package com.example.taskmanagementapp.entities;

import static com.example.taskmanagementapp.constants.entitities.EntitiesConstants.ATTACHMENTS;
import static com.example.taskmanagementapp.constants.entitities.EntitiesConstants.FILE_ID;
import static com.example.taskmanagementapp.constants.entitities.EntitiesConstants.FILE_NAME;
import static com.example.taskmanagementapp.constants.entitities.EntitiesConstants.TASK_ID;
import static com.example.taskmanagementapp.constants.entitities.EntitiesConstants.UPLOAD_DATE;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
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
@Table(name = ATTACHMENTS)
@Getter
@Setter
public class Attachment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne
    @JoinColumn(nullable = false, name = TASK_ID)
    private Task task;
    @Column(nullable = false, name = FILE_ID)
    private String fileId;
    @Column(nullable = false, name = FILE_NAME)
    private String fileName;
    @Column(nullable = false, name = UPLOAD_DATE)
    private LocalDate uploadDate;
}
