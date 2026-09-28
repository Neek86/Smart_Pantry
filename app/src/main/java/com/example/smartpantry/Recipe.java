package com.example.smartpantry;

import java.util.List;

public class Recipe {
    private long id;
    private String name;
    private List<RecipeIngredient> ingredients;
    private String instructions;

    public Recipe(long id, String name, List<RecipeIngredient> ingredients, String instructions) {
        this.id = id;
        this.name = name;
        this.ingredients = ingredients;
        this.instructions = instructions;
    }
    public long getId() { return id; }
    public String getName() { return name; }
    public List<RecipeIngredient> getIngredients() { return ingredients; }
    public String getInstructions() { return instructions; }
}