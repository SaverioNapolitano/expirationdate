package com.napolitanoveroni.expirationdate;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.Objects;

public class ExpirationDateApplication extends Application {
    public static void main(String[] args) {
        launch();
    }

    @Override
    public void start(Stage stage) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(ExpirationDateApplication.class.getResource("MainWindow-view.fxml"));
        Scene scene = new Scene(fxmlLoader.load());
        stage.setTitle("Expiration Date");
        stage.setScene(scene);
        stage.getIcons().add(new Image(Objects.requireNonNull(getClass().getResourceAsStream("icons/app-icon.png"))));
        stage.show();
    }
}