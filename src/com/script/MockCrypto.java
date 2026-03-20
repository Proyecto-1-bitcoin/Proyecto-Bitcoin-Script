package com.script;

import java.security.MessageDigest;

/**
 * Clase que tiene como función encargarse de la codificación y descodificación de datos usando hash.
 * @author Jeremhy López
 * @author Jonathan Cofiño
 * @author Henry Guzmán
 * @since 2026-03-19
 * @version 1.5
 */

public class MockCrypto {

	/**
	 * Se encarga de transformar una lista de datos en un mensaje codificado para ocultarlo. En este caso usa SHA-256.
	 * @param input Es la lista de datos que se codificaran (ingresada por usuario).
	 * @return El mensaje ya codificado en hash160.
	 * @throws RuntimeException Ocurre si los datos no estan en el formato correcto o necesario.
	 */
    public static byte[] hash160(byte[] input) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            return digest.digest(input);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Método que se encarga de verificar que la firma o contraseña ingresada por el usuario sea valida.
     * @param signature Contraseña o firma que solo conoce el usuario y debe ser validada.
     * @param pubKey Contraseña o firma pública que sirve para reconocer al usuario y debe ser mostrada.
     * @return Ambas firmas o contraseñas (la privada y la pública) que el usuario puede usar.
     */
    public static boolean checkSig(byte[] signature, byte[] pubKey) {
        return new String(signature).equals("valid")
                && new String(pubKey).equals("publicKey");
    }
}
