package com.cassio.app.cassio.fragmentLogic;

import android.content.Context;
import android.content.Intent;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.util.Log;

import com.cassio.app.cassio.models.Food;
import com.cassio.app.cassio.tools.DatabaseHelper;
import com.cassio.app.cassio.tools.InvalidValueException;
import com.github.johnpersano.supertoasts.library.Style;
import com.github.johnpersano.supertoasts.library.SuperActivityToast;
import com.github.johnpersano.supertoasts.library.utils.PaletteUtils;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;
import com.j256.ormlite.android.apptools.OpenHelperManager;
import com.j256.ormlite.dao.Dao;

import java.sql.SQLException;

public class CreateFoodLogic {

    private DatabaseHelper databaseHelper = null;
    private Context context;
    private SuperActivityToast CreateFoodToast = null;

    public CreateFoodLogic(Context context) {
        this.context = context;
    }

    private DatabaseHelper getHelper() {
        if (databaseHelper == null) {
            databaseHelper = OpenHelperManager.getHelper(context, DatabaseHelper.class);
        }
        return databaseHelper;
    }

    public void Add(String name, String calories, String grams, String carbohydrates, String protein, String fat) throws Exception {
        String Name = name;
        int Calories = 0;
        int Grams = 0;
        double Carbohydrates = 0;
        double Protein = 0;
        double Fat = 0;
        //validating values. Should be a smarter way to do it
        if (!isValidInt(calories) || calories.isEmpty()) {
            throw new InvalidValueException("Neteisingos kalorijos!");
        } else {
            Calories = Integer.parseInt(calories);
        }
        if (!isValidInt(grams) && !grams.isEmpty()) {
            throw new InvalidValueException("Neteisingi gramai!");
        } else {
            if (grams.equals("0") || grams.isEmpty()) {
                Grams = 100;
            } else Grams = Integer.parseInt(grams);
        }
        if (!isValidDouble(carbohydrates)) {
            throw new InvalidValueException("Neteisingi angliavandeniai!");
        } else {
            if (!carbohydrates.isEmpty())
                Carbohydrates = Double.parseDouble(carbohydrates);
        }
        if (!isValidDouble(protein)) {
            throw new InvalidValueException("Neteisingi baltymai!");
        } else {
            if (!protein.isEmpty())
                Protein = Double.parseDouble(protein);
        }

        if (!isValidDouble(fat)) {
            throw new InvalidValueException("Neteisingi riebalai!");
        } else {
            if (!fat.isEmpty())
                Fat = Double.parseDouble(fat);
        }

        Food food = new Food(Name, Calories, Grams, Carbohydrates, Protein, Fat);
        AddFood(food);
    }

    public void AddFood(Food food) {
        try {
            //paimam objekta duombazes operacijom
            DatabaseHelper helper = getHelper();
            final Dao<Food, Integer> foodDao = helper.getFoodDao();

            //idedam i duombaze
            foodDao.create(food);

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    //validate double value before parsing
    private boolean isValidDouble(String input) {
        boolean hasPoint = false;
        input = input.trim();
        String regex = "^[a-zA-Z]+$";


        if (input.matches(regex)) {
            return false;
        }

        for (int i = 0; i < input.length(); i++) {
            char ch = input.charAt(i);
            if (ch == '.') {
                if (!hasPoint) {
                    hasPoint = true;
                } else return false;
            }
            if (!Character.isDigit(ch) && ch != '.') {
                return false;
            }
        }

        return true;
    }

    public void onDestroy() {
        /*
         * You'll need this in your class to release the helper when done.
		 */
        if (databaseHelper != null) {
            OpenHelperManager.releaseHelper();
            databaseHelper = null;
        }
    }

    private boolean isValidInt(String input) {
        input = input.trim();
        String regex = "^[a-zA-Z]+$";

        if (input.matches(regex) || input.isEmpty()) {
            return false;
        }

        for (int i = 0; i < input.length(); i++) {
            char ch = input.charAt(i);
            if (!Character.isDigit(ch)) {
                return false;
            }
        }
        return true;
    }

    public void GenerateToast(String text) {
        if (CreateFoodToast != null) CreateFoodToast.dismiss();
        CreateFoodToast = (SuperActivityToast) SuperActivityToast.create(context, new Style(), Style.TYPE_STANDARD)
                .setFrame(Style.FRAME_STANDARD)
                .setText(text)
                .setColor(PaletteUtils.getSolidColor(PaletteUtils.MATERIAL_RED))
                .setAnimations(Style.ANIMATIONS_FADE);
        CreateFoodToast.show();
    }
}
