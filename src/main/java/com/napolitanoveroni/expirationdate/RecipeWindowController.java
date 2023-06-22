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
        durationTextField.setText(Double.toString(recipe.getDuration()));
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

        onEnterTagComboBox(new ActionEvent(lastTag, null));
    }

    @FXML
    void onEnterTagComboBox(ActionEvent event) {
        ComboBox<String> comboBox = (ComboBox<String>)event.getSource();
        int index = tagGridPane.getChildren().indexOf(comboBox);

        Recipe recipe = recipes.get(recipesIndex);
        String title = recipe.getTitle();

        String lastTag = comboBox.getEditor().getText();

        String oldTag;
        if (index < recipe.getTagList().size()) {
            oldTag = recipe.getTagList().get(index);
        } else {
            oldTag = lastTag;
        }

        if (lastTag.isEmpty()) {
            for (int i = index; i < tagGridPane.getChildren().size() - 1; i++) {
                int columnIndex = i % tagGridPane.getColumnCount();
                int rowIndex = i / tagGridPane.getColumnCount();
                GridPane.setConstraints(tagGridPane.getChildren().get(i + 1), columnIndex, rowIndex);
            }
            tagGridPane.getChildren().remove(index);

            try {
                removeDBTag(title, oldTag);
                recipe.getTagList().remove(index);
            } catch (SQLException ignored) {}

            return;
        }

        if (!((ComboBox<String>)(tagGridPane.getChildren().get(tagGridPane.getChildren().size() - 1))).getEditor().getText().isEmpty()) {
            int lastIndex = tagGridPane.getChildren().size();
            ComboBox<String> newComboBoxTag = new ComboBox<>();
            newComboBoxTag.setEditable(true);
            newComboBoxTag.getEditor().setPromptText("Add tag...");
            newComboBoxTag.setOnAction(this::onEnterTagComboBox);

            int columnIndex = lastIndex % tagGridPane.getColumnCount();
            int rowIndex = lastIndex / tagGridPane.getColumnCount();

            tagGridPane.add(newComboBoxTag, columnIndex, rowIndex);
        }

        try {
            try {
                removeDBTag(title, oldTag);
            } catch (SQLException ignored) {}
            insertDBTag(title, lastTag);

            if (index < recipe.getTagList().size()) {
                recipe.getTagList().remove(index);
            }
            recipe.getTagList().add(index, lastTag);
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
            recipes.get(recipesIndex).setCategory(categoryComboBoxSelected);
        } catch (SQLException e) {
            onSQLException("Error while updating category");
        }
    }

    @FXML
    void onDeleteMenuItemClicked(ActionEvent event) {
        //TODO removeDBREcipe

        recipes.remove(recipesIndex);
    }

    @FXML
    void onEnterDurationTextField(ActionEvent event) {
        Recipe recipe = recipes.get(recipesIndex);
        try {
            editDBRecipeDuration(recipe.getTitle(), recipe.getDuration());
        } catch (SQLException e) {
            onSQLException("Error while updating duration.");
        }
    }

    @FXML
    void onEnterPortionsTextField(ActionEvent event) {
        Recipe recipe = recipes.get(recipesIndex);

        try{
            editDBRecipePortion(recipe.getTitle(), recipe.getPortions());
        } catch (SQLException e) {
            onSQLException("Error while updating portions.");
        }
    }

    @FXML
    void onEnterTitleTextField(ActionEvent event) {
        Recipe recipe = recipes.get(recipesIndex);
    }

    @FXML
    void onExportMenuItemClicked(ActionEvent event) {
        //TODO see JSON
    }

    @FXML
    void onImportMenuItemClicked(ActionEvent event) {
        //TODO see JSON
    }

    @FXML
    void onLeftButtonClicked(ActionEvent event) {
        if(recipesIndex - 1 < 0){
            recipesIndex = recipes.size() - 1;
        } else {
            recipesIndex--;
        }

        Recipe recipe = recipes.get(recipesIndex);
        setRecipe(recipe);
    }

    @FXML
    void onRightButtonClicked(ActionEvent event) {
        recipesIndex = (recipesIndex + 1) % recipes.size();
        Recipe recipe = recipes.get(recipesIndex);
        setRecipe(recipe);
    }

    @FXML
    void onUnitComboBoxChosen(ActionEvent ignoredEvent) {
        unitComboBoxSelected = unitComboBox.getSelectionModel().getSelectedIndex();


        try {
            editDBRecipeUnit(recipes.get(recipesIndex).getTitle(), unitComboBoxSelected);
            recipes.get(recipesIndex).setUnit(switch (unitComboBoxSelected) {
                case 0 -> durationUnit.MIN;
                default -> durationUnit.H;
            });

        } catch (SQLException e) {
            onSQLException("Error while updating category");
        }
    }

}
