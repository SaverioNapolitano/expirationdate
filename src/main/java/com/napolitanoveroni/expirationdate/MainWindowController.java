/**
 * The MainWindowController class is responsible for handling user interactions and managing the main window of the application.
 * <p>This controller is associated with the MainWindow-view.fxml file, which defines the layout of the main window.</p>
 * <p>The controller handles actions related to the expiration list and the shopping list views, including adding and editing products,
 * deleting products, updating the calendar events, and managing the UI elements.</p>
 * <p>Note: This class assumes the usage of external libraries, such as biweekly and JavaFX.</p>
 * <p>Note: This class depends on the UtilsDB and AlertDialog classes.</p>
 * <p>Note: This class assumes the existence of the Product, EditProductController, and RecipeWindowController classes.</p>
 */

package com.napolitanoveroni.expirationdate;

import biweekly.ICalVersion;
import biweekly.ICalendar;
import biweekly.component.VAlarm;
import biweekly.component.VEvent;
import biweekly.io.text.ICalWriter;
import biweekly.parameter.Related;
import biweekly.property.Organizer;
import biweekly.property.Status;
import biweekly.property.Summary;
import biweekly.property.Trigger;
import biweekly.util.Duration;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Dialog;
import javafx.scene.control.TextField;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.sql.SQLIntegrityConstraintViolationException;
import java.time.LocalDate;
import java.util.List;
import java.util.*;
import java.util.stream.Collectors;

import static com.napolitanoveroni.expirationdate.UtilsDB.*;

/**
 * The MainWindowController class is responsible for handling user interactions and managing the main window of the application.
 *
 * @author SaverioNapolitano, MatteV02
 * @version 2023.07.10
 */
public class MainWindowController {

	ObservableList<Product> expirationList;
	@FXML
	private TableColumn<Product, LocalDate> expirationListExpirationDateColumn;
	@FXML
	private TableColumn<Product, String> expirationListProductColumn;
	@FXML
	private TableView<Product> expirationListTableView;
	@FXML
	private VBox shoppingListVBox;

	private boolean cancelEditProduct = false;


    /*


    WINDOW INITIALIZING METHODS


     */

	/**
	 * Initializes the controller and sets up the expiration list view.
	 * This method is automatically called by the JavaFX framework after loading the associated FXML file.
	 */
	@FXML
	public void initialize() {
		expirationListProductColumn.setCellValueFactory(new PropertyValueFactory<>("productName"));
		expirationListExpirationDateColumn.setCellValueFactory(new PropertyValueFactory<>("expirationDate"));

		try {
			dbConnection();
			expirationList = getProductData();
		} catch (SQLException e) {
			expirationList = FXCollections.observableArrayList();
			AlertDialog.alertError("Database Error: while loading data");
		}

		expirationListTableView.setItems(expirationList);
		editableCols();

		sortTableView(expirationListTableView);

		new ShoppingListItemUI();
	}

	/**
	 * Sets up editable columns in the expiration list table.
	 */
	private void editableCols() {
		expirationListProductColumn.setCellFactory(TextFieldTableCell.forTableColumn());
		expirationListProductColumn.setOnEditCommit(this::onEditProductColumn);

		expirationListExpirationDateColumn.setOnEditStart(this::onEditExpirationDateColumn);

		expirationListProductColumn.setEditable(true);
	}

    /*


    UTILITIES


     */

	/**
	 * Edits the calendar event for the given old product and new product.
	 *
	 * @param oldProduct The old product.
	 * @param newProduct The new product.
	 */
	void editCalendarEvent(Product oldProduct, Product newProduct) {
		//TODO: see https://datatracker.ietf.org/doc/html/rfc5546#page-80 for updating an existing event
		deleteCalendarEvent(oldProduct);
		try {
			Thread.sleep(500);
		} catch (InterruptedException e) {
			throw new RuntimeException(e);
		}
		addCalendarEvent(newProduct);
	}

	/**
	 * Handles the case when there are overlapping products with the same name and expiration date.
	 *
	 * @param oldProduct        The old product.
	 * @param newName           The new name.
	 * @param newExpirationDate The new expiration date.
	 */
	void onOverlappingProducts(Product oldProduct, String newName, LocalDate newExpirationDate) {
		final String onSQLExceptionMessage = "Database Error while editing item";

		// In this case, we renamed a product to an already existing item with the same expiration date
		expirationList.stream().filter(product -> product.getProductName().equals(newName) && product.getExpirationDate().equals(newExpirationDate)).forEach(product -> {
			int newQuantity = product.getQuantity() + oldProduct.getQuantity();
			try {
				editDBProductQuantity(product, newQuantity);
				product.setQuantity(newQuantity);
				removeDBProduct(oldProduct);
				expirationListTableView.getItems().remove(expirationListTableView.getSelectionModel().getSelectedIndex());
			} catch (SQLException ex) {
				AlertDialog.alertError(onSQLExceptionMessage);
			}
		});
	}

