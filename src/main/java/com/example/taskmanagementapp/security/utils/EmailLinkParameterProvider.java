package com.example.taskmanagementapp.security.utils;

import static com.example.taskmanagementapp.constants.security.SecurityConstants.ACTION;
import static com.example.taskmanagementapp.constants.security.SecurityConstants.RANDOM_LINK_STRENGTH;

import com.example.taskmanagementapp.entities.tokens.ParamToken;
import com.example.taskmanagementapp.repositories.paramtoken.ParamTokenRepository;
import com.example.taskmanagementapp.security.jwtutils.abstr.JwtAbstractUtil;
import com.example.taskmanagementapp.security.jwtutils.strategy.JwtStrategy;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Getter
@Setter
public class EmailLinkParameterProvider {
    private final RandomStringUtil randomStringUtil;
    private final ParamTokenRepository paramTokenRepository;
    private final JwtStrategy jwtStrategy;
    private String emailLinkParameter;
    private String token;

    public void formRandomParamTokenPair(String email) {
        setEmailLinkParameter(randomStringUtil.generateRandomString(RANDOM_LINK_STRENGTH));
        JwtAbstractUtil abstractUtil = jwtStrategy.getStrategy(ACTION);
        setToken(abstractUtil.generateToken(email));

        ParamToken paramToken = new ParamToken();
        paramToken.setParameter(emailLinkParameter);
        paramToken.setActionToken(token);
        paramTokenRepository.save(paramToken);
    }
}
