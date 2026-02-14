package com.script;

import java.util.List;

public class Main {

    public static void main(String[] args) {

        ScriptInterpreter interpreter = new ScriptInterpreter();

        // esto generam hash del publicKey
        byte[] hash = MockCrypto.hash160("publicKey".getBytes());
        String pubKeyHash = new String(hash);

        String script = "valid publicKey OP_DUP OP_HASH160 "
                + pubKeyHash + " OP_EQUALVERIFY OP_CHECKSIG";

        List<String> tokens = ScriptParser.parse(script);

        boolean result = interpreter.execute(tokens, true);

        System.out.println("resultado final: " + result);
    }
}
