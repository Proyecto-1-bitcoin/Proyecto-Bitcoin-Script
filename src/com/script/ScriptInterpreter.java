package com.script;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

class ScriptInterpreterTest {

    private ScriptInterpreter interpreter;

    @BeforeEach
    void setUp() {
        interpreter = new ScriptInterpreter();
    }

    // =========================================================
    // OP_DUP
    // =========================================================

    @Test
    void opDup_duplicaElTope() {
        var tokens = ScriptParser.parse("hello OP_DUP OP_EQUAL");
        assertTrue(interpreter.execute(tokens, false));
    }

    @Test
    void opDup_stackVacio_lanzaExcepcion() {
        var tokens = ScriptParser.parse("OP_DUP");
        assertThrows(ScriptException.class, () -> interpreter.execute(tokens, false));
    }

    // =========================================================
    // OP_DROP
    // =========================================================

    @Test
    void opDrop_eliminaElTope() {
        var tokens = ScriptParser.parse("OP_1 OP_1 OP_DROP");
        assertTrue(interpreter.execute(tokens, false));
    }

    @Test
    void opDrop_stackVacio_lanzaExcepcion() {
        var tokens = ScriptParser.parse("OP_DROP");
        assertThrows(ScriptException.class, () -> interpreter.execute(tokens, false));
    }

    // =========================================================
    // OP_SWAP
    // =========================================================

    @Test
    void opSwap_intercambiaLosDosElementos() {
        // Stack: [OP_1(fondo), OP_2(tope)] -> SWAP -> [OP_2(fondo), OP_1(tope)]
        // DROP elimina OP_1 -> queda OP_2 -> true
        var tokens = ScriptParser.parse("OP_1 OP_2 OP_SWAP OP_DROP");
        // Después del SWAP el tope es OP_1, lo dropeamos, queda OP_2 = true
        assertTrue(interpreter.execute(tokens, false));
    }

    @Test
    void opSwap_verificaOrdenCorrecto() {
        // Stack antes: [OP_1(fondo), OP_2(tope)]
        // Después SWAP: [OP_2(fondo), OP_1(tope)]
        // Después DROP: queda OP_2 -> true (no false)
        // Para obtener false: pusheamos OP_0 arriba, hacemos SWAP, el tope pasa a ser OP_1 -> true
        // Mejor test: OP_0 OP_1 OP_SWAP -> tope es OP_0 -> false
        var tokens = ScriptParser.parse("OP_1 OP_0 OP_SWAP OP_DROP");
        // Después SWAP el tope es OP_1, drop -> queda OP_0 -> false
        assertFalse(interpreter.execute(tokens, false));
    }

    @Test
    void opSwap_insuficientesElementos_lanzaExcepcion() {
        var tokens = ScriptParser.parse("OP_1 OP_SWAP");
        assertThrows(ScriptException.class, () -> interpreter.execute(tokens, false));
    }

    // =========================================================
    // OP_OVER
    // =========================================================

    @Test
    void opOver_copiaPrimerElementoAlTope() {
        // Stack: [OP_1(fondo), OP_2(tope)] -> OVER -> [OP_1, OP_2, OP_1(tope)]
        // DROP DROP -> queda OP_1 -> true
        var tokens = ScriptParser.parse("OP_1 OP_2 OP_OVER OP_DROP OP_DROP");
        assertTrue(interpreter.execute(tokens, false));
    }

    @Test
    void opOver_insuficientesElementos_lanzaExcepcion() {
        var tokens = ScriptParser.parse("OP_1 OP_OVER");
        assertThrows(ScriptException.class, () -> interpreter.execute(tokens, false));
    }

    // =========================================================
    // OP_EQUAL
    // =========================================================

    @Test
    void opEqual_valoresIguales_empuja1() {
        var tokens = ScriptParser.parse("OP_1 OP_1 OP_EQUAL");
        assertTrue(interpreter.execute(tokens, false));
    }

    @Test
    void opEqual_valoresDistintos_empuja0() {
        var tokens = ScriptParser.parse("OP_1 OP_2 OP_EQUAL");
        assertFalse(interpreter.execute(tokens, false));
    }

    @Test
    void opEqual_insuficientesElementos_lanzaExcepcion() {
        var tokens = ScriptParser.parse("OP_1 OP_EQUAL");
        assertThrows(ScriptException.class, () -> interpreter.execute(tokens, false));
    }

