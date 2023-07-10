/**
 * The Ingredient class represents an ingredient with its name, quantity, and unit of measurement.
 * <p>An ingredient object can be created with a specified name, quantity, and unit of measurement.
 * The class provides getters and setters for accessing and modifying the ingredient properties.</p>
 * <p>The Ingredient class also overrides the hashCode and equals methods to enable proper comparison
 * and hashing of Ingredient objects based on their name, quantity, and unit of measurement.</p>
 * <p>Note: This class assumes the usage of the java.util.Objects class.</p>
 */

package com.napolitanoveroni.expirationdate;

import java.util.Objects;

/**
 * The Ingredient class represents an ingredient with its name, quantity, and unit of measurement.
 *
 * @author SaverioNapolitano, MatteV02
 * @version 2023.07.10
 */
public class Ingredient {
	String ingredient;
	double quantity;
	String unit_of_measurement;

	/**
	 * Constructs an Ingredient object with the specified name, quantity, and unit of measurement.
	 *
	 * @param ingredient          the name of the ingredient
	 * @param quantity            the quantity of the ingredient
	 * @param unit_of_measurement the unit of measurement for the quantity
	 */
	public Ingredient(String ingredient, double quantity, String unit_of_measurement) {
		this.ingredient = ingredient;
		this.quantity = quantity;
		this.unit_of_measurement = unit_of_measurement;
	}

	/**
	 * Constructs an empty Ingredient object with default values.
	 */
	public Ingredient() {
		ingredient = "";
		quantity = 0;
		unit_of_measurement = "";
	}

	/**
	 * Retrieves the name of the ingredient.
	 *
	 * @return the name of the ingredient
	 */
	public String getIngredient() {
		return ingredient;
	}

	/**
	 * Sets the name of the ingredient.
	 *
	 * @param ingredient the name of the ingredient
	 */
	public void setIngredient(String ingredient) {
		this.ingredient = ingredient;
	}

	/**
	 * Retrieves the quantity of the ingredient.
	 *
	 * @return the quantity of the ingredient
	 */
	public double getQuantity() {
		return quantity;
	}

	/**
	 * Sets the quantity of the ingredient.
	 *
	 * @param quantity the quantity of the ingredient
	 */
	public void setQuantity(double quantity) {
		this.quantity = quantity;
	}

	/**
	 * Retrieves the unit of measurement for the ingredient quantity.
	 *
	 * @return the unit of measurement for the ingredient quantity
	 */
	public String getUnit_of_measurement() {
		return unit_of_measurement;
	}

	/**
	 * Sets the unit of measurement for the ingredient quantity.
	 *
	 * @param unit_of_measurement the unit of measurement for the ingredient quantity
	 */
	public void setUnit_of_measurement(String unit_of_measurement) {
		this.unit_of_measurement = unit_of_measurement;
	}

	/**
	 * Generates the hash code for the Ingredient object based on its name, quantity, and unit of measurement.
	 *
	 * @return the hash code for the Ingredient object
	 */
	@Override
	public int hashCode() {
		return Objects.hash(ingredient, quantity, unit_of_measurement);
	}

	/**
	 * Compares the Ingredient object with another object for equality.
	 * Two Ingredient objects are considered equal if they have the same name, quantity, and unit of measurement.
	 *
	 * @param o the object to compare
	 *
	 * @return true if the objects are equal, false otherwise
	 */
	@Override
	public boolean equals(Object o) {
		if (this == o)
			return true;
		if (o == null || getClass() != o.getClass())
			return false;
		Ingredient that = (Ingredient) o;
		return Double.compare(that.quantity, quantity) == 0 && Objects.equals(ingredient, that.ingredient) && Objects.equals(unit_of_measurement, that.unit_of_measurement);
	}
}
