package com.cassio.app.cassio.fragmentLogic;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Build;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.EditText;
import android.widget.Toast;

import com.cassio.app.cassio.CreateFoodFragment;
import com.cassio.app.cassio.interfaces.AsyncTaskCompleteListener;
import com.cassio.app.cassio.models.Food;
import com.cassio.app.cassio.MainActivity;
import com.cassio.app.cassio.R;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.SocketTimeoutException;
import java.net.URL;

import static java.lang.Integer.parseInt;

public class MainActivityLogic implements AsyncTaskCompleteListener<Food> {

    Context context;

    public MainActivityLogic(Activity activity) {
        this.context = activity;
    }

    public void processBarcode(String barcode) {
        GetFoodHtmlTask task = new GetFoodHtmlTask(context, this);
        task.execute(barcode);
    }

    @Override
    public void onTaskComplete(Food result) {
        CreateFoodFragment fragment = (CreateFoodFragment) ((MainActivity) context).fragment;
        fragment.processScan(result);
    }

    //------------------------------------------------------------------------------------
    private class GetFoodHtmlTask extends AsyncTask<String, Void, Food> {

        private AsyncTaskCompleteListener<Food> callback;
        private Context context;
        private ProgressDialog progressDialog;
        private Food finalResult;


        static final int NAME_FOUND = 0;
        static final int CALORIES_FOUND = 1;
        static final int CARBOHYDRATES_FOUND = 2;
        static final int PROTEIN_FOUND = 3;
        static final int FAT_FOUND = 4;
        static final int LAST_CARBOHYDRATES = 5;
        static final int LAST_PROTEIN = 6;
        static final int LAST_FAT = 7;
        static final int ELEMENT_COUNT = 8;

        public GetFoodHtmlTask(Context context, AsyncTaskCompleteListener<Food> cb) {
            this.context = context;
            this.callback = cb;
            progressDialog = new ProgressDialog(this.context);
        }

        protected void onPreExecute() {
            progressDialog.setMessage("Palaukite....");
            progressDialog.show();
        }

        //called on this.execute(String)
        @Override
        protected Food doInBackground(String... strings) {
            int calories = 0;
            String name = "notset";
            double carbohydrates = 0;
            double protein = 0;
            double fat = 0;

            try {
                String link = "https://app.rimi.lt/entry/" + strings[0];
                //setup connection
                URL url = new URL(link);
                HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setRequestProperty("User-Agent",
                        "Mozilla/5.0 (Windows NT 6.1;zh-tw; MSIE 6.0)");
                if (parseInt(Build.VERSION.SDK) < Build.VERSION_CODES.FROYO) {
                    System.setProperty("http.keepAlive", "false");
                }
                urlConnection.setReadTimeout(10000);
                urlConnection.setConnectTimeout(15000);
                urlConnection.setDoOutput(false);
                urlConnection.setDoInput(true);
                urlConnection.setRequestMethod("GET");
                urlConnection.setUseCaches(false);

                InputStreamReader in;
                //check connection
                int status = urlConnection.getResponseCode();

                //get connection
                if (status != HttpURLConnection.HTTP_OK) {
                    in = new InputStreamReader(urlConnection.getErrorStream());
                } else {
                    in = new InputStreamReader(urlConnection.getInputStream());
                }

                //read
                BufferedReader bufferedReader = new BufferedReader(in, 100000);

                String line = "";

                String numbers = "0123456789";
                //parameters for checking if variables are found. Array for simpler code
                boolean[] parameter = new boolean[ELEMENT_COUNT];


                while ((line = bufferedReader.readLine()) != null) {

                    //extract calories from current line
                    if (!parameter[CALORIES_FOUND]) {
                        int calIndex = line.indexOf(" kcal");
                        if (calIndex >= 0) {
                            calories = getCalories(line, calIndex);
                            parameter[CALORIES_FOUND] = true;
                        }
                    }

                    //extract name from current line
                    if (!parameter[NAME_FOUND]) {
                        int nameStartIndex = line.indexOf("<h1 itemprop=\"name\">");
                        int nameEndIndex = line.indexOf("</h1>");
                        if (nameStartIndex >= 0) {
                            name = getName(line, nameStartIndex, nameEndIndex);
                            parameter[NAME_FOUND] = true;
                        }
                    }

                    //check last line for tag because of HTML structure:
//                    <td>Riebalų</td>
//                        <td class="text-right">20,8 g</td>
                    if (parameter[LAST_CARBOHYDRATES] && !parameter[CARBOHYDRATES_FOUND]) {
                        carbohydrates = retrieveNutritionalGrams(line);
                        parameter[CARBOHYDRATES_FOUND] = true;
                    }
                    if (line.contains("Angliavandenių")) {
                        parameter[LAST_CARBOHYDRATES] = true;
                    } else parameter[LAST_CARBOHYDRATES] = false;

                    if (parameter[LAST_PROTEIN] && !parameter[PROTEIN_FOUND]) {
                        protein = retrieveNutritionalGrams(line);
                        parameter[PROTEIN_FOUND] = true;
                    }
                    if (line.contains("Baltymų")) {
                        parameter[LAST_PROTEIN] = true;
                    } else parameter[LAST_PROTEIN] = false;

                    if (parameter[LAST_FAT] && !parameter[FAT_FOUND]) {
                        fat = retrieveNutritionalGrams(line);
                        parameter[FAT_FOUND] = true;
                    }
                    if (line.contains("Riebalų")) {
                        parameter[LAST_FAT] = true;
                    } else parameter[LAST_FAT] = false;
                }

                in.close();
                urlConnection.disconnect();

            } catch (SocketTimeoutException e) {
                return null;
            } catch (IOException e) {
                e.printStackTrace();
            }

            return new Food(name, calories, 100, carbohydrates, protein, fat);
        }

        private String getName(String line, int nameStartIndex, int nameEndIndex) {
            StringBuilder sb = new StringBuilder();
            for (int i = nameStartIndex + 20; i < nameEndIndex; i++) {
                sb.append(line.charAt(i));
            }
            return sb.toString();
        }

        private int getCalories(String line, int index) {
//
            String numbers = "0123456789";
            StringBuilder sb = new StringBuilder();
            for (int i = index - 1; i > 0; i--) {
                if (numbers.indexOf(line.charAt(i)) != -1) {
                    sb.insert(0, line.charAt(i));
                } else {
                    return parseInt(sb.toString());
                }
            }
            return 0;
        }

        @Override
        protected void onPostExecute(Food result) {
            finalResult = result;
            progressDialog.dismiss();
            System.out.println("on Post execute called");
            callback.onTaskComplete(result);
        }

        private double retrieveNutritionalGrams(String line) {
            //extract grams
//                <td class="text-right">20,8 g</td>
            String numbersAndComma = "0123456789,";
            StringBuilder sb = new StringBuilder();
            int i = line.indexOf("<td class=\"text-right\">") + 23;
            while ((line.charAt(i) != ' ') || (line.charAt(i + 1) != 'g')) {
                if (numbersAndComma.indexOf(line.charAt(i)) != -1) {
                    if (line.charAt(i) == ',') {
                        sb.append('.');
                    } else sb.append(line.charAt(i));
                }
                i++;
            }
            return Double.parseDouble(sb.toString());
        }

    }
}
