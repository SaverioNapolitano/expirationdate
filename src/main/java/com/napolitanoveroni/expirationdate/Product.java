package com.napolitanoveroni.expirationdate;

import java.time.LocalDate;

public class Product extends Category{
    String productName;
    LocalDate estimatedExpirationDate;
    double estimatedPrice;

    public String getProductName() {
        return productName;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }

    public LocalDate getEstimatedExpirationDate() {
        return estimatedExpirationDate;
    }

    public void setEstimatedExpirationDate(LocalDate estimatedExpirationDate) {
        this.estimatedExpirationDate = estimatedExpirationDate;
    }

    public double getEstimatedPrice() {
        return estimatedPrice;
    }

    public void setEstimatedPrice(double estimatedPrice) {
        this.estimatedPrice = estimatedPrice;
    }

    public Product(String categoryName, String productName, LocalDate estimatedExpirationDate, double estimatedPrice) {
        super(categoryName);
        setProductName(productName);
        setEstimatedExpirationDate(estimatedExpirationDate);
        setEstimatedPrice(estimatedPrice);
    }
}
