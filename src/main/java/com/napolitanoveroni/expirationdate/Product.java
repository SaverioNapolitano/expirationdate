package com.napolitanoveroni.expirationdate;

import java.time.LocalDate;
import java.util.Objects;

/**
 * Represents a product with its attributes such as name, expiration date, category, quantity, and price.
 *
 * @author SaverioNapolitano, MatteV02
 * @version 2023.07.10
 */
public class Product {
	String productName;
	LocalDate expirationDate;
	String categoryName;
	int quantity;
	double price;

	/**
	 * Default constructor.
	 * Initializes the product with default attribute values.
	 */
	public Product() {
		setProductName("");
		setExpirationDate(LocalDate.now());
		setCategoryName("");
		setQuantity(1);
		setPrice(0);
	}

	/**
	 * Parameterized constructor.
	 * Initializes the product with the specified attribute values.
	 *
	 * @param productName    The name of the product.
	 * @param expirationDate The expiration date of the product.
	 * @param categoryName   The category name of the product.
	 * @param quantity       The quantity of the product.
	 * @param price          The price of the product.
	 */
	public Product(String productName, LocalDate expirationDate, String categoryName, int quantity, double price) {
		setProductName(productName);
		setExpirationDate(expirationDate);
		setCategoryName(categoryName);
		setQuantity(quantity);
		setPrice(price);
	}

	/**
	 * Constructor with only the product name.
	 * Initializes the product with the specified name and default attribute values.
	 *
	 * @param productName The name of the product.
	 */
	public Product(String productName) {
		setProductName(productName);
		setExpirationDate(LocalDate.now());
		setCategoryName("");
		setQuantity(1);
		setPrice(0);
	}

	/**
	 * Copy constructor.
	 * Creates a new product instance by copying the attributes from another product.
	 *
	 * @param other The product to copy the attributes from.
	 */
	public Product(Product other) {
		setProductName(other.getProductName());
		setExpirationDate(other.getExpirationDate());
		setCategoryName(other.getCategoryName());
		setQuantity(other.getQuantity());
		setPrice(other.getPrice());
	}

	/**
	 * Gets the name of the product.
	 *
	 * @return The name of the product.
	 */
	public String getProductName() {
		return productName;
	}

	/**
	 * Sets the name of the product.
	 *
	 * @param productName The name of the product.
	 */
	public void setProductName(String productName) {
		this.productName = productName;
	}

	/**
	 * Gets the expiration date of the product.
	 *
	 * @return The expiration date of the product.
	 */
	public LocalDate getExpirationDate() {
		return expirationDate;
	}

	/**
	 * Gets the category name of the product.
	 *
	 * @return The category name of the product.
	 */
	public String getCategoryName() {
		return categoryName;
	}

	/**
	 * Sets the category name of the product.
	 *
	 * @param categoryName The category name of the product.
	 */
	public void setCategoryName(String categoryName) {
		this.categoryName = categoryName;
	}

	/**
	 * Gets the quantity of the product.
	 *
	 * @return The quantity of the product.
	 */
	public int getQuantity() {
		return quantity;
	}

	/**
	 * Gets the price of the product.
	 *
	 * @return The price of the product.
	 */
	public double getPrice() {
		return price;
	}

	/**
	 * Sets the price of the product.
	 *
	 * @param price The price of the product.
	 */
	public void setPrice(double price) {
		this.price = price;
	}

	/**
	 * Sets the quantity of the product.
	 *
	 * @param quantity The quantity of the product.
	 */
	public void setQuantity(int quantity) {
		this.quantity = quantity;
	}

	/**
	 * Sets the expiration date of the product.
	 *
	 * @param expirationDate The expiration date of the product.
	 */
	public void setExpirationDate(LocalDate expirationDate) {
		this.expirationDate = expirationDate;
	}

	/**
	 * Generates the hash code for the product based on its attributes.
	 *
	 * @return The hash code of the product.
	 */
	@Override
	public int hashCode() {
		return Objects.hash(productName, expirationDate, categoryName, quantity, price);
	}

	/**
	 * Checks if the product is equal to another object.
	 * Two products are considered equal if their attributes are equal.
	 *
	 * @param o The object to compare.
	 *
	 * @return {@code true} if the product is equal to the object, {@code false} otherwise.
	 */
	@Override
	public boolean equals(Object o) {
		if (this == o)
			return true;
		if (o == null || getClass() != o.getClass())
			return false;
		Product product = (Product) o;
		return quantity == product.quantity && Double.compare(product.price, price) == 0 && Objects.equals(productName, product.productName) && Objects.equals(expirationDate, product.expirationDate) && Objects.equals(categoryName, product.categoryName);
	}
}
