package com.napolitanoveroni.expirationdate;

import javafx.animation.AnimationTimer;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.*;
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

        initializeTimer();
    }

    private void initializeTimer(){
        AnimationTimer timer = new AnimationTimer() {

            private long lastUpdate = 0;

            @Override
            public void handle(long now) {
                if (now - lastUpdate >= 500_000_000 && !titleTextField.getText().isBlank()) {
                    stepsTextAreaAutoSave();
                    lastUpdate = now;
                }
            }
        };
        timer.start();
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

        tagGridPane.getChildren().remove(0, lastTagGridIndex());
        GridPane.setConstraints(tagGridPane.getChildren().get(lastTagGridIndex()), 0,0);

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

        if (lastTag.isBlank()) {
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

        if (!((ComboBox<String>)(tagGridPane.getChildren().get(tagGridPane.getChildren().size() - 1))).getEditor().getText().isBlank()) {
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
        recipes.add(new Recipe());
        recipesIndex = recipes.size() - 1;

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
    void onDeleteMenuItemClicked(ActionEvent ignoredEvent) {
        try {
            removeDBRecipe(recipes.get(recipesIndex).getTitle());
            recipes.remove(recipesIndex);
            if(recipes.size() == 0){
                initializeCreationView();
            } else {
                Recipe recipe = recipes.get(recipesIndex%recipes.size());
                setRecipe(recipe);
            }
        } catch (SQLException e) {
            onSQLException("Error while removing recipe.");
        }


    }

    @FXML
    void onEnterDurationTextField(ActionEvent ignoredEvent) {
        Recipe recipe = recipes.get(recipesIndex);
        try {
            editDBRecipeDuration(recipe.getTitle(), recipe.getDuration());
        } catch (SQLException e) {
            onSQLException("Error while updating duration.");
        }
    }

    @FXML
    void onEnterPortionsTextField(ActionEvent ignoredEvent) {
        Recipe recipe = recipes.get(recipesIndex);

        try{
            editDBRecipePortion(recipe.getTitle(), recipe.getPortions());
        } catch (SQLException e) {
            onSQLException("Error while updating portions.");
        }
    }

    @FXML
    void onEnterTitleTextField(ActionEvent ignoredEvent) {
        Recipe recipe = recipes.get(recipesIndex);

        String newTitle = titleTextField.getText();
        if (newTitle.isBlank()) {
            new Alert(Alert.AlertType.ERROR, "The title of the window can not be empty").showAndWait();
            return;
        }

        String oldTitle = recipe.getTitle();

        try {
            if (!oldTitle.isBlank()) {
                removeDBRecipe(oldTitle);
                recipes.remove(recipesIndex);
            }

            List<Ingredient> newIngredientList = new ArrayList<>(); // TODO ingredients handling
            List<String> newTagList = new ArrayList<>();

            for (Node node : tagGridPane.getChildren()) {
                ComboBox<String> comboBox = (ComboBox<String>) node;
                String newTag = comboBox.getEditor().getText();
                if (!newTag.isBlank()) {
                    newTagList.add(newTag);
                }
            }

            double newDuration = (durationTextField.getText().isBlank()) ? 0 :
                    Double.parseDouble(durationTextField.getText());
            int newPortions = (portionsTextField.getText().isBlank()) ? 0 :
                    Integer.parseInt(portionsTextField.getText());

            Recipe newRecipe = new Recipe(
                    newTitle,
                    newDuration,
                    (unitComboBoxSelected == 0) ? durationUnit.MIN : durationUnit.H,
                    newPortions,
                    categoryComboBoxSelected,
                    stepsTextArea.getText(),
                    newIngredientList,
                    newTagList
            );

            insertDBRecipe(newRecipe);
            recipes.add(newRecipe);
        } catch (SQLException e) {
            onSQLException("Error while inserting/editing recipe");
        }
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
    void stepsTextAreaAutoSave(){
        String steps = stepsTextArea.getText();
        Recipe recipe = recipes.get(recipesIndex);
        if(!steps.equals(recipe.getSteps())){
            try{
                editDBRecipeSteps(recipe.getTitle(), steps);
                recipe.setSteps(steps);
            } catch (SQLException e) {
                onSQLException("Error while auto-saving steps.");
            }
        }
    }
}
