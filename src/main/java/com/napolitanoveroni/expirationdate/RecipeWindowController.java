/**
 * The RecipeWindowController class is responsible for managing the recipe window
 * and handling user interactions with the GUI components.
 */

package com.napolitanoveroni.expirationdate;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import javafx.animation.AnimationTimer;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Window;
import javafx.stage.WindowEvent;

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.sql.SQLIntegrityConstraintViolationException;
import java.util.*;

import static com.napolitanoveroni.expirationdate.UtilsDB.*;

public class RecipeWindowController {

	int recipesIndex;
	Set<String> notExpiredProducts;
	Set<String> existingTags;
	@FXML
	private ComboBox<String> categoryComboBox;
	@FXML
	private TextField durationTextField;
	@FXML
	private VBox ingredientVBox;
	@FXML
	private ProgressIndicator ingredientsProgressIndicator;
	@FXML
	private TextField portionsTextField;
	@FXML
	private TextArea stepsTextArea;
	@FXML
	private GridPane tagGridPane;
	@FXML
	private TextField titleTextField;
	@FXML
	private ComboBox<String> unitComboBox;
	@FXML
	private Button leftButton;
	@FXML
	private Button rightButton;
	@FXML
	private MenuItem addMenuitem;
	private ObservableList<Recipe> recipes;
	private int unitComboBoxSelected;
	private String categoryComboBoxSelected;
	private boolean suspendAutoSave;

	/**
	 * Sets the not expired products.
	 *
	 * @param notExpiredProducts the set of not expired products
	 */

	public void setNotExpiredProducts(Set<String> notExpiredProducts) {
		this.notExpiredProducts = notExpiredProducts;

		updateProgressIndicator(recipes.get(recipesIndex));
	}

	/**
	 * Updates the progress indicator based on the recipe's ingredients.
	 *
	 * @param recipe the recipe
	 */

	void updateProgressIndicator(Recipe recipe) {
		List<Ingredient> ingredients = recipe.getIngredientList();
		double count = 0;

		for (Ingredient ingredient : ingredients) {
			if (notExpiredProducts.contains(ingredient.getIngredient())) {
				count++;
			}
		}

		ingredientsProgressIndicator.setProgress(count / ingredients.size());
	}

	/**
	 * Initializes the controller and sets up the initial state of the view.
	 * JavaFX automatically calls this method when the corresponding FXML file is loaded.
	 * It performs the following tasks:
	 * Establishes a database connection and retrieves recipe data.
	 * Initializes the list of existing tags.
	 * Sets up the view for creating a new recipe or selecting an existing recipe.
	 * Initializes the category combo box with available options.
	 * Initializes the tag combo boxes with existing tags.
	 * Enables auto-saving functionality.
	 * Initializes the timer.
	 *
	 */

	@FXML
	public void initialize() {

		try {
			dbConnection();
			recipes = getRecipeData();
			existingTags = getAllTags();
		} catch (SQLException e) {
			recipes = FXCollections.observableArrayList();
			AlertDialog.alertError("Database Error: while loading data");
		}

		notExpiredProducts = new HashSet<>();

		if (recipes.isEmpty()) {
			initializeCreationView();
		} else {
			setRecipe(recipes.get(0));
		}

		recipesIndex = 0;

		categoryComboBox.setItems(FXCollections.observableArrayList("first course", "second course", "dessert", "side dish"));

		tagGridPane.getChildren().forEach(node -> {
			ComboBox<String> comboBox = (ComboBox<String>) node;
			comboBox.setItems(FXCollections.observableArrayList(existingTags));
		});

		for (Node node : tagGridPane.getChildren()) {
			if (node instanceof ComboBox<?> comboBox) {
				ComboBox<String> tag = (ComboBox<String>) comboBox;
				tag.setItems(FXCollections.observableArrayList(existingTags));
			}
		}

		suspendAutoSave = false;

		initializeTimer();
	}

	/**
	 * Initializes the auto-save timer.
	 */

	private void initializeTimer() {
		AnimationTimer timer = new AnimationTimer() {

			private long lastUpdate = 0;

			@Override
			public void handle(long now) {
				if (now - lastUpdate >= 500_000_000) {
					allFieldsAutoSave();
					lastUpdate = now;
				}
			}
		};
		timer.start();
	}

	/**
	 * Sets the recipe and updates the GUI components with the recipe's data.
	 *
	 * @param recipe the recipe to be set
	 */

