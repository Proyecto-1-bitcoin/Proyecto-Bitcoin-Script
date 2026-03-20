package com.script;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class StackMachineTest {
	@Test
	void testPushAndPop() {
		StackMachine stack = new StackMachine();
		byte[] data = "hola".getBytes();
		stack.push(data);
		byte[] result = stack.pop();
		assertArrayEquals(data, result, "El dato extraído debe ser igual al insertado");
		assertTrue(stack.isEmpty(), "La pila debería estar vacía después del pop");
	}
	
	@Test
	void testPeek() {
		StackMachine stack = new StackMachine();
		byte[] data = "test".getBytes();
		stack.push(data);
		byte[] result = stack.peek();
		assertArrayEquals(data, result, "Peek debe devolver el último elemento sin eliminarlo");
		assertFalse(stack.isEmpty(), "La pila no debería estar vacía después de peek");
	}
	
	@Test
	void testSize() {
		StackMachine stack = new StackMachine();
		stack.push("a".getBytes());
		stack.push("b".getBytes());
		assertEquals(2, stack.size(), "El tamaño debería ser 2");
	}
	
	@Test
	void testIsEmpty() {
		StackMachine stack = new StackMachine();
		assertTrue(stack.isEmpty(), "La pila debe iniciar vacía");
		stack.push("data".getBytes());
		assertFalse(stack.isEmpty(), "La pila no debe estar vacía después de push");
	}
	
	@Test
	void testPopEmptyStack() {
		StackMachine stack = new StackMachine();
		RuntimeException exception = assertThrows(RuntimeException.class, stack::pop);
		assertEquals("Stack underflow", exception.getMessage());
	}
	
	@Test void testPeekEmptyStack() {
		StackMachine stack = new StackMachine();
		RuntimeException exception = assertThrows(RuntimeException.class, stack::peek);
		assertEquals("Stack empty", exception.getMessage());
	}
	
	@Test
	void testMultiplePushPopOrder() {
		StackMachine stack = new StackMachine();
		byte[] first = "uno".getBytes();
		byte[] second = "dos".getBytes();
		stack.push(first);
		stack.push(second);
		assertArrayEquals(second, stack.pop(), "Debe salir el último en entrar (LIFO)");
		assertArrayEquals(first, stack.pop(), "Luego debe salir el primero");
	}
}