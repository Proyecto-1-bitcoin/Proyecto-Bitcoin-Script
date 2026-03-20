/**
 * @author Jeremhy López
 * @author Jonathan Cofiño
 * @author Henry Guzmán
 * @since 2026-03-19
 * @version 1.5
 * Es la clase principal que servira como vista, es la interfaz gráfica que permite que el usuario interactue con el programa.
 */

package com.script;

import java.util.List;

public class Main {

	/**
	 * Método para mostrar al usuario la ejecución del programa.
	 * @param args Argumentos para el comando y ejecución de interfaz gráfica.
	 */
    public static void main(String[] args) {

        ScriptInterpreter interpreter = new ScriptInterpreter();

        // esto genera hash del publicKey
        byte[] hash = MockCrypto.hash160("publicKey".getBytes());
        String pubKeyHash = new String(hash);

        String script = "valid publicKey OP_DUP OP_HASH160 "
                + pubKeyHash + " OP_EQUALVERIFY OP_CHECKSIG";

        List<String> tokens = ScriptParser.parse(script);

        boolean result = interpreter.execute(tokens, true);

        System.out.println("resultado final: " + result);
    }
}
