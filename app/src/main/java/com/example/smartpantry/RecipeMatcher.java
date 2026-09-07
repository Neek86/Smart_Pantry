package com.example.smartpantry;

import java.util.ArrayList;
import java.util.List;

public class RecipeMatcher {

    /**
     * Returns a list of recipes that can be made with the items currently in the pantry.
     * A recipe is suggested if all its ingredients are present in the pantry with 
     * sufficient quantity.
     */
    public static List<Recipe> getSuggestedRecipes(List<Recipe> allRecipes, List<PantryItem> pantry) {
        List<Recipe> suggestions = new ArrayList<>();

        for (Recipe recipe : allRecipes) {
            boolean hasAllIngredients = true;
            for (RecipeIngredient required : recipe.getIngredients()) {
                if (!hasSufficientIngredient(required, pantry)) {
                    hasAllIngredients = false;
                    break;
                }
            }
            if (hasAllIngredients) {
                suggestions.add(recipe);
            }
        }

        return suggestions;
    }

    private static boolean hasSufficientIngredient(RecipeIngredient required, List<PantryItem> pantry) {
        double totalFound = 0;
        for (PantryItem item : pantry) {
            if (item.getName().equalsIgnoreCase(required.getName())) {
                // For simplicity, we assume units match or we ignore them for basic suggestion
                // Ideally, we would handle unit conversion
                totalFound += item.getQuantity();
            }
        }
        return totalFound >= required.getRequiredQuantity();
    }
}
