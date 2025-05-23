package com.example.taskmanagementapp.services.utils;

import com.example.taskmanagementapp.exceptions.notfoundexceptions.ActionNotFoundException;
import com.example.taskmanagementapp.repositories.ParamTokenRepository;
import jakarta.servlet.http.HttpServletRequest;
import java.util.Map;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.springframework.stereotype.Component;

@Component
@Getter
@Setter
@RequiredArgsConstructor
public class ParamFromHttpRequestUtil {
    private static final int FIRST_PARAM_POSITION = 0;
    private final ParamTokenRepository paramTokenRepository;
    private String randomParameter;
    private String token;

    public String parseRandomParameterAndToken(HttpServletRequest request) {
        Map<String, String[]> parameterMap = request.getParameterMap();
        String randomParameter = null;
        String token = null;
        for (Map.Entry<String, String[]> entry : parameterMap.entrySet()) {
            if (entry.getKey().equals("newEmail")
                    || entry.getKey().equals("actionToken")) {
                continue;
            }
            randomParameter = entry.getKey();
            token = entry.getValue()[FIRST_PARAM_POSITION];
            break;
        }
        if (randomParameter != null && token != null) {
            if (!paramTokenRepository.existsByParameterAndActionToken(randomParameter, token)) {
                throw new ActionNotFoundException(
                        "No such request was found... The link might be expired or forged");
            } else {
                paramTokenRepository.deleteByParameterAndActionToken(randomParameter, token);
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
