package com.napolitanoveroni.expirationdate;

import java.util.Objects;

public class Ingredient {
    String ingredient;
    double quantity;
    String unit_of_measurement;

    public Ingredient(String ingredient, double quantity, String unit_of_measurement) {
        this.ingredient = ingredient;
        this.quantity = quantity;
        this.unit_of_measurement = unit_of_measurement;
    }

    public Ingredient() {
        ingredient = "";
        quantity = 0;
        unit_of_measurement = "";
    }

    public String getIngredient() {
        return ingredient;
    }

    public void setIngredient(String ingredient) {
        this.ingredient = ingredient;
    }

    public double getQuantity() {
        return quantity;
    }

    public void setQuantity(double quantity) {
        this.quantity = quantity;
    }

    public String getUnit_of_measurement() {
        return unit_of_measurement;
    }

    public void setUnit_of_measurement(String unit_of_measurement) {
        this.unit_of_measurement = unit_of_measurement;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        Ingredient that = (Ingredient) o;
        return Double.compare(that.quantity, quantity) == 0 && Objects.equals(ingredient, that.ingredient) && Objects.equals(unit_of_measurement, that.unit_of_measurement);
    }

    @Override
    public int hashCode() {
        return Objects.hash(ingredient, quantity, unit_of_measurement);
    }
}
