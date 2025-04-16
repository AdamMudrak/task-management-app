package com.example.taskmanagementapp.security.utils;

import org.springframework.stereotype.Component;

import java.util.Random;

import static com.example.budgetingapp.constants.security.SecurityConstants.RANDOM_STRING_BASE;

@Component
public class RandomStringUtil {
    public String generateRandomString(int strength) {
        String characters = RANDOM_STRING_BASE;
        StringBuilder randomString =
                new StringBuilder(strength);
        Random random = new Random();
        for (int i = 0; i < strength; i++) {
            randomString.append(characters.charAt(random.nextInt(characters.length())));
        }
        return randomString.toString();
    }
}
