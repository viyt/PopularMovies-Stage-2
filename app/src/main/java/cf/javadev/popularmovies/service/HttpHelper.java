package cf.javadev.popularmovies.service;


import android.net.Uri;
import android.os.Build;
import android.support.annotation.Nullable;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

class HttpHelper {
    @Nullable
    static synchronized String getJsonString(Uri uri) {
        HttpURLConnection urlConnection = null;
        BufferedReader reader = null;
        String jsonString = null;
        try {
            URL url = new URL(uri.toString());
            urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setRequestMethod("GET");
            urlConnection.connect();

            InputStream inputStream = urlConnection.getInputStream();
            StringBuilder builder = new StringBuilder();
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                reader = new BufferedReader(new InputStreamReader(inputStream,
                        StandardCharsets.UTF_8));
            } else {
                reader = new BufferedReader(new InputStreamReader(inputStream));
            }

            String line;
            while ((line = reader.readLine()) != null) {
                //noinspection StringConcatenationInsideStringBufferAppend
                builder.append(line + "\n");
            }

            if (builder.length() == 0) {
                return null;
            }

            jsonString = builder.toString();

        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (urlConnection != null) {
                urlConnection.disconnect();
            }
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return jsonString;
    }
}
