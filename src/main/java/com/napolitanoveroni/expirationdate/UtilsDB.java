package com.napolitanoveroni.expirationdate;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.Alert;

import java.sql.*;
import java.time.LocalDate;
import java.time.ZoneId;

public class UtilsDB {
    static private HikariDataSource dataSource;

    public static LocalDate convertSQLDateToLocalDate(Date SQLDate) {
        java.util.Date date = new java.util.Date(SQLDate.getTime());
        return date.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
    }

    public static void onSQLException(String message) {
        new Alert(Alert.AlertType.ERROR, message).showAndWait();
    }

    static void dbConnection() throws SQLException {
        HikariConfig config = new HikariConfig();
        config.setDriverClassName(PersonalConfigDB.JDBC_Driver);
        config.setJdbcUrl(PersonalConfigDB.JDBC_URL);
        config.setLeakDetectionThreshold(2000);
        dataSource = new HikariDataSource(config);
    }

    static ObservableList<Product> getProductData() throws SQLException {
        ObservableList<Product> products = FXCollections.observableArrayList();

        try (
                Connection connection = dataSource.getConnection();
                PreparedStatement getProducts = connection.prepareStatement("SELECT * FROM products");
                ResultSet rs = getProducts.executeQuery()
        ) {
            while (rs.next()) {
                products.add(new Product(
                        rs.getString("productName"),
                        UtilsDB.convertSQLDateToLocalDate(rs.getDate("expirationDate")),
                        rs.getString("categoryName"),
                        rs.getInt("quantity"),
                        rs.getDouble("price")
                ));
            }
        }

        return products;
    }

    static void productDBUpdate(Product product, PreparedStatement updateProduct) throws SQLException {
        updateProduct.setString(2, product.getProductName());
        updateProduct.setDate(3, Date.valueOf(product.getExpirationDate()));
        updateProduct.executeUpdate();
    }

    static void editDBProductName(Product oldProduct, String newName, MainWindowController main) throws SQLException{

        try (
                Connection connection = dataSource.getConnection();
                PreparedStatement updateProduct = connection.prepareStatement("UPDATE products SET " +
                        "productName=?" +
                        " WHERE productName=?" +
                        " AND " +
                        "expirationDate=?")
        ) {
            updateProduct.setString(1, newName);
            productDBUpdate(oldProduct, updateProduct);
        } catch (SQLIntegrityConstraintViolationException exception) {
            main.onOverlappingProducts(oldProduct, newName, oldProduct.getExpirationDate());
        }
    }

    static void editDBProductAllField(Product oldProduct, Product newProduct, MainWindowController main) throws SQLException {
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
        } catch (SQLIntegrityConstraintViolationException exception) {
            main.onOverlappingProducts(oldProduct, newProduct.getProductName(), newProduct.getExpirationDate());
        }
    }

    static void editDBProductQuantity(Product product, int newQuantity) throws SQLException{
        try (
                Connection connection = dataSource.getConnection();
                PreparedStatement updateProduct = connection.prepareStatement("UPDATE products SET " +
                        "quantity=?" +
                        " WHERE productName=?" +
                        " AND " +
                        "expirationDate=?")) {
            updateProduct.setInt(1, newQuantity);
            productDBUpdate(product, updateProduct);
        }
    }

    static void insertDBProduct(Product product) throws SQLException {
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

    static void removeDBProduct(Product product) throws SQLException {
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
}
