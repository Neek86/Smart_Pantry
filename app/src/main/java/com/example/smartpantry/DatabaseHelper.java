package com.example.smartpantrymanager;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;
import java.util.List;

public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "smart_pantry.db";
    private static final int DATABASE_VERSION = 1;

    // Pantry Table
    private static final String TABLE_PANTRY = "pantry";
    private static final String COL_PANTRY_ID = "id";
    private static final String COL_PANTRY_NAME = "name";
    private static final String COL_PANTRY_QTY = "quantity";
    private static final String COL_PANTRY_UNIT = "unit";
    private static final String COL_PANTRY_EXPIRY = "expiry_date";

    // Recipes Table
    private static final String TABLE_RECIPES = "recipes";
    private static final String COL_RECIPE_ID = "id";
    private static final String COL_RECIPE_NAME = "name";
    private static final String COL_RECIPE_INSTRUCTIONS = "instructions";

    // Recipe Ingredients Table
    private static final String TABLE_RECIPE_INGREDIENTS = "recipe_ingredients";
    private static final String COL_RI_ID = "id";
    private static final String COL_RI_RECIPE_ID = "recipe_id";
    private static final String COL_RI_NAME = "ingredient_name";
    private static final String COL_RI_QTY = "required_qty";
    private static final String COL_RI_UNIT = "unit";

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String createPantry = "CREATE TABLE " + TABLE_PANTRY + " (" +
                COL_PANTRY_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COL_PANTRY_NAME + " TEXT, " +
                COL_PANTRY_QTY + " REAL, " +
                COL_PANTRY_UNIT + " TEXT, " +
                COL_PANTRY_EXPIRY + " TEXT);";

        String createRecipes = "CREATE TABLE " + TABLE_RECIPES + " (" +
                COL_RECIPE_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COL_RECIPE_NAME + " TEXT, " +
                COL_RECIPE_INSTRUCTIONS + " TEXT);";

        String createRecipeIngredients = "CREATE TABLE " + TABLE_RECIPE_INGREDIENTS + " (" +
                COL_RI_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COL_RI_RECIPE_ID + " INTEGER, " +
                COL_RI_NAME + " TEXT, " +
                COL_RI_QTY + " REAL, " +
                COL_RI_UNIT + " TEXT, " +
                "FOREIGN KEY(" + COL_RI_RECIPE_ID + ") REFERENCES " + TABLE_RECIPES + "(" + COL_RECIPE_ID + "));";

        db.execSQL(createPantry);
        db.execSQL(createRecipes);
        db.execSQL(createRecipeIngredients);

        seedRecipes(db);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_PANTRY);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_RECIPE_INGREDIENTS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_RECIPES);
        onCreate(db);
    }

    // --- PANTRY CRUD OPERATIONS ---

    public long addPantryItem(PantryItem item) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COL_PANTRY_NAME, item.getName());
        values.put(COL_PANTRY_QTY, item.getQuantity());
        values.put(COL_PANTRY_UNIT, item.getUnit());
        values.put(COL_PANTRY_EXPIRY, item.getExpiryDate());
        return db.insert(TABLE_PANTRY, null, values);
    }

    public List<PantryItem> getAllPantryItems() {
        List<PantryItem> items = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM " + TABLE_PANTRY, null);

        if (cursor.moveToFirst()) {
            do {
                long id = cursor.getLong(cursor.getColumnIndexOrThrow(COL_PANTRY_ID));
                String name = cursor.getString(cursor.getColumnIndexOrThrow(COL_PANTRY_NAME));
                double qty = cursor.getDouble(cursor.getColumnIndexOrThrow(COL_PANTRY_QTY));
                String unit = cursor.getString(cursor.getColumnIndexOrThrow(COL_PANTRY_UNIT));
                String expiry = cursor.getString(cursor.getColumnIndexOrThrow(COL_PANTRY_EXPIRY));
                items.add(new PantryItem(id, name, qty, unit, expiry));
            } while (cursor.moveToNext());
        }
        cursor.close();
        return items;
    }

    public int updatePantryItem(PantryItem item) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COL_PANTRY_NAME, item.getName());
        values.put(COL_PANTRY_QTY, item.getQuantity());
        values.put(COL_PANTRY_UNIT, item.getUnit());
        values.put(COL_PANTRY_EXPIRY, item.getExpiryDate());

        return db.update(TABLE_PANTRY, values, COL_PANTRY_ID + " = ?",
                new String[]{String.valueOf(item.getId())});
    }

    public void deletePantryItem(long id) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_PANTRY, COL_PANTRY_ID + " = ?", new String[]{String.valueOf(id)});
    }

    // --- RECIPE READ OPERATIONS ---

    public List<Recipe> getAllRecipes() {
        List<Recipe> recipes = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM " + TABLE_RECIPES, null);

        if (cursor.moveToFirst()) {
            do {
                long recipeId = cursor.getLong(cursor.getColumnIndexOrThrow(COL_RECIPE_ID));
                String name = cursor.getString(cursor.getColumnIndexOrThrow(COL_RECIPE_NAME));
                String instructions = cursor.getString(cursor.getColumnIndexOrThrow(COL_RECIPE_INSTRUCTIONS));

                List<RecipeIngredient> ingredients = getIngredientsForRecipe(recipeId, db);
                recipes.add(new Recipe(recipeId, name, ingredients, instructions));
            } while (cursor.moveToNext());
        }
        cursor.close();
        return recipes;
    }

    private List<RecipeIngredient> getIngredientsForRecipe(long recipeId, SQLiteDatabase db) {
        List<RecipeIngredient> ingredients = new ArrayList<>();
        Cursor cursor = db.rawQuery("SELECT * FROM " + TABLE_RECIPE_INGREDIENTS + " WHERE " + COL_RI_RECIPE_ID + " = ?",
                new String[]{String.valueOf(recipeId)});

        if (cursor.moveToFirst()) {
            do {
                String name = cursor.getString(cursor.getColumnIndexOrThrow(COL_RI_NAME));
                double qty = cursor.getDouble(cursor.getColumnIndexOrThrow(COL_RI_QTY));
                String unit = cursor.getString(cursor.getColumnIndexOrThrow(COL_RI_UNIT));
                ingredients.add(new RecipeIngredient(name, qty, unit));
            } while (cursor.moveToNext());
        }
        cursor.close();
        return ingredients;
    }

    // --- SEED DATABASE WITH 15 RECIPES ---

    private void seedRecipes(SQLiteDatabase db) {
        addRecipeWithIngredients(db, "Scrambled Eggs", "Beat eggs with milk. Melt butter in pan, add eggs and cook until set.",
                new String[]{"egg", "milk", "butter"}, new double[]{2, 50, 10}, new String[]{"pcs", "ml", "g"});

        addRecipeWithIngredients(db, "Omelette", "Whisk eggs, pour into hot buttered skillet, add cheese and fold.",
                new String[]{"egg", "cheese", "butter"}, new double[]{3, 50, 15}, new String[]{"pcs", "g", "g"});

        addRecipeWithIngredients(db, "Grilled Cheese Sandwich", "Butter bread slices, place cheese inside, grill on pan until golden.",
                new String[]{"bread", "cheese", "butter"}, new double[]{2, 2, 10}, new String[]{"slices", "slices", "g"});

        addRecipeWithIngredients(db, "Pancakes", "Mix flour, milk, egg, and sugar. Pour batter on pan and flip when bubbly.",
                new String[]{"flour", "milk", "egg", "sugar"}, new double[]{150, 200, 1, 15}, new String[]{"g", "ml", "pcs", "g"});

        addRecipeWithIngredients(db, "French Toast", "Dip bread in mixture of egg, milk, and sugar. Fry until brown.",
                new String[]{"bread", "egg", "milk", "sugar"}, new double[]{2, 1, 50, 10}, new String[]{"slices", "pcs", "ml", "g"});

        addRecipeWithIngredients(db, "Garlic Rice", "Sauté garlic in oil, add cooked rice and stir thoroughly.",
                new String[]{"rice", "garlic", "oil"}, new double[]{200, 2, 15}, new String[]{"g", "cloves", "ml"});

        addRecipeWithIngredients(db, "Egg Fried Rice", "Sauté garlic, add cooked rice, push aside and scramble egg, mix together.",
                new String[]{"rice", "egg", "garlic", "oil"}, new double[]{250, 2, 2, 15}, new String[]{"g", "pcs", "cloves", "ml"});

        addRecipeWithIngredients(db, "Tomato Pasta", "Boil pasta. Sauté garlic, add tomato, mix with cooked pasta.",
                new String[]{"pasta", "tomato", "garlic", "oil"}, new double[]{200, 2, 2, 15}, new String[]{"g", "pcs", "cloves", "ml"});

        addRecipeWithIngredients(db, "Cheesy Pasta", "Boil pasta. Melt butter, mix with cheese and combine with pasta.",
                new String[]{"pasta", "cheese", "butter"}, new double[]{200, 100, 20}, new String[]{"g", "g", "g"});

        addRecipeWithIngredients(db, "Mashed Potatoes", "Boil potatoes until soft. Mash with butter and milk.",
                new String[]{"potato", "butter", "milk"}, new double[]{3, 30, 50}, new String[]{"pcs", "g", "ml"});

        addRecipeWithIngredients(db, "Tomato Salad", "Slice tomatoes and onion. Toss with oil and salt.",
                new String[]{"tomato", "onion", "oil"}, new double[]{3, 1, 15}, new String[]{"pcs", "pcs", "ml"});

        addRecipeWithIngredients(db, "Boiled Eggs with Bread", "Boil eggs for 8 minutes. Serve sliced with buttered bread.",
                new String[]{"egg", "bread", "butter"}, new double[]{2, 2, 10}, new String[]{"pcs", "slices", "g"});

        addRecipeWithIngredients(db, "Sautéed Vegetables", "Chop onion, tomato, and potato. Sauté in oil until tender.",
                new String[]{"onion", "tomato", "potato", "oil"}, new double[]{1, 2, 2, 20}, new String[]{"pcs", "pcs", "pcs", "ml"});

        addRecipeWithIngredients(db, "Garlic Butter Toast", "Mince garlic, mix with butter, spread on bread, and toast.",
                new String[]{"bread", "butter", "garlic"}, new double[]{2, 15, 2}, new String[]{"slices", "g", "cloves"});

        addRecipeWithIngredients(db, "Simple Rice Porridge", "Boil rice in excess water with milk and sugar until soft.",
                new String[]{"rice", "milk", "sugar"}, new double[]{100, 300, 20}, new String[]{"g", "ml", "g"});
    }

    private void addRecipeWithIngredients(SQLiteDatabase db, String name, String instructions,
                                          String[] ingNames, double[] ingQtys, String[] ingUnits) {
        ContentValues recipeValues = new ContentValues();
        recipeValues.put(COL_RECIPE_NAME, name);
        recipeValues.put(COL_RECIPE_INSTRUCTIONS, instructions);
        long recipeId = db.insert(TABLE_RECIPES, null, recipeValues);

        for (int i = 0; i < ingNames.length; i++) {
            ContentValues ingValues = new ContentValues();
            ingValues.put(COL_RI_RECIPE_ID, recipeId);
            ingValues.put(COL_RI_NAME, ingNames[i]);
            ingValues.put(COL_RI_QTY, ingQtys[i]);
            ingValues.put(COL_RI_UNIT, ingUnits[i]);
            db.insert(TABLE_RECIPE_INGREDIENTS, null, ingValues);
        }
    }
}