    // =========================================================
    // OP_EQUALVERIFY
    // =========================================================

    @Test
    void opEqualVerify_valoresIguales_continua() {
        var tokens = ScriptParser.parse("OP_1 OP_1 OP_1 OP_EQUALVERIFY");
        assertTrue(interpreter.execute(tokens, false));
    }

    @Test
    void opEqualVerify_valoresDistintos_lanzaExcepcion() {
        var tokens = ScriptParser.parse("OP_1 OP_2 OP_EQUALVERIFY");
        assertThrows(ScriptException.class, () -> interpreter.execute(tokens, false));
    }

    // =========================================================
    // OP_NOT
    // =========================================================

    @Test
    void opNot_cero_empuja1() {
        var tokens = ScriptParser.parse("OP_0 OP_NOT");
        assertTrue(interpreter.execute(tokens, false));
    }

    @Test
    void opNot_uno_empuja0() {
        var tokens = ScriptParser.parse("OP_1 OP_NOT");
        assertFalse(interpreter.execute(tokens, false));
    }

    @Test
    void opNot_stackVacio_lanzaExcepcion() {
        var tokens = ScriptParser.parse("OP_NOT");
        assertThrows(ScriptException.class, () -> interpreter.execute(tokens, false));
    }

    // =========================================================
    // OP_BOOLAND
    // =========================================================

    @Test
    void opBooland_ambosVerdaderos_empuja1() {
        var tokens = ScriptParser.parse("OP_1 OP_1 OP_BOOLAND");
        assertTrue(interpreter.execute(tokens, false));
    }

    @Test
    void opBooland_unoCero_empuja0() {
        var tokens = ScriptParser.parse("OP_1 OP_0 OP_BOOLAND");
        assertFalse(interpreter.execute(tokens, false));
    }

    @Test
    void opBooland_ambosCero_empuja0() {
        var tokens = ScriptParser.parse("OP_0 OP_0 OP_BOOLAND");
        assertFalse(interpreter.execute(tokens, false));
    }

    @Test
    void opBooland_insuficientesElementos_lanzaExcepcion() {
        var tokens = ScriptParser.parse("OP_1 OP_BOOLAND");
        assertThrows(ScriptException.class, () -> interpreter.execute(tokens, false));
    }

    // =========================================================
    // OP_BOOLOR
    // =========================================================

    @Test
    void opBoolor_ambosVerdaderos_empuja1() {
        var tokens = ScriptParser.parse("OP_1 OP_1 OP_BOOLOR");
        assertTrue(interpreter.execute(tokens, false));
    }

    @Test
    void opBoolor_unoCero_empuja1() {
        var tokens = ScriptParser.parse("OP_1 OP_0 OP_BOOLOR");
        assertTrue(interpreter.execute(tokens, false));
    }

    @Test
    void opBoolor_ambosCero_empuja0() {
        var tokens = ScriptParser.parse("OP_0 OP_0 OP_BOOLOR");
        assertFalse(interpreter.execute(tokens, false));
    }

    @Test
    void opBoolor_insuficientesElementos_lanzaExcepcion() {
        var tokens = ScriptParser.parse("OP_1 OP_BOOLOR");
        assertThrows(ScriptException.class, () -> interpreter.execute(tokens, false));
    }

    // =========================================================
    // OP_ADD
    // =========================================================

    @Test
    void opAdd_sumaCorrectamente() {
        var tokens = ScriptParser.parse("OP_3 OP_2 OP_ADD");
        assertTrue(interpreter.execute(tokens, false));
    }

    @Test
    void opAdd_resultadoCero_retornaFalse() {
        var tokens = ScriptParser.parse("OP_0 OP_0 OP_ADD");
        assertFalse(interpreter.execute(tokens, false));
    }

    @Test
    void opAdd_insuficientesElementos_lanzaExcepcion() {
        var tokens = ScriptParser.parse("OP_1 OP_ADD");
        assertThrows(ScriptException.class, () -> interpreter.execute(tokens, false));
    }

    // =========================================================
    // OP_SUB
    // =========================================================

    @Test
    void opSub_restaCorrectamente() {
        var tokens = ScriptParser.parse("OP_3 OP_1 OP_SUB");
        assertTrue(interpreter.execute(tokens, false));
    }

