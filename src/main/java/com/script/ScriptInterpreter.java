/**
 * @author Jeremhy López
 * @author Jonathan Cofiño
 * @author Henry Guzmán
 * @since 2026-03-19
 * @version 1.5
 * Clase que sirve como el interprete de las instrucciones de ScriptBitcoin
 */

package com.script;

import java.util.Arrays;
import java.util.List;




public class ScriptInterpreter {

	/**
	 * stack Es la pila que almacena las múltiples instrucciones a interpretar.
	 */
    private StackMachine stack = new StackMachine();

    /**
     * Método que se encarga de ejecutar y determinar cada una de las funciones de BitcoinScript.
     * @param script Lista de elementos que se usaran para ejecutar las operaciones, según se seleccione.
     * @param trace Valor que indica si hay que imprimir el resultado guardado en la memoria luego de realizar una operación.
     * @return El resultado obtenido al realizar una de las operaciones definidas eliminandolo de la memoria.
     */
    public boolean execute(List<String> script, boolean trace) {

        for (String token : script) {

            switch (token) {

                case "OP_0":
                    stack.push(new byte[]{0});
                    break;

                case "OP_1":
                    stack.push(new byte[]{1});
                    break;

                case "OP_2":
                    stack.push(new byte[]{2});
                    break;

                case "OP_3":
                    stack.push(new byte[]{3});
                    break;

                case "OP_DUP":
                    stack.push(stack.peek());
                    break;

                case "OP_DROP":
                    stack.pop();
                    break;

                case "OP_EQUAL":
                    byte[] a = stack.pop();
                    byte[] b = stack.pop();
                    boolean equals = Arrays.equals(a, b);
                    stack.push(equals ? new byte[]{1} : new byte[]{0});
                    break;

                case "OP_EQUALVERIFY":
                    byte[] x = stack.pop();
                    byte[] y = stack.pop();
                    if (!Arrays.equals(x, y)) {
                        return false;
                    }
                    break;

                case "OP_HASH160":
                    byte[] data = stack.pop();
                    stack.push(MockCrypto.hash160(data));
                    break;

                case "OP_CHECKSIG":
                    byte[] pubKey = stack.pop();
                    byte[] signature = stack.pop();
                    boolean valid = MockCrypto.checkSig(signature, pubKey);
                    stack.push(valid ? new byte[]{1} : new byte[]{0});
                    break;

                default:
                    try {
                        int number = Integer.parseInt(token);
                        stack.push(new byte[]{(byte) number});
                    } catch (NumberFormatException e) {
                        stack.push(token.getBytes());
                    }
            }

            if (trace) {
                stack.printStack();
            }
        }

        if (stack.isEmpty()) return false;

        byte[] result = stack.pop();
        return result.length > 0 && result[0] != 0;
    }
}
