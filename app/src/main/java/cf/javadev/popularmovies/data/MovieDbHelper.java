package cf.javadev.popularmovies.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

class MovieDbHelper extends SQLiteOpenHelper {
    private static final int DATABASE_VERSION = 1;
    private static final String DATABASE_NAME = "movie.db";
    private static final String TEXT_NOT_NULL = " TEXT NOT NULL, ";
    private static final String INTEGER_NOT_NULL = " INTEGER NOT NULL, ";

    MovieDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        final String SQL_CREATE_POPULAR_TABLE = "CREATE TABLE " +
                MovieContract.PopularMovieEntry.TABLE_POPULAR_MOVIE + "(" +
                MovieContract.PopularMovieEntry._ID + " INTEGER PRIMARY KEY, " +
                MovieContract.PopularMovieEntry.COLUMN_MOVIES_ID + INTEGER_NOT_NULL +
                MovieContract.PopularMovieEntry.COLUMN_POSTER_PATH + " TEXT NOT NULL);";

        final String SQL_CREATE_TOP_TABLE = "CREATE TABLE " +
                MovieContract.TopMovieEntry.TABLE_TOP_MOVIE + "(" +
                MovieContract.TopMovieEntry._ID + " INTEGER PRIMARY KEY, " +
                MovieContract.TopMovieEntry.COLUMN_MOVIES_ID + INTEGER_NOT_NULL +
                MovieContract.TopMovieEntry.COLUMN_POSTER_PATH + " TEXT NOT NULL);";

        final String SQL_CREATE_DETAIL_TABLE = "CREATE TABLE " +
                MovieContract.DetailMovieEntry.TABLE_DETAIL_MOVIE + "(" +
                MovieContract.DetailMovieEntry._ID + " INTEGER PRIMARY KEY, " +
                MovieContract.DetailMovieEntry.COLUMN_MOVIES_ID + INTEGER_NOT_NULL +
                MovieContract.DetailMovieEntry.COLUMN_POSTER_PATH + TEXT_NOT_NULL +
                MovieContract.DetailMovieEntry.COLUMN_BACKDROP_PATH + TEXT_NOT_NULL +
                MovieContract.DetailMovieEntry.COLUMN_ORIGINAL_TITLE + TEXT_NOT_NULL +
                MovieContract.DetailMovieEntry.COLUMN_OVERVIEW + TEXT_NOT_NULL +
                MovieContract.DetailMovieEntry.COLUMN_POPULARITY + TEXT_NOT_NULL +
                MovieContract.DetailMovieEntry.COLUMN_VOTE_AVERAGE + TEXT_NOT_NULL +
                MovieContract.DetailMovieEntry.COLUMN_RUNTIME + TEXT_NOT_NULL +
                MovieContract.DetailMovieEntry.COLUMN_RELEASE_DATE + TEXT_NOT_NULL +
                MovieContract.DetailMovieEntry.COLUMN_IS_FAVORITE + INTEGER_NOT_NULL +
                MovieContract.DetailMovieEntry.COLUMN_VIDEOS + TEXT_NOT_NULL +
                MovieContract.DetailMovieEntry.COLUMN_REVIEWS + " TEXT NOT NULL);";

        db.execSQL(SQL_CREATE_POPULAR_TABLE);
        db.execSQL(SQL_CREATE_TOP_TABLE);
        db.execSQL(SQL_CREATE_DETAIL_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int i, int i1) {
        db.execSQL("DROP TABLE IF EXISTS " +
                MovieContract.PopularMovieEntry.TABLE_POPULAR_MOVIE);
        db.execSQL("DROP TABLE IF EXISTS " +
                MovieContract.TopMovieEntry.TABLE_TOP_MOVIE);
        db.execSQL("DROP TABLE IF EXISTS " +
                MovieContract.DetailMovieEntry.TABLE_DETAIL_MOVIE);
        onCreate(db);
    }
}