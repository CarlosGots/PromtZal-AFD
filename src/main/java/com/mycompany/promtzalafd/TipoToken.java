package com.mycompany.promtzalafd;

/**
 * Enumera todos los tipos de token que reconoce el AFD de PromptZal.
 * Cada valor corresponde a un estado de aceptacion del automata
 * (ver docs/AFD_PromptZal_diseno.md).
 */
public enum TipoToken {

    // Directivas (@modelo, @rol, @formato) - estado S3b
    DIRECTIVA_MODELO,
    DIRECTIVA_ROL,
    DIRECTIVA_FORMATO,

    // Palabras reservadas de estructura - estado S1 (via tabla de reservadas)
    AGENTE,
    CONTEXTO,
    VARIABLE,
    EJECUTAR,
    EXPORTAR,

    // Comandos de IA - estado S1 (via tabla de reservadas)
    PREGUNTAR,
    GENERAR,
    RESUMIR,
    ANALIZAR,
    TRADUCIR,
    CLASIFICAR,
    EXTRAER,

    // Funcion del sistema - estado S1 (via tabla de reservadas)
    CARGAR,

    // Conectores - estado S1 (via tabla de reservadas) y estado S5 (flecha)
    SOBRE,
    DESDE,
    EN,
    COMO,
    FLECHA,          // ->

    // Identificador generico - estado S1 (si no coincide con la tabla)
    IDENTIFICADOR,

    // Literales - estados S2, S2c, S4
    ENTERO,
    DECIMAL,
    CADENA,

    // Operadores - directo desde S0
    ASIGNACION,      // =
    CONCATENACION,   // +

    // Delimitadores - directo desde S0
    LLAVE_ABRE,      // {
    LLAVE_CIERRA,    // }
    PAR_ABRE,        // (
    PAR_CIERRA       // )
}