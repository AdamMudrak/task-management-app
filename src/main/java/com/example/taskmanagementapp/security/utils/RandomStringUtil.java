package com.example.taskmanagementapp.security.utils;

import static com.example.taskmanagementapp.constants.security.SecurityConstants.RANDOM_STRING_BASE;

import java.util.Random;
import org.springframework.stereotype.Component;

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
