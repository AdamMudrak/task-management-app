package com.example.taskmanagementapp.security.jwtutils.impl;

import static com.example.taskmanagementapp.constants.security.SecurityConstants.JWT_REFRESH_EXPIRATION;
import static com.example.taskmanagementapp.constants.security.SecurityConstants.JWT_SECRET;
import static com.example.taskmanagementapp.constants.security.SecurityConstants.REFRESH;

import com.example.taskmanagementapp.security.jwtutils.abstr.JwtAbstractUtil;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
@Qualifier(REFRESH)
public class JwtRefreshUtil extends JwtAbstractUtil {
    public JwtRefreshUtil(@Value(JWT_SECRET) String secretString,
                          @Value(JWT_REFRESH_EXPIRATION) long expiration) {
        super(secretString, expiration);
    }
}
