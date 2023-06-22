package com.napolitanoveroni.expirationdate;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import static com.napolitanoveroni.expirationdate.UtilsDB.*;

public class RecipeWindowController {

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

    private ObservableList<Recipe> recipes;
    int recipesIndex;

    @FXML
    private ComboBox<String> firstTagComboBox;

    private int unitComboBoxSelected;

    private String categoryComboBoxSelected;

    @FXML
    public void initialize() {

        try {
            dbConnection();
            recipes = getRecipeData();
        } catch (SQLException e) {
            recipes = FXCollections.observableArrayList();
            UtilsDB.onSQLException("Database Error: while loading data");
        }

        if (recipes.isEmpty()) {
            initializeCreationView();
        } else {
            setRecipe(recipes.get(0));
        }

        recipesIndex = 0;
    }

    void setRecipe(Recipe recipe) {
        titleTextField.setText(recipe.getTitle());
        durationTextField.setText(Integer.toString(recipe.getDuration()));
        unitComboBox.setItems(FXCollections.observableArrayList("minutes", "hours"));
        unitComboBoxSelected = switch (recipe.getUnit()) {
            case MIN -> 0;
            case H -> 1;
        };
        unitComboBox.getSelectionModel().select(unitComboBoxSelected);
        portionsTextField.setText(Integer.toString(recipe.getPortions()));
        categoryComboBox.setItems(FXCollections.observableArrayList("first course", "second course", "dessert", "side dish"));

        categoryComboBoxSelected = recipe.getCategory();

        categoryComboBox.getSelectionModel().select(unitComboBoxSelected);

        stepsTextArea.setText(recipe.getSteps());

        List<String> tags = new ArrayList<>(recipe.getTagList());
        recipe.setTagList(new ArrayList<>());
        for (String tag : tags) {
            insertTag(tag);
        }
    }

    int lastTagGridIndex() {
        return tagGridPane.getChildren().size() - 1;
    }

    void insertTag(String tag) {
        ComboBox<String> lastTag = (ComboBox<String>)tagGridPane.getChildren().get(lastTagGridIndex());

        lastTag.getEditor().setText(tag);

        onEnterTagComboBox(new ActionEvent(firstTagComboBox, null));
    }

    @FXML
    void onEnterTagComboBox(ActionEvent event) {
        int index = lastTagGridIndex() + 1;

        ComboBox<String> comboBox = (ComboBox<String>)event.getSource();
        String lastTag = comboBox.getEditor().getText();

        if (lastTag.isEmpty()) {
            return;
        }

        ComboBox<String> newComboBoxTag = new ComboBox<>();
        newComboBoxTag.setEditable(true);
        newComboBoxTag.getEditor().setPromptText("Add tag...");
        newComboBoxTag.setOnAction(this::onEnterTagComboBox);

        int columnIndex = index % tagGridPane.getColumnCount();
        int rowIndex = index / tagGridPane.getColumnCount();

        tagGridPane.add(newComboBoxTag, columnIndex, rowIndex);

        String title = recipes.get(recipesIndex).getTitle();

        try {
            try {   // TODO check
                removeDBTag(title, lastTag);
            } catch (SQLException ignored) {}
            insertDBTag(title, lastTag);

            recipes.get(recipesIndex).getTagList().add(comboBox.getValue());
        } catch (SQLException e) {
            onSQLException("Error while changing tags.");
        }
    }

    void initializeCreationView() {
        stepsTextArea.setText("");
        portionsTextField.setText("");
        durationTextField.setText("");
        titleTextField.setText("");
        categoryComboBox.getSelectionModel().select("first course");
        unitComboBox.getSelectionModel().select("minutes");
        ingredientsProgressIndicator.setProgress(0);
        tagGridPane.getChildren().remove(0, lastTagGridIndex());
        GridPane.setConstraints(tagGridPane.getChildren().get(lastTagGridIndex()), 0,0);
        // TODO clear ingredients
    }

    @FXML
    void onAddMenuItemClicked(ActionEvent ignoredEvent) {
        initializeCreationView();
    }

    @FXML
    void onCategoryComboBoxChosen(ActionEvent ignoredEvent) {
        categoryComboBoxSelected = categoryComboBox.getValue();

        try {
            editDBRecipeCategory(recipes.get(recipesIndex).getTitle(), categoryComboBoxSelected);
        } catch (SQLException e) {
            onSQLException("Error while updating category");
        }
    }

    @FXML
    void onDeleteMenuItemClicked(ActionEvent event) {

    }

    @FXML
    void onEnterDurationTextField(ActionEvent event) {

    }

    @FXML
    void onEnterPortionsTextField(ActionEvent event) {

    }

    @FXML
    void onEnterTitleTextField(ActionEvent event) {

    }

    @FXML
    void onExportMenuItemClicked(ActionEvent event) {

    }

    @FXML
    void onImportMenuItemClicked(ActionEvent event) {

    }

    @FXML
    void onLeftButtonClicked(ActionEvent event) {

    }

    @FXML
    void onRightButtonClicked(ActionEvent event) {

    }

    @FXML
    void onUnitComboBoxChosen(ActionEvent ignoredEvent) {
        unitComboBoxSelected = unitComboBox.getSelectionModel().getSelectedIndex();


        try {
            editDBRecipeUnit(recipes.get(recipesIndex).getTitle(), unitComboBoxSelected);
        } catch (SQLException e) {
            onSQLException("Error while updating category");
        }
    }

}