	void setRecipe(Recipe recipe) {
		titleTextField.setText(recipe.getTitle());
		durationTextField.setText(Double.toString(recipe.getDuration()));
		unitComboBox.setItems(FXCollections.observableArrayList("minutes", "hours"));
		unitComboBoxSelected = switch (recipe.getUnit()) {
			case MIN -> 0;
			case H -> 1;
		};
		unitComboBox.getSelectionModel().select(unitComboBoxSelected);
		portionsTextField.setText(Integer.toString(recipe.getPortions()));

		categoryComboBoxSelected = recipe.getCategory();

		categoryComboBox.getSelectionModel().select(categoryComboBoxSelected);

		stepsTextArea.setText(recipe.getSteps());

		tagGridPane.getChildren().remove(0, lastTagGridIndex());
		GridPane.setConstraints(tagGridPane.getChildren().get(lastTagGridIndex()), 0, 0);

		List<String> tagsCopy = new ArrayList<>(recipe.getTagList());
		recipe.setTagList(new ArrayList<>());
		for (String tag : tagsCopy) {
			insertTag(tag);
		}

		clearIngredientsVBox();

		List<Ingredient> ingredientsCopy = new ArrayList<>(recipe.getIngredientList());
		recipe.setIngredientList(new ArrayList<>());
		for (Ingredient ingredient : ingredientsCopy) {
			addIngredient(ingredient);
		}

		updateProgressIndicator(recipe);

		if (recipe.getTitle().isBlank()) {
			disableRecipeFields(true);
		}
	}

	/**
	 * Disables or enables the recipe fields.
	 *
	 * @param disable true to disable, false to enable
	 */

	void disableRecipeFields(boolean disable) {
		durationTextField.setDisable(disable);
		unitComboBox.setDisable(disable);
		portionsTextField.setDisable(disable);
		categoryComboBox.setDisable(disable);
		durationTextField.setDisable(disable);
		stepsTextArea.setDisable(disable);
		tagGridPane.setDisable(disable);
		ingredientVBox.setDisable(disable);
		leftButton.setDisable(disable);
		rightButton.setDisable(disable);
		addMenuitem.setDisable(disable);
	}

	/**
	 * Returns the index of the last tag in the tag grid pane.
	 *
	 * @return the index of the last tag
	 */

	int lastTagGridIndex() {
		return tagGridPane.getChildren().size() - 1;
	}

	/**
	 * Inserts a new tag in the tag grid pane.
	 *
	 * @param tag the tag to be inserted
	 */

	void insertTag(String tag) {
		ComboBox<String> lastTag = (ComboBox<String>) tagGridPane.getChildren().get(lastTagGridIndex());

		lastTag.getEditor().setText(tag);

		onEnterTagComboBox(new ActionEvent(lastTag, null));
	}

	/**
	 * Handles the event when the enter key is pressed in a tag combo box.
	 *
	 * @param event the action event
	 */