    @Test
    void opSub_resultadoCero_retornaFalse() {
        var tokens = ScriptParser.parse("OP_2 OP_2 OP_SUB");
        assertFalse(interpreter.execute(tokens, false));
    }

    @Test
    void opSub_insuficientesElementos_lanzaExcepcion() {
        var tokens = ScriptParser.parse("OP_1 OP_SUB");
        assertThrows(ScriptException.class, () -> interpreter.execute(tokens, false));
    }

    // =========================================================
    // OP_NUMEQUALVERIFY
    // =========================================================

    @Test
    void opNumEqualVerify_iguales_continua() {
        var tokens = ScriptParser.parse("OP_1 OP_2 OP_2 OP_NUMEQUALVERIFY");
        assertTrue(interpreter.execute(tokens, false));
    }

    @Test
    void opNumEqualVerify_distintos_lanzaExcepcion() {
        var tokens = ScriptParser.parse("OP_1 OP_2 OP_NUMEQUALVERIFY");
        assertThrows(ScriptException.class, () -> interpreter.execute(tokens, false));
    }

    // =========================================================
    // OP_LESSTHAN
    // =========================================================

    @Test
    void opLessThan_menorQue_empuja1() {
        var tokens = ScriptParser.parse("OP_1 OP_2 OP_LESSTHAN");
        assertTrue(interpreter.execute(tokens, false));
    }

    @Test
    void opLessThan_mayorQue_empuja0() {
        var tokens = ScriptParser.parse("OP_3 OP_2 OP_LESSTHAN");
        assertFalse(interpreter.execute(tokens, false));
    }

    @Test
    void opLessThan_igual_empuja0() {
        var tokens = ScriptParser.parse("OP_2 OP_2 OP_LESSTHAN");
        assertFalse(interpreter.execute(tokens, false));
    }

    // =========================================================
    // OP_GREATERTHAN
    // =========================================================

    @Test
    void opGreaterThan_mayorQue_empuja1() {
        var tokens = ScriptParser.parse("OP_3 OP_2 OP_GREATERTHAN");
        assertTrue(interpreter.execute(tokens, false));
    }

    @Test
    void opGreaterThan_menorQue_empuja0() {
        var tokens = ScriptParser.parse("OP_1 OP_2 OP_GREATERTHAN");
        assertFalse(interpreter.execute(tokens, false));
    }

    // =========================================================
    // OP_LESSTHANOREQUAL
    // =========================================================

    @Test
    void opLessThanOrEqual_menorQue_empuja1() {
        var tokens = ScriptParser.parse("OP_1 OP_2 OP_LESSTHANOREQUAL");
        assertTrue(interpreter.execute(tokens, false));
    }

    @Test
    void opLessThanOrEqual_igual_empuja1() {
        var tokens = ScriptParser.parse("OP_2 OP_2 OP_LESSTHANOREQUAL");
        assertTrue(interpreter.execute(tokens, false));
    }

    @Test
    void opLessThanOrEqual_mayorQue_empuja0() {
        var tokens = ScriptParser.parse("OP_3 OP_2 OP_LESSTHANOREQUAL");
        assertFalse(interpreter.execute(tokens, false));
    }

    // =========================================================
    // OP_GREATERTHANOREQUAL
    // =========================================================

    @Test
    void opGreaterThanOrEqual_mayorQue_empuja1() {
        var tokens = ScriptParser.parse("OP_3 OP_2 OP_GREATERTHANOREQUAL");
        assertTrue(interpreter.execute(tokens, false));
    }

    @Test
    void opGreaterThanOrEqual_igual_empuja1() {
        var tokens = ScriptParser.parse("OP_2 OP_2 OP_GREATERTHANOREQUAL");
        assertTrue(interpreter.execute(tokens, false));
    }

    @Test
    void opGreaterThanOrEqual_menorQue_empuja0() {
        var tokens = ScriptParser.parse("OP_1 OP_2 OP_GREATERTHANOREQUAL");
        assertFalse(interpreter.execute(tokens, false));
    }

    // =========================================================
    // OP_VERIFY
    // =========================================================

    @Test
    void opVerify_valorVerdadero_continua() {
        var tokens = ScriptParser.parse("OP_1 OP_1 OP_VERIFY");
        assertTrue(interpreter.execute(tokens, false));
    }

