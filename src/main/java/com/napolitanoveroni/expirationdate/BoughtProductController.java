package com.napolitanoveroni.expirationdate;

import javafx.fxml.FXML;
import javafx.scene.control.DatePicker;
import javafx.scene.control.TextField;

public class BoughtProductController {

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

    BoughtProduct product;

    @FXML
    public void initialize() {
        nameTextField.textProperty().addListener((observable, oldValue, newValue) -> product.setProductName(newValue));
        categoryTextField.textProperty().addListener((observable, oldValue, newValue) -> product.setCategoryName(newValue));
        priceTextField.textProperty().addListener((observable, oldValue, newValue) -> product.setPrice(Double.parseDouble(newValue)));
        // TODO check parse errors (WARNING, ignore when the text field is empty)
        quantityTextField.textProperty().addListener((observable, oldValue, newValue) -> product.setQuantity(Integer.parseInt(newValue)));
        // TODO check parse errors (WARNING, ignore when the text field is empty)
        expirationDatePicker.valueProperty().addListener((observable, oldValue, newValue) -> product.setExpirationDate(newValue));
    }

    void update() {
        nameTextField.textProperty().set(product.getProductName());
        categoryTextField.textProperty().set(product.getCategoryName());
        priceTextField.textProperty().set(Double.toString(product.getPrice()));
        quantityTextField.textProperty().set(Integer.toString(product.getQuantity()));
        expirationDatePicker.valueProperty().set(product.getExpirationDate());
    }

    public void setProduct(BoughtProduct product) {
        this.product = product;
        update();
    }

    public BoughtProduct getProduct() {
        return product;
    }
}
