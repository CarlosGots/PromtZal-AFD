package com.mycompany.promtzalafd;

import java.util.ArrayList;
import java.util.List;

/**
 * Analizador lexico manual de PromptZal, guiado explicitamente por los
 * estados del AFD documentado en docs/AFD_PromptZal_diseno.md.
 * Cada metodo estadoXX corresponde a un estado del automata, para poder
 * explicar el codigo estado por estado en la comprobacion de conocimiento.
 *
 * No usa JFlex, JLex, ni expresiones regulares (Pattern/Matcher/String.matches),
 * segun lo exige el proyecto.
 */
public class Lexer {

    private final String fuente;
    private int pos;
    private int fila;
    private int columna;

    private final List<Token> tokens = new ArrayList<>();
    private final List<ErrorLexico> errores = new ArrayList<>();

    public Lexer(String fuente) {
        this.fuente = fuente;
        this.pos = 0;
        this.fila = 1;
        this.columna = 1;
    }

    public List<Token> getTokens() {
        return tokens;
    }

    public List<ErrorLexico> getErrores() {
        return errores;
    }

    /** Punto de entrada: recorre toda la fuente reiniciando en S0 cada vez. */
    public void analizar() {
        while (hayMas()) {
            estadoS0();
        }
    }

    // ---------- Utilidades de recorrido ----------

    private boolean hayMas() {
        return pos < fuente.length();
    }

    private char peek() {
        return fuente.charAt(pos);
    }

    /** Consume el caracter actual y actualiza fila/columna. */
    private char consumir() {
        char c = fuente.charAt(pos);
        pos++;
        if (c == '\n') {
            fila++;
            columna = 1;
        } else {
            columna++;
        }
        return c;
    }

    private boolean esLetra(char c) {
        return (c >= 'a' && c <= 'z') || (c >= 'A' && c <= 'Z');
    }

    private boolean esDigito(char c) {
        return c >= '0' && c <= '9';
    }

    private boolean esLetraDigitoOGuionBajo(char c) {
        return esLetra(c) || esDigito(c) || c == '_';
    }

    // ---------- S0: estado inicial / dispensador ----------

    private void estadoS0() {
        int filaInicio = fila;
        int colInicio = columna;
        char c = peek();

        if (esLetra(c)) {
            consumir();
            StringBuilder lex = new StringBuilder().append(c);
            estadoS1(lex, filaInicio, colInicio);
        } else if (esDigito(c)) {
            consumir();
            StringBuilder lex = new StringBuilder().append(c);
            estadoS2(lex, filaInicio, colInicio);
        } else if (c == '@') {
            consumir();
            estadoS3(filaInicio, colInicio);
        } else if (c == '"') {
            consumir();
            estadoS4(filaInicio, colInicio);
        } else if (c == '-') {
            consumir();
            estadoS5(filaInicio, colInicio);
        } else if (c == '/') {
            consumir();
            estadoS6(filaInicio, colInicio);
        } else if (c == '=') {
            consumir();
            tokens.add(new Token(TipoToken.ASIGNACION, "=", filaInicio, colInicio));
        } else if (c == '+') {
            consumir();
            tokens.add(new Token(TipoToken.CONCATENACION, "+", filaInicio, colInicio));
        } else if (c == '{') {
            consumir();
            tokens.add(new Token(TipoToken.LLAVE_ABRE, "{", filaInicio, colInicio));
        } else if (c == '}') {
            consumir();
            tokens.add(new Token(TipoToken.LLAVE_CIERRA, "}", filaInicio, colInicio));
        } else if (c == '(') {
            consumir();
            tokens.add(new Token(TipoToken.PAR_ABRE, "(", filaInicio, colInicio));
        } else if (c == ')') {
            consumir();
            tokens.add(new Token(TipoToken.PAR_CIERRA, ")", filaInicio, colInicio));
        } else if (c == ' ' || c == '\t' || c == '\r' || c == '\n') {
            consumir(); // se descarta, no genera token
        } else {
            consumir();
            errores.add(new ErrorLexico(
                    ErrorLexico.TipoErrorLexico.CARACTER_NO_RECONOCIDO,
                    String.valueOf(c), filaInicio, colInicio));
        }
    }

    // ---------- S1: identificador / palabra reservada ----------

    private void estadoS1(StringBuilder lex, int filaInicio, int colInicio) {
        while (hayMas() && esLetraDigitoOGuionBajo(peek())) {
            lex.append(consumir());
        }
        String lexema = lex.toString();
        TipoToken tipo = TablaPalabrasReservadas.buscar(lexema);
        if (tipo == null) {
            tipo = TipoToken.IDENTIFICADOR;
        }
        tokens.add(new Token(tipo, lexema, filaInicio, colInicio));
    }

    // ---------- S2 / S2b / S2c: numeros ----------

    private void estadoS2(StringBuilder lex, int filaInicio, int colInicio) {
        while (hayMas() && esDigito(peek())) {
            lex.append(consumir());
        }
        if (hayMas() && peek() == '.') {
            lex.append(consumir());
            estadoS2b(lex, filaInicio, colInicio);
        } else {
            tokens.add(new Token(TipoToken.ENTERO, lex.toString(), filaInicio, colInicio));
        }
    }

