package com.mycompany.promtzalafd;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.EnumMap;
import java.util.Map;

/**
 * Genera los 3 reportes HTML exigidos por el proyecto: tokens, errores
 * y estadisticas. El CSS se disena una sola vez y se comparte entre
 * los 3, para que se vean consistentes entre si.
 */
public class GeneradorReportes {

    private static final String CSS_COMPARTIDO = """
        body { font-family: Arial, Helvetica, sans-serif; background:#f4f5f7; margin:0; padding:30px; color:#1f2937; }
        h1 { font-size:20px; margin-bottom:4px; }
        p.meta { color:#6b7280; font-size:13px; margin-top:0; margin-bottom:20px; }
        table { border-collapse: collapse; width:100%; background:white; box-shadow:0 1px 3px rgba(0,0,0,0.1); border-radius:6px; overflow:hidden; }
        th { background:#1f2937; color:white; text-align:left; padding:10px 14px; font-size:13px; }
        td { padding:9px 14px; font-size:13px; border-bottom:1px solid #eee; }
        tr:nth-child(even) td { background:#fafafa; }
        .sin-datos { padding:20px; text-align:center; color:#6b7280; font-style:italic; background:white; border-radius:6px; }
        .badge { display:inline-block; padding:2px 8px; border-radius:10px; font-size:11px; font-weight:bold; color:white; }
        .cat-directiva { background:#7c3aed; }
        .cat-estructura { background:#2563eb; }
        .cat-comando { background:#0d9488; }
        .cat-sistema { background:#b45309; }
        .cat-conector { background:#475569; }
        .cat-literal { background:#c2410c; }
        .cat-operador { background:#6b7280; }
        .cat-delimitador { background:#9ca3af; }
        .cat-identificador { background:#4b5563; }
        .error-badge { background:#dc2626; }
        """;

    private static final Map<TipoToken, String> CATEGORIA = new EnumMap<>(TipoToken.class);
    static {
        CATEGORIA.put(TipoToken.DIRECTIVA_MODELO, "cat-directiva");
        CATEGORIA.put(TipoToken.DIRECTIVA_ROL, "cat-directiva");
        CATEGORIA.put(TipoToken.DIRECTIVA_FORMATO, "cat-directiva");
        CATEGORIA.put(TipoToken.AGENTE, "cat-estructura");
        CATEGORIA.put(TipoToken.CONTEXTO, "cat-estructura");
        CATEGORIA.put(TipoToken.VARIABLE, "cat-estructura");
        CATEGORIA.put(TipoToken.EJECUTAR, "cat-estructura");
        CATEGORIA.put(TipoToken.EXPORTAR, "cat-estructura");
        CATEGORIA.put(TipoToken.PREGUNTAR, "cat-comando");
        CATEGORIA.put(TipoToken.GENERAR, "cat-comando");
        CATEGORIA.put(TipoToken.RESUMIR, "cat-comando");
        CATEGORIA.put(TipoToken.ANALIZAR, "cat-comando");
        CATEGORIA.put(TipoToken.TRADUCIR, "cat-comando");
        CATEGORIA.put(TipoToken.CLASIFICAR, "cat-comando");
        CATEGORIA.put(TipoToken.EXTRAER, "cat-comando");
        CATEGORIA.put(TipoToken.CARGAR, "cat-sistema");
        CATEGORIA.put(TipoToken.SOBRE, "cat-conector");
        CATEGORIA.put(TipoToken.DESDE, "cat-conector");
        CATEGORIA.put(TipoToken.EN, "cat-conector");
        CATEGORIA.put(TipoToken.COMO, "cat-conector");
        CATEGORIA.put(TipoToken.FLECHA, "cat-conector");
        CATEGORIA.put(TipoToken.ENTERO, "cat-literal");
        CATEGORIA.put(TipoToken.DECIMAL, "cat-literal");
        CATEGORIA.put(TipoToken.CADENA, "cat-literal");
        CATEGORIA.put(TipoToken.ASIGNACION, "cat-operador");
        CATEGORIA.put(TipoToken.CONCATENACION, "cat-operador");
        CATEGORIA.put(TipoToken.LLAVE_ABRE, "cat-delimitador");
        CATEGORIA.put(TipoToken.LLAVE_CIERRA, "cat-delimitador");
        CATEGORIA.put(TipoToken.PAR_ABRE, "cat-delimitador");
        CATEGORIA.put(TipoToken.PAR_CIERRA, "cat-delimitador");
        CATEGORIA.put(TipoToken.IDENTIFICADOR, "cat-identificador");
    }

