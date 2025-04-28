package com.example.taskmanagementapp.security.utils;

import com.example.taskmanagementapp.entities.tokens.ParamToken;
import com.example.taskmanagementapp.exceptions.notfoundexceptions.ActionNotFoundException;
import com.example.taskmanagementapp.repositories.paramtoken.ParamTokenRepository;
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
public class RandomParamFromHttpRequestUtil {
    private static final int FIRST_PARAM_POSITION = 0;
    private final ParamTokenRepository paramTokenRepository;
    private String randomParameter;
    private String token;

    public void parseRandomParameterAndToken(HttpServletRequest request) {
        Map<String, String[]> parameterMap = request.getParameterMap();
        for (Map.Entry<String, String[]> entry : parameterMap.entrySet()) {
            if (entry.getKey().equals("newEmail")) {
                continue;
            }
            setRandomParameter(entry.getKey());
            setToken(entry.getValue()[FIRST_PARAM_POSITION]);
            break;
        }
    }

    public String getNewEmail(HttpServletRequest request) {
        Map<String, String[]> parameterMap = request.getParameterMap();
        return parameterMap.get("newEmail")[FIRST_PARAM_POSITION];
    }

    public String getTokenFromRepo(String randomParam, String token) {
        ParamToken paramToken = paramTokenRepository
                .findByParameterAndActionToken(randomParam, token)
                .orElseThrow(() -> new ActionNotFoundException(
                        "No such request was found... The link might be expired or forged"));
        setToken(paramToken.getActionToken());
        return token;
    }
}