	@FXML
	void onEnterTagComboBox(ActionEvent event) {
		if (suspendAutoSave) {
			return;
		}

		ComboBox<String> comboBox = (ComboBox<String>) event.getSource();
		int index = tagGridPane.getChildren().indexOf(comboBox);

		Recipe recipe = recipes.get(recipesIndex);
		String title = recipe.getTitle();

		String newTag = comboBox.getEditor().getText();

		String oldTag;
		if (index < recipe.getTagList().size()) {
			oldTag = recipe.getTagList().get(index);

			if (oldTag.equals(newTag) || recipe.getTagList().contains(newTag)) {
				return;
			}
		} else {
			oldTag = newTag;
		}

		if (newTag.isBlank()) {
			for (int i = index; i < tagGridPane.getChildren().size() - 1; i++) {
				int columnIndex = i % tagGridPane.getColumnCount();
				int rowIndex = i / tagGridPane.getColumnCount();
				GridPane.setConstraints(tagGridPane.getChildren().get(i + 1), columnIndex, rowIndex);
			}

			if (!oldTag.isBlank()) {
				tagGridPane.getChildren().remove(index);

				try {
					removeDBTag(title, oldTag);
					recipe.getTagList().remove(index);
				} catch (SQLException ignored) {
				}
			}

			return;
		}

		if (!((ComboBox<String>) (tagGridPane.getChildren().get(tagGridPane.getChildren().size() - 1))).getEditor().getText().isBlank()) {
			int lastIndex = tagGridPane.getChildren().size();
			ComboBox<String> newComboBoxTag = new ComboBox<>();
			newComboBoxTag.setItems(FXCollections.observableArrayList(existingTags));
			newComboBoxTag.setEditable(true);
			newComboBoxTag.getEditor().setPromptText("Add tag...");

			int columnIndex = lastIndex % tagGridPane.getColumnCount();
			int rowIndex = lastIndex / tagGridPane.getColumnCount();

			tagGridPane.add(newComboBoxTag, columnIndex, rowIndex);
			newComboBoxTag.setOnAction(this::onEnterTagComboBox);
			GridPane.setMargin(newComboBoxTag, new Insets(0, 5, 0, 5));
		}

		try {
			try {
				removeDBTag(title, oldTag);
				if (index < recipe.getTagList().size()) {
					recipe.getTagList().remove(index);
				}
			} catch (SQLException ignored) {
			}

			insertDBTag(title, newTag);
			recipe.getTagList().add(index, newTag);

		} catch (SQLException e) {
			AlertDialog.alertError("Error while changing tags.");
		}

		try {
			existingTags = getAllTags();
			suspendAutoSave = true;
			tagGridPane.getChildren().forEach(node -> {
				ComboBox<String> comboBox1 = (ComboBox<String>) node;
				if (!comboBox1.getItems().contains(newTag)) {
					comboBox1.setItems(FXCollections.observableArrayList(existingTags));
				}
			});
			suspendAutoSave = false;
		} catch (SQLException e) {
			AlertDialog.alertError("Error while updating tags.");
		}
	}

	/**
	 * Initializes the creation view by adding a new empty recipe to the list of recipes,
	 * setting the recipe index to the newly added recipe, and setting the current recipe
	 * in the UI.
	 */
	void initializeCreationView() {
		recipes.add(new Recipe());
		recipesIndex = recipes.size() - 1;

		setRecipe(recipes.get(recipesIndex));
	}

	/**
	 * Clears the ingredients VBox by removing all its children nodes except the first one.
	 */

	void clearIngredientsVBox() {
		ObservableList<Node> ingredientVBoxChildren = ingredientVBox.getChildren();
		ingredientVBoxChildren.remove(0, ingredientVBoxChildren.size() - 1);
	}

	/**
	 * Event handler for the "Add" menu item click. Initializes the creation view.
	 *
	 * @param ignoredEvent The action event (ignored).
	 */

	@FXML
	void onAddMenuItemClicked(ActionEvent ignoredEvent) {
		initializeCreationView();
	}

	/**
	 * Event handler for the category combo box selection change.
	 * Updates the selected
	 * category in the recipe, updates the recipe category in the database, and handles
	 * any SQL exceptions that occur.
	 *
	 * @param ignoredEvent The action event (ignored).
	 */

	@FXML
	void onCategoryComboBoxChosen(ActionEvent ignoredEvent) {
		categoryComboBoxSelected = categoryComboBox.getValue();

		try {
			editDBRecipeCategory(recipes.get(recipesIndex).getTitle(), categoryComboBoxSelected);
			recipes.get(recipesIndex).setCategory(categoryComboBoxSelected);
		} catch (SQLException e) {
			AlertDialog.alertError("Error while updating category");
		}
	}

	/**
	 * Event handler for the "Delete" menu item click.
	 * Removes the current recipe from
	 * the list of recipes, updates the recipe index, and performs necessary UI updates.
	 * Handles any SQL exceptions that occur.
	 *
	 * @param ignoredEvent The action event (ignored).
	 */

	@FXML
	void onDeleteMenuItemClicked(ActionEvent ignoredEvent) {
		try {
			removeDBRecipe(recipes.get(recipesIndex).getTitle());
			recipes.remove(recipesIndex);
			if (recipes.size() == 0) {
				initializeCreationView();
			} else {
				recipesIndex %= recipes.size();
				Recipe recipe = recipes.get(recipesIndex);
				setRecipe(recipe);

				if (suspendAutoSave) {
					suspendAutoSave = false;
					disableRecipeFields(false);
				}
			}
		} catch (SQLException e) {
			AlertDialog.alertError("Error while removing recipe.");
		}
	}

	/**
	 * Event handler for the duration text field on enter.
	 * Updates the duration of the
	 * current recipe, updates the recipe duration in the database, and handles any
	 * exceptions that occur.
	 *
	 * @param ignoredEvent The action event (ignored).
	 */

