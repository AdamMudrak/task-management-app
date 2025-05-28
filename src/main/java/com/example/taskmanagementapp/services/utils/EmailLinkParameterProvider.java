package com.example.taskmanagementapp.services.utils;

import static com.example.taskmanagementapp.constants.security.SecurityConstants.RANDOM_LINK_STRENGTH;

import com.example.taskmanagementapp.entities.ParamToken;
import com.example.taskmanagementapp.repositories.ParamTokenRepository;
import com.example.taskmanagementapp.security.jwtutils.abstr.JwtAbstractUtil;
import com.example.taskmanagementapp.security.jwtutils.strategy.JwtStrategy;
import com.example.taskmanagementapp.security.jwtutils.strategy.JwtType;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Getter
@Setter
public class EmailLinkParameterProvider {
    private final ParamTokenRepository paramTokenRepository;
    private final JwtStrategy jwtStrategy;
    private String emailLinkParameter;
    private String token;

    public void formRandomParamTokenPair(String email) {
        setEmailLinkParameter(RandomStringUtil.generateRandomString(RANDOM_LINK_STRENGTH));
        JwtAbstractUtil abstractUtil = jwtStrategy.getStrategy(JwtType.ACTION);
        setToken(abstractUtil.generateToken(email));

        ParamToken paramToken = new ParamToken();
        paramToken.setParameter(emailLinkParameter);
        paramToken.setActionToken(token);
        paramTokenRepository.save(paramToken);
    }
}
