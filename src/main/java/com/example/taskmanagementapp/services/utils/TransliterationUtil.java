package com.example.taskmanagementapp.services.utils;

import java.util.HashMap;
import java.util.Map;
import org.springframework.stereotype.Service;

@Service
public class TransliterationUtil {

    private static final Map<Character, String> TRANSLIT_MAP = new HashMap<>();

    static {
        // Ukrainian
        TRANSLIT_MAP.put('А', "a");
        TRANSLIT_MAP.put('а', "a");
        TRANSLIT_MAP.put('Б', "b");
        TRANSLIT_MAP.put('б', "b");
        TRANSLIT_MAP.put('В', "v");
        TRANSLIT_MAP.put('в', "v");
        TRANSLIT_MAP.put('Г', "h");
        TRANSLIT_MAP.put('г', "h");
        TRANSLIT_MAP.put('Ґ', "g");
        TRANSLIT_MAP.put('ґ', "g");
        TRANSLIT_MAP.put('Д', "d");
        TRANSLIT_MAP.put('д', "d");
        TRANSLIT_MAP.put('Е', "e");
        TRANSLIT_MAP.put('е', "e");
        TRANSLIT_MAP.put('Є', "ye");
        TRANSLIT_MAP.put('є', "ye");
        TRANSLIT_MAP.put('Ж', "zh");
        TRANSLIT_MAP.put('ж', "zh");
        TRANSLIT_MAP.put('З', "z");
        TRANSLIT_MAP.put('з', "z");
        TRANSLIT_MAP.put('И', "y");
        TRANSLIT_MAP.put('и', "y");
        TRANSLIT_MAP.put('І', "i");
        TRANSLIT_MAP.put('і', "i");
        TRANSLIT_MAP.put('Ї', "yi");
        TRANSLIT_MAP.put('ї', "yi");
        TRANSLIT_MAP.put('Й', "i");
        TRANSLIT_MAP.put('й', "i");
        TRANSLIT_MAP.put('К', "k");
        TRANSLIT_MAP.put('к', "k");
        TRANSLIT_MAP.put('Л', "l");
        TRANSLIT_MAP.put('л', "l");
        TRANSLIT_MAP.put('М', "m");
        TRANSLIT_MAP.put('м', "m");
        TRANSLIT_MAP.put('Н', "n");
        TRANSLIT_MAP.put('н', "n");
        TRANSLIT_MAP.put('О', "o");
        TRANSLIT_MAP.put('о', "o");
        TRANSLIT_MAP.put('П', "p");
        TRANSLIT_MAP.put('п', "p");
        TRANSLIT_MAP.put('Р', "r");
        TRANSLIT_MAP.put('р', "r");
        TRANSLIT_MAP.put('С', "s");
        TRANSLIT_MAP.put('с', "s");
        TRANSLIT_MAP.put('Т', "t");
        TRANSLIT_MAP.put('т', "t");
        TRANSLIT_MAP.put('У', "u");
        TRANSLIT_MAP.put('у', "u");
        TRANSLIT_MAP.put('Ф', "f");
        TRANSLIT_MAP.put('ф', "f");
        TRANSLIT_MAP.put('Х', "kh");
        TRANSLIT_MAP.put('х', "kh");
        TRANSLIT_MAP.put('Ц', "ts");
        TRANSLIT_MAP.put('ц', "ts");
        TRANSLIT_MAP.put('Ч', "ch");
        TRANSLIT_MAP.put('ч', "ch");
        TRANSLIT_MAP.put('Ш', "sh");
        TRANSLIT_MAP.put('ш', "sh");
        TRANSLIT_MAP.put('Щ', "shch");
        TRANSLIT_MAP.put('щ', "shch");
        TRANSLIT_MAP.put('Ь', "");
        TRANSLIT_MAP.put('ь', "");

        // Russian - additional
        TRANSLIT_MAP.put('Ё', "yo");
        TRANSLIT_MAP.put('ё', "yo");
        TRANSLIT_MAP.put('Ы', "y");
        TRANSLIT_MAP.put('ы', "y");
        TRANSLIT_MAP.put('Э', "e");
        TRANSLIT_MAP.put('э', "e");
        TRANSLIT_MAP.put('Ъ', "");
        TRANSLIT_MAP.put('ъ', "");
    }

    public String transliterate(String input) {
        if (input == null || input.isEmpty()) {
            return input;
        }
        StringBuilder result = new StringBuilder();
        for (char ch : input.toCharArray()) {
            result.append(TRANSLIT_MAP.getOrDefault(ch, String.valueOf(ch)));
        }
        return result.toString();
    }
}
