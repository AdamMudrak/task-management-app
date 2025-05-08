package com.example.taskmanagementapp.repositories.actiontoken;

import com.example.taskmanagementapp.entities.ActionToken;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ActionTokenRepository extends JpaRepository<ActionToken, Long> {
    boolean existsByActionToken(String actionToken);

    void deleteByActionToken(String actionToken);
}
