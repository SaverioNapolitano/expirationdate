/**
 * The EditProductController class is responsible for handling user interactions and updating the product information in the UI.
 * <p>This controller is associated with the edit_product.fxml file, which defines the layout of the edit product view.</p>
 * <p>The controller binds the UI elements to the corresponding fields of the Product object and provides methods for initializing,
 * updating, and retrieving the product information.</p>
 * <p>Note: This class assumes the existence of the Product class.</p>
 */

package com.napolitanoveroni.expirationdate;

import javafx.fxml.FXML;
import javafx.scene.control.DatePicker;
import javafx.scene.control.TextField;

/**
 * The EditProductController class is responsible for handling user interactions and updating the product information in the UI.
 *
 * @author SaverioNapolitano, MatteV02
 * @version 2023.07.10
 */
public class EditProductController {

	Product product;
	@FXML
	private TextField categoryTextField;
	@FXML
	private DatePicker expirationDatePicker;
	@FXML
	private TextField nameTextField;
	@FXML
	private TextField priceTextField;
	@FXML
	private TextField quantityTextField;

	/**
	 * Initializes the controller and sets up event listeners for the UI elements.
	 * This method is automatically called by the JavaFX framework after loading the associated FXML file.
	 */
	@FXML
	public void initialize() {
		nameTextField.textProperty().addListener((observable, oldValue, newValue) -> product.setProductName(newValue));
		categoryTextField.textProperty().addListener((observable, oldValue, newValue) -> product.setCategoryName(newValue));
		priceTextField.textProperty().addListener((observable, oldValue, newValue) -> {
			try {
				product.setPrice(Double.parseDouble(newValue));
			} catch (NumberFormatException exception) {
				product.setPrice(0);
			}
		});
		quantityTextField.textProperty().addListener((observable, oldValue, newValue) -> {
			try {
				product.setQuantity(Integer.parseInt(newValue));
			} catch (NumberFormatException exception) {
				product.setQuantity(1);
			}
		});
		expirationDatePicker.valueProperty().addListener((observable, oldValue, newValue) -> product.setExpirationDate(newValue));
	}

	/**
	 * Retrieves the current product.
	 *
	 * @return the current product
	 */
	public Product getProduct() {
		return product;
	}

	/**
	 * Sets the product to be edited and updates the UI with its information.
	 *
	 * @param product the product to be edited
	 */
	public void setProduct(Product product) {
		this.product = product;
		update();
	}

	/**
	 * Updates the UI with the current product information.
	 * This method is called when setting a new product or after modifying the product's properties.
	 */
	void update() {
		nameTextField.textProperty().set(product.getProductName());
		categoryTextField.textProperty().set(product.getCategoryName());
		priceTextField.textProperty().set(Double.toString(product.getPrice()));
		quantityTextField.textProperty().set(Integer.toString(product.getQuantity()));
		expirationDatePicker.valueProperty().set(product.getExpirationDate());
	}
}
