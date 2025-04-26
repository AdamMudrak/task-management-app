package com.example.taskmanagementapp.security.jwtutils.impl;

import static com.example.taskmanagementapp.constants.security.SecurityConstants.ACTION;
import static com.example.taskmanagementapp.constants.security.SecurityConstants.JWT_ACTION_EXPIRATION;
import static com.example.taskmanagementapp.constants.security.SecurityConstants.JWT_SECRET;

import com.example.taskmanagementapp.security.jwtutils.abstr.JwtAbstractUtil;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
@Qualifier(ACTION)
public class JwtResetUtil extends JwtAbstractUtil {
    public JwtResetUtil(@Value(JWT_SECRET) String secretString,
                        @Value(JWT_ACTION_EXPIRATION) long expiration) {
        super(secretString, expiration);
    }
}
