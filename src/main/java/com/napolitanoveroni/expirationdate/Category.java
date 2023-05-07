package com.napolitanoveroni.expirationdate;

import javafx.beans.property.StringProperty;

public class Category {
    String categoryName;

    public String getCategoryName() {
        return categoryName;
    }

    public void setCategoryName(String categoryName) {
        this.categoryName = categoryName;
    }

    public Category(String categoryName) {
        setCategoryName(categoryName);
    }
}
