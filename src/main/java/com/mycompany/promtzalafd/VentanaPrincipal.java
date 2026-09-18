package com.mycompany.promtzalafd;

import java.awt.Desktop;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.EnumMap;

import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.*;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

/**
 * Interfaz grafica de PromptZal. Editor + panel de tokens + panel de
 * errores + accesos a los reportes HTML y a la imagen del AFD.
 */
public class VentanaPrincipal extends Application {

    private TextArea editor;
    private TableView<Token> tablaTokens;
    private TableView<ErrorLexico> tablaErrores;
    private Label labelTotalTokens;
    private Label labelTotalErrores;
    private Label labelTotalLineas;
    private BarChart<String, Number> graficoDistribucion;
    private File archivoActual;

    private final ObservableList<Token> tokens = FXCollections.observableArrayList();
    private final ObservableList<ErrorLexico> errores = FXCollections.observableArrayList();

    @Override
    public void start(Stage stage) {
        BorderPane root = new BorderPane();
        root.setLeft(construirSidebar(stage));
        root.setCenter(construirAreaCentral());
        root.setRight(construirPanelDerecho());
        root.setPadding(new Insets(20));

        Scene scene = new Scene(root, 1300, 820);
        scene.getStylesheets().add(getClass().getResource("/styles.css").toExternalForm());

        stage.setTitle("PromptZal — Analizador Léxico");
        stage.setScene(scene);
        stage.show();
    }

    // ---------- Sidebar ----------

    private VBox construirSidebar(Stage stage) {
        VBox sidebar = new VBox(8);
        sidebar.getStyleClass().add("sidebar");
        sidebar.setPrefWidth(210);
        sidebar.setPadding(new Insets(10));

        Label titulo = new Label("PromptZal");
        titulo.getStyleClass().add("sidebar-titulo");

        Button btnAnalizar = crearBotonSidebar("Analizar", true);
        btnAnalizar.setOnAction(e -> analizar());

        Button btnNuevo = crearBotonSidebar("Nuevo", false);
        btnNuevo.setOnAction(e -> nuevoArchivo());

        Button btnAbrir = crearBotonSidebar("Abrir .pz", false);
        btnAbrir.setOnAction(e -> abrirArchivo(stage));

        Button btnGuardar = crearBotonSidebar("Guardar", false);
        btnGuardar.setOnAction(e -> guardarArchivo(stage));

        Separator sep = new Separator();
        sep.setPadding(new Insets(10, 0, 10, 0));

        Label reportesLabel = new Label("Reportes");
        reportesLabel.getStyleClass().add("card-subtitulo");

        Button btnReporteTokens = crearBotonSidebar("Reporte de tokens", false);
        btnReporteTokens.setOnAction(e -> generarYAbrirReporteTokens());

        Button btnReporteErrores = crearBotonSidebar("Reporte de errores", false);
        btnReporteErrores.setOnAction(e -> generarYAbrirReporteErrores());

        Button btnReporteStats = crearBotonSidebar("Estadísticas", false);
        btnReporteStats.setOnAction(e -> generarYAbrirReporteEstadisticas());

        Button btnAFD = crearBotonSidebar("Imagen del AFD", false);
        btnAFD.setOnAction(e -> generarYAbrirImagenAFD());

        sidebar.getChildren().addAll(
                titulo, new Separator(), btnAnalizar, btnNuevo, btnAbrir, btnGuardar,
                sep, reportesLabel, btnReporteTokens, btnReporteErrores, btnReporteStats, btnAFD
        );
        return sidebar;
    }

    private Button crearBotonSidebar(String texto, boolean activo) {
        Button b = new Button(texto);
        b.getStyleClass().add(activo ? "sidebar-boton-activo" : "sidebar-boton");
        b.setMaxWidth(Double.MAX_VALUE);
        return b;
    }

    // ---------- Area central: editor ----------

    private VBox construirAreaCentral() {
        VBox centro = new VBox(16);
        centro.setPadding(new Insets(0, 16, 0, 16));

        Label bienvenida = new Label("Editor de programa .pz");
        bienvenida.setStyle("-fx-font-size: 20px; -fx-font-weight: bold;");
        Label subtitulo = new Label("Escribe o pega tu programa PromptZal y presiona Analizar");
        subtitulo.getStyleClass().add("card-subtitulo");

        editor = new TextArea();
        editor.getStyleClass().add("editor");
        editor.setPromptText("@modelo \"claude-sonnet-4-6\"\n@rol \"analista de datos\"\n...");
        VBox.setVgrow(editor, Priority.ALWAYS);

        VBox editorCard = new VBox(10, editor);
        editorCard.getStyleClass().add("card");
        VBox.setVgrow(editorCard, Priority.ALWAYS);

        HBox accionesRapidas = new HBox(10);
        Button btnAnalizarGrande = new Button("Analizar programa");
        btnAnalizarGrande.getStyleClass().add("boton-primario");
        btnAnalizarGrande.setOnAction(e -> analizar());
        accionesRapidas.getChildren().add(btnAnalizarGrande);
        accionesRapidas.setAlignment(Pos.CENTER_RIGHT);

        centro.getChildren().addAll(bienvenida, subtitulo, editorCard, accionesRapidas);
        return centro;
    }

