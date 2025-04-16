package com.example.taskmanagementapp.security.utils;

import com.example.budgetingapp.entities.tokens.ParamToken;
import com.example.budgetingapp.exceptions.notfoundexceptions.ActionNotFoundException;
import com.example.budgetingapp.repositories.paramtoken.ParamTokenRepository;
import jakarta.servlet.http.HttpServletRequest;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.springframework.stereotype.Component;

import java.util.Map;

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
            setRandomParameter(entry.getKey());
            setToken(entry.getValue()[FIRST_PARAM_POSITION]);
            break;
        }
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
