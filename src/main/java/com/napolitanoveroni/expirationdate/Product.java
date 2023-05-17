package com.napolitanoveroni.expirationdate;

import java.time.LocalDate;

public class Product {
    String productName;
    LocalDate expirationDate;
    String categoryName;
    int quantity;
    double price;

    public String getProductName() {
        return productName;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }

    public String getCategoryName() {
        return categoryName;
    }

    public void setCategoryName(String categoryName) {
        this.categoryName = categoryName;
    }

    public Product() {
        setProductName("");
        setExpirationDate(LocalDate.now());
        setCategoryName("");
        setQuantity(1);
        setPrice(0);
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public LocalDate getExpirationDate() {
        return expirationDate;
    }

    public void setExpirationDate(LocalDate expirationDate) {
        this.expirationDate = expirationDate;
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
}