    // ---------- Panel derecho: tokens, errores, estadisticas ----------

    private VBox construirPanelDerecho() {
        VBox panel = new VBox(16);
        panel.setPrefWidth(420);
        panel.setPadding(new Insets(0, 0, 0, 4));

        HBox resumen = new HBox(20);
        labelTotalTokens = new Label("0");
        labelTotalErrores = new Label("0");
        labelTotalLineas = new Label("0");
        resumen.getChildren().addAll(
                crearMiniStat("Tokens", labelTotalTokens),
                crearMiniStat("Errores", labelTotalErrores),
                crearMiniStat("Líneas", labelTotalLineas)
        );
        VBox resumenCard = new VBox(resumen);
        resumenCard.getStyleClass().add("card-oscura");

        Label tituloTokens = new Label("Tokens reconocidos");
        tituloTokens.getStyleClass().add("card-titulo");
        tablaTokens = construirTablaTokens();
        VBox.setVgrow(tablaTokens, Priority.ALWAYS);
        VBox tokensCard = new VBox(8, tituloTokens, tablaTokens);
        tokensCard.getStyleClass().add("card");
        VBox.setVgrow(tokensCard, Priority.ALWAYS);

        Label tituloErrores = new Label("Errores léxicos");
        tituloErrores.getStyleClass().add("card-titulo");
        tablaErrores = construirTablaErrores();
        VBox erroresCard = new VBox(8, tituloErrores, tablaErrores);
        erroresCard.getStyleClass().add("card");

        Label tituloGrafico = new Label("Distribución por tipo");
        tituloGrafico.getStyleClass().add("card-titulo");

        CategoryAxis ejeX = new CategoryAxis();
        NumberAxis ejeY = new NumberAxis();
        ejeY.setLabel("Cantidad");
        graficoDistribucion = new BarChart<>(ejeX, ejeY);
        graficoDistribucion.setLegendVisible(false);
        graficoDistribucion.setAnimated(true);
        graficoDistribucion.setPrefHeight(220);
        graficoDistribucion.setCategoryGap(6);
        VBox graficoCard = new VBox(8, tituloGrafico, graficoDistribucion);
        graficoCard.getStyleClass().add("card");

        panel.getChildren().addAll(resumenCard, tokensCard, erroresCard, graficoCard);
        return panel;
    }

    private VBox crearMiniStat(String etiqueta, Label valor) {
        valor.setStyle("-fx-font-size: 22px; -fx-font-weight: bold; -fx-text-fill: white;");
        Label lbl = new Label(etiqueta);
        lbl.setStyle("-fx-font-size: 11px; -fx-text-fill: #C7C4D6;");
        VBox box = new VBox(2, valor, lbl);
        return box;
    }

    @SuppressWarnings("unchecked")
    private TableView<Token> construirTablaTokens() {
        TableView<Token> tabla = new TableView<>(tokens);
        TableColumn<Token, String> colLexema = new TableColumn<>("Lexema");
        colLexema.setCellValueFactory(new PropertyValueFactory<>("lexema"));
        TableColumn<Token, TipoToken> colTipo = new TableColumn<>("Tipo");
        colTipo.setCellValueFactory(new PropertyValueFactory<>("tipo"));
        TableColumn<Token, Integer> colFila = new TableColumn<>("Fila");
        colFila.setCellValueFactory(new PropertyValueFactory<>("fila"));
        colFila.setPrefWidth(50);
        TableColumn<Token, Integer> colCol = new TableColumn<>("Col");
        colCol.setCellValueFactory(new PropertyValueFactory<>("columna"));
        colCol.setPrefWidth(50);
        tabla.getColumns().addAll(colLexema, colTipo, colFila, colCol);
        tabla.setPlaceholder(new Label("Aún no se ha analizado nada"));
        return tabla;
    }

    @SuppressWarnings("unchecked")
    private TableView<ErrorLexico> construirTablaErrores() {
        TableView<ErrorLexico> tabla = new TableView<>(errores);
        TableColumn<ErrorLexico, String> colLexema = new TableColumn<>("Lexema/car.");
        colLexema.setCellValueFactory(new PropertyValueFactory<>("lexemaOCaracter"));
        TableColumn<ErrorLexico, ErrorLexico.TipoErrorLexico> colTipo = new TableColumn<>("Tipo de error");
        colTipo.setCellValueFactory(new PropertyValueFactory<>("tipo"));
        TableColumn<ErrorLexico, Integer> colFila = new TableColumn<>("Fila");
        colFila.setCellValueFactory(new PropertyValueFactory<>("fila"));
        colFila.setPrefWidth(50);
        tabla.getColumns().addAll(colLexema, colTipo, colFila);
        tabla.setPlaceholder(new Label("Sin errores todavía"));
        return tabla;
    }

    // ---------- Acciones ----------

