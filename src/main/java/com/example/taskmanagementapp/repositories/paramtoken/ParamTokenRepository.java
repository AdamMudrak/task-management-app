package com.example.taskmanagementapp.repositories.paramtoken;

import com.example.taskmanagementapp.entities.ParamToken;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ParamTokenRepository extends JpaRepository<ParamToken, Long> {
    Optional<ParamToken> findByParameterAndActionToken(String parameter, String actionToken);

    Optional<ParamToken> findByActionToken(String actionToken);
}
