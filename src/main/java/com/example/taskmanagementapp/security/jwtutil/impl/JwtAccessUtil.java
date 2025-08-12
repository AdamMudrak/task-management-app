package com.example.taskmanagementapp.security.jwtutil.impl;

import com.example.taskmanagementapp.security.jwtutil.abstr.JwtAbstractUtil;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
@Qualifier("ACCESS")
public class JwtAccessUtil extends JwtAbstractUtil {
    public JwtAccessUtil(@Value("${jwt.secret}") String secretString,
                         @Value("${jwt.access.expiration}") long expiration) {
        super(secretString, expiration);
    }
}
