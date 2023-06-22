package com.napolitanoveroni.expirationdate;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class Recipe {
    String title;
    int duration;
    durationUnit unit;
    int portions;
    String category;
    String steps;
    List<Ingredient> ingredientList;
    List<String> tagList;

    public durationUnit getUnit() {
        return unit;
    }

    public void setUnit(durationUnit unit) {
        this.unit = unit;
    }

    public Recipe(String title, int duration, durationUnit unit, int portions, String category, String steps,
                  List<Ingredient> ingredientList, List<String> tagList) {
        this.title = title;
        this.duration = duration;
        this.unit = unit;
        this.portions = portions;
        this.category = category;
        this.steps = steps;
        this.ingredientList = ingredientList;
        this.tagList = tagList;
    }

    public Recipe() {
        title = "";
        duration = 0;
        unit = durationUnit.MIN;
        portions = 0;
        category = "";
        steps = "";
        ingredientList = new ArrayList<>();
        tagList = new ArrayList<>();
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public int getDuration() {
        return duration;
    }

    public void setDuration(int duration) {
        this.duration = duration;
    }

    public int getPortions() {
        return portions;
    }

    public void setPortions(int portions) {
        this.portions = portions;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getSteps() {
        return steps;
    }

    public void setSteps(String steps) {
        this.steps = steps;
    }

    public List<Ingredient> getIngredientList() {
        return ingredientList;
    }

    public void setIngredientList(List<Ingredient> ingredientList) {
        this.ingredientList = ingredientList;
    }

    public List<String> getTagList() {
        return tagList;
    }

    public void setTagList(List<String> tagList) {
        this.tagList = tagList;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        Recipe recipe = (Recipe) o;
        return duration == recipe.duration && portions == recipe.portions && Objects.equals(title, recipe.title) && Objects.equals(category, recipe.category) && Objects.equals(steps, recipe.steps) && Objects.equals(ingredientList, recipe.ingredientList) && Objects.equals(tagList, recipe.tagList);
    }

    @Override
    public int hashCode() {
        return Objects.hash(title, duration, portions, category, steps, ingredientList, tagList);
    }
}

