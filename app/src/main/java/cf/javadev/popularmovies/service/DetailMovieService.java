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

import cf.javadev.popularmovies.BuildConfig;
import cf.javadev.popularmovies.data.MovieContract;


public class DetailMovieService extends IntentService implements ServiceColumn {
    private static final String KEY_MOVIE_ID = "MOVIE_ID";

    private static final String[] DETAIL_PROJECTION = {
            MovieContract.DetailMovieEntry.COLUMN_MOVIES_ID
    };

    public DetailMovieService() {
        super("detail_movie");
    }

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
        boolean isInternetConnection = NetworkUtil.isConnected(getApplicationContext());
        if (isInternetConnection) {
            assert intent != null;
            int movieId = intent.getIntExtra(KEY_MOVIE_ID, 0);
            if (movieId != 0) {

                Uri uriDetail = Uri.parse(BASE_URL).buildUpon()
                        .appendEncodedPath(String.valueOf(movieId))
                        .appendQueryParameter(API_KEY, BuildConfig.MOVIE_API_KEY)
                        .build();

                String TYPE_VIDEOS = "videos";
                Uri uriVideos = Uri.parse(BASE_URL).buildUpon()
                        .appendEncodedPath(String.valueOf(movieId))
                        .appendEncodedPath(TYPE_VIDEOS)
                        .appendQueryParameter(API_KEY, BuildConfig.MOVIE_API_KEY)
                        .build();

                String jsonStrDetail = HttpHelper.getJsonString(uriDetail);
                String stringVideos = HttpHelper.getJsonString(uriVideos);
                String mergeStringReviews = getMergeJsonString(movieId);
                updateLocalData(jsonStrDetail, stringVideos, mergeStringReviews);
            }
        }
    }

    private Uri getUriWithId(int movieId) {
        return MovieContract.DetailMovieEntry.buildDetailUriWithId(movieId);
    }

    private String getMergeJsonString(int movieId) {
        final String REVIEW_ID = "id";
        final String TOTAL_PAGES = "total_pages";
        final String RESULT_ARRAY = "results";

        String reviewJsonString = null;
        JSONObject reviewJsonObject;
        try {
            //Default page = 1
            Uri uriTrailer = getReviewUriWithPage(1, movieId);
            String jsonTrailerString = HttpHelper.getJsonString(uriTrailer);
            JSONObject objectTrailer = new JSONObject(jsonTrailerString);
            int totalPage = objectTrailer.getInt(TOTAL_PAGES);
            if (totalPage == 1) {
                reviewJsonString = jsonTrailerString;
            } else {
                reviewJsonObject = new JSONObject();
                reviewJsonObject.put(REVIEW_ID, objectTrailer.getString(REVIEW_ID));
                JSONArray reviewJsonArray = new JSONArray();

                for (int i = 1; i <= totalPage; i++) {
                    JSONObject newObject;
                    if (i == 1) {
                        newObject = objectTrailer;
                    } else {
                        Uri uri = getReviewUriWithPage(i, movieId);
                        String newJsonString = HttpHelper.getJsonString(uri);
                        newObject = new JSONObject(newJsonString);
                    }

                    JSONArray jsonArrayFromNewObject = newObject.getJSONArray(RESULT_ARRAY);
                    for (int j = 0; j < jsonArrayFromNewObject.length(); j++) {
                        JSONObject object = jsonArrayFromNewObject.getJSONObject(j);
                        reviewJsonArray.put(object);
                    }
                }
                reviewJsonObject.put(RESULT_ARRAY, reviewJsonArray);
                reviewJsonString = reviewJsonObject.toString();
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return reviewJsonString;
    }

    private Uri getReviewUriWithPage(int page, int movieId) {
        final String TYPE_REVIEWS = "reviews";
        final String PAGE = "page";
        return Uri.parse(BASE_URL).buildUpon()
                .appendEncodedPath(String.valueOf(movieId))
                .appendEncodedPath(TYPE_REVIEWS)
                .appendQueryParameter(API_KEY, BuildConfig.MOVIE_API_KEY)
                .appendQueryParameter(PAGE, String.valueOf(page))
                .build();
    }

    private void updateLocalData(String jsonStr, String videos, String reviews) {
        final int FAVORITE_DEFAULT_VALUE = 0;
        try {
            JSONObject object = new JSONObject(jsonStr);
            int movieId = object.getInt(MovieContract.BaseEntry.COLUMN_MOVIES_ID);
            String moviePath = object.getString(MovieContract.BaseEntry.COLUMN_POSTER_PATH);
            String backDropPath = object.getString(MovieContract.DetailMovieEntry.COLUMN_BACKDROP_PATH);
            String title = object.getString(MovieContract.DetailMovieEntry.COLUMN_ORIGINAL_TITLE);
            String overview = object.getString(MovieContract.DetailMovieEntry.COLUMN_OVERVIEW);
            String popularity = object.getString(MovieContract.DetailMovieEntry.COLUMN_POPULARITY);
            String voteAverage = object.getString(MovieContract.DetailMovieEntry.COLUMN_VOTE_AVERAGE);
            String runtime = object.getString(MovieContract.DetailMovieEntry.COLUMN_RUNTIME);
            String releaseDate = object.getString(MovieContract.DetailMovieEntry.COLUMN_RELEASE_DATE);

            ContentValues values = new ContentValues();
            values.put(MovieContract.BaseEntry.COLUMN_MOVIES_ID, movieId);
            values.put(MovieContract.BaseEntry.COLUMN_POSTER_PATH, moviePath);
            values.put(MovieContract.DetailMovieEntry.COLUMN_BACKDROP_PATH, backDropPath);
            values.put(MovieContract.DetailMovieEntry.COLUMN_ORIGINAL_TITLE, title);
            values.put(MovieContract.DetailMovieEntry.COLUMN_OVERVIEW, overview);
            values.put(MovieContract.DetailMovieEntry.COLUMN_POPULARITY, popularity);
            values.put(MovieContract.DetailMovieEntry.COLUMN_VOTE_AVERAGE, voteAverage);
            values.put(MovieContract.DetailMovieEntry.COLUMN_RUNTIME, runtime);
            values.put(MovieContract.DetailMovieEntry.COLUMN_RELEASE_DATE, releaseDate);
            values.put(MovieContract.DetailMovieEntry.COLUMN_IS_FAVORITE, FAVORITE_DEFAULT_VALUE);
            values.put(MovieContract.DetailMovieEntry.COLUMN_VIDEOS, videos);
            values.put(MovieContract.DetailMovieEntry.COLUMN_REVIEWS, reviews);

            Uri uriWithId = getUriWithId(movieId);
            boolean isMovieMatching = matchMovie(uriWithId);
            if (isMovieMatching) {
                this.getContentResolver().update(uriWithId, values, null, null);
            } else {
                this.getContentResolver().insert(MovieContract.DetailMovieEntry.CONTENT_URI, values);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private boolean matchMovie(Uri uri) {
        Cursor cursor = null;
        boolean match;
        try {
            cursor = this.getContentResolver().query(
                    uri,
                    DETAIL_PROJECTION,
                    null,
                    null,
                    null);
            assert cursor != null;
            match = cursor.getCount() > 0;

        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        return match;
    }
}
