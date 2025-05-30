package com.example.taskmanagementapp.services.utils;

import static com.example.taskmanagementapp.constants.security.SecurityConstants.RANDOM_STRING_BASE;

import java.util.Random;

public class RandomStringUtil {
    public static String generateRandomString(int strength) {
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
