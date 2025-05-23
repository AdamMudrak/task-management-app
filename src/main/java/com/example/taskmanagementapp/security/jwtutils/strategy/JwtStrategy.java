package com.example.taskmanagementapp.security.jwtutils.strategy;

import com.example.taskmanagementapp.security.JwtType;
import com.example.taskmanagementapp.security.jwtutils.abstr.JwtAbstractUtil;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

@Component
public class JwtStrategy {
    private final JwtAbstractUtil accessUtil;
    private final JwtAbstractUtil refreshUtil;
    private final JwtAbstractUtil resetUtil;

    public JwtStrategy(@Qualifier("ACCESS") JwtAbstractUtil accessUtil,
                      @Qualifier("REFRESH") JwtAbstractUtil refreshUtil,
                      @Qualifier("ACTION") JwtAbstractUtil resetUtil) {
        this.accessUtil = accessUtil;
        this.refreshUtil = refreshUtil;
        this.resetUtil = resetUtil;
    }

    public JwtAbstractUtil getStrategy(JwtType requestType) {
        return switch (requestType) {
            case ACCESS -> accessUtil;
            case REFRESHMENT -> refreshUtil;
            case ACTION -> resetUtil;
        };
    }
}
