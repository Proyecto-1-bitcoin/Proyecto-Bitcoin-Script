package com.script;

import java.util.Arrays;
import java.util.List;

public class ScriptParser {

    public static List<String> parse(String script) {
        return Arrays.asList(script.trim().split("\\s+"));
    }
    
}
