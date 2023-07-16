package chris.davison.cdweather.util;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class HttpRequests {
    public HttpRequests() {}
    public JSONObject makeHttpGETRequest(String url) {
        try {
            URL reqUrl = new URL(url);

            HttpURLConnection httpURLConnection = (HttpURLConnection) reqUrl.openConnection();
            httpURLConnection.setRequestMethod("GET");
            httpURLConnection.setRequestProperty("User-Agent", "Mozilla/5.0 (Android 13; Mobile; " +
                    "rv:68.0) Gecko/68.0 Firefox/115.0");
            int responseCode = httpURLConnection.getResponseCode();
            Log.d("HTTPRESPONSE: ", String.valueOf(responseCode));
            if (responseCode == HttpURLConnection.HTTP_OK) {
                BufferedReader in = new BufferedReader(new InputStreamReader(httpURLConnection
                        .getInputStream()));
                String inputLine;
                StringBuilder response = new StringBuilder();

                while ((inputLine = in.readLine()) != null) {
                    response.append(inputLine);
                }
                in.close();

                return new JSONObject(response.toString());
            } else {
                Log.d("HTTPGET: ", "GET request failed. Response code "+responseCode);
                return null;
            }
        } catch (Exception e) {
            Log.d("HTTPGET: ", e.getMessage());
        }
        return null;
    }

    public Bitmap getImageFromUrl(String url) {
        Bitmap bitmap = null;

        try {
            InputStream input = new java.net.URL(url).openStream();
            bitmap = BitmapFactory.decodeStream(input);
        } catch (Exception e) {
            Log.d("ICON: ", e.getMessage());
        }
        return bitmap;
    }
}