    private void nuevoArchivo() {
        editor.clear();
        tokens.clear();
        errores.clear();
        graficoDistribucion.getData().clear();
        archivoActual = null;
        actualizarResumen(0);
    }

    private void abrirArchivo(Stage stage) {
        FileChooser fc = new FileChooser();
        fc.setTitle("Abrir archivo .pz");
        fc.getExtensionFilters().add(new FileChooser.ExtensionFilter("Archivos PromptZal", "*.pz"));
        File archivo = fc.showOpenDialog(stage);
        if (archivo != null) {
            try {
                String contenido = Files.readString(archivo.toPath(), StandardCharsets.UTF_8);
                editor.setText(contenido);
                archivoActual = archivo;
            } catch (IOException e) {
                mostrarError("No se pudo abrir el archivo: " + e.getMessage());
            }
        }
    }

    private void guardarArchivo(Stage stage) {
        File destino = archivoActual;
        if (destino == null) {
            FileChooser fc = new FileChooser();
            fc.setTitle("Guardar archivo .pz");
            fc.getExtensionFilters().add(new FileChooser.ExtensionFilter("Archivos PromptZal", "*.pz"));
            destino = fc.showSaveDialog(stage);
        }
        if (destino != null) {
            try {
                Files.writeString(destino.toPath(), editor.getText(), StandardCharsets.UTF_8);
                archivoActual = destino;
            } catch (IOException e) {
                mostrarError("No se pudo guardar el archivo: " + e.getMessage());
            }
        }
    }

    private void analizar() {
        Lexer lexer = new Lexer(editor.getText());
        lexer.analizar();

        tokens.setAll(lexer.getTokens());
        errores.setAll(lexer.getErrores());

        int totalLineas = (int) editor.getText().lines().count();
        actualizarResumen(totalLineas);
        actualizarGrafico();
    }

    private void actualizarResumen(int totalLineas) {
        labelTotalTokens.setText(String.valueOf(tokens.size()));
        labelTotalErrores.setText(String.valueOf(errores.size()));
        labelTotalLineas.setText(String.valueOf(totalLineas));
    }

    private void actualizarGrafico() {
        Map<TipoToken, Integer> frecuencia = new EnumMap<>(TipoToken.class);
        for (Token t : tokens) {
            frecuencia.merge(t.getTipo(), 1, Integer::sum);
        }
        XYChart.Series<String, Number> serie = new XYChart.Series<>();
        for (Map.Entry<TipoToken, Integer> e : frecuencia.entrySet()) {
            serie.getData().add(new XYChart.Data<>(e.getKey().toString(), e.getValue()));
        }
        graficoDistribucion.getData().setAll(serie);
    }

    private void generarYAbrirReporteTokens() {
        try {
            GeneradorReportes gr = new GeneradorReportes();
            String ruta = "reporte_tokens.html";
            gr.generarReporteTokens(tokens, ruta);
            abrirEnNavegador(ruta);
        } catch (IOException e) {
            mostrarError("Error generando el reporte de tokens: " + e.getMessage());
        }
    }

    private void generarYAbrirReporteErrores() {
        try {
            GeneradorReportes gr = new GeneradorReportes();
            String ruta = "reporte_errores.html";
            gr.generarReporteErrores(errores, ruta);
            abrirEnNavegador(ruta);
        } catch (IOException e) {
            mostrarError("Error generando el reporte de errores: " + e.getMessage());
        }
    }

    private void generarYAbrirReporteEstadisticas() {
        try {
            GeneradorReportes gr = new GeneradorReportes();
            int totalLineas = (int) editor.getText().lines().count();
            String ruta = "reporte_estadisticas.html";
            gr.generarReporteEstadisticas(tokens, errores, totalLineas, ruta);
            abrirEnNavegador(ruta);
        } catch (IOException e) {
            mostrarError("Error generando el reporte de estadísticas: " + e.getMessage());
        }
    }

    private void generarYAbrirImagenAFD() {
        try {
            GeneradorDOT gd = new GeneradorDOT();
            String rutaDot = "afd_promptzal.dot";
            String rutaPng = "afd_promptzal.png";
            gd.guardarDot(rutaDot);
            gd.generarImagen(rutaDot, rutaPng);
            if (Desktop.isDesktopSupported()) {
                Desktop.getDesktop().open(new File(rutaPng));
            }
        } catch (IOException | InterruptedException e) {
            mostrarError("Error generando la imagen del AFD: " + e.getMessage()
                    + "\n¿Tienes Graphviz instalado y en el PATH?");
        }
    }

    private void abrirEnNavegador(String rutaHtml) {
        try {
            if (Desktop.isDesktopSupported()) {
                Desktop.getDesktop().browse(Path.of(rutaHtml).toUri());
            }
        } catch (IOException e) {
            mostrarError("No se pudo abrir el reporte: " + e.getMessage());
        }
    }

    private void mostrarError(String mensaje) {
        Alert alert = new Alert(Alert.AlertType.ERROR, mensaje);
        alert.showAndWait();
    }

    public static void main(String[] args) {
        launch(args);
    }
}