package com.example.taskmanagementapp.service.utils;

import com.example.taskmanagementapp.entity.ActionToken;
import com.example.taskmanagementapp.repository.ActionTokenRepository;
import com.example.taskmanagementapp.security.jwtutil.abstr.JwtAbstractUtil;
import com.example.taskmanagementapp.security.jwtutil.strategy.JwtStrategy;
import com.example.taskmanagementapp.security.jwtutil.strategy.JwtType;
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
