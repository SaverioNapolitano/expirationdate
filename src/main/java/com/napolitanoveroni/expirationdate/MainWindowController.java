package com.napolitanoveroni.expirationdate;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;

import java.io.IOException;
import java.sql.*;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ListIterator;
import java.util.NoSuchElementException;

public class MainWindowController {



    @FXML private TableColumn<Product, LocalDate> expirationListExpirationDateColumn;

    @FXML private TableColumn<Product, String> expirationListProductColumn;

    @FXML private TableView<Product> expirationListTableView;

    @FXML private GridPane shoppingListGridPane;

    ObservableList<Product> expirationList;

    @FXML private VBox shoppingListVBox;


    private HikariDataSource dataSource;

    @FXML
    public void initialize() {
        expirationListProductColumn.setCellValueFactory(new PropertyValueFactory<>("productName"));
        expirationListExpirationDateColumn.setCellValueFactory(new PropertyValueFactory<>("expirationDate"));

        try {
            dbConnection();
            expirationList = getBoughtProductData();
        } catch (SQLException e) {
            expirationList = FXCollections.observableArrayList();
            new Alert(Alert.AlertType.ERROR, "Database Error: while loading data").showAndWait();
        }

        expirationListTableView.setItems(expirationList);
        editableCols();

        //        expirationList.addListener(); TODO add listener to expirationList


    }

    private void dbConnection() {
        HikariConfig config = new HikariConfig();
        config.setDriverClassName(UtilsDB.JDBC_Driver);
        config.setJdbcUrl(UtilsDB.JDBC_URL);
        config.setLeakDetectionThreshold(2000);
        dataSource = new HikariDataSource(config);
    }

    ObservableList<Product> getBoughtProductData() throws SQLException {
        ObservableList<Product> products = FXCollections.observableArrayList();
        // test product
        // boughtProducts.add(new BoughtProduct("latte", LocalDate.now(), "alimentari colazione", 1, 2));  // only for

        // purpose, TODO database connection
        try (
                Connection connection = dataSource.getConnection();
                PreparedStatement getProducts = connection.prepareStatement("SELECT * FROM products");
                ResultSet rs = getProducts.executeQuery()
        ) {
            while (rs.next()) {
                products.add(new Product(
                        rs.getString("productName"),
                        convertSQLDateToLocalDate(rs.getDate("expirationDate")),
                        rs.getString("categoryName"),
                        rs.getInt("quantity"),
                        rs.getDouble("price")
                ));
            }
        }


        return products;
    }

    public static LocalDate convertSQLDateToLocalDate(Date SQLDate) {
        java.util.Date date = new java.util.Date(SQLDate.getTime());
        return date.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
    }

    private void editableCols() {
        expirationListProductColumn.setCellFactory(TextFieldTableCell.forTableColumn());
        expirationListProductColumn.setOnEditCommit(e ->
        {
            Product oldProduct = e.getTableView().getItems().get(e.getTablePosition().getRow());
            Product backupOldProduct = new Product(oldProduct);
            String newName = e.getNewValue();

            try {
                editDBProductName(oldProduct, newName);
                oldProduct.setProductName(newName);
                removeDBProduct(backupOldProduct);
            } catch (SQLIntegrityConstraintViolationException exception) {
                expirationList.stream().filter(
                        product -> product.getProductName().equals(newName) && product.getExpirationDate().equals(oldProduct.getExpirationDate())
                ).forEach(product -> {
                    int newQuantity = product.getQuantity() + oldProduct.getQuantity();
                    try {
                        editDBProductQuantity(product, newQuantity);
                        product.setQuantity(newQuantity);
                        removeDBProduct(backupOldProduct);
                        expirationListTableView.getItems().remove(expirationListTableView.getSelectionModel().getSelectedIndex());
                    } catch (SQLException ex) {
                        new Alert(Alert.AlertType.ERROR, "Database Error while editing item").showAndWait();
                    }
                });
            } catch (SQLException exception){
                new Alert(Alert.AlertType.ERROR, "Database Error while editing item").showAndWait();
            }

        });

        expirationListProductColumn.setEditable(true);
    }

