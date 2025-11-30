package org.drk.ProyectoHibernate;

import javafx.application.Application;
import javafx.stage.Stage;
import org.drk.ProyectoHibernate.utils.JavaFXUtil;

import java.io.IOException;

public class App extends Application {
    @Override
    public void start(Stage stage) throws IOException {
        JavaFXUtil.initStage(stage);
        JavaFXUtil.setScene("/org/drk/ProyectoHibernate/login-view.fxml");
    }
}
