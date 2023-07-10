package com.napolitanoveroni.expirationdate;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * The Recipe class represents a recipe with various properties such as title, duration, portions, category, steps,
 * ingredient list, and tag list.
 *
 * @author SaverioNapolitano
 * @version 2023.07.10
 */

public class Recipe {
	String title;
	double duration;
	durationUnit unit;
	int portions;
	String category;
	String steps;
	List<Ingredient> ingredientList;
	List<String> tagList;

	/**
	 * Constructs a new Recipe object with the specified properties.
	 *
	 * @param title          the title of the recipe
	 * @param duration       the duration of the recipe
	 * @param unit           the unit of the duration (e.g., minutes, hours)
	 * @param portions       the number of portions the recipe yields
	 * @param category       the category of the recipe
	 * @param steps          the steps to prepare the recipe
	 * @param ingredientList the list of ingredients required for the recipe
	 * @param tagList        the list of tags associated with the recipe
	 */

	public Recipe(String title, double duration, durationUnit unit, int portions, String category, String steps, List<Ingredient> ingredientList, List<String> tagList) {
		this.title = title;
		this.duration = duration;
		this.unit = unit;
		this.portions = portions;
		this.category = category;
		this.steps = steps;
		this.ingredientList = ingredientList;
		this.tagList = tagList;
	}

	/**
	 * Constructs a new Recipe object with default values for all properties.
	 */

	public Recipe() {
		title = "";
		duration = 0;
		unit = durationUnit.MIN;
		portions = 0;
		category = "first course";
		steps = "";
		ingredientList = new ArrayList<>();
		tagList = new ArrayList<>();
	}

	/**
	 * Constructs a new Recipe object by copying the properties from another Recipe object.
	 *
	 * @param recipe the recipe to copy
	 */

	public Recipe(Recipe recipe) {
		this.title = recipe.getTitle();
		this.duration = recipe.getDuration();
		this.unit = recipe.getUnit();
		this.portions = recipe.getPortions();
		this.category = recipe.getCategory();
		this.steps = recipe.getSteps();
		this.ingredientList = new ArrayList<>(recipe.getIngredientList());
		this.tagList = new ArrayList<>(recipe.getTagList());
	}

	/**
	 * Returns the title of the recipe.
	 *
	 * @return the title of the recipe
	 */
	public String getTitle() {
		return title;
	}

	/**
	 * Sets the title of the recipe.
	 *
	 * @param title the title of the recipe
	 */

	public void setTitle(String title) {
		this.title = title;
	}

	/**
	 * Returns the duration of the recipe.
	 *
	 * @return the duration of the recipe
	 */

	public double getDuration() {
		return duration;
	}

	/**
	 * Returns the unit of the duration.
	 *
	 * @return the unit of the duration
	 */

	public durationUnit getUnit() {
		return unit;
	}

	/**
	 * Sets the unit of the duration.
	 *
	 * @param unit the unit of the duration
	 */

	public void setUnit(durationUnit unit) {
		this.unit = unit;
	}

	/**
	 * Returns the number of portions the recipe yields.
	 *
	 * @return the number of portions
	 */

	public int getPortions() {
		return portions;
	}

	/**
	 * Sets the number of portions the recipe yields.
	 *
	 * @param portions the number of portions
	 */

	public void setPortions(int portions) {
		this.portions = portions;
	}

	/**
	 * Returns the category of the recipe.
	 *
	 * @return the category of the recipe
	 */

	public String getCategory() {
		return category;
	}

	/**
	 * Sets the category of the recipe.
	 *
	 * @param category the category of the recipe
	 */

	public void setCategory(String category) {
		this.category = category;
	}

	/**
	 * Returns the steps to prepare the recipe.
	 *
	 * @return the steps to prepare the recipe
	 */

	public String getSteps() {
		return steps;
	}

	/**
	 * Sets the steps to prepare the recipe.
	 *
	 * @param steps the steps to prepare the recipe
	 */

	public void setSteps(String steps) {
		this.steps = steps;
	}

	/**
	 * Returns the list of ingredients required for the recipe.
	 *
	 * @return the list of ingredients
	 */

	public List<Ingredient> getIngredientList() {
		return ingredientList;
	}

	/**
	 * Sets the list of ingredients required for the recipe.
	 *
	 * @param ingredientList the list of ingredients
	 */

	public void setIngredientList(List<Ingredient> ingredientList) {
		this.ingredientList = ingredientList;
	}

	/**
	 * Returns the list of tags associated with the recipe.
	 *
	 * @return the list of tags
	 */

	public List<String> getTagList() {
		return tagList;
	}

	/**
	 * Sets the list of tags associated with the recipe.
	 *
	 * @param tagList the list of tags
	 */

	public void setTagList(List<String> tagList) {
		this.tagList = tagList;
	}

	/**
	 * Sets the duration of the recipe.
	 *
	 * @param duration the duration of the recipe
	 */

	public void setDuration(double duration) {
		this.duration = duration;
	}

	/**
	 * Returns the hash code of the recipe.
	 *
	 * @return the hash code
	 */

	@Override
	public int hashCode() {
		return Objects.hash(title, duration, portions, category, steps, ingredientList, tagList);
	}

	/**
	 * Checks if this recipe is equal to another object.
	 *
	 * @param o the object to compare
	 *
	 * @return true if the recipes are equal, false otherwise
	 */
	@Override
	public boolean equals(Object o) {
		if (this == o)
			return true;
		if (o == null || getClass() != o.getClass())
			return false;
		Recipe recipe = (Recipe) o;
		return duration == recipe.duration && portions == recipe.portions && Objects.equals(title, recipe.title) && Objects.equals(category, recipe.category) && Objects.equals(steps, recipe.steps) && Objects.equals(ingredientList, recipe.ingredientList) && Objects.equals(tagList, recipe.tagList);
	}
}