    @Test
    void opVerify_valorFalso_lanzaExcepcion() {
        var tokens = ScriptParser.parse("OP_0 OP_VERIFY");
        assertThrows(ScriptException.class, () -> interpreter.execute(tokens, false));
    }

    @Test
    void opVerify_stackVacio_lanzaExcepcion() {
        var tokens = ScriptParser.parse("OP_VERIFY");
        assertThrows(ScriptException.class, () -> interpreter.execute(tokens, false));
    }

    // =========================================================
    // OP_RETURN
    // =========================================================

    @Test
    void opReturn_siempreLanzaExcepcion() {
        var tokens = ScriptParser.parse("OP_1 OP_RETURN");
        assertThrows(ScriptException.class, () -> interpreter.execute(tokens, false));
    }

    // =========================================================
    // OP_NOTIF
    // =========================================================

    @Test
    void opNotif_condicionFalsa_ejecutaBloque() {
        var tokens = ScriptParser.parse("OP_0 OP_NOTIF OP_1 OP_ENDIF");
        assertTrue(interpreter.execute(tokens, false));
    }

    @Test
    void opNotif_condicionVerdadera_ignoraBloque() {
        var tokens = ScriptParser.parse("OP_1 OP_NOTIF OP_1 OP_ENDIF");
        assertFalse(interpreter.execute(tokens, false));
    }

    // =========================================================
    // OP_SHA256
    // Nota: los hashes producen bytes arbitrarios que no pueden pasarse
    // como String en un script de texto. Por eso usamos executeWithData()
    // que acepta byte[] directamente, evitando corrupción de bytes.
    // =========================================================

    @Test
    void opSha256_produceFinalValido() {
        byte[] expected = MockCrypto.sha256("test".getBytes());
        List<Object> tokens = new ArrayList<>();
        tokens.add("test");
        tokens.add("OP_SHA256");
        tokens.add(expected);   // byte[] directo, sin pasar por String
        tokens.add("OP_EQUAL");
        assertTrue(interpreter.executeWithData(tokens, false));
    }

    @Test
    void opSha256_stackVacio_lanzaExcepcion() {
        var tokens = ScriptParser.parse("OP_SHA256");
        assertThrows(ScriptException.class, () -> interpreter.execute(tokens, false));
    }

    // =========================================================
    // OP_HASH256
    // =========================================================

    @Test
    void opHash256_produceFinalValido() {
        byte[] expected = MockCrypto.hash256("test".getBytes());
        List<Object> tokens = new ArrayList<>();
        tokens.add("test");
        tokens.add("OP_HASH256");
        tokens.add(expected);   // byte[] directo
        tokens.add("OP_EQUAL");
        assertTrue(interpreter.executeWithData(tokens, false));
    }

    @Test
    void opHash256_stackVacio_lanzaExcepcion() {
        var tokens = ScriptParser.parse("OP_HASH256");
        assertThrows(ScriptException.class, () -> interpreter.execute(tokens, false));
    }

    // =========================================================
    // OP_HASH160
    // =========================================================

    @Test
    void opHash160_produceHashDelTope() {
        byte[] expected = MockCrypto.hash160("publicKey".getBytes());
        List<Object> tokens = new ArrayList<>();
        tokens.add("publicKey");
        tokens.add("OP_HASH160");
        tokens.add(expected);   // byte[] directo
        tokens.add("OP_EQUAL");
        assertTrue(interpreter.executeWithData(tokens, false));
    }

    @Test
    void opHash160_stackVacio_lanzaExcepcion() {
        var tokens = ScriptParser.parse("OP_HASH160");
        assertThrows(ScriptException.class, () -> interpreter.execute(tokens, false));
    }

    // =========================================================
    // OP_CHECKSIG
    // =========================================================

    @Test
    void opCheckSig_firmaValida_empuja1() {
        var tokens = ScriptParser.parse("valid publicKey OP_CHECKSIG");
        assertTrue(interpreter.execute(tokens, false));
    }

    @Test
    void opCheckSig_firmaInvalida_empuja0() {
        var tokens = ScriptParser.parse("INVALIDA publicKey OP_CHECKSIG");
        assertFalse(interpreter.execute(tokens, false));
    }

