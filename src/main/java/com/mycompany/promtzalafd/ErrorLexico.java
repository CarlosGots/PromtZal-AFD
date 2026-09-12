package com.mycompany.promtzalafd;

/**
 * Representa un error lexico detectado por el Lexer: que tipo de error
 * fue, el lexema o caracter que lo causo, y su posicion (fila, columna).
 * Necesario para el reporte de errores y para el reporte de estadisticas.
 */
public class ErrorLexico {

    /**
     * Tipos de error que puede producir el AFD (cada uno corresponde
     * a una caida especifica a qE, ver docs/AFD_PromptZal_diseno.md).
     */
    public enum TipoErrorLexico {
        CARACTER_NO_RECONOCIDO,      // S0 -> qE (clase OTRO)
        CADENA_SIN_CERRAR,           // S4 -> qE (con \n o EOF)
        DECIMAL_MAL_FORMADO,         // S2b -> qE (sin digito tras el punto)
        DIRECTIVA_INVALIDA,          // S3 -> qE (caracter invalido tras @)
        DIRECTIVA_DESCONOCIDA,       // S3b -> qE (no es @modelo/@rol/@formato)
        OPERADOR_INVALIDO,           // S5 -> qE ("-" sin ">"), S6 -> qE ("/" sin "/" ni "*")
        COMENTARIO_SIN_CERRAR        // S6b/S6c -> qE (EOF sin cerrar el bloque)
    }

    private final TipoErrorLexico tipo;
    private final String lexemaOCaracter;
    private final int fila;
    private final int columna;

    public ErrorLexico(TipoErrorLexico tipo, String lexemaOCaracter, int fila, int columna) {
        this.tipo = tipo;
        this.lexemaOCaracter = lexemaOCaracter;
        this.fila = fila;
        this.columna = columna;
    }

    public TipoErrorLexico getTipo() {
        return tipo;
    }

    public String getLexemaOCaracter() {
        return lexemaOCaracter;
    }

    public int getFila() {
        return fila;
    }

    public int getColumna() {
        return columna;
    }

    @Override
    public String toString() {
        return String.format("ErrorLexico[tipo=%s, lexema='%s', fila=%d, columna=%d]",
                tipo, lexemaOCaracter, fila, columna);
    }
}