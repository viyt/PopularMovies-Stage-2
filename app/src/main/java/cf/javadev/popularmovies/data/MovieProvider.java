package cf.javadev.popularmovies.data;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.support.annotation.NonNull;

public class MovieProvider extends ContentProvider {
    private MovieDbHelper movieDbHelper;
    private static final String AUTHORITY = MovieContract.CONTENT_AUTHORITY;

    private static final int POPULAR_MOVIES = 1;
    private static final int TOP_MOVIES = 2;
    private static final int DETAIL_MOVIES = 3;
    private static final int DETAIL_MOVIES_ID = 4;

    private static final UriMatcher uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);

    static {
        uriMatcher.addURI(AUTHORITY, "popular_movies", POPULAR_MOVIES);
        uriMatcher.addURI(AUTHORITY, "top_movies", TOP_MOVIES);
        uriMatcher.addURI(AUTHORITY, "detail_movies", DETAIL_MOVIES);
        uriMatcher.addURI(AUTHORITY, "detail_movies/#", DETAIL_MOVIES_ID);
    }

    @Override
    public boolean onCreate() {
        movieDbHelper = new MovieDbHelper(getContext());
        return true;
    }

    @Override
    public String getType(@NonNull Uri uri) {
        final int match = uriMatcher.match(uri);
        switch (match) {
            case POPULAR_MOVIES:
                return MovieContract.PopularMovieEntry.CONTENT_TYPE;
            case TOP_MOVIES:
                return MovieContract.TopMovieEntry.CONTENT_TYPE;
            case DETAIL_MOVIES:
                return MovieContract.DetailMovieEntry.CONTENT_TYPE;
            case DETAIL_MOVIES_ID:
                return MovieContract.DetailMovieEntry.CONTENT_ITEM_TYPE;
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
    }

    @Override
    public Cursor query(@NonNull Uri uri, String[] projection, String selection,
                        String[] selectionArgs, String sortOrder) {
        final SQLiteDatabase db = movieDbHelper.getReadableDatabase();
        Context context = getContext();
        final int match = uriMatcher.match(uri);
        Cursor cursor;

        switch (match) {
            case POPULAR_MOVIES:
                cursor = db.query(MovieContract.PopularMovieEntry.TABLE_POPULAR_MOVIE,
                        projection, selection, selectionArgs, null, null, sortOrder);
                break;
            case TOP_MOVIES:
                cursor = db.query(MovieContract.TopMovieEntry.TABLE_TOP_MOVIE,
                        projection, selection, selectionArgs, null, null, sortOrder);
                break;
            case DETAIL_MOVIES:
                cursor = db.query(MovieContract.DetailMovieEntry.TABLE_DETAIL_MOVIE,
                        projection, selection, selectionArgs, null, null, sortOrder);
                break;
            case DETAIL_MOVIES_ID:
                selection = MovieContract.DetailMovieEntry.COLUMN_MOVIES_ID + "=?";
                selectionArgs = new String[]{String.valueOf(ContentUris.parseId(uri))};
                cursor = db.query(MovieContract.DetailMovieEntry.TABLE_DETAIL_MOVIE,
                        projection, selection, selectionArgs, null, null, sortOrder);
                break;
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
        assert context != null;
        cursor.setNotificationUri(context.getContentResolver(), uri);
        return cursor;
    }

    @Override
    public Uri insert(@NonNull Uri uri, ContentValues values) {
        final SQLiteDatabase db = movieDbHelper.getWritableDatabase();
        final int match = uriMatcher.match(uri);
        Uri result;
        long id;
        switch (match) {
            case POPULAR_MOVIES:
                id = db.insertOrThrow(MovieContract.PopularMovieEntry.TABLE_POPULAR_MOVIE,
                        null, values);
                if (id == -1) {
                    return null;
                }
                result = Uri.parse(MovieContract.PopularMovieEntry.CONTENT_URI + "/" + id);
                break;
            case TOP_MOVIES:
                id = db.insertOrThrow(MovieContract.TopMovieEntry.TABLE_TOP_MOVIE,
                        null, values);
                if (id == -1) {
                    return null;
                }
                result = Uri.parse(MovieContract.TopMovieEntry.CONTENT_URI + "/" + id);
                break;
            case DETAIL_MOVIES:
                id = db.insertOrThrow(MovieContract.DetailMovieEntry.TABLE_DETAIL_MOVIE,
                        null, values);
                if (id == -1) {
                    return null;
                }
                result = Uri.parse(MovieContract.DetailMovieEntry.CONTENT_URI + "/" + id);
                break;
            case DETAIL_MOVIES_ID:
                throw new UnsupportedOperationException("Insert not supported on URI: " + uri);
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
        Context context = getContext();
        assert context != null;
        context.getContentResolver().notifyChange(uri, null, false);
        return result;
    }

    @Override
    public int delete(@NonNull Uri uri, String selection, String[] selectionArgs) {
        final SQLiteDatabase db = movieDbHelper.getWritableDatabase();
        final int match = uriMatcher.match(uri);
        int count;
        switch (match) {
            case POPULAR_MOVIES:
                count = db.delete(MovieContract.PopularMovieEntry.TABLE_POPULAR_MOVIE,
                        selection, selectionArgs);
                break;
            case TOP_MOVIES:
                count = db.delete(MovieContract.TopMovieEntry.TABLE_TOP_MOVIE,
                        selection, selectionArgs);
                break;
            case DETAIL_MOVIES:
                count = db.delete(MovieContract.DetailMovieEntry.TABLE_DETAIL_MOVIE,
                        selection, selectionArgs);
                break;
            case DETAIL_MOVIES_ID:
                selection = MovieContract.DetailMovieEntry.COLUMN_MOVIES_ID + "=?";
                selectionArgs = new String[]{String.valueOf(ContentUris.parseId(uri))};
                count = db.delete(MovieContract.DetailMovieEntry.TABLE_DETAIL_MOVIE,
                        selection, selectionArgs);
                break;
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
        Context context = getContext();
        assert context != null;
        context.getContentResolver().notifyChange(uri, null, false);
        return count;
    }

    @Override
    public int update(@NonNull Uri uri, ContentValues contentValues, String selection,
                      String[] selectionArgs) {
        final SQLiteDatabase db = movieDbHelper.getWritableDatabase();
        final int match = uriMatcher.match(uri);
        int count;
        switch (match) {
            case POPULAR_MOVIES:
                count = db.update(MovieContract.PopularMovieEntry.TABLE_POPULAR_MOVIE,
                        contentValues, selection, selectionArgs);
                break;
            case TOP_MOVIES:
                count = db.update(MovieContract.TopMovieEntry.TABLE_TOP_MOVIE,
                        contentValues, selection, selectionArgs);
                break;
            case DETAIL_MOVIES:
                count = db.update(MovieContract.DetailMovieEntry.TABLE_DETAIL_MOVIE,
                        contentValues, selection, selectionArgs);
                break;
            case DETAIL_MOVIES_ID:
                selection = MovieContract.DetailMovieEntry.COLUMN_MOVIES_ID + "=?";
                selectionArgs = new String[]{String.valueOf(ContentUris.parseId(uri))};
                count = db.update(MovieContract.DetailMovieEntry.TABLE_DETAIL_MOVIE,
                        contentValues, selection, selectionArgs);
                break;
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
        Context context = getContext();
        assert context != null;
        context.getContentResolver().notifyChange(uri, null, false);
        return count;
    }

    @Override
    public int bulkInsert(@NonNull Uri uri, @NonNull ContentValues[] values) {
        final SQLiteDatabase db = movieDbHelper.getWritableDatabase();
        final int match = uriMatcher.match(uri);
        int count = 0;
        switch (match) {
            case POPULAR_MOVIES:
                db.beginTransaction();
                try {
                    for (ContentValues value : values) {
                        long id = db.insert(MovieContract.PopularMovieEntry.TABLE_POPULAR_MOVIE,
                                null, value);
                        if (id != -1) {
                            count++;
                        }
                    }
                    db.setTransactionSuccessful();
                } finally {
                    db.endTransaction();
                }
                break;
            case TOP_MOVIES:
                db.beginTransaction();
                try {
                    for (ContentValues value : values) {
                        long id = db.insert(MovieContract.TopMovieEntry.TABLE_TOP_MOVIE,
                                null, value);
                        if (id != -1) {
                            count++;
                        }
                    }
                    db.setTransactionSuccessful();
                } finally {
                    db.endTransaction();
                }
                break;
            default:
                return super.bulkInsert(uri, values);
        }
        Context context = getContext();
        assert context != null;
        context.getContentResolver().notifyChange(uri, null, false);
        return count;
    }
}
