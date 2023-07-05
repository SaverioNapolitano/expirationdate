package com.napolitanoveroni.expirationdate;

import javafx.geometry.NodeOrientation;
import javafx.scene.control.Alert;
import javafx.scene.image.ImageView;

public class AlertDialog {

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
