package main.java.com.script;

/**
 * Excepción lanzada cuando el script de Bitcoin contiene
 * un error en tiempo de ejecución (stack vacío, opcode mal formado,
 * condicionales desbalanceados, etc.).
 */
public class ScriptException extends RuntimeException {

    public ScriptException(String message) {
        super(message);
    }

    public ScriptException(String message, Throwable cause) {
        super(message, cause);
    }
}