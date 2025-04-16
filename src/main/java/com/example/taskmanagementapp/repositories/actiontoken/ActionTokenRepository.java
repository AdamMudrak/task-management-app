package com.example.taskmanagementapp.repositories.actiontoken;

import com.example.taskmanagementapp.entities.tokens.ActionToken;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ActionTokenRepository extends JpaRepository<ActionToken, Long> {
    Optional<ActionToken> findByActionToken(String actionToken);
}
