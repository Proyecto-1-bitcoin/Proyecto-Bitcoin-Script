package com.script;

import java.util.ArrayDeque;
import java.util.Deque;

public class StackMachine {

    private Deque<byte[]> stack = new ArrayDeque<>();

    public void push(byte[] data) {
        stack.push(data);
        
    }

    public byte[] pop() {
        if (stack.isEmpty()) {
            throw new RuntimeException("Stack underflow");
        }
        return stack.pop();
    }

    public byte[] peek() {
        if (stack.isEmpty()) {
            throw new RuntimeException("Stack empty");
        }
        return stack.peek();
    }

    public boolean isEmpty() {
        return stack.isEmpty();
    }

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
}
