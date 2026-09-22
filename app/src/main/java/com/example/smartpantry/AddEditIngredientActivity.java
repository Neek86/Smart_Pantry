package com.example.smartpantry;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.util.Calendar;
import java.util.Locale;

public class AddEditIngredientActivity extends AppCompatActivity {

    private EditText etName, etQuantity, etUnit, etExpiry;
    private Button btnSave;
    private TextView tvTitle;

    private DatabaseHelper dbHelper;
    private long itemId = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_edit_ingredient);

        dbHelper = new DatabaseHelper(this);

        tvTitle = findViewById(R.id.tvTitle);
        // Matched to layout IDs: etIngredientName & etExpiryDate
        etName = findViewById(R.id.etIngredientName);
        etQuantity = findViewById(R.id.etQuantity);
        etUnit = findViewById(R.id.etUnit);
        etExpiry = findViewById(R.id.etExpiryDate);
        btnSave = findViewById(R.id.btnSave);

        // DatePickerDialog for Expiry Date selection
        etExpiry.setOnClickListener(v -> showDatePickerDialog());

        // Check if editing existing item
        if (getIntent().hasExtra("ITEM_ID")) {
            itemId = getIntent().getLongExtra("ITEM_ID", -1);
            tvTitle.setText("Edit Ingredient");
            etName.setText(getIntent().getStringExtra("ITEM_NAME"));
            etQuantity.setText(String.valueOf(getIntent().getDoubleExtra("ITEM_QTY", 0.0)));
            etUnit.setText(getIntent().getStringExtra("ITEM_UNIT"));
            etExpiry.setText(getIntent().getStringExtra("ITEM_EXPIRY"));
        }

        btnSave.setOnClickListener(v -> saveIngredient());
    }

    private void showDatePickerDialog() {
        Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog = new DatePickerDialog(
                this,
                (view, selectedYear, selectedMonth, selectedDay) -> {
                    String formattedDate = String.format(Locale.getDefault(), "%04d-%02d-%02d", selectedYear, selectedMonth + 1, selectedDay);
                    etExpiry.setText(formattedDate);
                },
                year, month, day
        );
        datePickerDialog.show();
    }

    private void saveIngredient() {
        String name = etName.getText().toString().trim();
        String qtyStr = etQuantity.getText().toString().trim();
        String unit = etUnit.getText().toString().trim();
        String expiry = etExpiry.getText().toString().trim();

        // Form Validation
        if (TextUtils.isEmpty(name)) {
            etName.setError("Name is required");
            return;
        }

        if (TextUtils.isEmpty(qtyStr)) {
            etQuantity.setError("Quantity is required");
            return;
        }

        double quantity;
        try {
            quantity = Double.parseDouble(qtyStr);
        } catch (NumberFormatException e) {
            etQuantity.setError("Invalid number");
            return;
        }

        if (TextUtils.isEmpty(unit)) {
            unit = "pcs";
        }

        if (itemId == -1) {
            // Create New Item
            PantryItem newItem = new PantryItem(name, quantity, unit, expiry);
            dbHelper.addPantryItem(newItem);
            Toast.makeText(this, "Item added to pantry", Toast.LENGTH_SHORT).show();
        } else {
            // Update Existing Item
            PantryItem updatedItem = new PantryItem(itemId, name, quantity, unit, expiry);
            dbHelper.updatePantryItem(updatedItem);
            Toast.makeText(this, "Item updated", Toast.LENGTH_SHORT).show();
        }

        finish();
    }
}