package com.mycompany.promtzalafd;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Tabla de busqueda usada al salir del estado S1 (identificador/palabra
 * reservada en construccion): si el lexema completo coincide exactamente
 * con una entrada aqui, se emite ese TipoToken; si no, se emite
 * TipoToken.IDENTIFICADOR. La comparacion es sensible a mayusculas/minusculas,
 * tal como estan escritas las palabras reservadas del lenguaje.
 */
public class TablaPalabrasReservadas {

    private static final Map<String, TipoToken> PALABRAS = new HashMap<>();

    static {
        // Estructura
        PALABRAS.put("AGENTE", TipoToken.AGENTE);
        PALABRAS.put("contexto", TipoToken.CONTEXTO);
        PALABRAS.put("variable", TipoToken.VARIABLE);
        PALABRAS.put("EJECUTAR", TipoToken.EJECUTAR);
        PALABRAS.put("EXPORTAR", TipoToken.EXPORTAR);

        // Comandos de IA
        PALABRAS.put("PREGUNTAR", TipoToken.PREGUNTAR);
        PALABRAS.put("GENERAR", TipoToken.GENERAR);
        PALABRAS.put("RESUMIR", TipoToken.RESUMIR);
        PALABRAS.put("ANALIZAR", TipoToken.ANALIZAR);
        PALABRAS.put("TRADUCIR", TipoToken.TRADUCIR);
        PALABRAS.put("CLASIFICAR", TipoToken.CLASIFICAR);
        PALABRAS.put("EXTRAER", TipoToken.EXTRAER);

        // Funcion del sistema
        PALABRAS.put("CARGAR", TipoToken.CARGAR);

        // Conectores (palabra, no simbolo)
        PALABRAS.put("SOBRE", TipoToken.SOBRE);
        PALABRAS.put("DESDE", TipoToken.DESDE);
        PALABRAS.put("EN", TipoToken.EN);
        PALABRAS.put("COMO", TipoToken.COMO);
    }

    private static final Map<String, TipoToken> PALABRAS_INMUTABLE =
            Collections.unmodifiableMap(PALABRAS);

    private TablaPalabrasReservadas() {
        // Clase utilitaria, no se instancia
    }

    /**
     * Busca el lexema en la tabla de palabras reservadas.
     * @param lexema el lexema completo acumulado en S1
     * @return el TipoToken correspondiente, o null si no es una palabra reservada
     *         (en ese caso el Lexer debe emitir TipoToken.IDENTIFICADOR)
     */
    public static TipoToken buscar(String lexema) {
        return PALABRAS_INMUTABLE.get(lexema);
    }
}