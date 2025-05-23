package com.example.taskmanagementapp.security;

import static com.example.taskmanagementapp.constants.security.SecurityConstants.DIVIDER;

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
import org.springframework.beans.factory.annotation.Value;
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
    private final JwtAbstractUtil jwtAccessUtil;
    private final JwtAbstractUtil jwtRefreshUtil;
    @Value("${jwt.access.expiration}")
    private Long accessExpiration;

    public JwtAuthenticationFilter(@Autowired JwtStrategy jwtStrategy,
            @Autowired UserDetailsService userDetailsService) {
        this.userDetailsService = userDetailsService;
        this.jwtAccessUtil = jwtStrategy.getStrategy(JwtType.ACCESS);
        this.jwtRefreshUtil = jwtStrategy.getStrategy(JwtType.REFRESHMENT);
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {
        String accessToken = findAccessToken(request);
        if (accessToken == null || !(jwtAccessUtil.isValidToken(accessToken))) {
            accessToken = refreshAccessToken(request, response);
        }
        if (accessToken != null && jwtAccessUtil.isValidToken(accessToken)) {
            String username = jwtAccessUtil.getUsername(accessToken);
            UserDetails userDetails = userDetailsService.loadUserByUsername(username);
            Authentication authentication = new UsernamePasswordAuthenticationToken(
                    userDetails, null, userDetails.getAuthorities()
            );
            SecurityContextHolder.getContext().setAuthentication(authentication);
        }

        filterChain.doFilter(request, response);
    }

    private String getCookieValue(HttpServletRequest httpServletRequest, String cookieName) {
        Cookie cookie = null;
        if (httpServletRequest != null
                && httpServletRequest.getCookies() != null
                && httpServletRequest.getCookies().length > 0) {
            cookie = Arrays.stream(httpServletRequest.getCookies())
                    .filter(cookieObject -> cookieObject.getName().equals(cookieName))
                    .findFirst().orElse(null);
        }
        return cookie != null ? cookie.getValue() : null;
    }

    private String findAccessToken(HttpServletRequest httpServletRequest) {
        return getCookieValue(httpServletRequest, "accessToken");
    }

    private String findRefreshToken(HttpServletRequest httpServletRequest) {
        return getCookieValue(httpServletRequest, "refreshToken");
    }

    private String refreshAccessToken(HttpServletRequest request,
                               HttpServletResponse response) {
        String refreshToken = findRefreshToken(request);
        if (refreshToken != null && jwtRefreshUtil.isValidToken(refreshToken)) {
            String username = jwtRefreshUtil.getUsername(refreshToken);
            String accessToken = jwtAccessUtil.generateToken(username);
            String accessCookie = "accessToken" + "=" + accessToken
                    + "; Path=/"
                    + "; HttpOnly"
                    + "; Secure"
                    + "; SameSite=Strict"
                    + "; Max-Age=" + accessExpiration / DIVIDER;
            response.addHeader("Set-Cookie", accessCookie);
            return accessToken;
        }
        return null;
    }
}
