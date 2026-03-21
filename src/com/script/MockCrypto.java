package com.script;

import java.security.MessageDigest;

/**
 * Clase que tiene como función encargarse de la codificación de datos usando funciones hash simuladas.
 * En un entorno real, estas operaciones utilizarían librerías criptográficas externas.
 * Para este proyecto didáctico se implementan con {@link java.security.MessageDigest} de Java.
 * @author Jeremhy López
 * @author Jonathan Cofiño
 * @author Henry Guzmán
 * @since 2026-03-19
 * @version 2.0
 */
public class MockCrypto {

    /**
     * Simula OP_HASH160: en Bitcoin real aplica SHA-256 seguido de RIPEMD-160.
     * En esta implementación didáctica aplica únicamente SHA-256.
     * @param input Datos de entrada a hashear.
     * @return El hash resultante como arreglo de bytes.
     * @throws RuntimeException Si el algoritmo SHA-256 no está disponible.
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
     * Simula OP_SHA256: aplica una sola ronda de SHA-256 al input.
     * @param input Datos de entrada a hashear.
     * @return El hash SHA-256 como arreglo de bytes.
     * @throws RuntimeException Si el algoritmo SHA-256 no está disponible.
     */
    public static byte[] sha256(byte[] input) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            return digest.digest(input);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Simula OP_HASH256: aplica SHA-256 dos veces consecutivas al input (doble SHA-256).
     * Esta es la operación real que usa Bitcoin para el hash de transacciones y bloques.
     * @param input Datos de entrada a hashear.
     * @return El doble hash SHA-256 como arreglo de bytes.
     * @throws RuntimeException Si el algoritmo SHA-256 no está disponible.
     */
    public static byte[] hash256(byte[] input) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] firstHash = digest.digest(input);
            return digest.digest(firstHash);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Simula OP_CHECKSIG: verifica que una firma y clave pública sean válidas.
     * En Bitcoin real se verificaría una firma ECDSA contra la clave pública.
     * En esta implementación simulada, la firma es válida solo si signature=="valid"
     * y pubKey=="publicKey".
     * @param signature Firma digital del usuario (simulada).
     * @param pubKey Clave pública del usuario (simulada).
     * @return true si ambos valores coinciden con los esperados, false en caso contrario.
     */
    public static boolean checkSig(byte[] signature, byte[] pubKey) {
        return new String(signature).equals("valid")
                && new String(pubKey).equals("publicKey");
    }
}