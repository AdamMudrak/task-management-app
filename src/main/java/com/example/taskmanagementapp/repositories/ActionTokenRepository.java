package com.example.taskmanagementapp.repositories;

import com.example.taskmanagementapp.entities.ActionToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.transaction.annotation.Transactional;

public interface ActionTokenRepository extends JpaRepository<ActionToken, Long> {
    boolean existsByActionToken(String actionToken);

    @Transactional
    @Modifying
    void deleteByActionToken(String actionToken);
}
