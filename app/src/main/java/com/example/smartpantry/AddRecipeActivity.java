package com.example.smartpantry;

import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.List;

public class AddRecipeActivity extends AppCompatActivity {

    private EditText etRecipeName, etIngredients, etInstructions;
    private Button btnSaveRecipe;
    private DatabaseHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_recipe);

        dbHelper = new DatabaseHelper(this);

        etRecipeName = findViewById(R.id.etRecipeName);
        etIngredients = findViewById(R.id.etIngredients);
        etInstructions = findViewById(R.id.etInstructions);
        btnSaveRecipe = findViewById(R.id.btnSaveRecipe);

        btnSaveRecipe.setOnClickListener(v -> saveRecipe());
    }

    private void saveRecipe() {
        String name = etRecipeName.getText().toString().trim();
        String rawIngredients = etIngredients.getText().toString().trim();
        String instructions = etInstructions.getText().toString().trim();

        if (TextUtils.isEmpty(name)) {
            etRecipeName.setError("Recipe name required");
            return;
        }

        if (TextUtils.isEmpty(rawIngredients)) {
            etIngredients.setError("Ingredients required");
            return;
        }

        List<RecipeIngredient> ingredientList = new ArrayList<>();
        String[] lines = rawIngredients.split("\n");

        for (String line : lines) {
            String[] parts = line.split(",");
            if (parts.length >= 2) {
                String ingName = parts[0].trim();
                double qty = 1.0;
                try {
                    qty = Double.parseDouble(parts[1].trim());
                } catch (NumberFormatException ignored) {}

                String unit = parts.length > 2 ? parts[2].trim() : "";
                ingredientList.add(new RecipeIngredient(ingName, qty, unit));
            }
        }

        if (ingredientList.isEmpty()) {
            Toast.makeText(this, "Enter ingredients as: Name, Quantity, Unit", Toast.LENGTH_LONG).show();
            return;
        }

        Recipe newRecipe = new Recipe(System.currentTimeMillis(), name, ingredientList, instructions);
        long id = dbHelper.insertRecipe(newRecipe);

        if (id > 0) {
            Toast.makeText(this, "Recipe saved successfully!", Toast.LENGTH_SHORT).show();
            finish();
        } else {
            Toast.makeText(this, "Error saving recipe", Toast.LENGTH_SHORT).show();
        }
    }
}