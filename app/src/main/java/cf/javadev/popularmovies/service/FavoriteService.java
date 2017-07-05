package cf.javadev.popularmovies.service;

import android.app.IntentService;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.support.annotation.Nullable;

import cf.javadev.popularmovies.data.MovieContract;

public class FavoriteService extends IntentService implements ServiceColumn {
    private static final String KEY_MOVIE_ID = "MOVIE_ID";

    private static final String[] DETAIL_PROJECTION = {
            MovieContract.DetailMovieEntry.COLUMN_MOVIES_ID,
            MovieContract.DetailMovieEntry.COLUMN_IS_FAVORITE

    };

    private static final int COLUMN_IS_FAVORITE = 1;

    public FavoriteService() {
        super("favorite");
    }

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
        assert intent != null;
        int movieId = intent.getIntExtra(KEY_MOVIE_ID, 0);
        Uri uri = getUriWithId(movieId);
        int status = getFavoriteStatus(uri);
        int newStatus = swap(status);
        ContentValues values = new ContentValues();
        values.put(MovieContract.DetailMovieEntry.COLUMN_IS_FAVORITE, newStatus);
        this.getContentResolver().update(uri, values, null, null);
    }

    private int swap(int status) {
        if (status == 0) {
            return 1;
        } else {
            return 0;
        }
    }

    private Uri getUriWithId(int movieId) {
        return MovieContract.DetailMovieEntry.buildDetailUriWithId(movieId);
    }

    private int getFavoriteStatus(Uri uri) {
        Cursor cursor = null;
        int status;
        try {
            cursor = this.getContentResolver().query(
                    uri,
                    DETAIL_PROJECTION,
                    null,
                    null,
                    null);
            assert cursor != null;
            cursor.moveToFirst();
            cursor.getColumnCount();
            status = cursor.getInt(COLUMN_IS_FAVORITE);
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        return status;
    }
}
