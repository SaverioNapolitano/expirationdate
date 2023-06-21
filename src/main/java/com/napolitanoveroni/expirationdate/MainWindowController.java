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
import javafx.scene.control.Button;
import javafx.scene.control.Dialog;
import javafx.scene.control.TextField;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.sql.SQLIntegrityConstraintViolationException;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.ListIterator;
import java.util.NoSuchElementException;
import java.util.Optional;

import static com.napolitanoveroni.expirationdate.UtilsDB.*;

public class MainWindowController {

    @FXML
    private TableColumn<Product, LocalDate> expirationListExpirationDateColumn;

    @FXML
    private TableColumn<Product, String> expirationListProductColumn;

    @FXML
    private TableView<Product> expirationListTableView;

    @FXML
    private GridPane shoppingListGridPane;

    ObservableList<Product> expirationList;

    @FXML
    private VBox shoppingListVBox;

    private boolean cancelEditProduct = false;

    /*


    WINDOW INITIALIZING METHODS


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
            UtilsDB.onSQLException("Database Error: while loading data");
        }

        expirationListTableView.setItems(expirationList);
        editableCols();

        sortTableView(expirationListTableView);
    }

    private void editableCols() {
        expirationListProductColumn.setCellFactory(TextFieldTableCell.forTableColumn());
        expirationListProductColumn.setOnEditCommit(this::onEditProductColumn);

        expirationListExpirationDateColumn.setOnEditStart(this::onEditExpirationDateColumn);

        expirationListProductColumn.setEditable(true);
    }

    /*


    UTILITIES


     */

    void createExecuteICS(ICalendar iCalendar){
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

    void editCalendarEvent(Product oldProduct, Product newProduct){
        //TODO: see https://datatracker.ietf.org/doc/html/rfc5546#page-80 for updating an existing event
        deleteCalendarEvent(oldProduct);
        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        addCalendarEvent(newProduct);
    }

    void deleteCalendarEvent(Product product){

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

    void showNoProductSelectedAlert() {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle("No Selection");
        alert.setHeaderText("No Product Selected");
        alert.setContentText("Please select a product in the table.");
        alert.showAndWait();
    }

    /**
     * Returns the index of the selected person in the TableView component
     *
     * @return the index of the selected person
     */
    int selectedIndex() throws NoSuchElementException {
        int selectedIndex = expirationListTableView.getSelectionModel().getSelectedIndex();
        if (selectedIndex < 0) {
            throw new NoSuchElementException();
        }
        return selectedIndex;
    }

    void onOverlappingProducts(Product oldProduct, String newName, LocalDate newExpirationDate) {
        final String onSQLExceptionMessage = "Database Error while editing item";

        // In this case we renamed a product to an already existing item with the same expiration date
        expirationList.stream().filter(product -> product.getProductName().equals(newName) && product.getExpirationDate().equals(newExpirationDate)).forEach(product -> {
            int newQuantity = product.getQuantity() + oldProduct.getQuantity();
            try {
                editDBProductQuantity(product, newQuantity);
                product.setQuantity(newQuantity);
                removeDBProduct(oldProduct);
                expirationListTableView.getItems().remove(expirationListTableView.getSelectionModel().getSelectedIndex());
            } catch (SQLException ex) {
                UtilsDB.onSQLException(onSQLExceptionMessage);
            }
        });
    }

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
            dialog.initModality(Modality.WINDOW_MODAL);
            dialog.setDialogPane(view);

            // Show the dialog and wait until the user closes it
            Optional<ButtonType> clickedButton = dialog.showAndWait();
            if (clickedButton.orElse(ButtonType.CANCEL) == ButtonType.APPLY) {
                return controller.getProduct();
            }

            cancelEditProduct = true;

            return initialValue;
        } catch (NoSuchElementException e) {
            showNoProductSelectedAlert();
        } catch (IOException e) {
            e.printStackTrace();
        }

        throw new RuntimeException("Error while editing a product!");
    }

