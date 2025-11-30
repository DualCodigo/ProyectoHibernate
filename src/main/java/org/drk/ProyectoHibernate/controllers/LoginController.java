package org.drk.ProyectoHibernate.controllers;

import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.TextField;
import org.drk.ProyectoHibernate.session.AuthService;
import org.drk.ProyectoHibernate.session.SimpleSessionService;
import org.drk.ProyectoHibernate.user.User;
import org.drk.ProyectoHibernate.user.UserRepository;
import org.drk.ProyectoHibernate.utils.DataProvider;
import org.drk.ProyectoHibernate.utils.JavaFXUtil;

import java.net.URL;
import java.util.ResourceBundle;

public class LoginController implements Initializable {

    @FXML private TextField txtContraseña;
    @FXML private TextField txtCorreo;
    @FXML private Label info;
    @FXML private ComboBox<User> cmbUsuarios;

    private UserRepository userRepository;
    private AuthService authService;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        userRepository = new UserRepository(DataProvider.getSessionFactory());
        authService = new AuthService(userRepository);

        var users = userRepository.findAll();
        cmbUsuarios.setItems(FXCollections.observableArrayList(users));

        cmbUsuarios.setCellFactory(list -> new ListCell<>() {
            @Override
            protected void updateItem(User item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : formatUser(item));
            }
        });
        cmbUsuarios.setButtonCell(new ListCell<>() {
            @Override
            protected void updateItem(User item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : formatUser(item));
            }
        });

        cmbUsuarios.getSelectionModel().selectedItemProperty().addListener((obs, o, sel) -> {
            if (sel != null) {
                txtCorreo.setText(sel.getEmail());
                txtContraseña.setText(sel.getPassword());
            }
        });

        if (!users.isEmpty()) {
            cmbUsuarios.getSelectionModel().selectFirst();
        }
    }

    private String formatUser(User u) {
        String base = u.getEmail();
        if (isAdmin(u)) {
            return base + " *";
        }
        return base;
    }

    /**
     * Determina si el usuario es administrador.
     * Simplificado para usar directamente la propiedad is_admin
     * en lugar de reflexión.
     */
    private boolean isAdmin(User u) {
        return Boolean.TRUE.equals(u.getIs_admin());
    }

    @FXML
    public void entrar(ActionEvent e) {
        var selected = cmbUsuarios.getSelectionModel().getSelectedItem();
        if (selected != null) {
            SimpleSessionService session = new SimpleSessionService();
            session.login(selected);
            session.setObject("id", selected.getId());
            JavaFXUtil.setScene("/org/drk/ProyectoHibernate/main-view.fxml");
            return;
        }
        var user = authService.validateUser(txtCorreo.getText(), txtContraseña.getText());
        if (user.isPresent()) {
            SimpleSessionService session = new SimpleSessionService();
            session.login(user.get());
            session.setObject("id", user.get().getId());
            JavaFXUtil.setScene("/org/drk/ProyectoHibernate/main-view.fxml");
        } else {
            info.setText("Credenciales inválidas");
        }
    }

    @FXML
    public void Salir(ActionEvent e) {
        System.exit(0);
    }
}
