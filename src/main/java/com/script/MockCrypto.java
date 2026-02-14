package com.script;

import java.security.MessageDigest;

public class MockCrypto {

    public static byte[] hash160(byte[] input) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            return digest.digest(input);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static boolean checkSig(byte[] signature, byte[] pubKey) {
        return new String(signature).equals("valid")
                && new String(pubKey).equals("publicKey");
    }
}
