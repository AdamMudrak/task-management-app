package com.example.taskmanagementapp.repositories.attachment;

import com.example.taskmanagementapp.entities.Attachment;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AttachmentRepository extends JpaRepository<Attachment, Long> {
    List<Attachment> findAllByTaskId(Long taskId);
}
