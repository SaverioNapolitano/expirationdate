package com.napolitanoveroni.expirationdate;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.*;
import javafx.scene.control.cell.ComboBoxTableCell;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.scene.layout.GridPane;
import javafx.stage.Modality;

import java.io.IOException;
import java.time.LocalDate;
import java.util.NoSuchElementException;
import java.util.Optional;

public class MainWindowController {

    @FXML
    private TableColumn<BoughtProduct, LocalDate> expirationListExpirationDateColumn;

    @FXML
    private TableColumn<BoughtProduct, String> expirationListProductColumn;


    @FXML
    private TableView<BoughtProduct> expirationListTableView;

    @FXML
    private GridPane shoppingListGridPane;

    ObservableList<BoughtProduct> expirationList;

    public static final int NEW = 0;
    public static final int EDIT = 1;

    @FXML
    public void initialize() {
        expirationListProductColumn.setCellValueFactory(new PropertyValueFactory<>("productName"));
        expirationListExpirationDateColumn.setCellValueFactory(new PropertyValueFactory<>("expirationDate"));
        expirationList = getBoughtProductData();
        expirationListTableView.setItems(expirationList);
        editableCols();
        //expirationListTableView.getSelectionModel().selectedItemProperty().addListener((observable, oldValue,
                                                                                         //newValue) ->
        // onExpirationListItemSelected(newValue));
    }

    ObservableList<BoughtProduct> getBoughtProductData() {
        ObservableList<BoughtProduct> boughtProducts = FXCollections.observableArrayList();
        // test product
        boughtProducts.add(new BoughtProduct("alimentari colazione", "latte", LocalDate.now(), 1, 2, 1,
                LocalDate.now()));
        return boughtProducts;
    }

    private void editableCols(){
        expirationListProductColumn.setCellFactory(TextFieldTableCell.forTableColumn());
        expirationListProductColumn.setOnEditCommit(e->e.getTableView().getItems().get(e.getTablePosition().getRow()).setProductName(e.getNewValue()));

        expirationListExpirationDateColumn.setCellFactory(ComboBoxTableCell.forTableColumn()); //TODO fix datepicker
        expirationListExpirationDateColumn.setOnEditCommit(e->e.getTableView().getItems().get(e.getTablePosition().getRow()).setExpirationDate(e.getNewValue()));

        /* Allow for the values in each cell to be changable */
        expirationListTableView.setEditable(true);
    }

    @FXML
    void onCalendarExpirationListButtonClicked(ActionEvent event) {

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
    void onExpirationListItemSelected(BoughtProduct boughtProduct) {
        actionOnProduct(EDIT);
    }

    public void actionOnProduct(int mode){
        try {
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(getClass().getResource("EditBoughtProduct.fxml"));
            DialogPane view = loader.load();
            BoughtProductController controller = loader.getController();

            int selectedIndex = 0;
            if(mode == EDIT){
                // Set the product into the controller.
                selectedIndex = selectedIndex();
                controller.setProduct(new BoughtProduct(expirationListTableView.getItems().get(selectedIndex)));
            } else {
                controller.setProduct(new BoughtProduct());
            }

            // Create the dialog
            Dialog<ButtonType> dialog = new Dialog<>();
            dialog.setTitle("Edit Product");
            dialog.initModality(Modality.WINDOW_MODAL);
            dialog.setDialogPane(view);

            // Show the dialog and wait until the user closes it
            Optional<ButtonType> clickedButton = dialog.showAndWait();
            if (clickedButton.orElse(ButtonType.CANCEL) == ButtonType.APPLY) {
                if(mode == EDIT){
                    expirationListTableView.getItems().set(selectedIndex, controller.getProduct());
                } else {
                    expirationList.add(controller.getProduct());
                }
            }
        } catch (NoSuchElementException e) {
            showNoProductSelectedAlert();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    void onNewExpirationListButtonClicked(ActionEvent event) {
        actionOnProduct(NEW);
    }

    @FXML
    void onRecipesExpirationListButtonClicked(ActionEvent event) {

    }

}
