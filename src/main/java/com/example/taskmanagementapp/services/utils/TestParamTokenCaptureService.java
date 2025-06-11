package com.example.taskmanagementapp.services.utils;

public class TestParamTokenCaptureService {

    private static String[] lastParamTokenPair;

    public static void capture(String[] paramTokenPair) {
        lastParamTokenPair = paramTokenPair != null ? paramTokenPair.clone() : null;
    }

    public static String[] getLastParamTokenPair() {
        return lastParamTokenPair != null ? lastParamTokenPair.clone() : null;
    }

    public static void clear() {
        lastParamTokenPair = null;
    }
}
