package com.script;

import java.util.ArrayDeque;
import java.util.Arrays;
import java.util.Deque;
import java.util.List;

public class ScriptInterpreter {

    private StackMachine stack = new StackMachine();

    /**
     * Pila de ejecución condicional.
     * Cada OP_IF apila un booleano; OP_ENDIF lo saca.
     * Solo ejecutamos un opcode si todos los valores son true.
     */
    private Deque<Boolean> condStack = new ArrayDeque<>();

    /** Retorna true si estamos en una rama activa (no ignorada). */
    private boolean isExecuting() {
        for (boolean b : condStack) {
            if (!b) return false;
        }
        return true;
    }

    public boolean execute(List<String> script, boolean trace) {

        for (String token : script) {

            // OP_IF, OP_ELSE y OP_ENDIF se procesan SIEMPRE,
            // incluso si estamos dentro de una rama ignorada,
            // para mantener la pila condicional balanceada.
            switch (token) {

                case "OP_IF": {
                    if (isExecuting()) {
                        if (stack.isEmpty()) {
                            throw new ScriptException("OP_IF: stack vacío");
                        }
                        byte[] top = stack.pop();
                        boolean condition = top.length > 0 && top[0] != 0;
                        condStack.push(condition);
                    } else {
                        // Rama ignorada: apilamos false para mantener balance
                        condStack.push(false);
                    }
                    break;
                }

                case "OP_ELSE": {
                    if (condStack.isEmpty()) {
                        throw new ScriptException("OP_ELSE: sin OP_IF correspondiente");
                    }
                    // Solo invertimos si la rama "padre" está activa.
                    // Para eso revisamos si todo lo que está DEBAJO del tope es true.
                    boolean parentActive = true;
                    boolean[] arr = condStack.stream()
                            .mapToInt(b -> b ? 1 : 0)
                            .mapToObj(i -> i == 1)
                            .toArray(Boolean[]::new);
                    // arr[0] es el tope (el actual OP_IF), arr[1..] son los padres
                    for (int i = 1; i < arr.length; i++) {
                        if (!arr[i]) { parentActive = false; break; }
                    }
                    boolean current = condStack.pop();
                    condStack.push(parentActive && !current);
                    break;
                }

                case "OP_ENDIF": {
                    if (condStack.isEmpty()) {
                        throw new ScriptException("OP_ENDIF: sin OP_IF correspondiente");
                    }
                    condStack.pop();
                    break;
                }

                default: {
                    // Si estamos en una rama ignorada, saltamos el token
                    if (!isExecuting()) break;

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
                            if (stack.isEmpty()) throw new ScriptException("OP_DUP: stack vacío");
                            stack.push(stack.peek());
                            break;

                        case "OP_DROP":
                            if (stack.isEmpty()) throw new ScriptException("OP_DROP: stack vacío");
                            stack.pop();
                            break;

                        case "OP_EQUAL": {
                            if (stack.size() < 2) throw new ScriptException("OP_EQUAL: se necesitan 2 elementos");
                            byte[] a = stack.pop();
                            byte[] b = stack.pop();
                            stack.push(Arrays.equals(a, b) ? new byte[]{1} : new byte[]{0});
                            break;
                        }

                        case "OP_EQUALVERIFY": {
                            if (stack.size() < 2) throw new ScriptException("OP_EQUALVERIFY: se necesitan 2 elementos");
                            byte[] x = stack.pop();
                            byte[] y = stack.pop();
                            if (!Arrays.equals(x, y)) {
                                throw new ScriptException("OP_EQUALVERIFY: los valores no son iguales");
                            }
                            break;
                        }

                        case "OP_HASH160": {
                            if (stack.isEmpty()) throw new ScriptException("OP_HASH160: stack vacío");
                            byte[] data = stack.pop();
                            stack.push(MockCrypto.hash160(data));
                            break;
                        }

                        case "OP_CHECKSIG": {
                            if (stack.size() < 2) throw new ScriptException("OP_CHECKSIG: se necesitan 2 elementos");
                            byte[] pubKey = stack.pop();
                            byte[] signature = stack.pop();
                            boolean valid = MockCrypto.checkSig(signature, pubKey);
                            stack.push(valid ? new byte[]{1} : new byte[]{0});
                            break;
                        }

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

        // Al final no deben quedar OP_IF sin cerrar
        if (!condStack.isEmpty()) {
            throw new ScriptException("Script inválido: OP_IF sin OP_ENDIF");
        }

        if (stack.isEmpty()) return false;

        byte[] result = stack.pop();
        return result.length > 0 && result[0] != 0;
    }
}