    private String badge(TipoToken tipo) {
        String clase = CATEGORIA.getOrDefault(tipo, "cat-identificador");
        return "<span class=\"badge " + clase + "\">" + tipo + "</span>";
    }

    private String escapar(String s) {
        return s.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;").replace("\"", "&quot;");
    }

    // ---------- Reporte de tokens ----------

    public void generarReporteTokens(List<Token> tokens, String rutaSalida) throws IOException {
        StringBuilder filas = new StringBuilder();
        int numero = 1;
        for (Token t : tokens) {
            filas.append("<tr><td>").append(numero++).append("</td><td><code>")
                 .append(escapar(t.getLexema())).append("</code></td><td>")
                 .append(badge(t.getTipo())).append("</td><td>").append(t.getFila())
                 .append("</td><td>").append(t.getColumna()).append("</td></tr>\n");
        }
        String cuerpo = tokens.isEmpty()
                ? "<div class=\"sin-datos\">No se reconocieron tokens.</div>"
                : "<table><tr><th>#</th><th>Lexema</th><th>Tipo</th><th>Fila</th><th>Columna</th></tr>"
                  + filas + "</table>";

        String html = plantilla("Reporte de tokens", "Total de tokens: " + tokens.size(), cuerpo);
        Files.write(Path.of(rutaSalida), html.getBytes(StandardCharsets.UTF_8));
    }

    // ---------- Reporte de errores ----------

    public void generarReporteErrores(List<ErrorLexico> errores, String rutaSalida) throws IOException {
        StringBuilder filas = new StringBuilder();
        for (ErrorLexico e : errores) {
            filas.append("<tr><td><code>").append(escapar(e.getLexemaOCaracter()))
                 .append("</code></td><td><span class=\"badge error-badge\">").append(e.getTipo())
                 .append("</span></td><td>").append(e.getFila()).append("</td><td>")
                 .append(e.getColumna()).append("</td></tr>\n");
        }
        String cuerpo = errores.isEmpty()
                ? "<div class=\"sin-datos\">No se encontraron errores lexicos.</div>"
                : "<table><tr><th>Lexema / caracter</th><th>Tipo de error</th><th>Fila</th><th>Columna</th></tr>"
                  + filas + "</table>";

        String html = plantilla("Reporte de errores", "Total de errores: " + errores.size(), cuerpo);
        Files.write(Path.of(rutaSalida), html.getBytes(StandardCharsets.UTF_8));
    }

    // ---------- Reporte de estadisticas ----------

    public void generarReporteEstadisticas(List<Token> tokens, List<ErrorLexico> errores,
                                            int totalLineas, String rutaSalida) throws IOException {
        Map<TipoToken, Integer> frecuencia = new EnumMap<>(TipoToken.class);
        for (Token t : tokens) {
            frecuencia.merge(t.getTipo(), 1, Integer::sum);
        }

        StringBuilder filas = new StringBuilder();
        for (Map.Entry<TipoToken, Integer> entrada : frecuencia.entrySet()) {
            filas.append("<tr><td>").append(badge(entrada.getKey())).append("</td><td>")
                 .append(entrada.getValue()).append("</td></tr>\n");
        }
        String tablaFrecuencia = frecuencia.isEmpty()
                ? "<div class=\"sin-datos\">Sin tokens que contabilizar.</div>"
                : "<table><tr><th>Tipo de token</th><th>Frecuencia</th></tr>" + filas + "</table>";

        String resumen = """
            <table style="margin-bottom:20px;">
              <tr><th>Metrica</th><th>Valor</th></tr>
              <tr><td>Total de tokens</td><td>%d</td></tr>
              <tr><td>Total de lineas</td><td>%d</td></tr>
              <tr><td>Total de errores</td><td>%d</td></tr>
            </table>
            """.formatted(tokens.size(), totalLineas, errores.size());

        String cuerpo = resumen + tablaFrecuencia;
        String html = plantilla("Reporte de estadisticas", "Resumen general del analisis", cuerpo);
        Files.write(Path.of(rutaSalida), html.getBytes(StandardCharsets.UTF_8));
    }

    // ---------- Plantilla comun ----------

    private String plantilla(String titulo, String meta, String cuerpo) {
        return """
            <!DOCTYPE html>
            <html lang="es">
            <head>
            <meta charset="UTF-8">
            <title>%s - PromptZal</title>
            <style>%s</style>
            </head>
            <body>
            <h1>%s</h1>
            <p class="meta">%s</p>
            %s
            </body>
            </html>
            """.formatted(titulo, CSS_COMPARTIDO, titulo, meta, cuerpo);
    }
}