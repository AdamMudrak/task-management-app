package com.example.taskmanagementapp.security.jwtutils.strategy;

import static com.example.taskmanagementapp.constants.security.SecurityConstants.ACCESS;
import static com.example.taskmanagementapp.constants.security.SecurityConstants.ACTION;
import static com.example.taskmanagementapp.constants.security.SecurityConstants.REFRESH;

import com.example.taskmanagementapp.security.jwtutils.abstr.JwtAbstractUtil;
import io.jsonwebtoken.JwtException;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

@Component
public class JwtStrategy {
    private final JwtAbstractUtil accessUtil;
    private final JwtAbstractUtil refreshUtil;
    private final JwtAbstractUtil resetUtil;

    public JwtStrategy(@Qualifier(ACCESS) JwtAbstractUtil accessUtil,
                      @Qualifier(REFRESH) JwtAbstractUtil refreshUtil,
                      @Qualifier(ACTION) JwtAbstractUtil resetUtil) {
        this.accessUtil = accessUtil;
        this.refreshUtil = refreshUtil;
        this.resetUtil = resetUtil;
    }

    public JwtAbstractUtil getStrategy(String key) {
        return switch (key) {
            case ACCESS -> accessUtil;
            case REFRESH -> refreshUtil;
            case ACTION -> resetUtil;
            default -> throw new JwtException("No such Jwt util");
        };
    }
}
