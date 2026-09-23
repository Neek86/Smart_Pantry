package com.example.smartpantry;

import java.util.ArrayList;
import java.util.List;

public class RecipeMatcher {

    public static List<Recipe> getSuggestedRecipes(List<Recipe> allRecipes, List<PantryItem> pantry) {
        List<Recipe> suggestions = new ArrayList<>();

        if (allRecipes == null || pantry == null || pantry.isEmpty()) {
            return suggestions;
        }
        for (Recipe recipe : allRecipes) {
            boolean hasAllIngredients = true;

            for (RecipeIngredient required : recipe.getIngredients()) {
                if (!hasSufficientQuantity(required, pantry)) {
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
    private static boolean hasSufficientQuantity(RecipeIngredient required, List<PantryItem> pantry) {
        double totalQuantityFound = 0;
        String reqName = cleanString(required.getName());

        for (PantryItem item : pantry) {
            String itemName = cleanString(item.getName());

            if (isIngredientMatch(reqName, itemName)) {
                totalQuantityFound += item.getQuantity();
            }
        }
        return totalQuantityFound >= required.getRequiredQuantity();
    }

    private static boolean isIngredientMatch(String required, String item) {
        if (required.equals(item) || required.contains(item) || item.contains(required)) {
            return true;
        }
        if (required.startsWith(item) || item.startsWith(required)) {
            return true;
        }
        return false;
    }

    private static String cleanString(String input) {
        if (input == null) return "";
        return input.trim().toLowerCase();
    }
}