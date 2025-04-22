package com.example.taskmanagementapp.security;

import static com.example.taskmanagementapp.constants.security.SecurityConstants.ACCESS;
import static com.example.taskmanagementapp.constants.security.SecurityConstants.ACCESS_TOKEN;

import com.example.taskmanagementapp.security.jwtutils.abstr.JwtAbstractUtil;
import com.example.taskmanagementapp.security.jwtutils.strategy.JwtStrategy;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Arrays;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    private final UserDetailsService userDetailsService;
    private final JwtAbstractUtil jwtAbstractUtil;

    public JwtAuthenticationFilter(@Autowired JwtStrategy jwtStrategy,
            @Autowired UserDetailsService userDetailsService) {
        this.userDetailsService = userDetailsService;
        this.jwtAbstractUtil = jwtStrategy.getStrategy(ACCESS);
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {
        String token = findAccessCookie(request);

        if (token != null && jwtAbstractUtil.isValidToken(token)) {
            String username = jwtAbstractUtil.getUsername(token);
            UserDetails userDetails = userDetailsService.loadUserByUsername(username);
            Authentication authentication = new UsernamePasswordAuthenticationToken(
                    userDetails, null, userDetails.getAuthorities()
            );
            SecurityContextHolder.getContext().setAuthentication(authentication);
        }

        filterChain.doFilter(request, response);
    }

    private String findAccessCookie(HttpServletRequest httpServletRequest) {
        Cookie cookie = null;
        if (httpServletRequest != null
                && httpServletRequest.getCookies() != null
                && httpServletRequest.getCookies().length > 0) {
            cookie = Arrays.stream(httpServletRequest.getCookies())
                    .filter(refreshCookie -> refreshCookie.getName().equals(ACCESS_TOKEN))
                    .findFirst().orElse(null);
        }
        return cookie != null ? cookie.getValue() : null;
    }
}