	/**
	 * Checks if there is a blank item in the shopping list.
	 *
	 * @return {@code true} if there is a blank item, {@code false} otherwise.
	 */
	boolean containsBlankItem() {
		List<Boolean> textFieldsStatus = shoppingListVBox.getChildren().stream().map(node -> {
			if (node instanceof GridPane gridPane) {
				if (gridPane.getChildren().get(1) instanceof TextField textField) {
					return textField.getText().isBlank();
				}
			}
			return false;
		}).toList();

		return textFieldsStatus.contains(true);
	}

	/**
	 * Handles the event when the "Delete" button in the expiration list is clicked.
	 *
	 * @param ignoredEvent The action event (ignored).
	 */
	@FXML
	void onDeleteExpirationListButtonClicked(ActionEvent ignoredEvent) {
		try {
			int selectedIndex = selectedIndex();
			Product removeProduct = expirationListTableView.getItems().get(selectedIndex);

			deleteCalendarEvent(removeProduct);
			removeDBProduct(removeProduct);

			expirationListTableView.getItems().remove(selectedIndex);
		} catch (SQLException e) {
			//UtilsDB.onSQLException("Database Error while removing item");
			AlertDialog.alertError("Database Error while removing item.");
		} catch (NoSuchElementException e) {
			//showNoProductSelectedAlert();
			AlertDialog.alertWarning("No Selection", "No Product Selected", "Please select a product in the table.");
		}
	}

	/**
	 * Returns the index of the selected person in the TableView component
	 *
	 * @return the index of the selected person
	 *
	 * @throws NoSuchElementException If no selection is made.
	 */
	int selectedIndex() throws NoSuchElementException {
		int selectedIndex = expirationListTableView.getSelectionModel().getSelectedIndex();
		if (selectedIndex < 0) {
			throw new NoSuchElementException();
		}
		return selectedIndex;
	}

	/**
	 * Deletes the calendar event for the given product.
	 *
	 * @param product The product.
	 */
	void deleteCalendarEvent(Product product) {

		String uid = product.getProductName() + product.getExpirationDate().toString();
		ICalendar iCal = new ICalendar();
		iCal.setMethod("CANCEL");
		VEvent event = new VEvent();
		event.setOrganizer(new Organizer("expirationdate", "")); //the organizer of the existing event
		event.setUid(uid); //the UID of the existing event
		event.setSequence(1);
		event.setStatus(Status.cancelled());
		Summary summary = event.setSummary(product.getProductName());
		summary.setLanguage("en-us");

		event.setDateStart(DateUtils.asDate(product.getExpirationDate()), false);

		iCal.addEvent(event);

		createExecuteICS(iCal);
	}

	/**
	 * Creates and executes an ICS (iCalendar) file.
	 *
	 * @param iCalendar The iCalendar data.
	 */
	void createExecuteICS(ICalendar iCalendar) {
		File file = new File("temp.ics");
		try (ICalWriter writer = new ICalWriter(file, ICalVersion.V2_0)) {
			writer.write(iCalendar);
		} catch (IOException e) {
			new Alert(Alert.AlertType.ERROR, "Error during calendar event creation").showAndWait();
		}

		Desktop desktop = Desktop.getDesktop();
		try {
			desktop.open(file);
		} catch (IOException e) {
			new Alert(Alert.AlertType.ERROR, "Error while running calendar event").showAndWait();
		}
	}

	/**
	 * Edits the calendar event for the given old product and new product.
	 *
	 * @param event The cell edit event.
	 */
	void onEditExpirationDateColumn(TableColumn.CellEditEvent<Product, LocalDate> event) {
		final String onSQLExceptionMessage = "Database Error while editing item";

		int selectedIndex = selectedIndex();
		Product oldProduct = event.getRowValue();
		Product editedProduct = actionOnProduct(oldProduct);

		if (!editedProduct.getProductName().equals("") && !editedProduct.equals(oldProduct)) {
			try {
				editCalendarEvent(oldProduct, editedProduct);
				editDBProductAllField(oldProduct, editedProduct, this);
				expirationListTableView.getItems().set(selectedIndex, editedProduct);
			} catch (SQLException e) {
				AlertDialog.alertError(onSQLExceptionMessage);
			}
		}

		sortTableView(expirationListTableView);
	}