	@FXML
	void onEnterDurationTextField(ActionEvent ignoredEvent) {
		Recipe recipe = recipes.get(recipesIndex);

		try {
			double newDuration = Double.parseDouble(durationTextField.getText());

			if (recipe.getDuration() == newDuration) {
				return;
			}

			editDBRecipeDuration(recipe.getTitle(), newDuration);
			recipe.setDuration(newDuration);
		} catch (NumberFormatException e) {
			//new Alert(Alert.AlertType.ERROR, "What you typed wasn't a double number").show();
			AlertDialog.alertError("What you typed wasn't a double number");
			durationTextField.setText(Double.toString(recipe.getDuration()));
		} catch (SQLException e) {
			AlertDialog.alertError("Error while updating duration.");
		}
	}

	/**
	 * Event handler for the portions text field on enter.
	 * Updates the portions of the
	 * current recipe, updates the recipe portions in the database, and handles any
	 * exceptions that occur.
	 *
	 * @param ignoredEvent The action event (ignored).
	 */

	@FXML
	void onEnterPortionsTextField(ActionEvent ignoredEvent) {
		Recipe recipe = recipes.get(recipesIndex);

		try {
			int newPortions = Integer.parseInt(portionsTextField.getText());

			if (recipe.getPortions() == newPortions) {
				return;
			}

			editDBRecipePortion(recipe.getTitle(), newPortions);
			recipe.setPortions(newPortions);
		} catch (NumberFormatException e) {
			AlertDialog.alertError("What you typed wasn't an integer number");
			portionsTextField.setText(Integer.toString(recipe.getPortions()));
		} catch (SQLException e) {
			AlertDialog.alertError("Error while updating portions.");
		}
	}

	/**
	 * Event handler for the title text field on enter.
	 * Updates the title of the current
	 * recipe, updates the recipe in the database, and handles any exceptions that occur.
	 *
	 * @param ignoredEvent The action event (ignored).
	 */

	@FXML
	void onEnterTitleTextField(ActionEvent ignoredEvent) {
		Recipe recipe = new Recipe(recipes.get(recipesIndex));
		String oldTitle = recipe.getTitle();

		String newTitle = titleTextField.getText();
		if (newTitle.isBlank()) {
			//new Alert(Alert.AlertType.ERROR, "The title of the window can not be empty").show();
			AlertDialog.alertError("The title of the window can not be empty");
			disableRecipeFields(true);
			return;
		}

		if (oldTitle.equals(newTitle)) {
			return;
		}

		if (recipes.stream().map(Recipe::getTitle).toList().contains(newTitle)) {
			suspendAutoSave = true;
			disableRecipeFields(true);
			return;
		}

		suspendAutoSave = false;

		recipe.setTitle(newTitle);

		try {
			if (recipes.size() != 0) {
				removeDBRecipe(oldTitle);
				recipes.remove(recipesIndex);
			}

			insertDBRecipe(recipe);
			recipes.add(recipesIndex, recipe);

			disableRecipeFields(false);
		} catch (SQLException e) {
			AlertDialog.alertError("Error while inserting/editing recipe");
		}
	}

	/**
	 * Event handler for the "Export" menu item click.
	 * Opens a file chooser dialog and
	 * exports the recipes as JSON to the selected file.
	 * Handles any IO exceptions that
	 * occur.
	 *
	 * @param ignoredEvent The action event (ignored).
	 */
	@FXML
	void onExportMenuItemClicked(ActionEvent ignoredEvent) {
		try {
			FileChooser fileChooser = new FileChooser();
			FileChooser.ExtensionFilter extFilter = new FileChooser.ExtensionFilter("JSON files (*.json)", "*.json");
			fileChooser.getExtensionFilters().add(extFilter);

			File file = fileChooser.showSaveDialog(null);
			if (file != null) {
				ObjectMapper mapper = new ObjectMapper();
				mapper.registerModule(new JavaTimeModule());
				mapper.writerWithDefaultPrettyPrinter().writeValue(file, recipes);
			}
		} catch (IOException e) {
			//new Alert(Alert.AlertType.ERROR, "Could not save data").showAndWait();
			AlertDialog.alertError("Could not save data");
		}
	}

	/**
	 * Event handler for the "Import" menu item click.
	 * Opens a file chooser dialog and
	 * imports recipes from the selected JSON file.
	 * Handles any IO or SQL exceptions that
	 * occur.
	 *
	 * @param ignoredEvent The action event (ignored).
	 */

