package com.script;

import java.util.Arrays;
import java.util.List;

public class Main {

    public static void main(String[] args) {
        boolean trace = Arrays.asList(args).contains("--trace");

        System.out.println("=== Demo 1: P2PKH correcto ===");
        runP2PKH(trace, true);

        System.out.println("\n=== Demo 2: P2PKH incorrecto (firma inválida) ===");
        runP2PKH(trace, false);

        System.out.println("\n=== Demo 3a: OP_IF rama verdadera ===");
        runConditional(trace, "OP_1");

        System.out.println("\n=== Demo 3b: OP_IF rama falsa -> OP_ELSE ===");
        runConditional(trace, "OP_0");

        System.out.println("\n=== Demo 4: OP_IF anidado ===");
        runNestedConditional(trace);
    }

    /** P2PKH estándar: válido si useFirmValid=true, inválido si false. */
    static void runP2PKH(boolean trace, boolean useValidSig) {
        ScriptInterpreter interpreter = new ScriptInterpreter();

        byte[] hash = MockCrypto.hash160("publicKey".getBytes());
        String pubKeyHash = new String(hash);

        String sig = useValidSig ? "valid" : "INVALIDA";
        String script = sig + " publicKey OP_DUP OP_HASH160 "
                + pubKeyHash + " OP_EQUALVERIFY OP_CHECKSIG";

        List<String> tokens = ScriptParser.parse(script);
        try {
            boolean result = interpreter.execute(tokens, trace);
            System.out.println("Resultado: " + result);
        } catch (ScriptException e) {
            System.out.println("Error en script: " + e.getMessage());
        }
    }

    /**
     * Script condicional simple:
     *   <cond> OP_IF OP_1 OP_ELSE OP_0 OP_ENDIF
     * Si cond=OP_1 deja 1 en el stack (true), si cond=OP_0 deja 0 (false).
     */
    static void runConditional(boolean trace, String condition) {
        ScriptInterpreter interpreter = new ScriptInterpreter();
        String script = condition + " OP_IF OP_1 OP_ELSE OP_0 OP_ENDIF";
        List<String> tokens = ScriptParser.parse(script);
        boolean result = interpreter.execute(tokens, trace);
        System.out.println("Resultado: " + result);
    }

    /**
     * Condicional anidado:
     *   OP_1 OP_IF
     *     OP_1 OP_IF
     *       OP_1        <- solo este se ejecuta
     *     OP_ELSE
     *       OP_0
     *     OP_ENDIF
     *   OP_ELSE
     *     OP_0
     *   OP_ENDIF
     * Resultado esperado: true
     */
    static void runNestedConditional(boolean trace) {
        ScriptInterpreter interpreter = new ScriptInterpreter();
        String script = "OP_1 OP_IF OP_1 OP_IF OP_1 OP_ELSE OP_0 OP_ENDIF OP_ELSE OP_0 OP_ENDIF";
        List<String> tokens = ScriptParser.parse(script);
        boolean result = interpreter.execute(tokens, trace);
        System.out.println("Resultado: " + result);
    }
}