package com.example.taskmanagementapp.security.jwtutil.impl;

import com.example.taskmanagementapp.security.jwtutil.abstr.JwtAbstractUtil;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
@Qualifier("REFRESH")
public class JwtRefreshUtil extends JwtAbstractUtil {
    public JwtRefreshUtil(@Value("${jwt.secret}") String secretString,
                          @Value("${jwt.refresh.expiration}") long expiration) {
        super(secretString, expiration);
    }
}
