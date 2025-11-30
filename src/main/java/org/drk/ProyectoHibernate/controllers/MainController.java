package org.drk.ProyectoHibernate.controllers;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.control.SelectionMode;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import org.drk.ProyectoHibernate.copia.Copia;
import org.drk.ProyectoHibernate.copia.CopiaRepository;
import org.drk.ProyectoHibernate.pelicula.Pelicula;
import org.drk.ProyectoHibernate.pelicula.PeliculaRepository;
import org.drk.ProyectoHibernate.session.SimpleSessionService;
import org.drk.ProyectoHibernate.user.User;
import org.drk.ProyectoHibernate.utils.DataProvider;
import org.drk.ProyectoHibernate.utils.JavaFXUtil;

import java.net.URL;
import java.time.LocalDateTime;
import java.util.ResourceBundle;

public class MainController implements Initializable {

    @FXML private TableView<Pelicula> tablaPeliculas;
    @FXML private TableColumn<Pelicula,String> colPeliId;
    @FXML private TableColumn<Pelicula,String> colPeliTitulo;
    @FXML private TableColumn<Pelicula,String> colPeliAnio;
    @FXML private TableColumn<Pelicula,String> colPeliGenero;
    @FXML private TableColumn<Pelicula,String> colPeliDirector;

    @FXML private TableView<Copia> tablaCopias;
    @FXML private TableColumn<Copia,String> colCopiaId;
    @FXML private TableColumn<Copia,String> colCopiaTitulo;
    @FXML private TableColumn<Copia,String> colCopiaEstado;
    @FXML private TableColumn<Copia,String> colCopiaSoporte;

    @FXML private Label lblUsuarioActual;
    @FXML private HBox boxAdmin;

    @FXML private ComboBox<String> cmbEstado;
    @FXML private ComboBox<String> cmbSoporte;

    @FXML private Button btnAddPeli;
    @FXML private Button btnEditPeli;
    @FXML private Button btnDelPeli;
    @FXML private Button btnCerrarSesion;

    private final SimpleSessionService sessionService =
            new SimpleSessionService();
    private final PeliculaRepository peliculaRepository =
            new PeliculaRepository(DataProvider.getSessionFactory());
    private final CopiaRepository copiaRepository =
            new CopiaRepository(DataProvider.getSessionFactory());

    private User active;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // Usuario activo desde la sesión
        active = sessionService.getActive();

        // Configurar tablas y cargar datos
        setupTables();
        loadPeliculas();
        loadCopias();

        // Mostrar usuario actual
        lblUsuarioActual.setText(formatUser(active));

        // Mostrar/ocultar zona admin
        boolean admin = isAdmin(active);
        boxAdmin.setVisible(admin);
        boxAdmin.setManaged(admin);

        // Título de la ventana
        String winTitle = "Gestor de copias y películas"
                + (admin ? " (admin)" : "");
        if (JavaFXUtil.getStage() != null) {
            JavaFXUtil.getStage().setTitle(winTitle);
        }

        // Combos de estado y soporte
        cmbEstado.setItems(FXCollections.observableArrayList(
                "bueno", "nuevo", "regular", "dañado"
        ));
        cmbEstado.getSelectionModel().selectFirst();

        cmbSoporte.setItems(FXCollections.observableArrayList(
                "DVD", "BluRay", "Digital", "VHS"
        ));
        cmbSoporte.getSelectionModel().selectFirst();

