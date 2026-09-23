package com.example.smartpantry;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;
import java.util.List;

public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "smart_pantry.db";
    private static final int DATABASE_VERSION = 3;

    // Table Constants
    public static final String TABLE_PANTRY = "pantry";
    public static final String COLUMN_ID = "_id";
    public static final String COLUMN_NAME = "name";
    public static final String COLUMN_QUANTITY = "quantity";
    public static final String COLUMN_UNIT = "unit";
    public static final String COLUMN_EXPIRY_DATE = "expiry_date";

    // Recipe Tables Constants
    public static final String TABLE_RECIPES = "recipes";
    public static final String TABLE_RECIPE_INGREDIENTS = "recipe_ingredients";

    // SQLite CREATE TABLE Statements
    private static final String TABLE_CREATE_PANTRY =
            "CREATE TABLE " + TABLE_PANTRY + " (" +
                    COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    COLUMN_NAME + " TEXT NOT NULL, " +
                    COLUMN_QUANTITY + " REAL NOT NULL, " +
                    COLUMN_UNIT + " TEXT, " +
                    COLUMN_EXPIRY_DATE + " TEXT" +
                    ");";

    private static final String CREATE_TABLE_RECIPES =
            "CREATE TABLE " + TABLE_RECIPES + " (" +
                    "_id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "name TEXT NOT NULL, " +
                    "instructions TEXT" +
                    ");";

    private static final String CREATE_TABLE_RECIPE_INGREDIENTS =
            "CREATE TABLE " + TABLE_RECIPE_INGREDIENTS + " (" +
                    "_id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "recipe_id INTEGER, " +
                    "name TEXT NOT NULL, " +
                    "quantity REAL NOT NULL, " +
                    "unit TEXT" +
                    ");";

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(TABLE_CREATE_PANTRY);
        db.execSQL(CREATE_TABLE_RECIPES);
        db.execSQL(CREATE_TABLE_RECIPE_INGREDIENTS);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_PANTRY);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_RECIPES);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_RECIPE_INGREDIENTS);
        onCreate(db);
    }

    // ==========================================OPERATIONS
    public long insertItem(PantryItem item) {
        try (SQLiteDatabase db = this.getWritableDatabase()) {
            ContentValues values = new ContentValues();
            values.put(COLUMN_NAME, item.getName());
            values.put(COLUMN_QUANTITY, item.getQuantity());
            values.put(COLUMN_UNIT, item.getUnit());
            values.put(COLUMN_EXPIRY_DATE, item.getExpiryDate());

            return db.insert(TABLE_PANTRY, null, values);
        }
    }

    public List<PantryItem> getAllItems() {
        List<PantryItem> items = new ArrayList<>();
        String selectQuery = "SELECT * FROM " + TABLE_PANTRY + " ORDER BY " + COLUMN_ID + " DESC";

        try (SQLiteDatabase db = this.getReadableDatabase();
             Cursor cursor = db.rawQuery(selectQuery, null)) {

            if (cursor.moveToFirst()) {
                int idIdx = cursor.getColumnIndexOrThrow(COLUMN_ID);
                int nameIdx = cursor.getColumnIndexOrThrow(COLUMN_NAME);
                int qtyIdx = cursor.getColumnIndexOrThrow(COLUMN_QUANTITY);
                int unitIdx = cursor.getColumnIndexOrThrow(COLUMN_UNIT);
                int expiryIdx = cursor.getColumnIndexOrThrow(COLUMN_EXPIRY_DATE);

                do {
                    long id = cursor.getLong(idIdx);
                    String name = cursor.getString(nameIdx);
                    double quantity = cursor.getDouble(qtyIdx);
                    String unit = cursor.getString(unitIdx);
                    String expiryDate = cursor.getString(expiryIdx);

                    items.add(new PantryItem(id, name, quantity, unit, expiryDate));
                } while (cursor.moveToNext());
            }
        }
        return items;
    }

    public int deleteItem(long id) {
        try (SQLiteDatabase db = this.getWritableDatabase()) {
            return db.delete(TABLE_PANTRY, COLUMN_ID + " = ?", new String[]{String.valueOf(id)});
        }
    }

    public int updateItem(PantryItem item) {
        try (SQLiteDatabase db = this.getWritableDatabase()) {
            ContentValues values = new ContentValues();
            values.put(COLUMN_NAME, item.getName());
            values.put(COLUMN_QUANTITY, item.getQuantity());
            values.put(COLUMN_UNIT, item.getUnit());
            values.put(COLUMN_EXPIRY_DATE, item.getExpiryDate());

            return db.update(TABLE_PANTRY, values, COLUMN_ID + " = ?", new String[]{String.valueOf(item.getId())});
        }
    }


    // ==========================================RECIPE OPERATIONS
    public long insertRecipe(Recipe recipe) {
        try (SQLiteDatabase db = this.getWritableDatabase()) {
            ContentValues values = new ContentValues();
            values.put("name", recipe.getName());
            values.put("instructions", recipe.getInstructions());

            long recipeId = db.insert(TABLE_RECIPES, null, values);

            for (RecipeIngredient ing : recipe.getIngredients()) {
                ContentValues ingValues = new ContentValues();
                ingValues.put("recipe_id", recipeId);
                ingValues.put("name", ing.getName());
                ingValues.put("quantity", ing.getRequiredQuantity());
                ingValues.put("unit", ing.getUnit());
                db.insert(TABLE_RECIPE_INGREDIENTS, null, ingValues);
            }
            return recipeId;
        }
    }

    public List<Recipe> getAllRecipes() {
        List<Recipe> recipes = new ArrayList<>();

        // 1. Load Custom Saved Recipes from Database
        try (SQLiteDatabase db = this.getReadableDatabase();
             Cursor cursor = db.rawQuery("SELECT * FROM " + TABLE_RECIPES + " ORDER BY _id DESC", null)) {

            if (cursor.moveToFirst()) {
                int idIdx = cursor.getColumnIndexOrThrow("_id");
                int nameIdx = cursor.getColumnIndexOrThrow("name");
                int instIdx = cursor.getColumnIndexOrThrow("instructions");

                do {
                    long id = cursor.getLong(idIdx);
                    String name = cursor.getString(nameIdx);
                    String instructions = cursor.getString(instIdx);

                    // Fetching associated ingredients
                    List<RecipeIngredient> ingredients = getIngredientsForRecipe(id);
                    recipes.add(new Recipe(id, name, ingredients, instructions));
                } while (cursor.moveToNext());
            }
        } catch (Exception ignored) {}

        // 2. Adding Predefined Quick & Easy Recipes
        recipes.addAll(getPredefinedRecipes());

        return recipes;
    }

    private List<RecipeIngredient> getIngredientsForRecipe(long recipeId) {
        List<RecipeIngredient> ingredients = new ArrayList<>();
        String query = "SELECT * FROM " + TABLE_RECIPE_INGREDIENTS + " WHERE recipe_id = ?";

        try (SQLiteDatabase db = this.getReadableDatabase();
             Cursor cursor = db.rawQuery(query, new String[]{String.valueOf(recipeId)})) {

            if (cursor.moveToFirst()) {
                int nameIdx = cursor.getColumnIndexOrThrow("name");
                int qtyIdx = cursor.getColumnIndexOrThrow("quantity");
                int unitIdx = cursor.getColumnIndexOrThrow("unit");

                do {
                    String name = cursor.getString(nameIdx);
                    double qty = cursor.getDouble(qtyIdx);
                    String unit = cursor.getString(unitIdx);
                    ingredients.add(new RecipeIngredient(name, qty, unit));
                } while (cursor.moveToNext());
            }
        }
        return ingredients;
    }

    private List<Recipe> getPredefinedRecipes() {
        List<Recipe> recipes = new ArrayList<>();

        // 1. Omelette
        List<RecipeIngredient> omelette = new ArrayList<>();
        omelette.add(new RecipeIngredient("Egg", 2, "loose"));
        omelette.add(new RecipeIngredient("Milk", 0.5, "cup"));
        recipes.add(new Recipe(101, "Omelette", omelette, "Whisk eggs with milk and fry on low heat until cooked through."));

        // 2. Garden Salad
        List<RecipeIngredient> salad = new ArrayList<>();
        salad.add(new RecipeIngredient("Tomato", 2, "loose"));
        salad.add(new RecipeIngredient("Cucumber", 1, "loose"));
        salad.add(new RecipeIngredient("Onion", 0.5, "loose"));
        salad.add(new RecipeIngredient("Lettuce", 100, "g"));
        salad.add(new RecipeIngredient("Salt", 3, "g"));
        recipes.add(new Recipe(102, "Garden Salad", salad, "Chop tomato and cucumber. Toss together in a bowl with dressing."));

        // 3. Pancakes
        List<RecipeIngredient> pancakes = new ArrayList<>();
        pancakes.add(new RecipeIngredient("Flour", 1, "cup"));
        pancakes.add(new RecipeIngredient("Milk", 1, "cup"));
        pancakes.add(new RecipeIngredient("Egg", 1, "loose"));
        recipes.add(new Recipe(103, "Pancakes", pancakes, "Mix flour, milk, and egg into a smooth batter. Pour onto hot pan and flip when golden."));

        // 4. Grilled Cheese Sandwich
        List<RecipeIngredient> grilledCheese = new ArrayList<>();
        grilledCheese.add(new RecipeIngredient("Bread", 2, "slice"));
        grilledCheese.add(new RecipeIngredient("Cheese", 2, "slice"));
        grilledCheese.add(new RecipeIngredient("Butter", 1, "tbsp"));
        recipes.add(new Recipe(104, "Grilled Cheese Sandwich", grilledCheese, "Butter the outside of the bread, add cheese between slices, and grill on a skillet until golden brown."));

        // 5. Scrambled Eggs on Toast
        List<RecipeIngredient> scrambledEggs = new ArrayList<>();
        scrambledEggs.add(new RecipeIngredient("Egg", 2, "loose"));
        scrambledEggs.add(new RecipeIngredient("Butter", 1, "tbsp"));
        scrambledEggs.add(new RecipeIngredient("Bread", 2, "slice"));
        recipes.add(new Recipe(105, "Scrambled Eggs on Toast", scrambledEggs, "Melt butter in a pan, scramble the whisked eggs gently, and serve hot over toasted bread."));

        // 6. Garlic Butter Pasta
        List<RecipeIngredient> garlicPasta = new ArrayList<>();
        garlicPasta.add(new RecipeIngredient("Pasta", 200, "g"));
        garlicPasta.add(new RecipeIngredient("Garlic", 2, "clove"));
        garlicPasta.add(new RecipeIngredient("Butter", 2, "tbsp"));
        garlicPasta.add(new RecipeIngredient("Salt", 2, "g"));
        recipes.add(new Recipe(106, "Garlic Butter Pasta", garlicPasta, "Boil pasta until al dente. Saute minced garlic in butter and toss cooked pasta with garlic butter sauce."));

        // 7. Tomato Basil Pasta
        List<RecipeIngredient> tomatoPasta = new ArrayList<>();
        tomatoPasta.add(new RecipeIngredient("Pasta", 200, "g"));
        tomatoPasta.add(new RecipeIngredient("Tomato", 3, "loose"));
        tomatoPasta.add(new RecipeIngredient("Garlic", 2, "clove"));
        tomatoPasta.add(new RecipeIngredient("Olive Oil", 1, "tbsp"));
        recipes.add(new Recipe(107, "Tomato Basil Pasta", tomatoPasta, "Cook pasta. Dice tomatoes and garlic, saute in olive oil until soft, and toss directly with hot pasta."));

        // 8. French Toast
        List<RecipeIngredient> frenchToast = new ArrayList<>();
        frenchToast.add(new RecipeIngredient("Bread", 2, "slice"));
        frenchToast.add(new RecipeIngredient("Egg", 1, "loose"));
        frenchToast.add(new RecipeIngredient("Milk", 0.25, "cup"));
        frenchToast.add(new RecipeIngredient("Butter", 1, "tbsp"));
        recipes.add(new Recipe(108, "French Toast", frenchToast, "Whisk egg and milk. Dip bread slices into mixture and fry in buttered skillet until golden on both sides."));

        // 9. Rice and Beans Bowl
        List<RecipeIngredient> riceAndBeans = new ArrayList<>();
        riceAndBeans.add(new RecipeIngredient("Rice", 1, "cup"));
        riceAndBeans.add(new RecipeIngredient("Canned Beans", 1, "can"));
        riceAndBeans.add(new RecipeIngredient("Onion", 0.5, "loose"));
        riceAndBeans.add(new RecipeIngredient("Salt", 2, "g"));
        recipes.add(new Recipe(109, "Rice and Beans Bowl", riceAndBeans, "Cook rice. Saute diced onion with canned beans and season with salt. Serve warm over rice."));

        // 10. Chicken Quesadilla
        List<RecipeIngredient> quesadilla = new ArrayList<>();
        quesadilla.add(new RecipeIngredient("Tortilla", 2, "loose"));
        quesadilla.add(new RecipeIngredient("Chicken", 100, "g"));
        quesadilla.add(new RecipeIngredient("Cheese", 50, "g"));
        recipes.add(new Recipe(110, "Chicken Quesadilla", quesadilla, "Place shredded cooked chicken and cheese between tortillas. Toast on skillet until cheese melts completely."));

        // 11. Tuna Salad Wrap
        List<RecipeIngredient> tunaWrap = new ArrayList<>();
        tunaWrap.add(new RecipeIngredient("Canned Tuna", 1, "can"));
        tunaWrap.add(new RecipeIngredient("Mayonnaise", 2, "tbsp"));
        tunaWrap.add(new RecipeIngredient("Tortilla", 1, "loose"));
        tunaWrap.add(new RecipeIngredient("Lettuce", 30, "g"));
        recipes.add(new Recipe(111, "Tuna Salad Wrap", tunaWrap, "Mix drained canned tuna with mayonnaise. Layer onto tortilla with fresh lettuce and roll into a tight wrap."));

        // 12. Avocado Toast with Egg
        List<RecipeIngredient> avoToast = new ArrayList<>();
        avoToast.add(new RecipeIngredient("Bread", 1, "slice"));
        avoToast.add(new RecipeIngredient("Avocado", 1, "loose"));
        avoToast.add(new RecipeIngredient("Egg", 1, "loose"));
        recipes.add(new Recipe(112, "Avocado Toast with Egg", avoToast, "Toast bread and mash avocado on top. Fry or boil egg to preference and place over avocado toast."));

        // 13. Vegetable Stir Fry
        List<RecipeIngredient> stirFry = new ArrayList<>();
        stirFry.add(new RecipeIngredient("Carrot", 1, "loose"));
        stirFry.add(new RecipeIngredient("Broccoli", 100, "g"));
        stirFry.add(new RecipeIngredient("Soy Sauce", 2, "tbsp"));
        stirFry.add(new RecipeIngredient("Olive Oil", 1, "tbsp"));
        recipes.add(new Recipe(113, "Vegetable Stir Fry", stirFry, "Slice vegetables thinly. Stir fry on high heat with olive oil for 5 minutes, then toss with soy sauce."));

        // 14. Egg Fried Rice
        List<RecipeIngredient> eggFriedRice = new ArrayList<>();
        eggFriedRice.add(new RecipeIngredient("Rice", 1, "cup"));
        eggFriedRice.add(new RecipeIngredient("Egg", 2, "loose"));
        eggFriedRice.add(new RecipeIngredient("Soy Sauce", 1, "tbsp"));
        eggFriedRice.add(new RecipeIngredient("Oil", 1, "tbsp"));
        recipes.add(new Recipe(114, "Egg Fried Rice", eggFriedRice, "Heat oil in pan, scramble eggs quickly, add cooked rice and soy sauce, stir frying over high heat."));

        // 15. Mac and Cheese
        List<RecipeIngredient> macAndCheese = new ArrayList<>();
        macAndCheese.add(new RecipeIngredient("Pasta", 150, "g"));
        macAndCheese.add(new RecipeIngredient("Cheese", 100, "g"));
        macAndCheese.add(new RecipeIngredient("Milk", 0.5, "cup"));
        macAndCheese.add(new RecipeIngredient("Butter", 1, "tbsp"));
        recipes.add(new Recipe(115, "Mac and Cheese", macAndCheese, "Boil macaroni. Drain and return to low heat, stirring in butter, milk, and cheese until creamy."));

        // 16. Peanut Butter Banana Toast
        List<RecipeIngredient> pbBananaToast = new ArrayList<>();
        pbBananaToast.add(new RecipeIngredient("Bread", 2, "slice"));
        pbBananaToast.add(new RecipeIngredient("Peanut Butter", 2, "tbsp"));
        pbBananaToast.add(new RecipeIngredient("Banana", 1, "loose"));
        recipes.add(new Recipe(116, "Peanut Butter Banana Toast", pbBananaToast, "Toast bread slices, spread peanut butter generously, and top with sliced bananas."));

        // 17. Tomato Soup
        List<RecipeIngredient> tomatoSoup = new ArrayList<>();
        tomatoSoup.add(new RecipeIngredient("Canned Tomatoes", 1, "can"));
        tomatoSoup.add(new RecipeIngredient("Garlic", 1, "clove"));
        tomatoSoup.add(new RecipeIngredient("Onion", 0.5, "loose"));
        tomatoSoup.add(new RecipeIngredient("Milk", 0.5, "cup"));
        recipes.add(new Recipe(117, "Tomato Soup", tomatoSoup, "Saute chopped onion and garlic. Add canned tomatoes, simmer, blend smoothly with milk, and serve warm."));

        // 18. Chicken and Rice Bowl
        List<RecipeIngredient> chickenRice = new ArrayList<>();
        chickenRice.add(new RecipeIngredient("Chicken", 150, "g"));
        chickenRice.add(new RecipeIngredient("Rice", 1, "cup"));
        chickenRice.add(new RecipeIngredient("Olive Oil", 1, "tbsp"));
        chickenRice.add(new RecipeIngredient("Salt", 2, "g"));
        recipes.add(new Recipe(118, "Chicken and Rice Bowl", chickenRice, "Season chicken with salt and pan-fry in olive oil. Serve sliced chicken over freshly cooked rice."));

        // 19. Oatmeal with Banana
        List<RecipeIngredient> oatmeal = new ArrayList<>();
        oatmeal.add(new RecipeIngredient("Oats", 0.5, "cup"));
        oatmeal.add(new RecipeIngredient("Milk", 1, "cup"));
        oatmeal.add(new RecipeIngredient("Banana", 1, "loose"));
        recipes.add(new Recipe(119, "Oatmeal with Banana", oatmeal, "Cook oats in milk over medium heat for 5 minutes. Top with sliced banana and serve warm."));

        // 20. Loaded Baked Potato
        List<RecipeIngredient> bakedPotato = new ArrayList<>();
        bakedPotato.add(new RecipeIngredient("Potato", 1, "loose"));
        bakedPotato.add(new RecipeIngredient("Cheese", 30, "g"));
        bakedPotato.add(new RecipeIngredient("Butter", 1, "tbsp"));
        recipes.add(new Recipe(120, "Loaded Baked Potato", bakedPotato, "Bake or microwave potato until soft. Slice open, mash butter into potato flesh, and top with cheese."));

        return recipes;
    }
}