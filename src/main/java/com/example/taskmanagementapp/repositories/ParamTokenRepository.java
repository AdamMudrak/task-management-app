package com.example.taskmanagementapp.repositories;

import com.example.taskmanagementapp.entities.ParamToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.transaction.annotation.Transactional;

public interface ParamTokenRepository extends JpaRepository<ParamToken, Long> {

    boolean existsByParameterAndActionToken(String parameter, String actionToken);

    @Transactional
    @Modifying
    void deleteByParameterAndActionToken(String parameter, String actionToken);
}
