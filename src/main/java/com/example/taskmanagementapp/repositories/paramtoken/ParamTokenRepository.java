package com.example.taskmanagementapp.repositories.paramtoken;

import com.example.taskmanagementapp.entities.ParamToken;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ParamTokenRepository extends JpaRepository<ParamToken, Long> {

    boolean existsByParameterAndActionToken(String parameter, String actionToken);

    void deleteByParameterAndActionToken(String parameter, String actionToken);
}
