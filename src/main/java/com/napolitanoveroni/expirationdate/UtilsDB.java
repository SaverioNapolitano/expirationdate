/**
 * Utility class for interacting with the database.
 */

package com.napolitanoveroni.expirationdate;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.sql.Date;
import java.sql.*;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.*;

public class UtilsDB {
	static private HikariDataSource dataSource;

	/**
	 * Establishes a connection to the database.
	 *
	 * @throws SQLException if a database access error occurs.
	 */

	static void dbConnection() throws SQLException {
		HikariConfig config = new HikariConfig();
		config.setDriverClassName(PersonalConfigDB.JDBC_Driver);
		config.setJdbcUrl(PersonalConfigDB.JDBC_URL);
		config.setLeakDetectionThreshold(2000);
		dataSource = new HikariDataSource(config);
	}

	/**
	 * Retrieves product data from the database.
	 *
	 * @return an observable list of products.
	 * @throws SQLException if a database access error occurs.
	 */

	static ObservableList<Product> getProductData() throws SQLException {
		ObservableList<Product> products = FXCollections.observableArrayList();

		try (Connection connection = dataSource.getConnection(); PreparedStatement getProducts = connection.prepareStatement("SELECT * FROM products"); ResultSet rs = getProducts.executeQuery()) {
			while (rs.next()) {
				products.add(new Product(rs.getString("productName"), UtilsDB.convertSQLDateToLocalDate(rs.getDate("expirationDate")), rs.getString("categoryName"), rs.getInt("quantity"), rs.getDouble("price")));
			}
		}

		return products;
	}

	/**
	 * Converts an SQL date to a LocalDate object.
	 *
	 * @param SQLDate the SQL date to be converted.
	 * @return the converted LocalDate object.
	 */

	public static LocalDate convertSQLDateToLocalDate(Date SQLDate) {
		java.util.Date date = new java.util.Date(SQLDate.getTime());
		return date.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
	}

	/**
	 * Retrieves recipe data from the database.
	 *
	 * @return an observable list of recipes.
	 * @throws SQLException if a database access error occurs.
	 */

	static ObservableList<Recipe> getRecipeData() throws SQLException {
		ObservableList<Recipe> returnValue = FXCollections.observableArrayList();

		Map<String, List<Ingredient>> ingredientsMap = new HashMap<>();
		Map<String, List<String>> tagMap = new HashMap<>();

		try (Connection connection = dataSource.getConnection(); PreparedStatement getIngredients = connection.prepareStatement("SELECT consist.* " + "FROM recipe " + "NATURAL JOIN consist "); ResultSet rs = getIngredients.executeQuery()) {
			while (rs.next()) {
				String title = rs.getString("title");
				Ingredient ingredient =
					new Ingredient(rs.getString("ingredient"), rs.getDouble("quantity"), rs.getString("unit_of_measurement"));

				if (!ingredientsMap.containsKey(title)) {
					ingredientsMap.put(title, new ArrayList<>());
				}
				ingredientsMap.get(title).add(ingredient);
			}
		}

		try (Connection connection = dataSource.getConnection(); PreparedStatement getTag = connection.prepareStatement("SELECT tag.* " + "FROM recipe " + "NATURAL JOIN tag "); ResultSet rs = getTag.executeQuery()) {
			while (rs.next()) {
				String title = rs.getString("title");
				String tag = rs.getString("tag");

				if (!tagMap.containsKey(title)) {
					tagMap.put(title, new ArrayList<>());
				}
				tagMap.get(title).add(tag);
			}
		}

		try (Connection connection = dataSource.getConnection(); PreparedStatement getRecipe = connection.prepareStatement("SELECT * " + "FROM recipe "); ResultSet rs = getRecipe.executeQuery()) {
			while (rs.next()) {
				String title = rs.getString("title");

				Optional<List<Ingredient>> optionalIngredientList = Optional.ofNullable(ingredientsMap.get(title));
				Optional<List<String>> optionalTagList = Optional.ofNullable(tagMap.get(title));

				returnValue.add(new Recipe(title, rs.getDouble("duration"), (rs.getInt("unit") == 0) ? durationUnit.MIN : durationUnit.H, rs.getInt("portions"), rs.getString("category"), rs.getString("steps"), optionalIngredientList.orElse(new ArrayList<>()), optionalTagList.orElse(new ArrayList<>())));
			}
		}

		return returnValue;
	}

	/**
	 * Edits the product name in the database.
	 *
	 * @param oldProduct the old product object.
	 * @param newName    the new name of the product.
	 * @param main       the main window controller.
	 * @throws SQLException if a database access error occurs.
	 */

