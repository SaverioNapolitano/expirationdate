package com.napolitanoveroni.expirationdate;

import javafx.geometry.NodeOrientation;
import javafx.scene.control.Alert;
import javafx.scene.image.ImageView;

/**
 * The AlertDialog class provides static methods to display alert dialogs with different types and styles.
 *
 * @author SaverioNapolitano, MatteV02
 * @version 2023.07.10
 */
public class AlertDialog {

	/**
	 * Displays a warning alert dialog with the specified title, header, and content.
	 *
	 * @param title   the title of the alert dialog
	 * @param header  the header text of the alert dialog
	 * @param content the content text of the alert dialog
	 */
	public static void alertWarning(String title, String header, String content) {
		Alert alert = new Alert(Alert.AlertType.WARNING);

		alert.setTitle(title);
		alert.setHeaderText(header);
		alert.setContentText(content);

		alert.getDialogPane().getStylesheets().add("com/napolitanoveroni/expirationdate" + "/styles/jmetro/dark_theme.css");

		ImageView imageView = new ImageView("com/napolitanoveroni/expirationdate/icons/white-warning-icon.png");

		imageView.setFitWidth(40);
		imageView.setPreserveRatio(true);

		alert.setGraphic(imageView);

		alert.show();
	}

	/**
	 * Displays an error alert dialog with the specified error message.
	 *
	 * @param message the error message to be displayed in the alert dialog
	 */
	public static void alertError(String message) {
		Alert alert = new Alert(Alert.AlertType.ERROR, message);

		alert.getDialogPane().getStylesheets().add("com/napolitanoveroni/expirationdate" + "/styles/jmetro/dark_theme.css");

		ImageView imageView = new ImageView("com/napolitanoveroni/expirationdate/icons/white-sqlerror-icon.png");
		imageView.setFitWidth(40);
		imageView.setPreserveRatio(true);
		imageView.setNodeOrientation(NodeOrientation.INHERIT);

		alert.setGraphic(imageView);

		alert.show();
	}
}
