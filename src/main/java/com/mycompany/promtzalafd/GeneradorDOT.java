package com.mycompany.promtzalafd;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Genera el codigo DOT que describe el AFD de PromptZal (estructura fija,
 * documentada en docs/AFD_PromptZal_diseno.md) y usa Graphviz (comando
 * "dot", debe estar instalado y en el PATH del sistema) para producir
 * la imagen a partir de ese DOT. Graphviz solo dibuja: no participa en
 * el reconocimiento de tokens.
 */
public class GeneradorDOT {

    /** Construye el codigo DOT completo del AFD. */
    public String construirDot() {
        StringBuilder dot = new StringBuilder();
        dot.append("digraph AFD_PromptZal {\n");
        dot.append("  rankdir=LR;\n");
        dot.append("  node [fontname=\"Helvetica\", fontsize=11];\n");
        dot.append("  edge [fontname=\"Helvetica\", fontsize=9];\n\n");

        // Estado inicial
        dot.append("  inicio [shape=point];\n");
        dot.append("  inicio -> S0;\n\n");

        // Estados de aceptacion (doble circulo) vs intermedios (circulo simple) vs error
        String[] aceptacion = {"S1", "S2", "S2c", "S3b", "S4", "S5"};
        String[] intermedios = {"S0", "S2b", "S3", "S6", "S6a", "S6b", "S6c"};

        for (String s : aceptacion) {
            dot.append("  ").append(s).append(" [shape=doublecircle, style=filled, fillcolor=\"#DBEAFE\"];\n");
        }
        for (String s : intermedios) {
            dot.append("  ").append(s).append(" [shape=circle];\n");
        }
        dot.append("  qE [shape=circle, style=filled, fillcolor=\"#FEE2E2\", fontcolor=\"#991B1B\"];\n\n");

        // Transiciones
        dot.append("  S0 -> S1 [label=\"letra\"];\n");
        dot.append("  S1 -> S1 [label=\"letra|digito|_\"];\n");
        dot.append("  S1 -> S0 [label=\"otro (acepta)\"];\n\n");

        dot.append("  S0 -> S2 [label=\"digito\"];\n");
        dot.append("  S2 -> S2 [label=\"digito\"];\n");
        dot.append("  S2 -> S2b [label=\".\"];\n");
        dot.append("  S2 -> S0 [label=\"otro (acepta ENTERO)\"];\n");
        dot.append("  S2b -> S2c [label=\"digito\"];\n");
        dot.append("  S2b -> qE [label=\"otro\", color=\"#B91C1C\", fontcolor=\"#B91C1C\"];\n");
        dot.append("  S2c -> S2c [label=\"digito\"];\n");
        dot.append("  S2c -> S0 [label=\"otro (acepta DECIMAL)\"];\n\n");

        dot.append("  S0 -> S3 [label=\"@\"];\n");
        dot.append("  S3 -> S3b [label=\"letra\"];\n");
        dot.append("  S3 -> qE [label=\"otro\", color=\"#B91C1C\", fontcolor=\"#B91C1C\"];\n");
        dot.append("  S3b -> S3b [label=\"letra\"];\n");
        dot.append("  S3b -> S0 [label=\"otro (acepta DIRECTIVA)\"];\n\n");

        dot.append("  S0 -> S4 [label=\"comilla\"];\n");
        dot.append("  S4 -> S4 [label=\"cualquiera\"];\n");
        dot.append("  S4 -> S0 [label=\"comilla (acepta CADENA)\"];\n");
        dot.append("  S4 -> qE [label=\"salto de linea / EOF\", color=\"#B91C1C\", fontcolor=\"#B91C1C\"];\n\n");

        dot.append("  S0 -> S5 [label=\"guion\"];\n");
        dot.append("  S5 -> S0 [label=\"mayor-que (acepta FLECHA)\"];\n");
        dot.append("  S5 -> qE [label=\"otro\", color=\"#B91C1C\", fontcolor=\"#B91C1C\"];\n\n");

        dot.append("  S0 -> S6 [label=\"diagonal\"];\n");
        dot.append("  S6 -> S6a [label=\"diagonal\"];\n");
        dot.append("  S6 -> S6b [label=\"asterisco\"];\n");
        dot.append("  S6 -> qE [label=\"otro\", color=\"#B91C1C\", fontcolor=\"#B91C1C\"];\n");
        dot.append("  S6a -> S6a [label=\"cualquiera\"];\n");
        dot.append("  S6a -> S0 [label=\"salto de linea / EOF\"];\n");
        dot.append("  S6b -> S6b [label=\"cualquiera\"];\n");
        dot.append("  S6b -> S6c [label=\"asterisco\"];\n");
        dot.append("  S6b -> qE [label=\"EOF sin cerrar\", color=\"#B91C1C\", fontcolor=\"#B91C1C\"];\n");
        dot.append("  S6c -> S0 [label=\"diagonal (cierra)\"];\n");
        dot.append("  S6c -> S6c [label=\"asterisco\"];\n");
        dot.append("  S6c -> S6b [label=\"otro\"];\n");
        dot.append("  S6c -> qE [label=\"EOF sin cerrar\", color=\"#B91C1C\", fontcolor=\"#B91C1C\"];\n\n");

        dot.append("  S0 -> S0 [label=\"simbolo, espacio o salto\"];\n");
        dot.append("  S0 -> qE [label=\"caracter no reconocido\", color=\"#B91C1C\", fontcolor=\"#B91C1C\"];\n");

        dot.append("}\n");
        return dot.toString();
    }

    /** Escribe el codigo DOT a un archivo .dot */
    public void guardarDot(String rutaDot) throws IOException {
        Files.write(Path.of(rutaDot), construirDot().getBytes(StandardCharsets.UTF_8));
    }

    /**
     * Invoca Graphviz (comando "dot") para producir una imagen PNG a partir
     * del archivo .dot. Requiere Graphviz instalado y "dot" en el PATH.
     */
    public void generarImagen(String rutaDot, String rutaPng) throws IOException, InterruptedException {
        ProcessBuilder pb = new ProcessBuilder("dot", "-Tpng", rutaDot, "-o", rutaPng);
        pb.redirectErrorStream(true);
        Process proceso = pb.start();
        int codigo = proceso.waitFor();
        if (codigo != 0) {
            throw new IOException("Graphviz termino con codigo " + codigo
                    + ". Verifica que 'dot' este instalado y en el PATH.");
        }
    }
}