/**
 * The ExpirationDateApplication class is the entry point for the Expiration Date application.
 * <p>This class extends the JavaFX Application class and provides the main method to launch the application.</p>
 * <p>Upon starting, the application loads the MainWindow-view.fxml file using FXMLLoader,
 * sets up the main scene, configures the stage with the application title and icon,
 * and displays the main window.</p>
 * <p>Note: This class assumes the existence of the MainWindow-view.fxml file and the icons/app-icon.png resource.</p>
 */

package com.napolitanoveroni.expirationdate;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.Objects;

/**
 * The ExpirationDateApplication class is the entry point for the Expiration Date application.
 *
 * @author SaverioNapolitano, MatteV02
 * @version 2023.07.10
 */
public class ExpirationDateApplication extends Application {
	/**
	 * The main method that launches the Expiration Date application.
	 *
	 * @param args the command-line arguments
	 */
	public static void main(String[] args) {
		launch();
	}

	/**
	 * The start method that initializes and starts the application.
	 *
	 * @param stage the primary stage for the application
	 *
	 * @throws IOException if an error occurs during loading the FXML file
	 */
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