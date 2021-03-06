package com.cassio.app.cassio.fragmentLogic;

import android.app.Activity;
import android.content.Context;
import android.widget.Toast;

import com.cassio.app.cassio.models.Food;
import com.cassio.app.cassio.models.LogItem;
import com.cassio.app.cassio.models.Recipe;
import com.cassio.app.cassio.tools.DatabaseHelper;
import com.github.johnpersano.supertoasts.library.utils.PaletteUtils;
import com.github.johnpersano.supertoasts.library.Style;
import com.github.johnpersano.supertoasts.library.SuperActivityToast;
import com.j256.ormlite.android.apptools.OpenHelperManager;
import com.j256.ormlite.dao.Dao;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ChooseFoodLogic {
    DatabaseHelper databaseHelper = null;
    Context context;
    public SuperActivityToast mSuper = null;

    public ChooseFoodLogic(Context context) {
        this.context = context;
    }

    private DatabaseHelper getHelper() {
        if (databaseHelper == null) {
            databaseHelper = OpenHelperManager.getHelper(context, DatabaseHelper.class);
        }
        return databaseHelper;
    }

    public List<Food> getSortedFoods() {
        List<Food> foods = new ArrayList<Food>();
        List<Recipe> recipes;
        try {
            DatabaseHelper helper = getHelper();
            final Dao<Food, Integer> foodDao = helper.getFoodDao();
            Dao<Recipe, Integer> reciperDao = helper.getRecipeDao();
            recipes = reciperDao.queryForAll();
            foods = new ArrayList<>(foodDao.queryForAll());
            foods.addAll(recipes);
        } catch (SQLException e) {
            Toast.makeText(context, e.getMessage(), Toast.LENGTH_LONG);
        }

        Collections.sort(foods);
        return foods;
    }


    public void addLogItem(LogItem item) {
        try {

            DatabaseHelper helper = getHelper();
            final Dao<LogItem, Integer> logDao = helper.getLogDao();

            //idedam i duombaze
            logDao.create(item);


        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void deleteFoodItem(int id) {
        try {
            final Dao<Food, Integer> FoodDao = getHelper().getFoodDao();
            FoodDao.deleteById(id);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void updateGramsFoodItem(int grams, Food food) {
        try {
            final Dao<Food, Integer> FoodDao = getHelper().getFoodDao();
            food.setGrams(grams);
//            Food newFood = new Food(food.Grams, food.foodId, food.Name, food.Calories, grams, food.Carbohydrates, food.Protein, food.Fat);
            FoodDao.update(food);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void dismissToast() {
        if (mSuper != null) mSuper.dismiss();
    }

    public void generateToast(Activity activity, boolean male, String name) {
        if (mSuper != null) mSuper.dismiss();
        mSuper = (SuperActivityToast) SuperActivityToast.create(activity, new Style(), Style.TYPE_STANDARD)
                .setFrame(Style.FRAME_KITKAT)
                .setText(male ? "Pridėtas " + name : "Pridėta " + name)
                .setColor(PaletteUtils.getSolidColor(PaletteUtils.MATERIAL_DEEP_ORANGE))
                .setAnimations(Style.ANIMATIONS_POP);
        mSuper.show();
    }


}
