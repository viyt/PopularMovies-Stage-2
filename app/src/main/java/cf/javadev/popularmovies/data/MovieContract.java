package cf.javadev.popularmovies.data;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.net.Uri;
import android.provider.BaseColumns;

public class MovieContract {
    public static final String CONTENT_AUTHORITY = "cf.javadev.popularmovies";
    private static final Uri BASE_CONTENT_URI = Uri.parse("content://" + CONTENT_AUTHORITY);

    private static final String PATH_POPULAR_MOVIES = "popular_movies";
    private static final String PATH_TOP_MOVIES = "top_movies";
    private static final String PATH_DETAIL_MOVIE = "detail_movies";

    public static abstract class BaseEntry implements BaseColumns {
        public static final String COLUMN_MOVIES_ID = "id";
        public static final String COLUMN_POSTER_PATH = "poster_path";
    }

    public static abstract class PopularMovieEntry extends BaseEntry {
        public static final String CONTENT_TYPE =
                ContentResolver.CURSOR_DIR_BASE_TYPE + "/" +
                        CONTENT_AUTHORITY + "/" + PATH_POPULAR_MOVIES;
        public static final Uri CONTENT_URI =
                BASE_CONTENT_URI.buildUpon().appendPath(PATH_POPULAR_MOVIES).build();

        public static final String TABLE_POPULAR_MOVIE = "popular_movies";

    }

    public static abstract class TopMovieEntry extends BaseEntry {
        public static final String CONTENT_TYPE =
                ContentResolver.CURSOR_DIR_BASE_TYPE + "/" +
                        CONTENT_AUTHORITY + "/" + PATH_TOP_MOVIES;
        public static final Uri CONTENT_URI =
                BASE_CONTENT_URI.buildUpon().appendPath(PATH_TOP_MOVIES).build();

        public static final String TABLE_TOP_MOVIE = "top_movies";
    }

    public static abstract class DetailMovieEntry extends BaseEntry {
        public static final String CONTENT_TYPE =
                ContentResolver.CURSOR_DIR_BASE_TYPE + "/" +
                        CONTENT_AUTHORITY + "/" + PATH_DETAIL_MOVIE;
        public static final String CONTENT_ITEM_TYPE =
                ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" +
                        CONTENT_AUTHORITY + "/" + PATH_DETAIL_MOVIE;
        public static final Uri CONTENT_URI =
                BASE_CONTENT_URI.buildUpon().appendPath(PATH_DETAIL_MOVIE).build();

        public static Uri buildDetailUriWithId(int id) {
            return ContentUris.withAppendedId(CONTENT_URI, id);
        }

        public static final String TABLE_DETAIL_MOVIE = "detail_movie";
        public static final String COLUMN_BACKDROP_PATH = "backdrop_path";
        public static final String COLUMN_ORIGINAL_TITLE = "original_title";
        public static final String COLUMN_OVERVIEW = "overview";
        public static final String COLUMN_POPULARITY = "popularity";
        public static final String COLUMN_VOTE_AVERAGE = "vote_average";
        public static final String COLUMN_RUNTIME = "runtime";
        public static final String COLUMN_RELEASE_DATE = "release_date";
        public static final String COLUMN_IS_FAVORITE = "favorite";
        public static final String COLUMN_VIDEOS= "videos";
        public static final String COLUMN_REVIEWS= "reviews";
    }
}
