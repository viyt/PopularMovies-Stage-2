package cf.javadev.popularmovies.service;


import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import cf.javadev.popularmovies.R;
import cf.javadev.popularmovies.data.MovieContract;

public class NetworkReceiver extends BroadcastReceiver {
    private static final String KEY_RELOAD = "RELOAD";
    private static final String SORTING_ORDER = "sort_order";
    private static final String SORT_POPULAR = "popular";
    private static final String SORT_TOP = "top_rated";
    private static final String KEY_URI = "URI";

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getExtras() != null
                && "android.net.conn.CONNECTIVITY_CHANGE".equals(intent.getAction())) {
            if (NetworkUtil.isConnected(context.getApplicationContext())) {
                SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
                final String sortingOrder = preferences.getString(SORTING_ORDER,
                        context.getString(R.string.pref_sort_popular_value));
                Intent movieIntent = new Intent(context, MovieService.class);
                intent.putExtra(KEY_RELOAD, false);
                switch (sortingOrder) {
                    case SORT_POPULAR:
                        movieIntent.putExtra(SORTING_ORDER, SORT_POPULAR);
                        movieIntent.putExtra(KEY_URI, MovieContract.PopularMovieEntry.CONTENT_URI.toString());
                        context.startService(movieIntent);
                        break;
                    case SORT_TOP:
                        movieIntent.putExtra(SORTING_ORDER, SORT_TOP);
                        movieIntent.putExtra(KEY_URI, MovieContract.TopMovieEntry.CONTENT_URI.toString());
                        context.startService(movieIntent);
                        break;
                    default:
                        break;
                }
            }
        }
    }
}
