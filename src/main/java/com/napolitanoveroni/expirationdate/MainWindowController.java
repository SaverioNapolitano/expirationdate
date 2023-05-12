package com.napolitanoveroni.expirationdate;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;

import java.io.IOException;
import java.time.LocalDate;
import java.util.NoSuchElementException;

public class MainWindowController {

    @FXML private TableColumn<BoughtProduct, LocalDate> expirationListExpirationDateColumn;

    @FXML private TableColumn<BoughtProduct, String> expirationListProductColumn;


    @FXML private TableView<BoughtProduct> expirationListTableView;

    @FXML private GridPane shoppingListGridPane;

    ObservableList<BoughtProduct> expirationList;

    @FXML private VBox shoppingListVBox;

    @FXML
    public void initialize() {
        expirationListProductColumn.setCellValueFactory(new PropertyValueFactory<>("productName"));
        expirationListExpirationDateColumn.setCellValueFactory(new PropertyValueFactory<>("expirationDate"));
        expirationList = getBoughtProductData();
        expirationListTableView.setItems(expirationList);
        editableCols();

//        expirationList.addListener(); TODO add listener to expirationList
    }

    ObservableList<BoughtProduct> getBoughtProductData() {
        ObservableList<BoughtProduct> boughtProducts = FXCollections.observableArrayList();
        // test product
        boughtProducts.add(new BoughtProduct("alimentari colazione", "latte", LocalDate.now(), 1, 2, 1,
                LocalDate.now()));  // only for test purpose, TODO database connection
        return boughtProducts;
    }

    private void editableCols(){
        expirationListProductColumn.setCellFactory(TextFieldTableCell.forTableColumn());
        expirationListProductColumn.setOnEditCommit(e->e.getTableView().getItems().get(e.getTablePosition().getRow()).setProductName(e.getNewValue()));

        expirationListProductColumn.setEditable(true);
    }

    @FXML
    void onCalendarExpirationListButtonClicked(ActionEvent event) {
        // TODO calendar integration
    }

    void showNoProductSelectedAlert() {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle("No Selection");
        alert.setHeaderText("No Product Selected");
        alert.setContentText("Please select a product in the table.");
        alert.showAndWait();
    }
    @FXML
    void onDeleteExpirationListButtonClicked(ActionEvent event) {
        try {
            int selectedIndex = selectedIndex();
            expirationListTableView.getItems().remove(selectedIndex);
        } catch (NoSuchElementException e) {
            showNoProductSelectedAlert();
        }
    }

    /**
     * Returns the index of the selected person in the TableView component
     * @return the index of the selected person
     */
    int selectedIndex() {
        int selectedIndex = expirationListTableView.getSelectionModel().getSelectedIndex();
        if (selectedIndex < 0) {
            throw new NoSuchElementException();
        }
        return selectedIndex;
    }

    @FXML
    void onEditExpirationDateColumn(TableColumn.CellEditEvent<BoughtProduct, LocalDate> event) {
        int selectedIndex = selectedIndex();
        BoughtProduct edited = actionOnProduct(event.getRowValue());

        expirationListTableView.getItems().set(selectedIndex, edited);
    }

    public BoughtProduct actionOnProduct(BoughtProduct initialValue) {
        try {
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(getClass().getResource("EditBoughtProduct.fxml"));
            DialogPane view = loader.load();
            BoughtProductController controller = loader.getController();

            controller.setProduct(initialValue);

            // Create the dialog
            Dialog<ButtonType> dialog = new Dialog<>();
            dialog.setTitle("Edit Product");
            dialog.initModality(Modality.WINDOW_MODAL);
            dialog.setDialogPane(view);

            // Show the dialog and wait until the user closes it
            dialog.showAndWait();
            return controller.getProduct();
        } catch (NoSuchElementException e) {
            showNoProductSelectedAlert();
        } catch (IOException e) {
            e.printStackTrace();
        }

        throw new RuntimeException("Error while editing a product!");
    }

    @FXML
    void onNewExpirationListButtonClicked(ActionEvent ignoredEvent) {
        BoughtProduct edited = actionOnProduct(new BoughtProduct());
        expirationList.add(edited);
    }

    @FXML
    void onRecipesExpirationListButtonClicked(ActionEvent event) {
        // TODO database connection with recipes
    }

    @FXML
    void onEnterShoppingTextField(ActionEvent event) {
        CheckBox newCheckBox = new CheckBox();

        TextField newTextField = new TextField();
        GridPane.setMargin(newTextField, new Insets(0, 10, 0, 10));
        newTextField.setOnAction(this::onEnterShoppingTextField);

        Button newButton = new Button("Delete");    // TODO add graphic
        newButton.setOnAction(this::onDeleteShoppingListButtonClicked);

        shoppingListGridPane.addRow(shoppingListGridPane.getRowCount(), newCheckBox, newTextField, newButton);
        GridPane newGridPane = new GridPane();
        newGridPane.setPadding(new Insets(10, 10, 0, 10));
        newGridPane.setAlignment(Pos.TOP_CENTER);
        newGridPane.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
        newGridPane.setMinSize(Region.USE_COMPUTED_SIZE, Region.USE_COMPUTED_SIZE);
        newGridPane.setPrefSize(Region.USE_COMPUTED_SIZE, Region.USE_COMPUTED_SIZE);
        shoppingListVBox.getChildren().add(newGridPane);
        newGridPane.addRow(newGridPane.getRowCount(), newCheckBox, newTextField, newButton);
        newTextField.requestFocus(); //TODO resize newGridPane
    }

    @FXML
    void onDeleteShoppingListButtonClicked(ActionEvent event) {
        if(event.getSource() instanceof Button deleteButton){
            shoppingListVBox.getChildren().remove(deleteButton.getParent());
        }
        if(shoppingListVBox.getChildren().size() == 0){
            onEnterShoppingTextField(event);
        }
    }

}
