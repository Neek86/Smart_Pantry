package com.example.smartpantry;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements PantryAdapter.OnItemClickListener {

    private RecyclerView rvPantry;
    private TextView tvEmptyPantry;
    private FloatingActionButton fabAdd;
    private Button btnSuggestedRecipes;

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
        fabAdd = findViewById(R.id.fabAdd);
        btnSuggestedRecipes = findViewById(R.id.btnSuggestedRecipes);

        rvPantry.setLayoutManager(new LinearLayoutManager(this));
        adapter = new PantryAdapter(pantryList, this);
        rvPantry.setAdapter(adapter);

        fabAdd.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, AddEditIngredientActivity.class);
            startActivity(intent);
        });

        btnSuggestedRecipes.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, SuggestedRecipesActivity.class);
            startActivity(intent);
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadPantryItems();
    }

    private void loadPantryItems() {
        pantryList = dbHelper.getAllPantryItems();
        if (pantryList.isEmpty()) {
            tvEmptyPantry.setVisibility(View.VISIBLE);
            rvPantry.setVisibility(View.GONE);
        } else {
            tvEmptyPantry.setVisibility(View.GONE);
            rvPantry.setVisibility(View.VISIBLE);
            adapter.updateData(pantryList);
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
        dbHelper.deletePantryItem(item.getId());
        Toast.makeText(this, item.getName() + " deleted", Toast.LENGTH_SHORT).show();
        loadPantryItems();
    }
}