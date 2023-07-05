package com.napolitanoveroni.expirationdate;

import javafx.fxml.FXML;
import javafx.scene.control.DatePicker;
import javafx.scene.control.TextField;

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

	public Product getProduct() {
		return product;
	}

	public void setProduct(Product product) {
		this.product = product;
		update();
	}

	void update() {
		nameTextField.textProperty().set(product.getProductName());
		categoryTextField.textProperty().set(product.getCategoryName());
		priceTextField.textProperty().set(Double.toString(product.getPrice()));
		quantityTextField.textProperty().set(Integer.toString(product.getQuantity()));
		expirationDatePicker.valueProperty().set(product.getExpirationDate());
	}
}
