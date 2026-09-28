package com.example.smartpantry;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements PantryAdapter.OnItemClickListener {

    private RecyclerView rvPantry;
    private TextView tvEmptyPantry;

    private Button btnAddIngredient;
    private Button btnSuggestedRecipes;
    private Button btnAllRecipes;
    private Button btnAddRecipe;
    private Button btnAboutApp;

    private DatabaseHelper dbHelper;
    private PantryAdapter adapter;
    private List<PantryItem> pantryList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        dbHelper = new DatabaseHelper(this);

        rvPantry = findViewById(R.id.rvPantry);
        tvEmptyPantry = findViewById(R.id.tvEmptyPantry);

        btnAddIngredient = findViewById(R.id.btnAddIngredient);
        btnSuggestedRecipes = findViewById(R.id.btnSuggestedRecipes);
        btnAllRecipes = findViewById(R.id.btnAllRecipes);
        btnAddRecipe = findViewById(R.id.btnAddRecipe);
        btnAboutApp = findViewById(R.id.btnAboutApp);

        rvPantry.setLayoutManager(new LinearLayoutManager(this));
        adapter = new PantryAdapter(pantryList, this);
        rvPantry.setAdapter(adapter);

        if (btnAddIngredient != null) {
            btnAddIngredient.setOnClickListener(v -> {
                Intent intent = new Intent(MainActivity.this, AddEditIngredientActivity.class);
                startActivity(intent);
            });
        }

        if (btnSuggestedRecipes != null) {
            btnSuggestedRecipes.setOnClickListener(v -> {
                Intent intent = new Intent(MainActivity.this, SuggestedRecipesActivity.class);
                intent.putExtra("SHOW_ALL_RECIPES", false);
                startActivity(intent);
            });
        }

        if (btnAllRecipes != null) {
            btnAllRecipes.setOnClickListener(v -> {
                Intent intent = new Intent(MainActivity.this, SuggestedRecipesActivity.class);
                intent.putExtra("SHOW_ALL_RECIPES", true);
                startActivity(intent);
            });
        }

        if (btnAddRecipe != null) {
            btnAddRecipe.setOnClickListener(v -> {
                Intent intent = new Intent(MainActivity.this, AddRecipeActivity.class);
                startActivity(intent);
            });
        }

        if (btnAboutApp != null) {
            btnAboutApp.setOnClickListener(v -> showAboutDialog());
        }
    }
    private void showAboutDialog() {
        new AlertDialog.Builder(this)
                .setTitle("About Smart Pantry")
                .setMessage("Smart Pantry v1.0\n\nAn ingredient management system designed to track household pantry stock and suggest recipes based on available inventory.")
                .setPositiveButton("OK", (dialog, which) -> dialog.dismiss())
                .show();
    }
    @Override
    protected void onResume() {
        super.onResume();
        loadPantryItems();
    }
    private void loadPantryItems() {
        pantryList = dbHelper.getAllItems();
        if (pantryList.isEmpty()) {
            if (tvEmptyPantry != null) tvEmptyPantry.setVisibility(View.VISIBLE);
            if (rvPantry != null) rvPantry.setVisibility(View.GONE);
        } else {
            if (tvEmptyPantry != null) tvEmptyPantry.setVisibility(View.GONE);
            if (rvPantry != null) {
                rvPantry.setVisibility(View.VISIBLE);
                if (adapter != null) {
                    adapter.updateData(pantryList);
                }
            }
        }
    }
    @Override
    public void onItemClick(PantryItem item) {
        Intent intent = new Intent(MainActivity.this, AddEditIngredientActivity.class);
        intent.putExtra("ITEM_ID", item.getId());
        intent.putExtra("ITEM_NAME", item.getName());
        intent.putExtra("ITEM_QTY", item.getQuantity());
        intent.putExtra("ITEM_UNIT", item.getUnit());
        intent.putExtra("ITEM_EXPIRY", item.getExpiryDate());
        startActivity(intent);
    }
    @Override
    public void onDeleteClick(PantryItem item) {
        dbHelper.deleteItem(item.getId());
        Toast.makeText(this, item.getName() + " deleted", Toast.LENGTH_SHORT).show();
        loadPantryItems();
    }
}