	/**
	 * Edits the product column based on the new value.
	 *
	 * @param event The cell edit event.
	 */
	void onEditProductColumn(TableColumn.CellEditEvent<Product, String> event) {
		final String onSQLExceptionMessage = "Database Error while editing item";

		Product oldProduct = event.getTableView().getItems().get(event.getTablePosition().getRow());
		String newName = event.getNewValue();

		Product editedProduct = new Product(oldProduct);
		editedProduct.setProductName(newName);

		try {
			editCalendarEvent(oldProduct, editedProduct);
			editDBProductName(oldProduct, newName, this);
			oldProduct.setProductName(newName);
		} catch (SQLException exception) {
			AlertDialog.alertError(onSQLExceptionMessage);
		}
	}

    /*


    UI INTERACTION - LEFT (EXPIRATION LIST) VIEW


     */

	/**
	 * Handles the event when the "New Expiration List" button is clicked.
	 * Creates a new product and opens the edit dialog to modify the product details.
	 * If the product is valid (not empty), it is added to the expiration list and the database.
	 * If a product with the same name and expiration date already exists in the list, the quantities are updated.
	 * Finally, the expiration list table is sorted.
	 *
	 * @param ignoredEvent The action event (ignored).
	 */
	@FXML
	void onNewExpirationListButtonClicked(ActionEvent ignoredEvent) {
		Product edited = actionOnProduct(new Product());
		if (!edited.getProductName().equals("")) {
			try {
				insertDBProduct(edited);
				addCalendarEvent(edited);
				expirationList.add(edited);
			} catch (SQLIntegrityConstraintViolationException e) {
				expirationList.stream().filter(product -> product.getProductName().equals(edited.getProductName()) && product.getExpirationDate().equals(edited.getExpirationDate())).forEach(product -> {
					int newQuantity = product.getQuantity() + edited.getQuantity();
					try {
						editDBProductQuantity(product, newQuantity);
						product.setQuantity(newQuantity);
					} catch (SQLException ex) {
						new Alert(Alert.AlertType.ERROR, "Database Error while editing item").showAndWait();
					}
				});
			} catch (SQLException e) {
				new Alert(Alert.AlertType.ERROR, "Database Error: while adding item").showAndWait();
			}
		}

		sortTableView(expirationListTableView);
	}

	/**
	 * Opens the edit product dialog for the given initial product value.
	 * Allows the user to modify the product details.
	 *
	 * @param initialValue The initial value of the product.
	 *
	 * @return The edited product if the user applies the changes, otherwise returns the initial product.
	 *
	 * @throws RuntimeException If an error occurs during editing a product.
	 */
	public Product actionOnProduct(Product initialValue) {
		try {
			FXMLLoader loader = new FXMLLoader();
			loader.setLocation(getClass().getResource("EditProduct-view.fxml"));
			DialogPane view = loader.load();
			EditProductController controller = loader.getController();

			controller.setProduct(new Product(initialValue));

			// Create the dialog
			Dialog<ButtonType> dialog = new Dialog<>();
			dialog.setTitle("Edit Product");
			dialog.initModality(Modality.APPLICATION_MODAL);
			dialog.setDialogPane(view);
			//ImageView icon = new ImageView("com/napolitanoveroni/expirationdate/icons/app-icon.png");
			Stage stage = (Stage) dialog.getDialogPane().getScene().getWindow();
			stage.getIcons().add(new Image(Objects.requireNonNull(this.getClass().getResource("icons/app-icon.png")).toString()));

			// Show the dialog and wait until the user closes it
			Optional<ButtonType> clickedButton = dialog.showAndWait();
			if (clickedButton.orElse(ButtonType.CANCEL) == ButtonType.APPLY) {
				return controller.getProduct();
			}

			cancelEditProduct = true;

			return initialValue;
		} catch (NoSuchElementException e) {
			//showNoProductSelectedAlert();
			AlertDialog.alertWarning("No Selection", "No Product Selected", "Please select a product in the table.");
		} catch (IOException e) {
			e.printStackTrace();
		}

		throw new RuntimeException("Error while editing a product!");
	}

