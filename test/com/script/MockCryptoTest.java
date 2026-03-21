package com.script;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import java.security.MessageDigest;

class MockCryptoTest {

    @Test
    void testHash160() throws Exception {
        byte[] input = "hola".getBytes();

        // Resultado esperado usando directamente SHA-256
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        byte[] expected = digest.digest(input);

        byte[] result = MockCrypto.hash160(input);

        assertArrayEquals(expected, result, "El hash generado no coincide con SHA-256");
    }

    @Test
    void testCheckSigValid() {
        byte[] signature = "valid".getBytes();
        byte[] pubKey = "publicKey".getBytes();

        boolean result = MockCrypto.checkSig(signature, pubKey);

        assertTrue(result, "La firma debería ser válida");
    }

    @Test
    void testCheckSigInvalidSignature() {
        byte[] signature = "invalid".getBytes();
        byte[] pubKey = "publicKey".getBytes();

        boolean result = MockCrypto.checkSig(signature, pubKey);

        assertFalse(result, "La firma debería ser inválida");
    }

    @Test
    void testCheckSigInvalidPubKey() {
        byte[] signature = "valid".getBytes();
        byte[] pubKey = "wrongKey".getBytes();

        boolean result = MockCrypto.checkSig(signature, pubKey);

        assertFalse(result, "La clave pública debería ser inválida");
    }

    @Test
    void testCheckSigBothInvalid() {
        byte[] signature = "bad".getBytes();
        byte[] pubKey = "badKey".getBytes();

        boolean result = MockCrypto.checkSig(signature, pubKey);

        assertFalse(result, "Ambos valores inválidos deberían retornar false");
    }
}
