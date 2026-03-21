package com.script;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Clase principal que sirve como punto de entrada del programa.
 * Ejecuta todas las demostraciones requeridas por la fase 2 del proyecto.
 * @author Jeremhy López
 * @author Jonathan Cofiño
 * @author Henry Guzmán
 * @since 2026-03-19
 * @version 2.0
 */
public class Main {

    /**
     * Método principal. Ejecuta todas las demos del intérprete.
     * Usar el argumento --trace para imprimir el estado de la pila tras cada instrucción.
     * @param args Argumentos de línea de comandos. Se acepta --trace.
     */
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

        System.out.println("\n=== Demo 5: Multisig 2-de-3 (2 firmas válidas) ===");
        runMultisig(trace, true);

        System.out.println("\n=== Demo 6: Multisig 2-de-3 (solo 1 firma válida) ===");
        runMultisig(trace, false);
    }

    /**
     * Demo de P2PKH (Pay-to-Public-Key-Hash).
     * Script: scriptSig = <firma> <pubKey>
     *         scriptPubKey = OP_DUP OP_HASH160 <pubKeyHash> OP_EQUALVERIFY OP_CHECKSIG
     *
     * El problema de usar un String para el script es que hash160 produce bytes
     * arbitrarios que se corrompen al convertirlos a String. Por eso construimos
     * la lista de tokens directamente, pasando el hash como objeto byte[] a través
     * de un token especial en el intérprete.
     *
     * Solución: usar ScriptInterpreter.executeWithData() que acepta una mezcla de
     * String opcodes y byte[] data directamente, sin pasar por un String.
     *
     * @param trace Si true, imprime el estado de la pila tras cada instrucción.
     * @param useValidSig Si true usa firma válida ("valid"), si false usa firma inválida.
     */
    static void runP2PKH(boolean trace, boolean useValidSig) {
        ScriptInterpreter interpreter = new ScriptInterpreter();

        // Calculamos el hash de la clave pública
        byte[] pubKeyHash = MockCrypto.hash160("publicKey".getBytes());

        // Construimos los tokens directamente como objetos (evita corrupción de bytes)
        // scriptSig: <firma> <pubKey>
        // scriptPubKey: OP_DUP OP_HASH160 <pubKeyHash> OP_EQUALVERIFY OP_CHECKSIG
        List<Object> tokens = new ArrayList<>();
        tokens.add(useValidSig ? "valid" : "INVALIDA");  // firma
        tokens.add("publicKey");                          // clave pública
        tokens.add("OP_DUP");
        tokens.add("OP_HASH160");
        tokens.add(pubKeyHash);                           // el hash como byte[] directo
        tokens.add("OP_EQUALVERIFY");
        tokens.add("OP_CHECKSIG");

        try {
            boolean result = interpreter.executeWithData(tokens, trace);
            System.out.println("Resultado: " + result);
        } catch (ScriptException e) {
            System.out.println("Error en script: " + e.getMessage());
        }
    }

    /**
     * Demo de script condicional simple.
     * Script: {@code <cond> OP_IF OP_1 OP_ELSE OP_0 OP_ENDIF}
     * Si cond es verdadero deja 1 en el stack (true), si es falso deja 0 (false).
     * @param trace Si true, imprime el estado de la pila tras cada instrucción.
     * @param condition Token de condición: "OP_1" para verdadero, "OP_0" para falso.
     */
    static void runConditional(boolean trace, String condition) {
        ScriptInterpreter interpreter = new ScriptInterpreter();
        String script = condition + " OP_IF OP_1 OP_ELSE OP_0 OP_ENDIF";
        List<String> tokens = ScriptParser.parse(script);
        boolean result = interpreter.execute(tokens, trace);
        System.out.println("Resultado: " + result);
    }

    /**
     * Demo de condicional anidado.
     * Script:
     * <pre>
     *   OP_1 OP_IF
     *     OP_1 OP_IF
     *       OP_1        (rama que se ejecuta)
     *     OP_ELSE
     *       OP_0
     *     OP_ENDIF
     *   OP_ELSE
     *     OP_0
     *   OP_ENDIF
     * </pre>
     * Resultado esperado: true (ambas condiciones son verdaderas).
     * @param trace Si true, imprime el estado de la pila tras cada instrucción.
     */
    static void runNestedConditional(boolean trace) {
        ScriptInterpreter interpreter = new ScriptInterpreter();
        String script = "OP_1 OP_IF OP_1 OP_IF OP_1 OP_ELSE OP_0 OP_ENDIF OP_ELSE OP_0 OP_ENDIF";
        List<String> tokens = ScriptParser.parse(script);
        boolean result = interpreter.execute(tokens, trace);
        System.out.println("Resultado: " + result);
    }

    /**
     * Demo de Multisig 2-de-3.
     * Requiere 2 firmas válidas de entre 3 claves públicas posibles.
     * Stack de entrada (de abajo a arriba):
     * {@code OP_0 <firma1> <firma2> OP_2 <clave1> <clave2> <clave3> OP_3 OP_CHECKMULTISIG}
     * El OP_0 al inicio es el bug histórico de Bitcoin que consume un elemento extra.
     * @param trace Si true, imprime el estado de la pila tras cada instrucción.
     * @param bothValid Si true, ambas firmas son válidas. Si false, solo la primera lo es.
     */
    static void runMultisig(boolean trace, boolean bothValid) {
        ScriptInterpreter interpreter = new ScriptInterpreter();
        String sig2 = bothValid ? "valid" : "INVALIDA";
        String script = "OP_0 "
                + "valid " + sig2 + " "
                + "OP_2 "
                + "publicKey publicKey publicKey "
                + "OP_3 "
                + "OP_CHECKMULTISIG";
        List<String> tokens = ScriptParser.parse(script);
        try {
            boolean result = interpreter.execute(tokens, trace);
            System.out.println("Resultado: " + result);
        } catch (ScriptException e) {
            System.out.println("Error en script: " + e.getMessage());
        }
    }
}