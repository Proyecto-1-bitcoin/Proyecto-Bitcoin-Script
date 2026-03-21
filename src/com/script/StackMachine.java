package com.script;

import java.util.ArrayDeque;
import java.util.Deque;

/**
 * Clase que determina la memoria del programa y sus operaciones básicas.
 * @author Jeremhy López
 * @author Jonathan Cofiño
 * @author Henry Guzmán
 * @since 2026-03-19
 * @version 1.5
 */

public class StackMachine {

	/**
	 * Es la memoria, en esta caso se usa la estructura ArrayDeque por conveniencia.
	 */
    private Deque<byte[]> stack = new ArrayDeque<>();

    /**
     * Método que sube el dato determinado a la memoria.
     * @param data Es el dato que se sube a la memoria.
     */
    public void push(byte[] data) {
        stack.push(data);

    }

    /**
     * Método que elimina el último elemento ingresado a la memoria.
     * @return Devuelve este mismo elemento a eliminar.
     * @throws RuntimeException Ocurre si la memoria se encuentra vacia.
     */
    public byte[] pop() {
        if (stack.isEmpty()) {
            throw new RuntimeException("Stack underflow");
        }
        return stack.pop();
    }

    /**
     * Método que muestra el último elemento almacenado en la memoria.
     * @return Devuelve lo que último guardado que posee la memoria (lo muestra).
     * @throws RuntimeException Ocurre si la memoria se encuentra vacia.
     */
    public byte[] peek() {
        if (stack.isEmpty()) {
            throw new RuntimeException("Stack empty");
        }
        return stack.peek();
    }

    /**
     * Método que elimina a todos los elementos de la memoria.
     * @return Devuelve la memoria vacia.
     */
    public boolean isEmpty() {
        return stack.isEmpty();
    }

    /**
     * Imprime cada uno de los elementos que posee la memoria, uno por uno.
     */
    public void printStack() {
        System.out.print("STACK: ");
        for (byte[] item : stack) {
            if (item.length == 1) {
                System.out.print(item[0] + " ");
            } else {
                System.out.print(new String(item) + " ");
            }
        }
        System.out.println();
    }
    
    /**
     * Método que devuelve el valor del número de elementos que posee la pila.
     * @return El número de elementos en la pila.
     */
    public int size() {
        return stack.size();
    }
}