    void sortTableView(TableView<Product> expirationListTableView) {
        FXCollections.sort(expirationListTableView.getItems(), Comparator.comparing(Product::getExpirationDate));
    }

    /*


    UI INTERACTION - LEFT (EXPIRATION LIST) VIEW


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
            UtilsDB.onSQLException("Database Error while removing item");
        } catch (NoSuchElementException e) {
            showNoProductSelectedAlert();
        }


    }

    void onEditExpirationDateColumn(TableColumn.CellEditEvent<Product, LocalDate> event) {
        final String onSQLExceptionMessage = "Database Error while editing item";

        int selectedIndex = selectedIndex();
        Product oldProduct = event.getRowValue();
        Product editedProduct = actionOnProduct(oldProduct);

        if (!editedProduct.getProductName().equals("")) {
            try {
                editCalendarEvent(oldProduct, editedProduct);
                editDBProductAllField(oldProduct, editedProduct, this);
                expirationListTableView.getItems().set(selectedIndex, editedProduct);
            } catch (SQLException e) {
                UtilsDB.onSQLException(onSQLExceptionMessage);
            }
        }


        sortTableView(expirationListTableView);
    }

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
            UtilsDB.onSQLException(onSQLExceptionMessage);
        }
    }

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

    @FXML
    void onRecipesExpirationListButtonClicked(ActionEvent ignoredEvent) {
        // TODO database connection with recipes
    }

    /*


    UI INTERACTION - RIGHT (SHOPPING LIST) VIEW


     */

    @FXML
    void onEnterShoppingTextField(ActionEvent event) {  // TODO the new item is placed before the checked items
        CheckBox newCheckBox = new CheckBox();
        newCheckBox.setOnAction(this::onCheckBoxChecked);
        TextField newTextField = new TextField();
        GridPane.setMargin(newTextField, new Insets(0, 10, 0, 10));
        newTextField.setOnAction(this::onEnterShoppingTextField);

        Button newButton = new Button("Delete");    // TODO add graphic
        newButton.setOnAction(this::onDeleteShoppingListButtonClicked);

        shoppingListGridPane.addRow(shoppingListGridPane.getRowCount(), newCheckBox, newTextField, newButton);
        GridPane newGridPane = new GridPane();
        newGridPane.setPadding(new Insets(10, 10, 0, 10));
        newGridPane.setAlignment(Pos.TOP_CENTER);

        shoppingListVBox.getChildren().add(newGridPane);
        newGridPane.addRow(newGridPane.getRowCount(), newCheckBox, newTextField, newButton);
        newTextField.requestFocus(); //TODO resize newGridPane
    }

    @FXML
    void onDeleteShoppingListButtonClicked(ActionEvent event) {
        if (event.getSource() instanceof Button deleteButton) {
            shoppingListVBox.getChildren().remove(deleteButton.getParent());
        }
        if (shoppingListVBox.getChildren().size() == 0) {
            onEnterShoppingTextField(event);
        }
    }

    @FXML
    void onCheckBoxChecked(ActionEvent event) {
        if (event.getSource() instanceof CheckBox checkBox) {
            int index = shoppingListVBox.getChildren().indexOf(checkBox.getParent());
            if (!checkBox.isSelected() && !checkBox.isIndeterminate()) { //unchecked
                shoppingListVBox.getChildren().get(index).toBack();
            }
            if (checkBox.isSelected() && !checkBox.isIndeterminate()) { //checked
                shoppingListVBox.getChildren().get(index).toFront();
                index = shoppingListVBox.getChildren().size() - 1;

                String productName = "";

                if (shoppingListVBox.getChildren().get(index) instanceof GridPane indexthGridPane) {
                    if (indexthGridPane.getChildren().get(1) instanceof TextField textField) {
                        productName = textField.getText();
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
                    }
                } catch (SQLException e) {
                    new Alert(Alert.AlertType.ERROR, "Database Error: while adding item").showAndWait();
                }
            }
        }

        sortTableView(expirationListTableView);
    }

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
            onEnterShoppingTextField(ignoredEvent);
        }
    }
}
