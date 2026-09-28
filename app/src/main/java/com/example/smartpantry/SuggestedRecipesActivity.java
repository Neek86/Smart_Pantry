package com.example.smartpantry;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.ImageButton;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.smartpantry.R; // EXPLICIT IMPORT TO RESOLVE SYMBOL

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

        if (rvSuggestedRecipes != null) {
            rvSuggestedRecipes.setLayoutManager(new LinearLayoutManager(this));
            adapter = new RecipeAdapter(new ArrayList<>(), this);
            rvSuggestedRecipes.setAdapter(adapter);
        }
        ImageButton btnHome = findViewById(R.id.btnHome);
        if (btnHome != null) {
            btnHome.setOnClickListener(v -> {
                Intent intent = new Intent(SuggestedRecipesActivity.this, MainActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                startActivity(intent);
                finish();
            });
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadSuggestedRecipes();
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
        loadSuggestedRecipes();
    }

    private void loadSuggestedRecipes() {
        boolean showAll = getIntent().getBooleanExtra("SHOW_ALL_RECIPES", false);
        List<Recipe> recipesToDisplay;

        if (showAll) {
            if (getSupportActionBar() != null) {
                getSupportActionBar().setTitle("All Predefined Recipes");
            }
            // Loads all 20 recipes directly without stock checking
            recipesToDisplay = dbHelper.getAllRecipes();
        } else {
            if (getSupportActionBar() != null) {
                getSupportActionBar().setTitle("Suggested Recipes");
            }
            // Matchs recipes against pantry inventory
            List<Recipe> allRecipes = dbHelper.getAllRecipes();
            List<PantryItem> pantry = dbHelper.getAllItems();
            recipesToDisplay = RecipeMatcher.getSuggestedRecipes(allRecipes, pantry);
        }

        if (recipesToDisplay == null || recipesToDisplay.isEmpty()) {
            if (tvEmptySuggestions != null) tvEmptySuggestions.setVisibility(View.VISIBLE);
            if (rvSuggestedRecipes != null) rvSuggestedRecipes.setVisibility(View.GONE);
        } else {
            if (tvEmptySuggestions != null) tvEmptySuggestions.setVisibility(View.GONE);
            if (rvSuggestedRecipes != null) {
                rvSuggestedRecipes.setVisibility(View.VISIBLE);
                if (adapter != null) {
                    adapter.updateData(recipesToDisplay);
                }
            }
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