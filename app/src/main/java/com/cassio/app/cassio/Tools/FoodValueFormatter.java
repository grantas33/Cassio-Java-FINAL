package com.cassio.app.cassio.tools;

import com.cassio.app.cassio.models.Food;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.formatter.IValueFormatter;
import com.github.mikephil.charting.utils.ViewPortHandler;

import java.text.DecimalFormat;


public class FoodValueFormatter implements IValueFormatter {
    private DecimalFormat mFormat;
    public Food item;

    public FoodValueFormatter(Food item) {
        this.item = item;
        mFormat = new DecimalFormat("########0.0"); // use one decimal
    }

    @Override
    public String getFormattedValue(float value, Entry entry, int dataSetIndex, ViewPortHandler viewPortHandler) {
        // write your logic here
        if (value == 0.00001f) return "";
        else return mFormat.format(value) + "g";
    }
}
