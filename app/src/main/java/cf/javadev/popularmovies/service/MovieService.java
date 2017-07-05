package cf.javadev.popularmovies.service;

import android.app.IntentService;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.support.annotation.Nullable;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import cf.javadev.popularmovies.BuildConfig;
import cf.javadev.popularmovies.data.MovieContract;

public class MovieService extends IntentService implements ServiceColumn {
    private static final String KEY_URI = "URI";
    private static final String KEY_RELOAD = "RELOAD";
    private static final String SORTING_ORDER = "sort_order";
    private static final String PAGE = "page";

    public MovieService() {
        super("movie");
    }

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
        boolean isInternetConnection = NetworkUtil.isConnected(getApplicationContext());
        if (isInternetConnection) {
            assert intent != null;
            boolean flagReload = intent.getBooleanExtra(KEY_RELOAD, false);
            String sortingOrder = intent.getStringExtra(SORTING_ORDER);
            String stringUri = intent.getStringExtra(KEY_URI);
            Uri uriTable = Uri.parse(stringUri);
            //remove data, if pull refresh layout
            if (flagReload) {
                this.getContentResolver().delete(uriTable, null, null);
            }

            int nextPage = getCurrentPage(uriTable) + 1;

            Uri uri = Uri.parse(BASE_URL).buildUpon()
                    .appendEncodedPath(sortingOrder)
                    .appendQueryParameter(API_KEY, BuildConfig.MOVIE_API_KEY)
                    .appendQueryParameter(PAGE, String.valueOf(nextPage))
                    .build();

            String jsonStr = HttpHelper.getJsonString(uri);
            updateLocalData(jsonStr, uriTable);
        }
    }

    private void updateLocalData(String jsonStr, Uri uri) {
        final String RESULT_ARRAY = "results";

        try {
            JSONObject jsonObjectMovie = new JSONObject(jsonStr);
            JSONArray jsonArrayMovies = jsonObjectMovie.getJSONArray(RESULT_ARRAY);
            List<ContentValues> contentValuesList = new ArrayList<>(jsonArrayMovies.length());

            for (int i = 0; i < jsonArrayMovies.length(); i++) {
                JSONObject object = jsonArrayMovies.getJSONObject(i);
                int movieId = object.getInt(MovieContract.BaseEntry.COLUMN_MOVIES_ID);
                String moviePath = object.getString(MovieContract.BaseEntry.COLUMN_POSTER_PATH);

                ContentValues contentValues = new ContentValues();
                contentValues.put(MovieContract.BaseEntry.COLUMN_MOVIES_ID, movieId);
                contentValues.put(MovieContract.BaseEntry.COLUMN_POSTER_PATH, moviePath);
                contentValuesList.add(contentValues);
            }

            if (contentValuesList.size() > 0) {
                ContentValues[] contentValues = new ContentValues[contentValuesList.size()];
                contentValuesList.toArray(contentValues);
                this.getContentResolver()
                        .bulkInsert(uri, contentValues);
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private int getCurrentPage(Uri uri) {
        int page = 0;
        final int PAGE_SIZE = 20;
        Cursor cursor = null;
        try {
            cursor = this.getContentResolver().query(uri, null, null, null, null);
            page = (cursor != null ? cursor.getCount() / PAGE_SIZE : 1);
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        return page;
    }
}
