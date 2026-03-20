package main.java.com.script;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class ScriptInterpreterTest {

    private ScriptInterpreter interpreter;

    @BeforeEach
    void setUp() {
        interpreter = new ScriptInterpreter();
    }

    // OP_DUP

    @Test
    void opDup_duplicaElTope() {
        // "hello OP_DUP" -> stack: [hello, hello] -> resultado true (no vacío)
        var tokens = ScriptParser.parse("hello OP_DUP OP_EQUAL");
        assertTrue(interpreter.execute(tokens, false));
    }

    @Test
    void opDup_stackVacio_lanzaExcepcion() {
        var tokens = ScriptParser.parse("OP_DUP");
        assertThrows(ScriptException.class, () -> interpreter.execute(tokens, false));
    }

    // OP_DROP

    @Test
    void opDrop_eliminaElTope() {
        // "OP_1 OP_1 OP_DROP" -> queda OP_1 -> true
        var tokens = ScriptParser.parse("OP_1 OP_1 OP_DROP");
        assertTrue(interpreter.execute(tokens, false));
    }

    @Test
    void opDrop_stackVacio_lanzaExcepcion() {
        var tokens = ScriptParser.parse("OP_DROP");
        assertThrows(ScriptException.class, () -> interpreter.execute(tokens, false));
    }

    // OP_EQUAL

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

    // OP_EQUALVERIFY

    @Test
    void opEqualVerify_valoresIguales_continua() {
        // Si son iguales no lanza excepción y queda OP_1 -> true
        var tokens = ScriptParser.parse("OP_1 OP_1 OP_1 OP_EQUALVERIFY");
        assertTrue(interpreter.execute(tokens, false));
    }

    @Test
    void opEqualVerify_valoresDistintos_lanzaExcepcion() {
        var tokens = ScriptParser.parse("OP_1 OP_2 OP_EQUALVERIFY");
        assertThrows(ScriptException.class, () -> interpreter.execute(tokens, false));
    }

    // OP_HASH160

    @Test
    void opHash160_produceHashDelTope() {
        // hash("publicKey") dos veces debe ser igual
        byte[] expected = MockCrypto.hash160("publicKey".getBytes());
        String hashStr = new String(expected);
        var tokens = ScriptParser.parse("publicKey OP_HASH160 " + hashStr + " OP_EQUAL");
        assertTrue(interpreter.execute(tokens, false));
    }

    @Test
    void opHash160_stackVacio_lanzaExcepcion() {
        var tokens = ScriptParser.parse("OP_HASH160");
        assertThrows(ScriptException.class, () -> interpreter.execute(tokens, false));
    }

    // OP_CHECKSIG

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

    // OP_IF / OP_ELSE / OP_ENDIF

    @Test
    void opIf_condicionVerdadera_ejecutaRamaIf() {
        // OP_1 -> condición true -> ejecuta OP_1 dentro del IF -> resultado true
        var tokens = ScriptParser.parse("OP_1 OP_IF OP_1 OP_ELSE OP_0 OP_ENDIF");
        assertTrue(interpreter.execute(tokens, false));
    }

    @Test
    void opIf_condicionFalsa_ejecutaRamaElse() {
        // OP_0 -> condición false -> ejecuta OP_0 dentro del ELSE -> resultado false
        var tokens = ScriptParser.parse("OP_0 OP_IF OP_1 OP_ELSE OP_0 OP_ENDIF");
        assertFalse(interpreter.execute(tokens, false));
    }

    @Test
    void opIf_sinElse_condicionFalsa_stackVacio() {
        // OP_0 -> condición false, no hay ELSE -> stack vacío -> false
        var tokens = ScriptParser.parse("OP_0 OP_IF OP_1 OP_ENDIF");
        assertFalse(interpreter.execute(tokens, false));
    }

    @Test
    void opIf_anidado_ambasCondicionesVerdaderas() {
        // Ambos OP_IF son true -> ejecuta el OP_1 más interno -> true
        var tokens = ScriptParser.parse(
            "OP_1 OP_IF OP_1 OP_IF OP_1 OP_ELSE OP_0 OP_ENDIF OP_ELSE OP_0 OP_ENDIF");
        assertTrue(interpreter.execute(tokens, false));
    }

    @Test
    void opIf_anidado_primeraVerdaderaSegundaFalsa() {
        // Outer=true, inner=false -> ejecuta ELSE interno -> OP_0 -> false
        var tokens = ScriptParser.parse(
            "OP_1 OP_IF OP_0 OP_IF OP_1 OP_ELSE OP_0 OP_ENDIF OP_ELSE OP_0 OP_ENDIF");
        assertFalse(interpreter.execute(tokens, false));
    }

    @Test
    void opIf_anidado_primeraFalsa_ignoraTodo() {
        // Outer=false -> todo el bloque interno se ignora -> stack vacío -> false
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

    // P2PKH completo

    @Test
    void p2pkh_firmaYClaveCorrectas_retornaTrue() {
        byte[] hash = MockCrypto.hash160("publicKey".getBytes());
        String pubKeyHash = new String(hash);
        var tokens = ScriptParser.parse(
            "valid publicKey OP_DUP OP_HASH160 " + pubKeyHash + " OP_EQUALVERIFY OP_CHECKSIG");
        assertTrue(interpreter.execute(tokens, false));
    }

    @Test
    void p2pkh_firmaIncorrecta_retornaFalse() {
        byte[] hash = MockCrypto.hash160("publicKey".getBytes());
        String pubKeyHash = new String(hash);
        var tokens = ScriptParser.parse(
            "INVALIDA publicKey OP_DUP OP_HASH160 " + pubKeyHash + " OP_EQUALVERIFY OP_CHECKSIG");
        assertFalse(interpreter.execute(tokens, false));
    }

    @Test
    void p2pkh_hashIncorrecto_lanzaExcepcion() {
        // El hash no coincide -> OP_EQUALVERIFY lanza ScriptException
        var tokens = ScriptParser.parse(
            "valid publicKey OP_DUP OP_HASH160 hashFalso OP_EQUALVERIFY OP_CHECKSIG");
        assertThrows(ScriptException.class, () -> interpreter.execute(tokens, false));
    }

    // Casos borde generales

    @Test
    void stackVacioAlFinal_retornaFalse() {
        // Script vacío -> stack vacío -> false
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
}