	static void editDBProductName(Product oldProduct, String newName, MainWindowController main) throws SQLException {

		try (Connection connection = dataSource.getConnection(); PreparedStatement updateProduct = connection.prepareStatement("UPDATE products SET " + "productName=?" + " WHERE productName=?" + " AND " + "expirationDate=?")) {
			updateProduct.setString(1, newName);
			productDBUpdate(oldProduct, updateProduct);
		} catch (SQLIntegrityConstraintViolationException exception) {
			main.onOverlappingProducts(oldProduct, newName, oldProduct.getExpirationDate());
		}
	}

	/**
	 * Updates the product information in the database.
	 *
	 * @param product       the product object to be updated.
	 * @param updateProduct the prepared statement for updating the product.
	 * @throws SQLException if a database access error occurs.
	 */

	static void productDBUpdate(Product product, PreparedStatement updateProduct) throws SQLException {
		updateProduct.setString(2, product.getProductName());
		updateProduct.setDate(3, Date.valueOf(product.getExpirationDate()));
		updateProduct.executeUpdate();
	}

	/**
	 * Edits all fields of a product in the database.
	 *
	 * @param oldProduct the old product object.
	 * @param newProduct the new product object.
	 * @param main       the main window controller.
	 * @throws SQLException if a database access error occurs.
	 */

