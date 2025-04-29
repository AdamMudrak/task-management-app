package com.example.taskmanagementapp.repositories.attachment;

import com.example.taskmanagementapp.entities.Attachment;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AttachmentRepository extends JpaRepository<Attachment, Long> {
}
