package com.napolitanoveroni.expirationdate;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.Alert;

import java.sql.*;
import java.sql.Date;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.*;

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

    static ObservableList<Recipe> getRecipeData() throws SQLException {
        ObservableList<Recipe> returnValue = FXCollections.observableArrayList();

        Map<String, List<Ingredient>> ingredientsMap = new HashMap<>();
        Map<String, List<String>> tagMap = new HashMap<>();

        try (
                Connection connection = dataSource.getConnection();
                PreparedStatement getIngredients = connection.prepareStatement(
                    "SELECT consist.* " +
                        "FROM recipe " +
                            "NATURAL JOIN consist ");
                ResultSet rs = getIngredients.executeQuery()
        ) {
            while (rs.next()) {
                String title = rs.getString("title");
                Ingredient ingredient = new Ingredient(
                        rs.getString("ingredient"),
                        rs.getDouble("quantity"),
                        rs.getString("unit_of_measurement")
                );

                if (!ingredientsMap.containsKey(title))  {
                    ingredientsMap.put(title, new ArrayList<>());
                }
                ingredientsMap.get(title).add(ingredient);
            }
        }

        try (
                Connection connection = dataSource.getConnection();
                PreparedStatement getTag = connection.prepareStatement(
                        "SELECT tag.* " +
                                "FROM recipe " +
                                "NATURAL JOIN tag ");
                ResultSet rs = getTag.executeQuery()
        ) {
            while (rs.next()) {
                String title = rs.getString("title");
                String tag = rs.getString("tag");

                if (!tagMap.containsKey(title))  {
                    tagMap.put(title, new ArrayList<>());
                }
                tagMap.get(title).add(tag);
            }
        }

        try (
                Connection connection = dataSource.getConnection();
                PreparedStatement getRecipe = connection.prepareStatement(
                        "SELECT * " +
                        "FROM recipe ");
                ResultSet rs = getRecipe.executeQuery()
        ) {
            while (rs.next()) {
                String title = rs.getString("title");

                Optional<List<Ingredient>> optionalIngredientList = Optional.ofNullable(ingredientsMap.get(title));
                Optional<List<String>> optionalTagList = Optional.ofNullable(tagMap.get(title));

                returnValue.add(new Recipe(
                        title,
                        rs.getDouble("duration"),
                        (rs.getInt("unit") == 0) ? durationUnit.MIN : durationUnit.H,
                        rs.getInt("portions"),
                        rs.getString("category"),
                        rs.getString("steps"),
                        optionalIngredientList.orElse(new ArrayList<>()),
                        optionalTagList.orElse(new ArrayList<>())
                ));
            }
        }

        return returnValue;
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

    static void insertDBTag(String title, String tag) throws SQLException {
        try (
                Connection connection = dataSource.getConnection();
                PreparedStatement insertTag =
                        connection.prepareStatement(
                            "INSERT INTO tag (title, tag) VALUES (?, ?)")
        ) {
            insertTag.setString(1, title);
            insertTag.setString(2, tag);
            insertTag.executeUpdate();
        }
    }

    static void removeDBTag(String title, String tag) throws SQLException {
        try (
                Connection connection = dataSource.getConnection();
                PreparedStatement deleteTag = connection.prepareStatement("DELETE FROM tag WHERE title=? " +
                        "AND tag=?")
        ) {
            deleteTag.setString(1, title);
            deleteTag.setString(2, tag);
            deleteTag.executeUpdate();
        }
    }

    static void editDBRecipeCategory(String title, String category) throws SQLException{

        try (
                Connection connection = dataSource.getConnection();
                PreparedStatement updateRecipe = connection.prepareStatement("UPDATE recipe SET " +
                        "category=?" +
                        " WHERE title=?")
        ) {
            updateRecipe.setString(1, category);
            updateRecipe.setString(2, title);
            updateRecipe.executeUpdate();
        } catch (SQLIntegrityConstraintViolationException ignored) {

        }
    }

    static void editDBRecipeUnit(String title, int unit) throws SQLException{

        try (
                Connection connection = dataSource.getConnection();
                PreparedStatement updateRecipe = connection.prepareStatement("UPDATE recipe SET " +
                        "unit=?" +
                        " WHERE title=?")
        ) {
            updateRecipe.setInt(1, unit);
            updateRecipe.setString(2, title);
            updateRecipe.executeUpdate();
        } catch (SQLIntegrityConstraintViolationException ignored) {

        }
    }

    static void editDBRecipeDuration(String title, double duration) throws SQLException{

        try (
                Connection connection = dataSource.getConnection();
                PreparedStatement updateRecipe = connection.prepareStatement("UPDATE recipe SET " +
                        "duration=?" +
                        " WHERE title=?")
        ) {
            updateRecipe.setDouble(1, duration);
            updateRecipe.setString(2, title);
            updateRecipe.executeUpdate();
        } catch (SQLIntegrityConstraintViolationException ignored) {

        }
    }

    static void editDBRecipePortion(String title, int portions) throws SQLException{

        try (
                Connection connection = dataSource.getConnection();
                PreparedStatement updateRecipe = connection.prepareStatement("UPDATE recipe SET " +
                        "portions=?" +
                        " WHERE title=?")
        ) {
            updateRecipe.setInt(1, portions);
            updateRecipe.setString(2, title);
            updateRecipe.executeUpdate();
        } catch (SQLIntegrityConstraintViolationException ignored) {

        }
    }

    /*TODO
    static void editDBRecipeTitle(String oldTitle, String newTitle) throws SQLException{

        try (
                Connection connection = dataSource.getConnection();
                PreparedStatement updateRecipe = connection.prepareStatement("UPDATE recipe SET " +
                        "duration=?" +
                        " WHERE title=?")
        ) {
            updateRecipe.setDouble(1, duration);
            updateRecipe.setString(2, title);
            updateRecipe.executeUpdate();
        } catch (SQLIntegrityConstraintViolationException ignored) {

        }
    }*/
}