	/**
	 * Adds a calendar event for the given product.
	 * The event includes the product name, expiration date, and an alarm for one day before the expiration date.
	 * The calendar event is stored in an iCalendar object and then executed.
	 *
	 * @param product The product for which to create the calendar event.
	 */
	void addCalendarEvent(Product product) {
		ICalendar iCal = new ICalendar();
		VEvent event = new VEvent();
		Summary summary = event.setSummary(product.getProductName());
		summary.setLanguage("en-us");

		event.setDateStart(DateUtils.asDate(product.getExpirationDate()), false);

		Duration triggerDuration = Duration.builder().prior(true).days(1).build();
		Trigger trigger = new Trigger(triggerDuration, Related.START);
		VAlarm alarm = VAlarm.display(trigger, product.getProductName() + " is expiring.");
		event.addAlarm(alarm);

		iCal.addEvent(event);

		String uid = product.getProductName() + product.getExpirationDate().toString();

		event.setOrganizer(new Organizer("expirationdate", ""));
		event.setSequence(1);
		event.setUid(uid);

		createExecuteICS(iCal);
	}

	/**
	 * Sorts the expiration list table based on the expiration dates of the products.
	 *
	 * @param expirationListTableView The table view to be sorted.
	 */
	void sortTableView(TableView<Product> expirationListTableView) {
		FXCollections.sort(expirationListTableView.getItems(), Comparator.comparing(Product::getExpirationDate));
	}

	/**
	 * Handles the event when the "Recipes" button in the expiration list view is clicked.
	 * Opens the recipe window and passes the names of the non-expired products to the controller.
	 *
	 * @param ignoredEvent The action event (ignored).
	 *
	 * @throws IOException If an error occurs while loading the recipe window.
	 */
	@FXML
	void onRecipesExpirationListButtonClicked(ActionEvent ignoredEvent) throws IOException {

		Stage stage = new Stage();
		FXMLLoader fxmlLoader = new FXMLLoader(ExpirationDateApplication.class.getResource("RecipeWindow-view.fxml"));

		Scene scene = new Scene(fxmlLoader.load());

		RecipeWindowController controller = fxmlLoader.getController();

		controller.setNotExpiredProducts(expirationList.stream().filter(product -> product.getExpirationDate().isAfter(LocalDate.now())).map(Product::getProductName).collect(Collectors.toSet()));

		stage.setTitle("Recipe");
		stage.initModality(Modality.APPLICATION_MODAL);
		stage.setScene(scene);

		scene.getWindow().addEventFilter(WindowEvent.WINDOW_CLOSE_REQUEST, controller::onWindowCloseRequest);

		stage.getIcons().add(new Image(Objects.requireNonNull(getClass().getResourceAsStream("icons/app-icon.png"))));
		stage.show();
	}

    /*


    UI INTERACTION - RIGHT (SHOPPING LIST) VIEW


     */

	/**
	 * Handles the event when the "Clear" button is clicked in the shopping list view.
	 * Removes the selected items from the shopping list.
	 * If the shopping list becomes empty, a new blank item is added.
	 *
	 * @param ignoredEvent The action event (ignored).
	 */
	@FXML
	void onClearButtonClicked(ActionEvent ignoredEvent) {
		ObservableList<Node> children = shoppingListVBox.getChildren();
		for (ListIterator<Node> nodeListIterator = children.listIterator(); nodeListIterator.hasNext(); ) {
			Node child = nodeListIterator.next();
			if (child instanceof GridPane gridPane) {
				if (gridPane.getChildren().get(0) instanceof CheckBox checkBox) {
					if (checkBox.isSelected() && !checkBox.isIndeterminate()) {
						nodeListIterator.remove();
					}
				}
			}
		}
		if (children.isEmpty()) {
			new ShoppingListItemUI();
		}
	}

	/**
	 * Represents a shopping list item in the UI.
	 * Contains a checkbox, a text field, and a delete button for each item.
	 * Provides methods to handle events associated with the shopping list item.
	 */

	private class ShoppingListItemUI {
		GridPane container;
		CheckBox productCheckBox;
		TextField productTextField;
		Button deleteButton;

