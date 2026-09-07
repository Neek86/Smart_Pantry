package com.example.smartpantry;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class SuggestedRecipesActivity extends AppCompatActivity implements RecipeAdapter.OnRecipeClickListener {

    private RecyclerView rvSuggestedRecipes;
    private TextView tvEmptySuggestions;

    private DatabaseHelper dbHelper;
    private RecipeAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_suggested_recipes);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Suggested Recipes");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        dbHelper = new DatabaseHelper(this);

        rvSuggestedRecipes = findViewById(R.id.rvSuggestedRecipes);
        tvEmptySuggestions = findViewById(R.id.tvEmptySuggestions);

        rvSuggestedRecipes.setLayoutManager(new LinearLayoutManager(this));
        adapter = new RecipeAdapter(new ArrayList<>(), this);
        rvSuggestedRecipes.setAdapter(adapter);

        loadSuggestedRecipes();
    }

    private void loadSuggestedRecipes() {
        List<Recipe> allRecipes = dbHelper.getAllRecipes();
        List<PantryItem> pantry = dbHelper.getAllPantryItems();

        // Strict-matching algorithm call
        List<Recipe> matches = RecipeMatcher.getSuggestedRecipes(allRecipes, pantry);

        if (matches.isEmpty()) {
            tvEmptySuggestions.setVisibility(View.VISIBLE);
            rvSuggestedRecipes.setVisibility(View.GONE);
        } else {
            tvEmptySuggestions.setVisibility(View.GONE);
            rvSuggestedRecipes.setVisibility(View.VISIBLE);
            adapter.updateData(matches);
        }
    }

    @Override
    public void onRecipeClick(Recipe recipe) {
        Intent intent = new Intent(this, RecipeDetailActivity.class);
        intent.putExtra("RECIPE_NAME", recipe.getName());
        intent.putExtra("RECIPE_INSTRUCTIONS", recipe.getInstructions());

        StringBuilder ingBuilder = new StringBuilder();
        for (RecipeIngredient ing : recipe.getIngredients()) {
            ingBuilder.append("• ").append(ing.getRequiredQuantity()).append(" ")
                    .append(ing.getUnit()).append(" ").append(ing.getName()).append("\n");
        }
        intent.putExtra("RECIPE_INGREDIENTS", ingBuilder.toString());
        startActivity(intent);
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }
}