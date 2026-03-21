package com.script;

/**
 * Clase que tiene como función encargarse de manejar los errores principales en el programa.
 * @author Jeremhy López
 * @author Jonathan Cofiño
 * @author Henry Guzmán
 * @since 2026-03-19
 * @version 1.5
 */

/**
 * Excepción lanzada cuando el script de Bitcoin contiene
 * un error en tiempo de ejecución (stack vacío, opcode mal formado,
 * condicionales desbalanceados, etc.).
 */
public class ScriptException extends RuntimeException {

	/**
	 * Método para manejar un error dentro del programa e indicar cuál es.
	 * @param message Mensaje de error enviado por el programa.
	 */
    public ScriptException(String message) {
        super(message);
    }

    /**
     * Método para manejar un error dentro del programa, indicar cúal es y la causa.
     * @param message Mensaje de error enviado por el programa.
     * @param cause Indicación de porque se ha causado el error dentro del programa.
     */
    public ScriptException(String message, Throwable cause) {
        super(message, cause);
    }
}