        // Cuando seleccionamos una copia, reflejar su estado/soporte en los combos
        tablaCopias.getSelectionModel().selectedItemProperty()
                .addListener((obs, oldSel, sel) -> {
                    if (sel != null) {
                        if (sel.getEstado() != null) {
                            cmbEstado.setValue(sel.getEstado());
                        }
                        if (sel.getSoporte() != null) {
                            cmbSoporte.setValue(sel.getSoporte());
                        }
                    }
                });
    }

    // --- Métodos privados de apoyo ------------------------------------------

    private void setupTables() {
        colPeliId.setCellValueFactory(r ->
                new SimpleStringProperty(String.valueOf(r.getValue().getId())));
        colPeliTitulo.setCellValueFactory(r ->
                new SimpleStringProperty(r.getValue().getTitulo()));
        colPeliAnio.setCellValueFactory(r ->
                new SimpleStringProperty(
                        r.getValue().getAnio() != null
                                ? r.getValue().getAnio().toString()
                                : "-"
                ));
        colPeliGenero.setCellValueFactory(r ->
                new SimpleStringProperty(r.getValue().getGenero()));
        colPeliDirector.setCellValueFactory(r ->
                new SimpleStringProperty(r.getValue().getDirector()));

        colCopiaId.setCellValueFactory(r ->
                new SimpleStringProperty(String.valueOf(r.getValue().getId())));
        colCopiaTitulo.setCellValueFactory(r ->
                new SimpleStringProperty(
                        r.getValue().getMovie() != null
                                ? r.getValue().getMovie().getTitulo()
                                : "-"
                ));
        colCopiaEstado.setCellValueFactory(r ->
                new SimpleStringProperty(r.getValue().getEstado()));
        colCopiaSoporte.setCellValueFactory(r ->
                new SimpleStringProperty(r.getValue().getSoporte()));

        // Selección simple en ambas tablas
        tablaPeliculas.getSelectionModel()
                .setSelectionMode(SelectionMode.SINGLE);
        tablaCopias.getSelectionModel()
                .setSelectionMode(SelectionMode.SINGLE);
    }

    private void loadPeliculas() {
        tablaPeliculas.setItems(FXCollections.observableArrayList(
                peliculaRepository.findAll()
        ));
    }

    private void loadCopias() {
        tablaCopias.setItems(FXCollections.observableArrayList(
                copiaRepository.findByUser(active)
        ));
    }

    private String formatUser(User u) {
        return isAdmin(u) ? u.getEmail() + " *" : u.getEmail();
    }

    private boolean isAdmin(User u) {
        return u != null && Boolean.TRUE.equals(u.getIs_admin());
    }

    // --- ACCIONES SOBRE COPIAS ----------------------------------------------

    @FXML
    public void onCreateCopia(ActionEvent e) {
        Pelicula selected = tablaPeliculas.getSelectionModel()
                .getSelectedItem();
        if (selected == null) {
            JavaFXUtil.showModal(
                    Alert.AlertType.WARNING,
                    "Copia",
                    "Película no seleccionada",
                    "Selecciona una película primero."
            );
            return;
        }

        Copia c = new Copia();
        c.setMovie(selected);
        c.setUser(active);
        c.setEstado(cmbEstado.getValue());
        c.setSoporte(cmbSoporte.getValue());
        copiaRepository.save(c);
        loadCopias();
    }


    @FXML
    public void onEditCopia(ActionEvent e) {
        Copia copia = tablaCopias.getSelectionModel().getSelectedItem();
        if (copia == null) {
            JavaFXUtil.showModal(
                    Alert.AlertType.WARNING,
                    "Editar copia",
                    "Copia no seleccionada",
                    "Selecciona primero una copia en la tabla."
            );
            return;
        }

        // Solo permitir editar las copias del usuario activo
        if (copia.getUser() == null || copia.getUser().getId() == null
                || active == null || active.getId() == null
                || !copia.getUser().getId().equals(active.getId())) {

            JavaFXUtil.showModal(
                    Alert.AlertType.ERROR,
                    "Editar copia",
                    "No permitido",
                    "Solo puedes modificar tus propias copias."
            );
            return;
        }

        // Abrimos el diálogo modal
        boolean ok = showCopiaDialog(copia);
        if (!ok) {
            return; // usuario canceló
        }

        // Persistimos cambios
        copiaRepository.update(copia);

        // Opciones para refrescar:
        // 1) Solo refrescar tabla:
        tablaCopias.refresh();

        // 2) (si quieres estar 100% seguro de recargar desde BD):
        // loadCopias();
        // tablaCopias.getSelectionModel().clearSelection();
    }


    @FXML
    public void onDeleteCopia(ActionEvent e) {
        // 1) ¿Hay copia seleccionada?
        Copia copia = tablaCopias.getSelectionModel().getSelectedItem();
        if (copia == null) {
            JavaFXUtil.showModal(
                    Alert.AlertType.WARNING,
                    "Eliminar copia",
                    "Copia no seleccionada",
                    "Selecciona primero una copia en la tabla."
            );
            return;
        }

        // 2) Solo permitir eliminar copias del usuario activo
        if (copia.getUser() == null || copia.getUser().getId() == null
                || active == null || active.getId() == null
                || !copia.getUser().getId().equals(active.getId())) {

            JavaFXUtil.showModal(
                    Alert.AlertType.ERROR,
                    "Eliminar copia",
                    "No permitido",
                    "Solo puedes eliminar tus propias copias."
            );
            return;
        }

        // 3) Confirmación
        String tituloPeli = (copia.getMovie() != null && copia.getMovie().getTitulo() != null)
                ? copia.getMovie().getTitulo()
                : "-";

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Eliminar copia");
        confirm.setHeaderText("¿Eliminar la copia seleccionada?");
        confirm.setContentText(
                "Película: " + tituloPeli +
                        "\nEstado: " + copia.getEstado() +
                        "\nSoporte: " + copia.getSoporte()
        );

        var result = confirm.showAndWait();
        if (result.isEmpty() || result.get() != ButtonType.OK) {
            return; // usuario canceló
        }

        // 4) Borrar en BD + 5) refrescar UI
        try {
            copiaRepository.delete(copia);           // elimina en la BD
            tablaCopias.getItems().remove(copia);   // elimina de la tabla actual
            tablaCopias.getSelectionModel().clearSelection();
            cmbEstado.getSelectionModel().selectFirst();
            cmbSoporte.getSelectionModel().selectFirst();
        } catch (Exception ex) {
            ex.printStackTrace();
            JavaFXUtil.showModal(
                    Alert.AlertType.ERROR,
                    "Eliminar copia",
                    "Error al eliminar la copia",
                    (ex.getMessage() != null && !ex.getMessage().isBlank())
                            ? ex.getMessage()
                            : "Revisa la consola para más detalles."
            );
        }
    }

    // --- Diálogo genérico para añadir / editar película ----------------------

    /**
     * Muestra un diálogo para editar todos los campos de una película.
     * Si el usuario acepta, actualiza el objeto recibido y devuelve true.
     */
    private boolean showPeliculaDialog(Pelicula pelicula, boolean esNueva) {
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle(esNueva ? "Añadir película" : "Editar película");
        dialog.setHeaderText(esNueva ? "Crear nueva película"
                : "Modificar datos de la película");

        ButtonType okButtonType =
                new ButtonType("Aceptar", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes()
                .addAll(okButtonType, ButtonType.CANCEL);

        TextField txtTitulo = new TextField(
                pelicula.getTitulo() != null ? pelicula.getTitulo() : ""
        );
        TextField txtAnio = new TextField(
                pelicula.getAnio() != null ? pelicula.getAnio().toString() : ""
        );
        TextField txtGenero = new TextField(
                pelicula.getGenero() != null ? pelicula.getGenero() : ""
        );
        TextField txtDirector = new TextField(
                pelicula.getDirector() != null ? pelicula.getDirector() : ""
        );

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(10));

        grid.addRow(0, new Label("Título:"), txtTitulo);
        grid.addRow(1, new Label("Año:"), txtAnio);
        grid.addRow(2, new Label("Género:"), txtGenero);
        grid.addRow(3, new Label("Director:"), txtDirector);

        dialog.getDialogPane().setContent(grid);

        dialog.setResultConverter(button -> {
            if (button == okButtonType) {
                pelicula.setTitulo(txtTitulo.getText());

                // Año: intentar parsear, si no se puede queda null
                Integer anio = null;
                String anioText = txtAnio.getText();
                if (anioText != null && !anioText.isBlank()) {
                    try {
                        anio = Integer.parseInt(anioText.trim());
                    } catch (NumberFormatException ex) {
                        // Aquí podrías mostrar un aviso, de momento lo dejamos en null
                    }
                }
                pelicula.setAnio(anio);

                pelicula.setGenero(txtGenero.getText());
                pelicula.setDirector(txtDirector.getText());
            }
            return button;
        });

        var result = dialog.showAndWait();
        return result.isPresent() && result.get() == okButtonType;
    }
    /**
     * Diálogo modal para editar una copia (estado y soporte) usando
     * listas predefinidas:
     *   Estado  ∈ {nuevo, bueno, regular, dañado}
     *   Soporte ∈ {DVD, BluRay, Digital, VHS}
     * Devuelve true si el usuario pulsa Aceptar y se actualiza el objeto copia.
     */
    private boolean showCopiaDialog(Copia copia) {
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Editar copia");
        dialog.setHeaderText("Modificar datos de la copia");

        ButtonType okButtonType =
                new ButtonType("Aceptar", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes()
                .addAll(okButtonType, ButtonType.CANCEL);

        // ComboBox para ESTADO
        ComboBox<String> cmbEstadoDlg = new ComboBox<>();
        cmbEstadoDlg.getItems().addAll("nuevo", "bueno", "regular", "dañado");

        // Preseleccionar el estado actual si está en la lista
        if (copia.getEstado() != null &&
                cmbEstadoDlg.getItems().contains(copia.getEstado())) {
            cmbEstadoDlg.setValue(copia.getEstado());
        } else {
            cmbEstadoDlg.getSelectionModel().selectFirst();
        }

        // ComboBox para SOPORTE
        ComboBox<String> cmbSoporteDlg = new ComboBox<>();
        cmbSoporteDlg.getItems().addAll("DVD", "BluRay", "Digital", "VHS");

        // Preseleccionar el soporte actual si está en la lista
        if (copia.getSoporte() != null &&
                cmbSoporteDlg.getItems().contains(copia.getSoporte())) {
            cmbSoporteDlg.setValue(copia.getSoporte());
        } else {
            cmbSoporteDlg.getSelectionModel().selectFirst();
        }

        // Layout del diálogo
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(10));

        grid.addRow(0, new Label("Estado:"), cmbEstadoDlg);
        grid.addRow(1, new Label("Soporte:"), cmbSoporteDlg);

        dialog.getDialogPane().setContent(grid);

        dialog.setResultConverter(button -> {
            if (button == okButtonType) {
                String estadoSel  = cmbEstadoDlg.getValue();
                String soporteSel = cmbSoporteDlg.getValue();

                if (estadoSel != null && !estadoSel.isBlank()) {
                    copia.setEstado(estadoSel);
                }
                if (soporteSel != null && !soporteSel.isBlank()) {
                    copia.setSoporte(soporteSel);
                }
            }
            return button;
        });

        var result = dialog.showAndWait();
        return result.isPresent() && result.get() == okButtonType;
    }



    // --- ACCIONES SOBRE PELÍCULAS (ADMIN) -----------------------------------

    @FXML
    public void onAddPelicula(ActionEvent e) {
        if (!isAdmin(active)) {
            return;
        }

        Pelicula p = new Pelicula();
        // valor por defecto para el año, visible en el diálogo
        p.setAnio(LocalDateTime.now().getYear());
        p.setGenero("");
        p.setDirector("");

        boolean ok = showPeliculaDialog(p, true);
        if (ok) {
            peliculaRepository.save(p);
            loadPeliculas();
        }
    }

    @FXML
    public void onEditPelicula(ActionEvent e) {
        if (!isAdmin(active)) {
            return;
        }
        Pelicula sel = tablaPeliculas.getSelectionModel().getSelectedItem();
        if (sel == null) {
            return;
        }

        boolean ok = showPeliculaDialog(sel, false);
        if (ok) {
            peliculaRepository.update(sel);
            tablaPeliculas.refresh();
        }
    }

    @FXML
    public void onDeletePelicula(ActionEvent e) {
        if (!isAdmin(active)) {
            return;
        }
        Pelicula sel = tablaPeliculas.getSelectionModel().getSelectedItem();
        if (sel == null) {
            JavaFXUtil.showModal(
                    Alert.AlertType.WARNING,
                    "Eliminar película",
                    "Película no seleccionada",
                    "Selecciona primero una película en la tabla."
            );
            return;
        }

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Eliminar película");
        confirm.setHeaderText("¿Eliminar la película seleccionada?");
        confirm.setContentText("Título: " + sel.getTitulo());
        var result = confirm.showAndWait();
        if (result.isEmpty() || result.get() != ButtonType.OK) {
            return;
        }

        try {
            peliculaRepository.delete(sel);
            tablaPeliculas.getItems().remove(sel);
            // Actualizar también las copias visibles por si han quedado huérfanas en la tabla
            loadCopias();
        } catch (Exception ex) {
            ex.printStackTrace();

            // Buscar causa raíz para detectar la violación de FK
            Throwable cause = ex;
            while (cause.getCause() != null) {
                cause = cause.getCause();
            }

            if (cause instanceof java.sql.SQLIntegrityConstraintViolationException) {
                JavaFXUtil.showModal(
                        Alert.AlertType.ERROR,
                        "Eliminar película",
                        "No se puede eliminar la película",
                        "La película tiene copias asociadas en la base de datos.\n" +
                                "Elimina primero todas las copias relacionadas."
                );
            } else {
                JavaFXUtil.showModal(
                        Alert.AlertType.ERROR,
                        "Eliminar película",
                        "Error al eliminar",
                        (cause.getMessage() != null && !cause.getMessage().isBlank())
                                ? cause.getMessage()
                                : "Revisa la consola para más detalles."
                );
            }
        }
    }

    // --- CERRAR SESIÓN -------------------------------------------------------

    @FXML
    public void onCerrarSesion(ActionEvent e) {
        // Si tu SimpleSessionService tiene un método logout(), podrías llamarlo aquí.
        // sessionService.logout();

        // Volver a la pantalla de login
        JavaFXUtil.setScene("/org/drk/ProyectoHibernate/login-view.fxml");
    }
}