	@FXML
	void onImportMenuItemClicked(ActionEvent ignoredEvent) {
		try {
			FileChooser fileChooser = new FileChooser();
			FileChooser.ExtensionFilter extFilter = new FileChooser.ExtensionFilter("JSON files (*.json)", "*.json");
			fileChooser.getExtensionFilters().add(extFilter);

			File file = fileChooser.showOpenDialog(null);
			if (file != null) {
				ObjectMapper mapper = new ObjectMapper();
				mapper.registerModule(new JavaTimeModule());
				List<Recipe> recipeList = mapper.readValue(file, new TypeReference<>() {
				});
				for (ListIterator<Recipe> recipeListIterator =
					recipeList.listIterator(); recipeListIterator.hasNext(); ) {
					Recipe recipe = recipeListIterator.next();
					try {
						insertDBRecipe(recipe);
					} catch (SQLIntegrityConstraintViolationException e) {
						recipeListIterator.remove();
					}
				}
				recipes.addAll(recipeList);
				if (recipes.get(recipesIndex).getTitle().isBlank()) {
					recipes.remove(recipesIndex);
					recipesIndex %= recipes.size();
					setRecipe(recipes.get(recipesIndex));
				}
				disableRecipeFields(false);
			}
		} catch (IOException e) {
			AlertDialog.alertError("Could not load data");
		} catch (SQLException e) {
			AlertDialog.alertError("Error while inserting recipes in the database.");
		}
	}

	/**
	 * Event handler for the "Left" button click.
	 * Decrements the recipe index and sets
	 * the current recipe in the UI.
	 *
	 * @param ignoredEvent The action event (ignored).
	 */
	@FXML
	void onLeftButtonClicked(ActionEvent ignoredEvent) {
		if (recipesIndex - 1 < 0) {
			recipesIndex = recipes.size() - 1;
		} else {
			recipesIndex--;
		}

		Recipe recipe = recipes.get(recipesIndex);
		setRecipe(recipe);
	}

	/**
	 * Event handler for the "Right" button click.
	 * Increments the recipe index and sets
	 * the current recipe in the UI.
	 *
	 * @param ignoredEvent The action event (ignored).
	 */
	@FXML
	void onRightButtonClicked(ActionEvent ignoredEvent) {
		recipesIndex = (recipesIndex + 1) % recipes.size();
		Recipe recipe = recipes.get(recipesIndex);
		setRecipe(recipe);
	}

	/**
	 * Event handler for the unit combo box selection change.
	 * Updates the selected unit
	 * in the recipe, updates the recipe unit in the database, and handles any SQL
	 * exceptions that occur.
	 *
	 * @param ignoredEvent The action event (ignored).
	 */

	@FXML
	void onUnitComboBoxChosen(ActionEvent ignoredEvent) {
		unitComboBoxSelected = unitComboBox.getSelectionModel().getSelectedIndex();

		try {
			editDBRecipeUnit(recipes.get(recipesIndex).getTitle(), unitComboBoxSelected);
			recipes.get(recipesIndex).setUnit(unitComboBoxSelected == 0 ? durationUnit.MIN : durationUnit.H);
		} catch (SQLException e) {
			AlertDialog.alertError("Error while updating unit");
		}
	}

	/**
	 * Automatically saves all fields of the current recipe.
	 * If the title field is blank,
	 * no action is performed.
	 * Otherwise, the title, tags, ingredients, duration, portions,
	 * steps, and ingredient units are updated and saved to the database.
	 */

	void allFieldsAutoSave() {
		if (titleTextField.getText().isBlank()) {
			return;
		}
		onEnterTitleTextField(new ActionEvent());

		for (Node node : tagGridPane.getChildren()) {
			onEnterTagComboBox(new ActionEvent(node, null));
		}

		for (Node hBoxNode : ingredientVBox.getChildren()) {
			if (hBoxNode instanceof HBox hBox) {
				for (Node node : hBox.getChildren()) {
					if (node instanceof TextField textField) {
						textField.getOnAction().handle(new ActionEvent());
					}
				}
			}
		}

		if (suspendAutoSave) {
			return;
		}

		if (!durationTextField.getText().isBlank()) {
			onEnterDurationTextField(new ActionEvent());
		}

		if (!portionsTextField.getText().isBlank()) {
			onEnterPortionsTextField(new ActionEvent());
		}
		saveStepsTextArea();

		for (Node hBoxNode : ingredientVBox.getChildren()) {
			if (hBoxNode instanceof HBox hBox) {
				for (Node node : hBox.getChildren()) {
					if (node instanceof ComboBox<?> comboBox) {
						comboBox.getOnAction().handle(new ActionEvent());
					}
				}
			}
		}
		updateProgressIndicator(recipes.get(recipesIndex));
	}

