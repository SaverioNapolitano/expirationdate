package com.napolitanoveroni.expirationdate;

import java.time.LocalDate;

public class BoughtProduct extends Product {
    double price;
    int quantity;
    LocalDate expirationDate;

    public BoughtProduct() {
        super("", "", LocalDate.now(), 0);
        price = 0;
        quantity = 0;
        expirationDate = LocalDate.now();
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

    public BoughtProduct(String categoryName, String productName, LocalDate estimatedExpirationDate,
                         double estimatedPrice, double price, int quantity, LocalDate expirationDate) {
        super(categoryName, productName, estimatedExpirationDate, estimatedPrice);
        setPrice(price);
        setQuantity(quantity);
        setExpirationDate(expirationDate);
    }

    public BoughtProduct(BoughtProduct other) {
        super(other.getCategoryName(), other.getProductName(), other.getEstimatedExpirationDate(),
                other.getEstimatedPrice());
        setPrice(other.getPrice());
        setQuantity(other.getQuantity());
        setExpirationDate(other.getExpirationDate());
    }
}
