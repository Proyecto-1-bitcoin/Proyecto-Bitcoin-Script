package com.script;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import java.util.List;

class ScriptParserTest {

    @Test
    void testParseNormalText() {
        String input = "hola mundo prueba";

        List<String> result = ScriptParser.parse(input);

        assertEquals(List.of("hola", "mundo", "prueba"), result);
    }

    @Test
    void testParseWithExtraSpaces() {
        String input = "   hola   mundo   ";

        List<String> result = ScriptParser.parse(input);

        assertEquals(List.of("hola", "mundo"), result);
    }

    @Test
    void testParseSingleWord() {
        String input = "hola";

        List<String> result = ScriptParser.parse(input);

        assertEquals(List.of("hola"), result);
    }

    @Test
    void testParseEmptyString() {
        String input = "   ";

        List<String> result = ScriptParser.parse(input);

        // split("\\s+") sobre string vacío devuelve lista con un elemento ""
        assertEquals(List.of(""), result);
    }

    @Test
    void testParseWithTabsAndNewLines() {
        String input = "hola\tmundo\nprueba";

        List<String> result = ScriptParser.parse(input);

        assertEquals(List.of("hola", "mundo", "prueba"), result);
    }
}