    @FXML
    void onCalendarExpirationListButtonClicked(ActionEvent ignoredEvent) {
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
    void onDeleteExpirationListButtonClicked(ActionEvent ignoredEvent) {
        try {
            int selectedIndex = selectedIndex();
            Product removeProduct = expirationListTableView.getItems().get(selectedIndex);

            removeDBProduct(removeProduct);

            expirationListTableView.getItems().remove(selectedIndex);
        } catch (SQLException e) {
            new Alert(Alert.AlertType.ERROR, "Database Error while removing item").showAndWait();
        } catch (NoSuchElementException e) {
            showNoProductSelectedAlert();
        }
    }

    /**
     * Returns the index of the selected person in the TableView component
     *
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
    void onEditExpirationDateColumn(TableColumn.CellEditEvent<Product, LocalDate> event) {
        int selectedIndex = selectedIndex();
        Product oldProduct = event.getRowValue();
        Product editedProduct = actionOnProduct(event.getRowValue());
        try{
            editDBProductAllField(oldProduct, editedProduct);
            expirationListTableView.getItems().set(selectedIndex, editedProduct);
        } catch (SQLIntegrityConstraintViolationException e){
            expirationList.stream().filter(
                    product -> product.getProductName().equals(editedProduct.getProductName()) && product.getExpirationDate().equals(editedProduct.getExpirationDate())
            ).forEach(product -> {
                int newQuantity = product.getQuantity() + editedProduct.getQuantity();
                try {
                    editDBProductQuantity(product, newQuantity);
                    product.setQuantity(newQuantity);
                } catch (SQLException ex) {
                    new Alert(Alert.AlertType.ERROR, "Database Error while editing item").showAndWait();
                }
            });

            try{
                removeDBProduct(oldProduct);
                expirationListTableView.getItems().remove(selectedIndex);
            } catch (SQLException exception){
                new Alert(Alert.AlertType.ERROR, "Database Error while editing item").showAndWait();
            }
        } catch (SQLException e){
            new Alert(Alert.AlertType.ERROR, "Database Error while editing item").showAndWait();
        }
    }

    void editDBProductName(Product product, String newName) throws SQLException{
        try (
                Connection connection = dataSource.getConnection();
                PreparedStatement updateProduct = connection.prepareStatement("UPDATE products SET " +
                        "productName=?" +
                        " WHERE productName=?" +
                        " AND " +
                        "expirationDate=?")) {
            updateProduct.setString(1, newName);
            updateProduct.setString(2, product.getProductName());
            updateProduct.setDate(3, Date.valueOf(product.getExpirationDate()));
            updateProduct.executeUpdate();
        }
    }

    void editDBProductQuantity(Product product, int newQuantity) throws SQLException{
        try (
                Connection connection = dataSource.getConnection();
                PreparedStatement updateProduct = connection.prepareStatement("UPDATE products SET " +
                        "quantity=?" +
                        " WHERE productName=?" +
                        " AND " +
                        "expirationDate=?")) {
            updateProduct.setInt(1, newQuantity);
            updateProduct.setString(2, product.getProductName());
            updateProduct.setDate(3, Date.valueOf(product.getExpirationDate()));
            updateProduct.executeUpdate();
        }
    }
    void editDBProductAllField(Product oldProduct, Product newProduct) throws SQLException {
        try (
            Connection connection = dataSource.getConnection();
            PreparedStatement updateProduct = connection.prepareStatement("UPDATE products SET productName=?, expirationDate=?, " +
                         "categoryName=?, quantity=?, price=?" +
                         " WHERE productName=?" +
                         " AND " +
                         "expirationDate=?")) {
            updateProduct.setString(1, newProduct.getProductName());
            updateProduct.setDate(2, Date.valueOf(newProduct.getExpirationDate()));
            updateProduct.setString(3, newProduct.getCategoryName());
            updateProduct.setInt(4, newProduct.getQuantity());
            updateProduct.setDouble(5, newProduct.getPrice());
            updateProduct.setString(6, oldProduct.getProductName());
            updateProduct.setDate(7, Date.valueOf(oldProduct.getExpirationDate()));
            updateProduct.executeUpdate();
        }
    }

    public Product actionOnProduct(Product initialValue) {
        try {
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(getClass().getResource("EditBoughtProduct.fxml"));
            DialogPane view = loader.load();
            BoughtProductController controller = loader.getController();

            controller.setProduct(new Product(initialValue));

            // Create the dialog
            Dialog<ButtonType> dialog = new Dialog<>();
            dialog.setTitle("Edit Product");
            dialog.initModality(Modality.WINDOW_MODAL);
            dialog.setDialogPane(view);

            // Show the dialog and wait until the user closes it
            dialog.showAndWait();       // TODO add cancel button Check
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
        Product edited = actionOnProduct(new Product());
        try {
            insertDBProduct(edited);
            expirationList.add(edited);
        } catch (SQLIntegrityConstraintViolationException e) {
            expirationList.stream().filter(
                    product -> product.getProductName().equals(edited.getProductName()) && product.getExpirationDate().equals(edited.getExpirationDate())
            ).forEach(product -> {
                int newQuantity = product.getQuantity() + edited.getQuantity();
                try {
                    editDBProductQuantity(product, newQuantity);
                    product.setQuantity(newQuantity);
                } catch (SQLException ex) {
                    new Alert(Alert.AlertType.ERROR, "Database Error while editing item").showAndWait();
                }
            });
        }
        catch (SQLException e) {
            new Alert(Alert.AlertType.ERROR, "Database Error: while adding item").showAndWait();
        }
    }

    void insertDBProduct(Product product) throws SQLException {
        try (
            Connection connection = dataSource.getConnection();
            PreparedStatement insertProduct =
                    connection.prepareStatement("INSERT INTO products (productName, " + "expirationDate, " +
                            "categoryName, " + "quantity, price) VALUES (?, ?, ?, ?, ?)")
        ) {
            insertProduct.setString(1, product.getProductName());
            insertProduct.setDate(2, Date.valueOf(product.getExpirationDate()));
            insertProduct.setString(3, product.getCategoryName());
            insertProduct.setInt(4, product.getQuantity());
            insertProduct.setDouble(5, product.getPrice());
            insertProduct.executeUpdate();
        }
    }

    @FXML
    void onRecipesExpirationListButtonClicked(ActionEvent ignoredEvent) {
        // TODO database connection with recipes
    }

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
        newGridPane.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
        newGridPane.setMinSize(Region.USE_COMPUTED_SIZE, Region.USE_COMPUTED_SIZE);
        newGridPane.setPrefSize(Region.USE_COMPUTED_SIZE, Region.USE_COMPUTED_SIZE);
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

    void removeDBProduct(Product product) throws SQLException {
        try (
            Connection connection = dataSource.getConnection();
            PreparedStatement deleteProduct = connection.prepareStatement("DELETE FROM products WHERE productName=? " +
                    "AND expirationDate=?")
        ) {
            deleteProduct.setString(1, product.getProductName());
            deleteProduct.setDate(2, Date.valueOf(product.getExpirationDate()));
            deleteProduct.executeUpdate();
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
                    insertDBProduct(edited);
                    expirationList.add(edited);
                } catch (SQLException e) {
                    new Alert(Alert.AlertType.ERROR, "Database Error: while adding item").showAndWait();
                }
            }
        }
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
    }
}
