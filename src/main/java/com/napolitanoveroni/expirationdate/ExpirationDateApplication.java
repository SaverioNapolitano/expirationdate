package com.napolitanoveroni.expirationdate;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class ExpirationDateApplication extends Application {
    @Override
    public void start(Stage stage) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(ExpirationDateApplication.class.getResource("MainWindow-view.fxml"));
        Scene scene = new Scene(fxmlLoader.load());
        stage.setTitle("Expiration Date");
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        launch();
    }
}