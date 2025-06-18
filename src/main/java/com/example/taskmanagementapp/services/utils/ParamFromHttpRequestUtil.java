package com.example.taskmanagementapp.services.utils;

import com.example.taskmanagementapp.exceptions.ActionNotFoundException;
import com.example.taskmanagementapp.repositories.ActionTokenRepository;
import jakarta.servlet.http.HttpServletRequest;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ParamFromHttpRequestUtil {
    private static final int FIRST_PARAM_POSITION = 0;
    private final ActionTokenRepository actionTokenRepository;

    public String parseRandomParameterAndToken(HttpServletRequest request) {
        String token = request.getParameter("token");
        if (token != null && !token.isBlank()) {
            if (!actionTokenRepository.existsByActionToken(token)) {
                throw new ActionNotFoundException(
                        "No such request was found... The link might be expired or forged");
            } else {
                actionTokenRepository.deleteByActionToken(token);
                return token;
            }
        } else {
            throw new ActionNotFoundException(
                    "Wasn't able to parse link...Might be expired or forged");
        }
    }

    public String getNamedParameter(HttpServletRequest request, String paramName) {
        Map<String, String[]> parameterMap = request.getParameterMap();
        return parameterMap.get(paramName)[FIRST_PARAM_POSITION];
    }
}