	/**
	 * Saves the text in the steps text area to the current recipe.
	 * If the text is
	 * different from the current recipe's steps, the steps are updated in the database.
	 */

	void saveStepsTextArea() {
		String steps = stepsTextArea.getText();
		Recipe recipe = recipes.get(recipesIndex);
		if (!steps.equals(recipe.getSteps())) {
			try {
				editDBRecipeSteps(recipe.getTitle(), steps);
				recipe.setSteps(steps);
			} catch (SQLException e) {
				AlertDialog.alertError("Error while auto-saving steps.");
			}
		}
	}

	/**
	 * Deletes the given ingredient UI from the ingredients VBox.
	 *
	 * @param ingredientUI The ingredient UI to delete.
	 */

	void deleteIngredientUI(IngredientUI ingredientUI) {
		ingredientVBox.getChildren().remove(ingredientUI.getContainer());
	}

	/**
	 * Event handler for the "Add Ingredient" button click.
	 * Adds a new ingredient to the
	 * current recipe and updates the UI.
	 *
	 * @param ignoredEvent The action event (ignored).
	 */

	@FXML
	void onAddIngredientButtonClicked(ActionEvent ignoredEvent) {
		if (ingredientVBox.getChildren().get(0) instanceof HBox hBox) {
			if (hBox.getChildren().get(0) instanceof TextField ingredientTextField) {
				if (ingredientTextField.getText().isBlank()) {
					return;
				}
			}
		}
		addIngredient(new Ingredient());
	}

	/**
	 * Adds an ingredient to the current recipe and updates the UI.
	 *
	 * @param ingredient The ingredient to add.
	 */
	void addIngredient(Ingredient ingredient) {
		Recipe recipe = recipes.get(recipesIndex);
		IngredientUI ingredientUI = new IngredientUI(recipe.getTitle(), ingredient);
		ingredientVBox.getChildren().add(0, ingredientUI.getContainer());

		recipe.getIngredientList().add(ingredient);
	}

	/**
	 * Event handler for the window close request.
	 * Prompts the user to confirm closing
	 * the application and canceling the current recipe if it exists.
	 *
	 * @param event The window event.
	 */

	public void onWindowCloseRequest(WindowEvent event) {
		if (suspendAutoSave) {
			Alert alert = new Alert(Alert.AlertType.INFORMATION);
			alert.getButtonTypes().remove(ButtonType.OK);
			alert.getButtonTypes().add(ButtonType.CANCEL);
			alert.getButtonTypes().add(ButtonType.YES);
			alert.setTitle("Quit application");
			alert.setContentText("The current recipe already exists: do you want to cancel it and close?");
			alert.initOwner((Window) event.getSource());
			Optional<ButtonType> res = alert.showAndWait();

			if (res.isPresent()) {
				if (res.get().equals(ButtonType.CANCEL)) {
					event.consume();
				} else {
					try {
						String oldTitle = recipes.get(recipesIndex).getTitle();
						removeDBRecipe(oldTitle);
						recipes.remove(recipesIndex);
					} catch (SQLException e) {
						AlertDialog.alertError("Error while removing recipe.");
					}
				}
			}
		}
	}

	/**
	 * Represents the UI for an ingredient in the recipe creation view.
	 */

	private class IngredientUI {
		Ingredient ingredient;
		String recipeTitle;
		HBox container;
		private TextField ingredientTextField;
		private TextField quantityTextField;
		private ComboBox<String> unitComboBox;
		private Button deleteButton;

		/**
		 * Creates a new instance of IngredientUI.
		 *
		 * @param recipeTitle The title of the recipe.
		 * @param ingredient  The ingredient to display.
		 */
		public IngredientUI(String recipeTitle, Ingredient ingredient) {
			setRecipeTitle(recipeTitle);

			initializeGraphics();

			setIngredient(ingredient);

			setIcon();
		}