    private void estadoS2b(StringBuilder lex, int filaInicio, int colInicio) {
        if (hayMas() && esDigito(peek())) {
            lex.append(consumir());
            estadoS2c(lex, filaInicio, colInicio);
        } else {
            errores.add(new ErrorLexico(
                    ErrorLexico.TipoErrorLexico.DECIMAL_MAL_FORMADO,
                    lex.toString(), filaInicio, colInicio));
        }
    }

    private void estadoS2c(StringBuilder lex, int filaInicio, int colInicio) {
        while (hayMas() && esDigito(peek())) {
            lex.append(consumir());
        }
        tokens.add(new Token(TipoToken.DECIMAL, lex.toString(), filaInicio, colInicio));
    }

    // ---------- S3 / S3b: directivas ----------

    private void estadoS3(int filaInicio, int colInicio) {
        if (hayMas() && esLetra(peek())) {
            StringBuilder lex = new StringBuilder("@").append(consumir());
            estadoS3b(lex, filaInicio, colInicio);
        } else {
            errores.add(new ErrorLexico(
                    ErrorLexico.TipoErrorLexico.DIRECTIVA_INVALIDA,
                    "@", filaInicio, colInicio));
        }
    }

    private void estadoS3b(StringBuilder lex, int filaInicio, int colInicio) {
        while (hayMas() && esLetra(peek())) {
            lex.append(consumir());
        }
        String lexema = lex.toString();
        switch (lexema) {
            case "@modelo":
                tokens.add(new Token(TipoToken.DIRECTIVA_MODELO, lexema, filaInicio, colInicio));
                break;
            case "@rol":
                tokens.add(new Token(TipoToken.DIRECTIVA_ROL, lexema, filaInicio, colInicio));
                break;
            case "@formato":
                tokens.add(new Token(TipoToken.DIRECTIVA_FORMATO, lexema, filaInicio, colInicio));
                break;
            default:
                errores.add(new ErrorLexico(
                        ErrorLexico.TipoErrorLexico.DIRECTIVA_DESCONOCIDA,
                        lexema, filaInicio, colInicio));
        }
    }

    // ---------- S4: cadena de texto ----------

    private void estadoS4(int filaInicio, int colInicio) {
        StringBuilder lex = new StringBuilder();
        while (true) {
            if (!hayMas()) {
                errores.add(new ErrorLexico(
                        ErrorLexico.TipoErrorLexico.CADENA_SIN_CERRAR,
                        lex.toString(), filaInicio, colInicio));
                return;
            }
            char c = peek();
            if (c == '"') {
                consumir();
                tokens.add(new Token(TipoToken.CADENA, lex.toString(), filaInicio, colInicio));
                return;
            }
            if (c == '\n') {
                // No se consume el \n: se deja para que S0 actualice la fila normalmente.
                errores.add(new ErrorLexico(
                        ErrorLexico.TipoErrorLexico.CADENA_SIN_CERRAR,
                        lex.toString(), filaInicio, colInicio));
                return;
            }
            lex.append(consumir());
        }
    }

    // ---------- S5: flecha ----------

    private void estadoS5(int filaInicio, int colInicio) {
        if (hayMas() && peek() == '>') {
            consumir();
            tokens.add(new Token(TipoToken.FLECHA, "->", filaInicio, colInicio));
        } else {
            errores.add(new ErrorLexico(
                    ErrorLexico.TipoErrorLexico.OPERADOR_INVALIDO,
                    "-", filaInicio, colInicio));
        }
    }

    // ---------- S6 / S6a / S6b / S6c: comentarios ----------

    private void estadoS6(int filaInicio, int colInicio) {
        if (hayMas() && peek() == '/') {
            consumir();
            estadoS6a();
        } else if (hayMas() && peek() == '*') {
            consumir();
            estadoS6b(filaInicio, colInicio);
        } else {
            errores.add(new ErrorLexico(
                    ErrorLexico.TipoErrorLexico.OPERADOR_INVALIDO,
                    "/", filaInicio, colInicio));
        }
    }

    private void estadoS6a() {
        while (hayMas() && peek() != '\n') {
            consumir();
        }
        // no genera token; el \n (si existe) lo procesa S0 en la siguiente vuelta
    }

    private void estadoS6b(int filaInicio, int colInicio) {
        while (true) {
            if (!hayMas()) {
                errores.add(new ErrorLexico(
                        ErrorLexico.TipoErrorLexico.COMENTARIO_SIN_CERRAR,
                        "/*", filaInicio, colInicio));
                return;
            }
            char c = consumir();
            if (c == '*') {
                if (estadoS6c(filaInicio, colInicio)) {
                    return; // comentario cerrado (o error ya reportado)
                }
                // si no se cerro, seguimos en S6b normalmente
            }
            // cualquier otro caracter (incluye \n) se descarta dentro del comentario
        }
    }

    /** @return true si el comentario quedo cerrado (o si se reporto EOF sin cerrar) */
    private boolean estadoS6c(int filaInicio, int colInicio) {
        while (true) {
            if (!hayMas()) {
                errores.add(new ErrorLexico(
                        ErrorLexico.TipoErrorLexico.COMENTARIO_SIN_CERRAR,
                        "/*", filaInicio, colInicio));
                return true;
            }
            char c = peek();
            if (c == '/') {
                consumir();
                return true; // cierra el comentario de bloque
            } else if (c == '*') {
                consumir(); // se queda en S6c (maneja casos como "**/")
            } else {
                return false; // regresa a S6b; ese caracter lo consume el loop de S6b
            }
        }
    }
}