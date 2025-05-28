package com.example.taskmanagementapp.services.utils;

import java.time.LocalDate;

public class UpdateValueValidatorUtil {
    public static boolean areStringsValid(String str1, String str2) {
        return str1 != null
                && !str1.isBlank()
                && !str1.equals(str2);
    }

    public static boolean areDatesValid(LocalDate date1, LocalDate date2) {
        return date1 != null && !date1.isEqual(date2);
    }
}
