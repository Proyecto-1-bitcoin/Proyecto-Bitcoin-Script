package com.script;

import java.util.ArrayDeque;
import java.util.Arrays;
import java.util.Deque;
import java.util.List;

/**
 * Clase que sirve como el interprete de las instrucciones de ScriptBitcoin.
 * Implementa un subconjunto completo de Bitcoin Script, incluyendo operaciones
 * de pila, lógica, aritmética, control de flujo y criptografía simulada.
 * @author Jeremhy López
 * @author Jonathan Cofiño
 * @author Henry Guzmán
 * @since 2026-03-19
 * @version 2.0
 */
public class ScriptInterpreter {

    /**
     * stack Es la pila principal que almacena los datos durante la ejecución del script.
     * Se utiliza {@link StackMachine} que internamente usa {@link java.util.ArrayDeque},
     * elegida por su complejidad O(1) en push/pop/peek y uso eficiente de memoria.
     */
    private StackMachine stack = new StackMachine();

    /**
     * Pila de ejecución condicional (condStack).
     * Cada OP_IF/OP_NOTIF apila un booleano; OP_ENDIF lo saca.
     * Solo se ejecuta un opcode si todos los valores son true.
     * Se usa {@link java.util.ArrayDeque} como {@link java.util.Deque} por O(1) en push/pop.
     */
    private Deque<Boolean> condStack = new ArrayDeque<>();

    /**
     * Se encarga de retornar true si estamos en una rama activa (no ignorada).
     * @return Devuelve true si toda la pila condicional tiene valores verdaderos.
     */
    private boolean isExecuting() {
        for (boolean b : condStack) {
            if (!b) return false;
        }
        return true;
    }

    /**
     * Versión de execute que acepta tokens mixtos: String (opcodes/literales) y byte[] (datos binarios).
     * Esto evita la corrupción de bytes al pasar hashes SHA-256 directamente sin convertirlos a String.
     * Usado para la demo P2PKH donde el pubKeyHash es un byte[] producido por MockCrypto.
     * @param tokens Lista de objetos: String para opcodes/literales, byte[] para datos binarios directos.
     * @param trace Si true, imprime el estado de la pila tras cada instrucción.
     * @return true si la ejecución es válida (tope de pila != 0), false en caso contrario.
     * @throws ScriptException Si ocurre un error durante la ejecución.
     */
    public boolean executeWithData(List<Object> tokens, boolean trace) {
        for (Object obj : tokens) {
            if (obj instanceof byte[]) {
                if (isExecuting()) {
                    stack.push((byte[]) obj);
                }
                if (trace) {
                    System.out.println("[TRACE] token: <binary data>");
                    stack.printStack();
                    System.out.println("  condStack: " + condStack);
                }
            } else {
                processToken((String) obj, trace);
            }
        }
        if (!condStack.isEmpty()) throw new ScriptException("Script inválido: OP_IF sin OP_ENDIF");
        if (stack.isEmpty()) return false;
        byte[] result = stack.pop();
        return result.length > 0 && result[0] != 0;
    }

    /**
     * Método que se encarga de ejecutar y determinar cada una de las instrucciones de BitcoinScript.
     * El script se procesa de izquierda a derecha. La ejecución es válida si termina
     * con un valor verdadero en la cima de la pila y ninguna instrucción falla.
     * @param script Lista de tokens (instrucciones y datos) a ejecutar.
     * @param trace Si es true, imprime el estado de la pila tras cada instrucción.
     * @return true si la ejecución es válida (tope de pila != 0), false en caso contrario.
     * @throws ScriptException Si ocurre un error durante la ejecución del script.
     */
    public boolean execute(List<String> script, boolean trace) {
        for (String token : script) {
            processToken(token, trace);
        }
        if (!condStack.isEmpty()) throw new ScriptException("Script inválido: OP_IF sin OP_ENDIF");
        if (stack.isEmpty()) return false;
        byte[] result = stack.pop();
        return result.length > 0 && result[0] != 0;
    }