		/**
		 * Creates a new shopping list item UI component.
		 * Initializes the checkbox, text field, and delete button.
		 * Adds the UI component to the shopping list view.
		 * Sets the focus on the text field.
		 */
		public ShoppingListItemUI() {
			productCheckBox = new CheckBox();
			productCheckBox.setOnAction(this::onCheckBoxChecked);

			productTextField = new TextField();
			GridPane.setMargin(productTextField, new Insets(0, 10, 0, 10));
			productTextField.setOnAction(this::onEnterShoppingTextField);

			deleteButton = new Button("");
			ImageView imageView =
				new ImageView("com/napolitanoveroni/expirationdate/icons/white-delete-shoppingList-icon.png");
			imageView.setFitWidth(25);
			imageView.setPreserveRatio(true);
			deleteButton.setGraphic(imageView);
			deleteButton.setOnAction(this::onDeleteShoppingListButtonClicked);

			container = new GridPane();
			container.setPadding(new Insets(10, 10, 0, 10));
			container.setAlignment(Pos.TOP_CENTER);

			List<Boolean> checkBoxesStatus = shoppingListVBox.getChildren().stream().map(node -> {
				if (node instanceof GridPane gridPane) {
					if (gridPane.getChildren().get(0) instanceof CheckBox checkBox) {
						return checkBox.isSelected() && !checkBox.isIndeterminate();
					}
				}
				return false;
			}).toList();

			int insertIndex = checkBoxesStatus.indexOf(true);
			if (insertIndex == -1) {
				insertIndex = shoppingListVBox.getChildren().size();
			}

			shoppingListVBox.getChildren().add(insertIndex, container);
			container.addRow(container.getRowCount(), productCheckBox, productTextField, deleteButton);

			productTextField.requestFocus();
		}

		/**
		 * Handles the event when the checkbox of the shopping list item is checked or unchecked.
		 * Moves the item to the front or back based on its checked status.
		 * If the checkbox is checked, creates a new shopping list item if all existing items are filled.
		 *
		 * @param event The action event triggered by the checkbox.
		 */

		@FXML
		void onCheckBoxChecked(ActionEvent event) {
			if (event.getSource() instanceof CheckBox checkBox) {
				int index = shoppingListVBox.getChildren().indexOf(checkBox.getParent());

				if (!checkBox.isSelected() && !checkBox.isIndeterminate()) { //unchecked
					if (shoppingListVBox.getChildren().get(index) instanceof GridPane indexthGridPane) {
						indexthGridPane.getChildren().get(1).setDisable(false);
					}
					shoppingListVBox.getChildren().get(index).toBack();
				}
				if (checkBox.isSelected() && !checkBox.isIndeterminate()) { //checked
					shoppingListVBox.getChildren().get(index).toFront();
					index = shoppingListVBox.getChildren().size() - 1;

					String productName = "";

					TextField productTextField = new TextField();

					if (shoppingListVBox.getChildren().get(index) instanceof GridPane indexthGridPane) {
						if (indexthGridPane.getChildren().get(1) instanceof TextField textField) {
							productName = textField.getText();
							if (productName.isBlank()) {
								checkBox.setSelected(false);
								return;
							}
							productTextField = textField;
							productTextField.setDisable(checkBox.isSelected());
							if (shoppingListVBox.getChildren().size() == 1) {
								new ShoppingListItemUI();
							}
						}
					}

					try {
						Product edited = actionOnProduct(new Product(productName));
						if (!cancelEditProduct && !edited.getProductName().equals("")) {
							insertDBProduct(edited);
							addCalendarEvent(edited);
							expirationList.add(edited);
						} else {
							checkBox.setSelected(false);
							cancelEditProduct = false;
							productTextField.setDisable(false);
						}
					} catch (SQLException e) {
						AlertDialog.alertError("Database Error: while adding item");
					}
				}
			}

			sortTableView(expirationListTableView);
		}

		/**
		 * Handles the event when the Enter key is pressed in the shopping list item's text field.
		 * Adds a new shopping list item if all existing items are filled and the current item is not blank.
		 *
		 * @param event The action event triggered by the Enter key press.
		 */
		@FXML
		void onEnterShoppingTextField(ActionEvent event) {
			if (!containsBlankItem()) {
				new ShoppingListItemUI();
			}
		}

		/**
		 * Handles the event when the delete button of the shopping list item is clicked.
		 * Removes the shopping list item from the UI.
		 * Adds a new shopping list item if all existing items are filled or deleted.
		 *
		 * @param event The action event triggered by the delete button click.
		 */

		@FXML
		void onDeleteShoppingListButtonClicked(ActionEvent event) {
			if (event.getSource() instanceof Button button) {
				shoppingListVBox.getChildren().remove(button.getParent());
			}
			if (shoppingListVBox.getChildren().size() == 0 || !containsBlankItem()) {
				onEnterShoppingTextField(event);
			}
		}
	}
}
