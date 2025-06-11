package com.example.taskmanagementapp.services.utils;

public class TestCaptureService {

    private static String[] lastValue;

    public static void capture(String[] value) {
        lastValue = value != null ? value.clone() : null;
    }

    public static String[] getLastValue() {
        return lastValue != null ? lastValue.clone() : null;
    }

    public static void clear() {
        lastValue = null;
    }
}