	static void editDBProductAllField(Product oldProduct, Product newProduct, MainWindowController main) throws
		SQLException {
		try (Connection connection = dataSource.getConnection(); PreparedStatement updateProduct = connection.prepareStatement("UPDATE products SET productName=?, expirationDate=?, " + "categoryName=?, quantity=?, price=?" + " WHERE productName=?" + " AND " + "expirationDate=?")) {
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

	/**
	 * Edits the quantity of a product in the database.
	 *
	 * @param product     the product object.
	 * @param newQuantity the new quantity of the product.
	 * @throws SQLException if a database access error occurs.
	 */

	static void editDBProductQuantity(Product product, int newQuantity) throws SQLException {
		try (Connection connection = dataSource.getConnection(); PreparedStatement updateProduct = connection.prepareStatement("UPDATE products SET " + "quantity=?" + " WHERE productName=?" + " AND " + "expirationDate=?")) {
			updateProduct.setInt(1, newQuantity);
			productDBUpdate(product, updateProduct);
		}
	}

	/**
	 * Inserts a new product into the database.
	 *
	 * @param product the product object to be inserted.
	 * @throws SQLException if a database access error occurs.
	 */

	static void insertDBProduct(Product product) throws SQLException {
		try (Connection connection = dataSource.getConnection(); PreparedStatement insertProduct = connection.prepareStatement("INSERT INTO products (productName, " + "expirationDate, " + "categoryName, " + "quantity, price) VALUES (?, ?, ?, ?, ?)")) {
			insertProduct.setString(1, product.getProductName());
			insertProduct.setDate(2, Date.valueOf(product.getExpirationDate()));
			insertProduct.setString(3, product.getCategoryName());
			insertProduct.setInt(4, product.getQuantity());
			insertProduct.setDouble(5, product.getPrice());
			insertProduct.executeUpdate();
		}
	}

	/**
	 * Removes a product from the database.
	 *
	 * @param product the product object to be removed.
	 * @throws SQLException if a database access error occurs.
	 */

	static void removeDBProduct(Product product) throws SQLException {
		try (Connection connection = dataSource.getConnection(); PreparedStatement deleteProduct = connection.prepareStatement("DELETE FROM products WHERE productName=? " + "AND expirationDate=?")) {
			deleteProduct.setString(1, product.getProductName());
			deleteProduct.setDate(2, Date.valueOf(product.getExpirationDate()));
			deleteProduct.executeUpdate();
		}
	}

	/**
	 * Removes a tag from a recipe in the database.
	 *
	 * @param title the title of the recipe.
	 * @param tag   the tag to be removed.
	 * @throws SQLException if a database access error occurs.
	 */

	static void removeDBTag(String title, String tag) throws SQLException {
		try (Connection connection = dataSource.getConnection(); PreparedStatement deleteTag = connection.prepareStatement("DELETE FROM tag WHERE title=? " + "AND tag=?")) {
			deleteTag.setString(1, title);
			deleteTag.setString(2, tag);
			deleteTag.executeUpdate();
		}
	}

	/**
	 * Edits the category of a recipe in the database.
	 *
	 * @param title    the title of the recipe.
	 * @param category the new category of the recipe.
	 * @throws SQLException if a database access error occurs.
	 */

	static void editDBRecipeCategory(String title, String category) throws SQLException {

		try (Connection connection = dataSource.getConnection(); PreparedStatement updateRecipe = connection.prepareStatement("UPDATE recipe SET " + "category=?" + " WHERE title=?")) {
			updateRecipe.setString(1, category);
			updateRecipe.setString(2, title);
			updateRecipe.executeUpdate();
		} catch (SQLIntegrityConstraintViolationException ignored) {

		}
	}

	/**
	 * Edits the unit of a recipe in the database.
	 *
	 * @param title the title of the recipe.
	 * @param unit  the new unit of the recipe.
	 * @throws SQLException if a database access error occurs.
	 */

	static void editDBRecipeUnit(String title, int unit) throws SQLException {

		try (Connection connection = dataSource.getConnection(); PreparedStatement updateRecipe = connection.prepareStatement("UPDATE recipe SET " + "unit=?" + " WHERE title=?")) {
			updateRecipe.setInt(1, unit);
			updateRecipe.setString(2, title);
			updateRecipe.executeUpdate();
		} catch (SQLIntegrityConstraintViolationException ignored) {

		}
	}

	/**
	 * Edits the duration of a recipe in the database.
	 *
	 * @param title    the title of the recipe.
	 * @param duration the new duration of the recipe.
	 * @throws SQLException if a database access error occurs.
	 */

	static void editDBRecipeDuration(String title, double duration) throws SQLException {

		try (Connection connection = dataSource.getConnection(); PreparedStatement updateRecipe = connection.prepareStatement("UPDATE recipe SET " + "duration=?" + " WHERE title=?")) {
			updateRecipe.setDouble(1, duration);
			updateRecipe.setString(2, title);
			updateRecipe.executeUpdate();
		} catch (SQLIntegrityConstraintViolationException ignored) {

		}
	}

	/**
	 * Edits the portion size of a recipe in the database.
	 *
	 * @param title    the title of the recipe.
	 * @param portions the new portion size of the recipe.
	 * @throws SQLException if a database access error occurs.
	 */

	static void editDBRecipePortion(String title, int portions) throws SQLException {

		try (Connection connection = dataSource.getConnection(); PreparedStatement updateRecipe = connection.prepareStatement("UPDATE recipe SET " + "portions=?" + " WHERE title=?")) {
			updateRecipe.setInt(1, portions);
			updateRecipe.setString(2, title);
			updateRecipe.executeUpdate();
		} catch (SQLIntegrityConstraintViolationException ignored) {

		}
	}

	/**
	 * Edits the steps of a recipe in the database.
	 *
	 * @param title the title of the recipe.
	 * @param steps the new steps of the recipe.
	 * @throws SQLException if a database access error occurs.
	 */

	static void editDBRecipeSteps(String title, String steps) throws SQLException {

		try (Connection connection = dataSource.getConnection(); PreparedStatement updateRecipe = connection.prepareStatement("UPDATE recipe SET " + "steps=?" + " WHERE title=?")) {
			updateRecipe.setString(1, steps);
			updateRecipe.setString(2, title);
			updateRecipe.executeUpdate();
		} catch (SQLIntegrityConstraintViolationException ignored) {

		}
	}

	/**
	 * Removes a recipe from the database.
	 *
	 * @param title the title of the recipe to be removed.
	 * @throws SQLException if a database access error occurs.
	 */

	static void removeDBRecipe(String title) throws SQLException {
		try (Connection connection = dataSource.getConnection(); PreparedStatement removeRecipe = connection.prepareStatement("DELETE FROM TAG " + " WHERE title=?")) {
			removeRecipe.setString(1, title);
			removeRecipe.executeUpdate();
		} catch (SQLIntegrityConstraintViolationException ignored) {

		}

		try (Connection connection = dataSource.getConnection(); PreparedStatement removeRecipe = connection.prepareStatement("DELETE FROM CONSIST " + " WHERE title=?")) {
			removeRecipe.setString(1, title);
			removeRecipe.executeUpdate();
		} catch (SQLIntegrityConstraintViolationException ignored) {

		}

		try (Connection connection = dataSource.getConnection(); PreparedStatement removeRecipe = connection.prepareStatement("DELETE FROM RECIPE " + " WHERE title=?")) {
			removeRecipe.setString(1, title);
			removeRecipe.executeUpdate();
		} catch (SQLIntegrityConstraintViolationException ignored) {

		}
	}

	/**
	 * Inserts a new recipe into the database.
	 *
	 * @param recipe the recipe object to be inserted.
	 * @throws SQLException if a database access error occurs.
	 */

	static void insertDBRecipe(Recipe recipe) throws SQLException {
		String title = recipe.getTitle();

		try (Connection connection = dataSource.getConnection(); PreparedStatement insertRecipe = connection.prepareStatement("INSERT INTO recipe (title, duration, unit, portions, category, steps) VALUES " + "(?,?,?,?,?,?)")) {
			insertRecipe.setString(1, title);
			insertRecipe.setDouble(2, recipe.getDuration());
			insertRecipe.setInt(3, switch (recipe.getUnit()) {
				case MIN -> 0;
				case H -> 1;
			});
			insertRecipe.setInt(4, recipe.getPortions());
			insertRecipe.setString(5, recipe.getCategory());
			insertRecipe.setString(6, recipe.getSteps());
			insertRecipe.executeUpdate();
		}

		for (Ingredient ingredient : recipe.getIngredientList()) {
			insertDBIngredient(title, ingredient);
		}

		for (String tag : recipe.getTagList()) {
			insertDBTag(title, tag);
		}
	}

	/**
	 * Inserts a new ingredient into the database for a specific recipe.
	 *
	 * @param title      the title of the recipe.
	 * @param ingredient the ingredient object to be inserted.
	 * @throws SQLException if a database access error occurs.
	 */

	static void insertDBIngredient(String title, Ingredient ingredient) throws SQLException {
		try (Connection connection = dataSource.getConnection(); PreparedStatement insertIngredient = connection.prepareStatement("INSERT INTO CONSIST (title, ingredient, quantity, unit_of_measurement) VALUES " + "(?,?,?,?)")) {
			insertIngredient.setString(1, title);
			insertIngredient.setString(2, ingredient.getIngredient());
			insertIngredient.setDouble(3, ingredient.getQuantity());
			insertIngredient.setString(4, ingredient.getUnit_of_measurement());
			insertIngredient.executeUpdate();
		}
	}

	/**
	 * Inserts a new tag into the database for a specific recipe.
	 *
	 * @param title the title of the recipe.
	 * @param tag   the tag to be inserted.
	 * @throws SQLException if a database access error occurs.
	 */

	static void insertDBTag(String title, String tag) throws SQLException {
		try (Connection connection = dataSource.getConnection(); PreparedStatement insertTag = connection.prepareStatement("INSERT INTO tag (title, tag) VALUES (?, ?)")) {
			insertTag.setString(1, title);
			insertTag.setString(2, tag);
			insertTag.executeUpdate();
		}
	}

	/**
	 * Updates an existing ingredient in the database for a specific recipe.
	 *
	 * @param title      the title of the recipe.
	 * @param ingredient the updated ingredient object.
	 * @throws SQLException if a database access error occurs.
	 */

	static void updateDBIngredient(String title, Ingredient ingredient) throws SQLException {
		try (Connection connection = dataSource.getConnection(); PreparedStatement insertIngredient = connection.prepareStatement("UPDATE CONSIST SET quantity=?, unit_of_measurement=? WHERE title=? AND ingredient=?")) {
			insertIngredient.setDouble(1, ingredient.getQuantity());
			insertIngredient.setString(2, ingredient.getUnit_of_measurement());
			insertIngredient.setString(3, title);
			insertIngredient.setString(4, ingredient.getIngredient());
			insertIngredient.executeUpdate();
		}
	}

	/**
	 * Removes an ingredient from the database for a specific recipe.
	 *
	 * @param title      the title of the recipe.
	 * @param ingredient the ingredient to be removed.
	 * @throws SQLException if a database access error occurs.
	 */
	static void removeDBIngredient(String title, Ingredient ingredient) throws SQLException {
		try (Connection connection = dataSource.getConnection(); PreparedStatement deleteIngredient = connection.prepareStatement("DELETE FROM CONSIST WHERE title=? AND ingredient=?")) {
			deleteIngredient.setString(1, title);
			deleteIngredient.setString(2, ingredient.getIngredient());
			deleteIngredient.executeUpdate();
		}
	}

	/**
	 * Retrieves all tags from the database.
	 *
	 * @return a set of tags.
	 * @throws SQLException if a database access error occurs.
	 */

	static Set<String> getAllTags() throws SQLException {
		Set<String> tags = new HashSet<>();

		try (Connection connection = dataSource.getConnection(); PreparedStatement getTags = connection.prepareStatement("SELECT tag FROM tag"); ResultSet rs = getTags.executeQuery()) {
			while (rs.next()) {
				tags.add(rs.getString("tag"));
			}
		}

		return tags;
	}
}
