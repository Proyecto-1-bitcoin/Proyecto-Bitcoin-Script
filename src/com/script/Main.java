package com.script;

import java.util.Arrays;
import java.util.List;

/**
 * Clase principal que servira como vista, es la interfaz gráfica que permite que el usuario interactue con el programa.
 * @author Jeremhy López
 * @author Jonathan Cofiño
 * @author Henry Guzmán
 * @since 2026-03-19
 * @version 1.5
 */

public class Main {

	/**
	 * Método para mostrar al usuario la ejecución del programa.
	 * @param args Argumentos para el comando y ejecución de interfaz gráfica.
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

    /** P2PKH estándar: válido si useFirmValid=true, inválido si false. */
    /**
     * Ejecución del interprete de las instrucciones de BitcoinScript con contraseña de usuario.
     * @param trace Permiso necesario para imprimir resultado de las operaciones almacenada en la memoria.
     * @param useValidSig Verifica si las contraseñas o firmas que usa el usuario son validas.
     */
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
    /**
     * Ejecución del interprete de las instrucciones de BitcoinScript usando una condicional simple.
     * @param trace Permiso necesario para imprimir resultado de las operaciones almacenada en la memoria.
     * @param condition Condicional para que el interpreta se ejecute.
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
    /**
     * Ejecución del interprete de las instrucciones de BitcoinScript usando una condicional anidada.
     * @param trace Permiso necesario para imprimir resultado de las operaciones almacenada en la memoria.
     */
    static void runNestedConditional(boolean trace) {
        ScriptInterpreter interpreter = new ScriptInterpreter();
        String script = "OP_1 OP_IF OP_1 OP_IF OP_1 OP_ELSE OP_0 OP_ENDIF OP_ELSE OP_0 OP_ENDIF";
        List<String> tokens = ScriptParser.parse(script);
        boolean result = interpreter.execute(tokens, trace);
        System.out.println("Resultado: " + result);
    }

    /**
     * Multisig 2-de-3: se necesitan 2 firmas válidas de 3 claves posibles.
     * Stack esperado (de abajo hacia arriba):
     *   OP_0 <firma1> <firma2> OP_2 <clave1> <clave2> <clave3> OP_3 OP_CHECKMULTISIG
     *
     * MockCrypto.checkSig valida si sig="valid" y pubKey="publicKeyN".
     * Con bothValid=true  -> firma1="valid"/clave1, firma2="valid"/clave2 -> 2 válidas -> true
     * Con bothValid=false -> firma1="valid"/clave1, firma2="INVALIDA"     -> 1 válida  -> false
     */
    /**
     * Ejecución de interprete usando dos firmas del usuario que deban ser validadas.
     * @param trace Permiso necesario para imprimir resultado de las operaciones almacenada en la memoria.
     * @param bothValid Las dos firmas que deban ser validadas.
     */
    static void runMultisig(boolean trace, boolean bothValid) {
        ScriptInterpreter interpreter = new ScriptInterpreter();

        String sig2 = bothValid ? "valid" : "INVALIDA";

        // Nota: MockCrypto.checkSig compara sig=="valid" && pubKey=="publicKey".
        // Para multisig simulamos que todas las claves se llaman "publicKey"
        // y las firmas válidas son "valid".
        String script = "OP_0 "                        // bug histórico
                + "valid " + sig2 + " "                // 2 firmas
                + "OP_2 "                              // nFirmas = 2
                + "publicKey publicKey publicKey "     // 3 claves
                + "OP_3 "                              // nClaves = 3
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