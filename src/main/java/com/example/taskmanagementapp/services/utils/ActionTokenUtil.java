package com.example.taskmanagementapp.services.utils;

import com.example.taskmanagementapp.entities.ActionToken;
import com.example.taskmanagementapp.repositories.ActionTokenRepository;
import com.example.taskmanagementapp.security.jwtutils.abstr.JwtAbstractUtil;
import com.example.taskmanagementapp.security.jwtutils.strategy.JwtStrategy;
import com.example.taskmanagementapp.security.jwtutils.strategy.JwtType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ActionTokenUtil {
    private final JwtStrategy jwtStrategy;
    private final ActionTokenRepository actionTokenRepository;

    public String generateActionToken(String email) {
        JwtAbstractUtil actionUtil = jwtStrategy.getStrategy(JwtType.ACTION);
        ActionToken actionToken = new ActionToken();
        actionToken.setActionToken(actionUtil.generateToken(email));
        actionTokenRepository.save(actionToken);
        return actionToken.getActionToken();
    }
}
