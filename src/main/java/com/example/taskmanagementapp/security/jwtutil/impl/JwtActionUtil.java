package com.example.taskmanagementapp.security.jwtutil.impl;

import com.example.taskmanagementapp.security.jwtutil.abstr.JwtAbstractUtil;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
@Qualifier("ACTION")
public class JwtActionUtil extends JwtAbstractUtil {
    public JwtActionUtil(@Value("${jwt.secret}") String secretString,
                         @Value("${jwt.action.expiration}") long expiration) {
        super(secretString, expiration);
    }
}
