package com.script;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class ScriptExceptionTest {

    @Test
    void testConstructorWithMessage() {
        String message = "Error de prueba";

        ScriptException exception = new ScriptException(message);

        assertEquals(message, exception.getMessage(), "El mensaje no coincide");
        assertNull(exception.getCause(), "La causa debería ser null");
    }

    @Test
    void testConstructorWithMessageAndCause() {
        String message = "Error con causa";
        Throwable cause = new RuntimeException("Causa original");

        ScriptException exception = new ScriptException(message, cause);

        assertEquals(message, exception.getMessage(), "El mensaje no coincide");
        assertEquals(cause, exception.getCause(), "La causa no coincide");
    }
}

