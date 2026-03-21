package com.script;

import java.util.Arrays;
import java.util.List;

/**
 * Clase que cumple la unica función de transformar un texto al formato deseado (sin espacios y separado como se indica).
 * @author Jeremhy López
 * @author Jonathan Cofiño
 * @author Henry Guzmán
 * @since 2026-03-19
 * @version 1.5
 */

public class ScriptParser {

	/**
	 * Método que sirve para transformar un texto a un formato deseado (sin espacios y separado como se indica).
	 * @param script Es el texto al cual se le aplicara el formato.
	 * @return Devuelve el texto ya transformado.
	 */
    public static List<String> parse(String script) {
        return Arrays.asList(script.trim().split("\\s+"));
    }

}