		/**
		 * Sets the recipe title.
		 *
		 * @param recipeTitle The title of the recipe.
		 */

		public void setRecipeTitle(String recipeTitle) {
			this.recipeTitle = recipeTitle;
		}

		/**
		 * Initializes the graphics components of the ingredient UI.
		 */

		private void initializeGraphics() {

			ingredientTextField = new TextField();
			ingredientTextField.setPromptText("Add ingredient...");

			Label quantityLabel = new Label("Quantity: ");

			quantityTextField = new TextField();
			quantityTextField.setPromptText("Add quantity...");

			Label unitLabel = new Label("Unit: ");

			ImageView imageView =
				new ImageView("com/napolitanoveroni/expirationdate/icons/white-delete-shoppingList-icon.png");
			imageView.setPreserveRatio(true);
			deleteButton = new Button();
			deleteButton.setGraphic(imageView);
			imageView.setFitWidth(20);

			unitComboBox = new ComboBox<>(FXCollections.observableArrayList("g", "kg", "ml", "l", "spoons"));
			unitComboBox.getEditor().setPromptText("Add unit of measurement...");

			container = new HBox(ingredientTextField, quantityLabel, quantityTextField, unitLabel, unitComboBox,
				deleteButton, new ImageView());

			ingredientTextField.setOnAction(this::onEnterIngredientTextField);

			quantityTextField.setDisable(true);
			quantityTextField.setOnAction(this::onEnterQuantityTextField);

			unitComboBox.setDisable(true);
			unitComboBox.setEditable(true);
			unitComboBox.setOnAction(this::onEnterUnitComboBox);

			deleteButton.setDisable(true);
			deleteButton.setOnAction(this::onDeleteIngredientButtonClicked);

			container.setAlignment(Pos.CENTER);
			container.setPrefSize(Region.USE_COMPUTED_SIZE, Region.USE_COMPUTED_SIZE);

			VBox.setMargin(container, new Insets(5, 0, 5, 0));

			HBox.setMargin(quantityLabel, new Insets(0, 5, 0, 10));
			HBox.setMargin(unitLabel, new Insets(0, 5, 0, 10));
			HBox.setMargin(deleteButton, new Insets(0, 5, 0, 10));
		}

		/**
		 * Sets the ingredient to display in the UI.
		 *
		 * @param ingredient The ingredient to display.
		 */

		public void setIngredient(Ingredient ingredient) {
			this.ingredient = ingredient;
			ingredientTextField.setText(ingredient.getIngredient());

			quantityTextField.setText(Double.toString(ingredient.getQuantity()));

			unitComboBox.getEditor().setText(ingredient.getUnit_of_measurement());

			if (!ingredient.getIngredient().isBlank()) {
				quantityTextField.setDisable(false);
				unitComboBox.setDisable(false);
				deleteButton.setDisable(false);
			}
		}

		/**
		 * Sets the icon for the ingredient UI based on whether it is in the notExpiredProducts
		 * list or not.
		 */

		public void setIcon() {
			ImageView icon =
				notExpiredProducts.contains(this.ingredient.getIngredient()) ? new ImageView("com/napolitanoveroni/expirationdate/icons/tick-icon.png") : new ImageView("com/napolitanoveroni/expirationdate/icons/not-taken-icon.png");
			icon.setPreserveRatio(true);
			icon.setFitHeight(20);

			container.getChildren().remove(container.getChildren().size() - 1);
			container.getChildren().add(icon);
			HBox.setMargin(icon, new Insets(0, 10, 0, 10));
		}

		/**
		 * Event handler for the enter key press in the ingredient text field.
		 * Updates
		 * the ingredient name in the UI and saves the changes to the database.
		 *
		 * @param ignoredEvent The action event (ignored).
		 */

