package com.mycompany.promtzalafd;

/**
 * Representa un token reconocido por el Lexer: su tipo, el lexema
 * exacto que lo origino, y su posicion (fila, columna) en el archivo
 * fuente. Necesario para los 3 reportes HTML.
 */
public class Token {

    private final TipoToken tipo;
    private final String lexema;
    private final int fila;
    private final int columna;

    public Token(TipoToken tipo, String lexema, int fila, int columna) {
        this.tipo = tipo;
        this.lexema = lexema;
        this.fila = fila;
        this.columna = columna;
    }

    public TipoToken getTipo() {
        return tipo;
    }

    public String getLexema() {
        return lexema;
    }

    public int getFila() {
        return fila;
    }

    public int getColumna() {
        return columna;
    }

    @Override
    public String toString() {
        return String.format("Token[tipo=%s, lexema='%s', fila=%d, columna=%d]",
                tipo, lexema, fila, columna);
    }
}