    @Test
    void opCheckSig_insuficientesElementos_lanzaExcepcion() {
        var tokens = ScriptParser.parse("publicKey OP_CHECKSIG");
        assertThrows(ScriptException.class, () -> interpreter.execute(tokens, false));
    }

    // =========================================================
    // OP_CHECKSIGVERIFY
    // =========================================================

    @Test
    void opCheckSigVerify_firmaValida_continua() {
        var tokens = ScriptParser.parse("OP_1 valid publicKey OP_CHECKSIGVERIFY");
        assertTrue(interpreter.execute(tokens, false));
    }

    @Test
    void opCheckSigVerify_firmaInvalida_lanzaExcepcion() {
        var tokens = ScriptParser.parse("INVALIDA publicKey OP_CHECKSIGVERIFY");
        assertThrows(ScriptException.class, () -> interpreter.execute(tokens, false));
    }

    // =========================================================
    // OP_IF / OP_ELSE / OP_ENDIF
    // =========================================================

    @Test
    void opIf_condicionVerdadera_ejecutaRamaIf() {
        var tokens = ScriptParser.parse("OP_1 OP_IF OP_1 OP_ELSE OP_0 OP_ENDIF");
        assertTrue(interpreter.execute(tokens, false));
    }

    @Test
    void opIf_condicionFalsa_ejecutaRamaElse() {
        var tokens = ScriptParser.parse("OP_0 OP_IF OP_1 OP_ELSE OP_0 OP_ENDIF");
        assertFalse(interpreter.execute(tokens, false));
    }

    @Test
    void opIf_sinElse_condicionFalsa_stackVacio() {
        var tokens = ScriptParser.parse("OP_0 OP_IF OP_1 OP_ENDIF");
        assertFalse(interpreter.execute(tokens, false));
    }

    @Test
    void opIf_anidado_ambasCondicionesVerdaderas() {
        var tokens = ScriptParser.parse(
            "OP_1 OP_IF OP_1 OP_IF OP_1 OP_ELSE OP_0 OP_ENDIF OP_ELSE OP_0 OP_ENDIF");
        assertTrue(interpreter.execute(tokens, false));
    }

    @Test
    void opIf_anidado_primeraVerdaderaSegundaFalsa() {
        var tokens = ScriptParser.parse(
            "OP_1 OP_IF OP_0 OP_IF OP_1 OP_ELSE OP_0 OP_ENDIF OP_ELSE OP_0 OP_ENDIF");
        assertFalse(interpreter.execute(tokens, false));
    }

    @Test
    void opIf_anidado_primeraFalsa_ignoraTodo() {
        var tokens = ScriptParser.parse(
            "OP_0 OP_IF OP_1 OP_IF OP_1 OP_ENDIF OP_ENDIF");
        assertFalse(interpreter.execute(tokens, false));
    }

    @Test
    void opElse_sinOpIf_lanzaExcepcion() {
        var tokens = ScriptParser.parse("OP_1 OP_ELSE OP_0 OP_ENDIF");
        assertThrows(ScriptException.class, () -> interpreter.execute(tokens, false));
    }

    @Test
    void opEndif_sinOpIf_lanzaExcepcion() {
        var tokens = ScriptParser.parse("OP_1 OP_ENDIF");
        assertThrows(ScriptException.class, () -> interpreter.execute(tokens, false));
    }

    @Test
    void opIf_sinOpEndif_lanzaExcepcion() {
        var tokens = ScriptParser.parse("OP_1 OP_IF OP_1");
        assertThrows(ScriptException.class, () -> interpreter.execute(tokens, false));
    }

    @Test
    void opIf_stackVacioAlEvaluar_lanzaExcepcion() {
        var tokens = ScriptParser.parse("OP_IF OP_1 OP_ENDIF");
        assertThrows(ScriptException.class, () -> interpreter.execute(tokens, false));
    }

    // =========================================================
    // P2PKH completo
    // Usamos executeWithData() para pasar el hash como byte[] directo
    // y evitar la corrupción que ocurre al convertirlo a String.
    // =========================================================

