package com.napolitanoveroni.expirationdate;

import java.time.LocalDate;
import java.util.Objects;

public class Product {
	String productName;
	LocalDate expirationDate;
	String categoryName;
	int quantity;
	double price;

	public Product() {
		setProductName("");
		setExpirationDate(LocalDate.now());
		setCategoryName("");
		setQuantity(1);
		setPrice(0);
	}

	public Product(String productName, LocalDate expirationDate, String categoryName, int quantity, double price) {
		setProductName(productName);
		setExpirationDate(expirationDate);
		setCategoryName(categoryName);
		setQuantity(quantity);
		setPrice(price);
	}

	public Product(String productName) {
		setProductName(productName);
		setExpirationDate(LocalDate.now());
		setCategoryName("");
		setQuantity(1);
		setPrice(0);
	}

	public Product(Product other) {
		setProductName(other.getProductName());
		setExpirationDate(other.getExpirationDate());
		setCategoryName(other.getCategoryName());
		setQuantity(other.getQuantity());
		setPrice(other.getPrice());
	}

	public String getProductName() {
		return productName;
	}

	public void setProductName(String productName) {
		this.productName = productName;
	}

	public LocalDate getExpirationDate() {
		return expirationDate;
	}

	public String getCategoryName() {
		return categoryName;
	}

	public void setCategoryName(String categoryName) {
		this.categoryName = categoryName;
	}

	public int getQuantity() {
		return quantity;
	}

	public double getPrice() {
		return price;
	}

	public void setPrice(double price) {
		this.price = price;
	}

	public void setQuantity(int quantity) {
		this.quantity = quantity;
	}

	public void setExpirationDate(LocalDate expirationDate) {
		this.expirationDate = expirationDate;
	}

	@Override
	public int hashCode() {
		return Objects.hash(productName, expirationDate, categoryName, quantity, price);
	}

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