    /**
     * Procesa un único token de texto: opcode o dato literal.
     * Método interno compartido entre execute() y executeWithData().
     * @param token El token a procesar (opcode o dato literal).
     * @param trace Si true, imprime estado de la pila tras procesar el token.
     */
    private void processToken(String token, boolean trace) {

        // OP_IF, OP_NOTIF, OP_ELSE y OP_ENDIF se procesan SIEMPRE,
        // incluso dentro de ramas ignoradas, para mantener condStack balanceado.
        switch (token) {

            case "OP_IF": {
                if (isExecuting()) {
                    if (stack.isEmpty()) throw new ScriptException("OP_IF: stack vacío");
                    byte[] top = stack.pop();
                    condStack.push(top.length > 0 && top[0] != 0);
                } else {
                    condStack.push(false);
                }
                break;
            }

            case "OP_NOTIF": {
                if (isExecuting()) {
                    if (stack.isEmpty()) throw new ScriptException("OP_NOTIF: stack vacío");
                    byte[] top = stack.pop();
                    condStack.push(!(top.length > 0 && top[0] != 0));
                } else {
                    condStack.push(false);
                }
                break;
            }

            case "OP_ELSE": {
                if (condStack.isEmpty()) throw new ScriptException("OP_ELSE: sin OP_IF correspondiente");
                boolean parentActive = true;
                Boolean[] arr = condStack.stream()
                        .mapToInt(b -> b ? 1 : 0)
                        .mapToObj(i -> i == 1)
                        .toArray(Boolean[]::new);
                for (int i = 1; i < arr.length; i++) {
                    if (!arr[i]) { parentActive = false; break; }
                }
                boolean current = condStack.pop();
                condStack.push(parentActive && !current);
                break;
            }

            case "OP_ENDIF": {
                if (condStack.isEmpty()) throw new ScriptException("OP_ENDIF: sin OP_IF correspondiente");
                condStack.pop();
                break;
            }

            default: {
                if (!isExecuting()) break;

                switch (token) {

                    // -----------------------------------------------
                    // Literales / push de datos
                    // -----------------------------------------------
                    case "OP_FALSE":
                    case "OP_0":  stack.push(new byte[]{0});  break;
                    case "OP_1":  stack.push(new byte[]{1});  break;
                    case "OP_2":  stack.push(new byte[]{2});  break;
                    case "OP_3":  stack.push(new byte[]{3});  break;
                    case "OP_4":  stack.push(new byte[]{4});  break;
                    case "OP_5":  stack.push(new byte[]{5});  break;
                    case "OP_6":  stack.push(new byte[]{6});  break;
                    case "OP_7":  stack.push(new byte[]{7});  break;
                    case "OP_8":  stack.push(new byte[]{8});  break;
                    case "OP_9":  stack.push(new byte[]{9});  break;
                    case "OP_10": stack.push(new byte[]{10}); break;
                    case "OP_11": stack.push(new byte[]{11}); break;
                    case "OP_12": stack.push(new byte[]{12}); break;
                    case "OP_13": stack.push(new byte[]{13}); break;
                    case "OP_14": stack.push(new byte[]{14}); break;
                    case "OP_15": stack.push(new byte[]{15}); break;
                    case "OP_16": stack.push(new byte[]{16}); break;

                    // -----------------------------------------------
                    // Operaciones de pila
                    // -----------------------------------------------

                    /** OP_DUP: duplica el elemento en la cima de la pila. */
                    case "OP_DUP":
                        if (stack.isEmpty()) throw new ScriptException("OP_DUP: stack vacío");
                        stack.push(stack.peek());
                        break;

                    /** OP_DROP: elimina el elemento en la cima de la pila. */
                    case "OP_DROP":
                        if (stack.isEmpty()) throw new ScriptException("OP_DROP: stack vacío");
                        stack.pop();
                        break;

                    /** OP_SWAP: intercambia los dos elementos superiores de la pila. */
                    case "OP_SWAP": {
                        if (stack.size() < 2) throw new ScriptException("OP_SWAP: se necesitan 2 elementos");
                        byte[] top    = stack.pop();
                        byte[] second = stack.pop();
                        stack.push(top);
                        stack.push(second);
                        break;
                    }

                    /**
                     * OP_OVER: copia el segundo elemento al tope.
                     * Antes: [a, b(tope)] → Después: [a, b, a]
                     */
                    case "OP_OVER": {
                        if (stack.size() < 2) throw new ScriptException("OP_OVER: se necesitan 2 elementos");
                        byte[] topO    = stack.pop();
                        byte[] secondO = stack.peek();
                        stack.push(topO);
                        stack.push(secondO);
                        break;
                    }

                    // -----------------------------------------------
                    // Lógica y comparación
                    // -----------------------------------------------

                    /** OP_EQUAL: empuja 1 si los dos topes son iguales byte a byte, 0 si no. */
                    case "OP_EQUAL": {
                        if (stack.size() < 2) throw new ScriptException("OP_EQUAL: se necesitan 2 elementos");
                        byte[] a = stack.pop();
                        byte[] b = stack.pop();
                        stack.push(Arrays.equals(a, b) ? new byte[]{1} : new byte[]{0});
                        break;
                    }

                    /** OP_EQUALVERIFY: igual que OP_EQUAL pero lanza excepción si no son iguales. */
                    case "OP_EQUALVERIFY": {
                        if (stack.size() < 2) throw new ScriptException("OP_EQUALVERIFY: se necesitan 2 elementos");
                        byte[] x = stack.pop();
                        byte[] y = stack.pop();
                        if (!Arrays.equals(x, y)) throw new ScriptException("OP_EQUALVERIFY: los valores no son iguales");
                        break;
                    }

                    /** OP_NOT: invierte el booleano del tope. 0→1, cualquier otro→0. */
                    case "OP_NOT": {
                        if (stack.isEmpty()) throw new ScriptException("OP_NOT: stack vacío");
                        byte[] val = stack.pop();
                        stack.push((val.length == 0 || val[0] == 0) ? new byte[]{1} : new byte[]{0});
                        break;
                    }

                    /** OP_BOOLAND: AND lógico de los dos topes. Empuja 1 si ambos son != 0. */
                    case "OP_BOOLAND": {
                        if (stack.size() < 2) throw new ScriptException("OP_BOOLAND: se necesitan 2 elementos");
                        byte[] b2 = stack.pop();
                        byte[] b1 = stack.pop();
                        boolean r1 = b1.length > 0 && b1[0] != 0;
                        boolean r2 = b2.length > 0 && b2[0] != 0;
                        stack.push((r1 && r2) ? new byte[]{1} : new byte[]{0});
                        break;
                    }

                    /** OP_BOOLOR: OR lógico de los dos topes. Empuja 1 si al menos uno es != 0. */
                    case "OP_BOOLOR": {
                        if (stack.size() < 2) throw new ScriptException("OP_BOOLOR: se necesitan 2 elementos");
                        byte[] bo2 = stack.pop();
                        byte[] bo1 = stack.pop();
                        boolean ro1 = bo1.length > 0 && bo1[0] != 0;
                        boolean ro2 = bo2.length > 0 && bo2[0] != 0;
                        stack.push((ro1 || ro2) ? new byte[]{1} : new byte[]{0});
                        break;
                    }

                    // -----------------------------------------------
                    // Aritmética básica
                    // -----------------------------------------------

                    /** OP_ADD: suma los dos valores superiores y empuja el resultado. */
                    case "OP_ADD": {
                        if (stack.size() < 2) throw new ScriptException("OP_ADD: se necesitan 2 elementos");
                        int n2 = stack.pop()[0];
                        int n1 = stack.pop()[0];
                        stack.push(new byte[]{(byte)(n1 + n2)});
                        break;
                    }

                    /** OP_SUB: resta el tope al segundo (segundo - tope) y empuja el resultado. */
                    case "OP_SUB": {
                        if (stack.size() < 2) throw new ScriptException("OP_SUB: se necesitan 2 elementos");
                        int s2 = stack.pop()[0];
                        int s1 = stack.pop()[0];
                        stack.push(new byte[]{(byte)(s1 - s2)});
                        break;
                    }

                    /** OP_NUMEQUALVERIFY: verifica igualdad numérica, lanza excepción si no son iguales. */
                    case "OP_NUMEQUALVERIFY": {
                        if (stack.size() < 2) throw new ScriptException("OP_NUMEQUALVERIFY: se necesitan 2 elementos");
                        int nv2 = stack.pop()[0];
                        int nv1 = stack.pop()[0];
                        if (nv1 != nv2) throw new ScriptException("OP_NUMEQUALVERIFY: los valores no son iguales");
                        break;
                    }

                    /** OP_LESSTHAN: empuja 1 si segundo &lt; tope. */
                    case "OP_LESSTHAN": {
                        if (stack.size() < 2) throw new ScriptException("OP_LESSTHAN: se necesitan 2 elementos");
                        int lt2 = stack.pop()[0];
                        int lt1 = stack.pop()[0];
                        stack.push(lt1 < lt2 ? new byte[]{1} : new byte[]{0});
                        break;
                    }

                    /** OP_GREATERTHAN: empuja 1 si segundo &gt; tope. */
                    case "OP_GREATERTHAN": {
                        if (stack.size() < 2) throw new ScriptException("OP_GREATERTHAN: se necesitan 2 elementos");
                        int gt2 = stack.pop()[0];
                        int gt1 = stack.pop()[0];
                        stack.push(gt1 > gt2 ? new byte[]{1} : new byte[]{0});
                        break;
                    }

                    /** OP_LESSTHANOREQUAL: empuja 1 si segundo &lt;= tope. */
                    case "OP_LESSTHANOREQUAL": {
                        if (stack.size() < 2) throw new ScriptException("OP_LESSTHANOREQUAL: se necesitan 2 elementos");
                        int lte2 = stack.pop()[0];
                        int lte1 = stack.pop()[0];
                        stack.push(lte1 <= lte2 ? new byte[]{1} : new byte[]{0});
                        break;
                    }

                    /** OP_GREATERTHANOREQUAL: empuja 1 si segundo &gt;= tope. */
                    case "OP_GREATERTHANOREQUAL": {
                        if (stack.size() < 2) throw new ScriptException("OP_GREATERTHANOREQUAL: se necesitan 2 elementos");
                        int gte2 = stack.pop()[0];
                        int gte1 = stack.pop()[0];
                        stack.push(gte1 >= gte2 ? new byte[]{1} : new byte[]{0});
                        break;
                    }

                    // -----------------------------------------------
                    // Control de flujo adicional
                    // -----------------------------------------------

                    /** OP_VERIFY: falla si el tope es falso (0 o vacío). No empuja resultado. */
                    case "OP_VERIFY": {
                        if (stack.isEmpty()) throw new ScriptException("OP_VERIFY: stack vacío");
                        byte[] topV = stack.pop();
                        if (topV.length == 0 || topV[0] == 0)
                            throw new ScriptException("OP_VERIFY: falló (valor falso)");
                        break;
                    }

                    /** OP_RETURN: termina la ejecución con error (script inválido). */
                    case "OP_RETURN":
                        throw new ScriptException("OP_RETURN: script terminado con error");

                    // -----------------------------------------------
                    // Criptográficas (simuladas con MockCrypto)
                    // -----------------------------------------------

                    /** OP_HASH160: aplica SHA-256 al tope (simulado). */
                    case "OP_HASH160": {
                        if (stack.isEmpty()) throw new ScriptException("OP_HASH160: stack vacío");
                        stack.push(MockCrypto.hash160(stack.pop()));
                        break;
                    }

                    /** OP_SHA256: aplica SHA-256 al tope. */
                    case "OP_SHA256": {
                        if (stack.isEmpty()) throw new ScriptException("OP_SHA256: stack vacío");
                        stack.push(MockCrypto.sha256(stack.pop()));
                        break;
                    }

                    /** OP_HASH256: aplica SHA-256 dos veces al tope (doble SHA-256). */
                    case "OP_HASH256": {
                        if (stack.isEmpty()) throw new ScriptException("OP_HASH256: stack vacío");
                        stack.push(MockCrypto.hash256(stack.pop()));
                        break;
                    }

                    // -----------------------------------------------
                    // Firmas (simuladas con MockCrypto)
                    // -----------------------------------------------

                    /**
                     * OP_CHECKSIG: verifica firma contra clave pública.
                     * Empuja 1 si es válida, 0 si no.
                     * Orden: [firma, pubKey(tope)]
                     */
                    case "OP_CHECKSIG": {
                        if (stack.size() < 2) throw new ScriptException("OP_CHECKSIG: se necesitan 2 elementos");
                        byte[] pubKey    = stack.pop();
                        byte[] signature = stack.pop();
                        stack.push(MockCrypto.checkSig(signature, pubKey) ? new byte[]{1} : new byte[]{0});
                        break;
                    }

                    /** OP_CHECKSIGVERIFY: igual que OP_CHECKSIG pero lanza excepción si la firma es inválida. */
                    case "OP_CHECKSIGVERIFY": {
                        if (stack.size() < 2) throw new ScriptException("OP_CHECKSIGVERIFY: se necesitan 2 elementos");
                        byte[] pubKeyCV = stack.pop();
                        byte[] sigCV    = stack.pop();
                        if (!MockCrypto.checkSig(sigCV, pubKeyCV))
                            throw new ScriptException("OP_CHECKSIGVERIFY: firma inválida");
                        break;
                    }

                    // -----------------------------------------------
                    // OP_CHECKMULTISIG (avanzado, opcional)
                    // -----------------------------------------------

                    /**
                     * OP_CHECKMULTISIG: verifica M-de-N firmas.
                     * Incluye el bug histórico de Bitcoin: consume un elemento extra (OP_0) del fondo.
                     * Orden esperado en pila: OP_0 firma1..firmaM OP_M clave1..claveN OP_N
                     */
                    case "OP_CHECKMULTISIG": {
                        if (stack.isEmpty()) throw new ScriptException("OP_CHECKMULTISIG: falta nClaves");
                        int nKeys = stack.pop()[0];
                        if (nKeys < 0 || nKeys > 20) throw new ScriptException("OP_CHECKMULTISIG: nClaves inválido");
                        if (stack.size() < nKeys) throw new ScriptException("OP_CHECKMULTISIG: no hay suficientes claves");
                        byte[][] pubKeys = new byte[nKeys][];
                        for (int i = 0; i < nKeys; i++) pubKeys[i] = stack.pop();

                        if (stack.isEmpty()) throw new ScriptException("OP_CHECKMULTISIG: falta nFirmas");
                        int nSigs = stack.pop()[0];
                        if (nSigs < 0 || nSigs > nKeys) throw new ScriptException("OP_CHECKMULTISIG: nFirmas inválido");
                        if (stack.size() < nSigs) throw new ScriptException("OP_CHECKMULTISIG: no hay suficientes firmas");
                        byte[][] sigs = new byte[nSigs][];
                        for (int i = 0; i < nSigs; i++) sigs[i] = stack.pop();

                        // Bug histórico: consumir elemento extra
                        if (stack.isEmpty()) throw new ScriptException("OP_CHECKMULTISIG: falta el elemento extra (bug histórico)");
                        stack.pop();

                        int keyIndex = 0, validSigs = 0;
                        for (byte[] sig : sigs) {
                            while (keyIndex < nKeys) {
                                if (MockCrypto.checkSig(sig, pubKeys[keyIndex])) {
                                    validSigs++;
                                    keyIndex++;
                                    break;
                                }
                                keyIndex++;
                            }
                        }
                        stack.push(validSigs >= nSigs ? new byte[]{1} : new byte[]{0});
                        break;
                    }

                    // -----------------------------------------------
                    // Datos literales (números enteros y strings)
                    // -----------------------------------------------
                    default:
                        try {
                            int number = Integer.parseInt(token);
                            stack.push(new byte[]{(byte) number});
                        } catch (NumberFormatException e) {
                            stack.push(token.getBytes());
                        }
                }
            }
        }

        if (trace) {
            System.out.println("[TRACE] token: " + token);
            stack.printStack();
            System.out.println("  condStack: " + condStack);
        }
    }
}