    @Test
    void p2pkh_firmaYClaveCorrectas_retornaTrue() {
        byte[] pubKeyHash = MockCrypto.hash160("publicKey".getBytes());
        List<Object> tokens = new ArrayList<>();
        tokens.add("valid");
        tokens.add("publicKey");
        tokens.add("OP_DUP");
        tokens.add("OP_HASH160");
        tokens.add(pubKeyHash);
        tokens.add("OP_EQUALVERIFY");
        tokens.add("OP_CHECKSIG");
        assertTrue(interpreter.executeWithData(tokens, false));
    }

    @Test
    void p2pkh_firmaIncorrecta_retornaFalse() {
        byte[] pubKeyHash = MockCrypto.hash160("publicKey".getBytes());
        List<Object> tokens = new ArrayList<>();
        tokens.add("INVALIDA");
        tokens.add("publicKey");
        tokens.add("OP_DUP");
        tokens.add("OP_HASH160");
        tokens.add(pubKeyHash);
        tokens.add("OP_EQUALVERIFY");
        tokens.add("OP_CHECKSIG");
        assertFalse(interpreter.executeWithData(tokens, false));
    }

    @Test
    void p2pkh_hashIncorrecto_lanzaExcepcion() {
        var tokens = ScriptParser.parse(
            "valid publicKey OP_DUP OP_HASH160 hashFalso OP_EQUALVERIFY OP_CHECKSIG");
        assertThrows(ScriptException.class, () -> interpreter.execute(tokens, false));
    }

    // =========================================================
    // OP_CHECKMULTISIG
    // =========================================================

    @Test
    void opCheckMultisig_2de3_ambasFirmasValidas_retornaTrue() {
        var tokens = ScriptParser.parse(
            "OP_0 valid valid OP_2 publicKey publicKey publicKey OP_3 OP_CHECKMULTISIG");
        assertTrue(interpreter.execute(tokens, false));
    }

    @Test
    void opCheckMultisig_2de3_soloUnaFirmaValida_retornaFalse() {
        var tokens = ScriptParser.parse(
            "OP_0 valid INVALIDA OP_2 publicKey publicKey publicKey OP_3 OP_CHECKMULTISIG");
        assertFalse(interpreter.execute(tokens, false));
    }

    @Test
    void opCheckMultisig_2de3_ningunaFirmaValida_retornaFalse() {
        var tokens = ScriptParser.parse(
            "OP_0 INVALIDA INVALIDA OP_2 publicKey publicKey publicKey OP_3 OP_CHECKMULTISIG");
        assertFalse(interpreter.execute(tokens, false));
    }

    @Test
    void opCheckMultisig_faltaElementoExtraBug_lanzaExcepcion() {
        var tokens = ScriptParser.parse(
            "valid valid OP_2 publicKey publicKey publicKey OP_3 OP_CHECKMULTISIG");
        assertThrows(ScriptException.class, () -> interpreter.execute(tokens, false));
    }

    @Test
    void opCheckMultisig_nFirmasMayorQueNClaves_lanzaExcepcion() {
        var tokens = ScriptParser.parse(
            "OP_0 valid valid valid OP_3 publicKey publicKey OP_2 OP_CHECKMULTISIG");
        assertThrows(ScriptException.class, () -> interpreter.execute(tokens, false));
    }

    @Test
    void opCheckMultisig_stackVacioAlLeerNClaves_lanzaExcepcion() {
        var tokens = ScriptParser.parse("OP_CHECKMULTISIG");
        assertThrows(ScriptException.class, () -> interpreter.execute(tokens, false));
    }

    // =========================================================
    // Casos borde generales
    // =========================================================

    @Test
    void stackVacioAlFinal_retornaFalse() {
        var tokens = ScriptParser.parse("");
        assertFalse(interpreter.execute(tokens, false));
    }

    @Test
    void valorCeroEnTope_retornaFalse() {
        var tokens = ScriptParser.parse("OP_0");
        assertFalse(interpreter.execute(tokens, false));
    }

    @Test
    void valorUnoEnTope_retornaTrue() {
        var tokens = ScriptParser.parse("OP_1");
        assertTrue(interpreter.execute(tokens, false));
    }

    @Test
    void scriptEjemploDelPDF_debeRetornarFalse() {
        // Del PDF: "1 2 OP_ADD 5 OP_GREATERTHAN" -> 1+2=3, 3>5 -> false
        var tokens = ScriptParser.parse("1 2 OP_ADD 5 OP_GREATERTHAN");
        assertFalse(interpreter.execute(tokens, false));
    }
}