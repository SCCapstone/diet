package edu.sc.snacktrack.barcode;

import android.os.AsyncTask;
import android.util.Log;
import android.view.View;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Created by josh on 3/22/16.
 */
public class RetrieveFeedTask extends AsyncTask<String, Void, String> {

    private Exception exception;

    protected String doInBackground(String... urlStr) {
        String yourProduct = urlStr[0];
        // Do some validation here

        try {
            //URL url = new URL(API_URL + "email=" + email + "&apiKey=" + API_KEY);
            yourProduct = "https://api.outpan.com/v2/products/" + yourProduct;
            yourProduct = yourProduct.concat("?apikey=0486422639a1db6223dd81a97c9f996f");
            Log.d("myTag", yourProduct);
            URL url = new URL(yourProduct);
            HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
            try {
                BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(urlConnection.getInputStream()));
                StringBuilder stringBuilder = new StringBuilder();
                String line;
                while ((line = bufferedReader.readLine()) != null) {
                    stringBuilder.append(line).append("\n");
                }
                bufferedReader.close();
                return stringBuilder.toString();
            }
            finally{
                urlConnection.disconnect();
            }
        }
        catch(Exception e) {
            Log.e("ERROR", e.getMessage(), e);
            return null;
        }
    }

    protected void onPostExecute(String response) {
        if(response == null) {
            response = "THERE WAS AN ERROR";
        }
        Log.i("INFO", response);
    }


}