		void onEnterIngredientTextField(ActionEvent ignoredEvent) {
			String newIngredient = ingredientTextField.getText();

			if (!newIngredient.isBlank()) {
				if (newIngredient.equals(ingredient.getIngredient())) {
					return;
				}

				List<Ingredient> ingredientList = recipes.get(recipesIndex).getIngredientList();
				try {
					try {
						removeDBIngredient(recipeTitle, ingredient);
						ingredientList.remove(ingredient);
					} catch (SQLException ignored) {
					}

					ingredient.setIngredient(newIngredient);

					insertDBIngredient(recipeTitle, ingredient);
					ingredientList.add(ingredient);

					setIcon();

					suspendAutoSave = false;
					disableRecipeFields(false);
					titleTextField.setDisable(false);
					ingredientVBox.getChildren().forEach(node -> node.setDisable(false));
					quantityTextField.setDisable(false);
					unitComboBox.setDisable(false);
					deleteButton.setDisable(false);
				} catch (SQLException e) {
					AlertDialog.alertError("Error while inserting ingredient.");
				}
			} else {
				String oldValue = ingredient.getIngredient();
				if (!oldValue.isBlank()) {
					suspendAutoSave = true;
					disableRecipeFields(true);
					titleTextField.setDisable(true);
					ingredientVBox.setDisable(false);
					ingredientVBox.getChildren().forEach(node -> node.setDisable(true));
					container.setDisable(false);
					container.getChildren().forEach(node -> node.setDisable(true));
					ingredientTextField.setDisable(false);
				}
			}
		}

		/**
		 * Event handler for the enter key press in the quantity text field.
		 * Updates the
		 * ingredient quantity in the UI and saves the changes to the database.
		 *
		 * @param event The action event.
		 */

		void onEnterQuantityTextField(ActionEvent event) {
			String quantityString = quantityTextField.getText();

			if (!quantityString.isBlank()) {
				try {
					double newQuantity = Double.parseDouble(quantityString);
					if (newQuantity == ingredient.getQuantity()) {
						return;
					}
					List<Ingredient> ingredientList = recipes.get(recipesIndex).getIngredientList();
					int ingredientIndex = ingredientList.indexOf(ingredient);
					ingredient.setQuantity(newQuantity);

					updateIngredient(ingredientIndex, ingredientList);
				} catch (NumberFormatException e) {
					//new Alert(Alert.AlertType.ERROR,"Error, the quantity typed is not a decimal number").show();
					AlertDialog.alertError("Error, the quantity typed is not a decimal number");
					quantityTextField.setText(Double.toString(ingredient.getQuantity()));
				}
			}
		}

		/**
		 * Event handler for the unit combo box selection change.
		 * Updates the selected unit
		 * in the ingredient UI and saves the changes to the database.
		 *
		 * @param event The action event.
		 */

		void onEnterUnitComboBox(ActionEvent event) {
			String unit = unitComboBox.getEditor().getText();
			if (!unit.isBlank()) {
				if (unit.equals(ingredient.getUnit_of_measurement())) {
					return;
				}
				List<Ingredient> ingredientList = recipes.get(recipesIndex).getIngredientList();
				int ingredientIndex = ingredientList.indexOf(ingredient);
				ingredient.setUnit_of_measurement(unit);

				updateIngredient(ingredientIndex, ingredientList);
			}
		}

		/**
		 * Event handler for the "Delete Ingredient" button click.
		 * Deletes the ingredient from
		 * the recipe, updates the database, and removes the ingredient UI from the UI.
		 *
		 * @param event The action event.
		 */

		@FXML
		void onDeleteIngredientButtonClicked(ActionEvent event) {
			if (!ingredientTextField.getText().isBlank()) {
				try {
					removeDBIngredient(recipeTitle, ingredient);

					List<Ingredient> ingredientList = recipes.get(recipesIndex).getIngredientList();
					ingredientList.remove(ingredient);

					deleteIngredientUI(this);
				} catch (SQLException e) {
					AlertDialog.alertError("Error while deleting ingredient");
				}
			}
		}

		/**
		 * Updates the ingredient in the recipe and the database.
		 * If the ingredient index is not -1,
		 * it updates the ingredient at the specified index in the ingredient list.
		 * Otherwise, it adds
		 * the ingredient to the end of the list.
		 * Handles any SQL exceptions that occur.
		 *
		 * @param ingredientIndex The index of the ingredient in the ingredient list (-1 if not found).
		 * @param ingredientList  The list of ingredients.
		 */

		void updateIngredient(int ingredientIndex, List<Ingredient> ingredientList) {
			try {
				updateDBIngredient(recipeTitle, ingredient);

				if (ingredientIndex != -1) {
					ingredientList.set(ingredientIndex, ingredient);
				} else {
					ingredientList.add(ingredient);
				}
			} catch (SQLException e) {
				AlertDialog.alertError("Error while updating ingredient");
			}
		}

		/**
		 * Returns the container HBox of the ingredient UI.
		 *
		 * @return The container HBox.
		 */

		public HBox getContainer() {
			return container;
		}
	}
}