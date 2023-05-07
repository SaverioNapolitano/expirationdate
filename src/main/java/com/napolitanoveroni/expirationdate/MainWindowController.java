package com.napolitanoveroni.expirationdate;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
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

    @FXML
    public void initialize() {
        expirationListProductColumn.setCellValueFactory(new PropertyValueFactory<>("productName"));
        expirationListExpirationDateColumn.setCellValueFactory(new PropertyValueFactory<>("expirationDate"));
        expirationListTableView.setItems(getBoughtProductData());

        expirationListTableView.getSelectionModel().selectedItemProperty().addListener((observable, oldValue,
                                                                                        newValue) -> onExpirationListItemSelected(newValue));
    }

    ObservableList<BoughtProduct> getBoughtProductData() {
        ObservableList<BoughtProduct> boughtProducts = FXCollections.observableArrayList();
        // test product
        boughtProducts.add(new BoughtProduct("alimentari colazione", "latte", LocalDate.now(), 1, 2, 1,
                LocalDate.now()));
        return boughtProducts;
    }

    @FXML
    void onCalendarExpirationListButtonClicked(ActionEvent event) {

    }

    @FXML
    void onDeleteExpirationListButtonClicked(ActionEvent event) {

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
        try {
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(getClass().getResource("EditBoughtProduct.fxml"));
            DialogPane view = loader.load();
            BoughtProductController controller = loader.getController();

            // Set the person into the controller.
            int selectedIndex = selectedIndex();
            controller.setProduct(new BoughtProduct(expirationListTableView.getItems().get(selectedIndex)));

            // Create the dialog
            Dialog<ButtonType> dialog = new Dialog<>();
            dialog.setTitle("Edit Person");
            dialog.initModality(Modality.WINDOW_MODAL);
            dialog.setDialogPane(view);

            // Show the dialog and wait until the user closes it
            Optional<ButtonType> clickedButton = dialog.showAndWait();
            if (clickedButton.orElse(ButtonType.CANCEL) == ButtonType.APPLY) {
                expirationListTableView.getItems().set(selectedIndex, controller.getProduct());
            }
        } catch (NoSuchElementException e) {
            //TODO IMPLEMENT showNoProductSelectedAlert();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    void onNewExpirationListButtonClicked(ActionEvent event) {
    }

    @FXML
    void onRecipesExpirationListButtonClicked(ActionEvent event) {

    }

}
