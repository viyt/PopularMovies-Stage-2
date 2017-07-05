package cf.javadev.popularmovies.ui;

import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import butterknife.BindView;
import butterknife.ButterKnife;
import cf.javadev.popularmovies.R;
import cf.javadev.popularmovies.data.MovieContract;
import cf.javadev.popularmovies.service.MovieService;


@SuppressWarnings("WeakerAccess")
public class MovieActivityFragment extends Fragment {
    private static final int LOADER_ID = 0;
    private static final String SORTING_ORDER = "sort_order";
    private static final String SORT_POPULAR = "popular";
    private static final String SORT_TOP = "top_rated";
    private static final String SORT_FAVORITE = "favorite";
    private static final String KEY_RECYCLER_POSITION = "RECYCLER_POSITION";
    private static final String KEY_RELOAD = "RELOAD";
    private static final String KEY_URI = "URI";

    private static final String[] POPULAR_PROJECTION = {
            MovieContract.PopularMovieEntry._ID,
            MovieContract.PopularMovieEntry.COLUMN_MOVIES_ID,
            MovieContract.PopularMovieEntry.COLUMN_POSTER_PATH
    };

    private static final String[] TOP_RATING_PROJECTION = {
            MovieContract.TopMovieEntry._ID,
            MovieContract.TopMovieEntry.COLUMN_MOVIES_ID,
            MovieContract.TopMovieEntry.COLUMN_POSTER_PATH
    };

    private static final String[] DETAIL_PROJECTION = {
            MovieContract.DetailMovieEntry._ID,
            MovieContract.DetailMovieEntry.COLUMN_MOVIES_ID,
            MovieContract.DetailMovieEntry.COLUMN_POSTER_PATH
    };

    static final int COLUMN_MOVIES_ID = 1;
    static final int COLUMN_POSTER_PATH = 2;

    @BindView(R.id.recycler_movie)
    RecyclerView recyclerViewPoster;
    @BindView(R.id.swipe_layout)
    SwipeRefreshLayout swipeRefreshLayout;
    @BindView(R.id.fab)
    FloatingActionButton floatingActionButton;

    private MovieAdapter movieAdapter;
    private SharedPreferences preferences;
    private GridLayoutManager gridLayoutManager;
    private EndlessRecyclerViewScrollListener scrollListener;
    private int currentVisiblePosition = 0;

    public MovieActivityFragment() {
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        preferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
        if (savedInstanceState != null) {
            currentVisiblePosition = savedInstanceState.getInt(KEY_RECYCLER_POSITION);
        }
        gridLayoutManager = new GridLayoutManager(getActivity(), getColumnCount());
        movieAdapter = new MovieAdapter(getActivity(),
                movieId -> ((MovieAdapterCallback) getActivity()).onItemClick(movieId));
        scrollListener = new EndlessRecyclerViewScrollListener(gridLayoutManager) {
            @Override
            public void onLoadMore() {
                startMovieService(false);
            }

            @Override
            public void enableFabButton(boolean enabled) {
                if (enabled) {
                    if (floatingActionButton.getVisibility() != View.VISIBLE) {
                        floatingActionButton.setVisibility(View.VISIBLE);
                    }
                } else {
                    if (floatingActionButton.getVisibility() == View.VISIBLE) {
                        floatingActionButton.setVisibility(View.GONE);
                    }
                }
            }
        };
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_movie, container, false);
        ButterKnife.bind(this, view);

        swipeRefreshLayout.setColorSchemeResources(
                R.color.swipe_color_1, R.color.swipe_color_2,
                R.color.swipe_color_3, R.color.swipe_color_4);
        swipeRefreshLayout.setOnRefreshListener(()
                -> startMovieService(true));

        recyclerViewPoster.setLayoutManager(gridLayoutManager);
        recyclerViewPoster.setHasFixedSize(true);
        recyclerViewPoster.setAdapter(movieAdapter);
        recyclerViewPoster.addOnScrollListener(scrollListener);

        floatingActionButton.setOnClickListener(v -> {
            recyclerViewPoster.scrollToPosition(0);
            currentVisiblePosition = 0;
        });
        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        // FIXME: 27.01.17 bundle always == null
        runCallbackSelector();
        super.onActivityCreated(savedInstanceState);
    }

    private void runCallbackSelector() {
        String sortingOrder = preferences.getString(SORTING_ORDER,
                getActivity().getString(R.string.pref_sort_popular_value));
        if (getLoaderManager().getLoader(LOADER_ID) != null) {
            getLoaderManager().getLoader(LOADER_ID).cancelLoad();
            getLoaderManager().destroyLoader(LOADER_ID);
        }

        switch (sortingOrder) {
            case SORT_POPULAR:
                getLoaderManager().initLoader(LOADER_ID, null, popularLoaderCallback);
                setSubtitleOnActivity(R.string.subtitle_popular);
                break;
            case SORT_TOP:
                getLoaderManager().initLoader(LOADER_ID, null, topLoaderCallback);
                setSubtitleOnActivity(R.string.subtitle_top);
                break;
            case SORT_FAVORITE:
                getLoaderManager().initLoader(LOADER_ID, null, favoriteLoaderCallback);
                setSubtitleOnActivity(R.string.subtitle_favorite);
                break;
            default:
                break;
        }
    }

    private int getColumnCount() {
        DisplayMetrics displaymetrics = new DisplayMetrics();
        getActivity().getWindowManager().getDefaultDisplay().getMetrics(displaymetrics);
        int rootContainerWidth = displaymetrics.widthPixels;
        int imageWidth = getActivity().getResources()
                .getDimensionPixelSize(R.dimen.default_image_width);
        return rootContainerWidth / imageWidth;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        recyclerViewPoster.clearOnScrollListeners();
        scrollListener = null;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_most_popular) {
            saveChangeSortOrder(R.string.pref_sort_popular_value);
            runCallbackSelector();
            return true;
        } else if (id == R.id.action_top_rating) {
            saveChangeSortOrder(R.string.pref_sort_rating_value);
            runCallbackSelector();
            return true;
        } else if (id == R.id.action_favorite) {
            saveChangeSortOrder(R.string.pref_sort_favorite_value);
            runCallbackSelector();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void setSubtitleOnActivity(int resId) {
        ActionBar actionBar = ((AppCompatActivity) getActivity()).getSupportActionBar();
        if (actionBar != null) {
            actionBar.setSubtitle(getString(resId));
        }
    }

    private void saveChangeSortOrder(int sortOrder) {
        String orderKey = getResources().getString(sortOrder);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString(SORTING_ORDER, orderKey);
        editor.apply();
        editor.commit();
    }

    private final LoaderManager.LoaderCallbacks<Cursor> popularLoaderCallback
            = new LoaderManager.LoaderCallbacks<Cursor>() {
        @Override
        public Loader<Cursor> onCreateLoader(int id, Bundle args) {
            return new CursorLoader(getActivity(),
                    MovieContract.PopularMovieEntry.CONTENT_URI,
                    POPULAR_PROJECTION, null, null, null);
        }

        @Override
        public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
            if (data.getCount() == 0) {
                startMovieService(false);
            }
            movieAdapter.swapCursor(data);
            onFinishedLoad();
        }

        @Override
        public void onLoaderReset(Loader<Cursor> loader) {
            movieAdapter.swapCursor(null);
        }
    };

    private final LoaderManager.LoaderCallbacks<Cursor> topLoaderCallback
            = new LoaderManager.LoaderCallbacks<Cursor>() {
        @Override
        public Loader<Cursor> onCreateLoader(int id, Bundle args) {
            return new CursorLoader(getActivity(),
                    MovieContract.TopMovieEntry.CONTENT_URI,
                    TOP_RATING_PROJECTION, null, null, null);
        }

        @Override
        public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
            if (data.getCount() == 0) {
                startMovieService(false);
            }
            movieAdapter.swapCursor(data);
            onFinishedLoad();
        }

        @Override
        public void onLoaderReset(Loader<Cursor> loader) {
            movieAdapter.swapCursor(null);
        }
    };

    private final LoaderManager.LoaderCallbacks<Cursor> favoriteLoaderCallback
            = new LoaderManager.LoaderCallbacks<Cursor>() {
        @Override
        public Loader<Cursor> onCreateLoader(int id, Bundle args) {
            String selection = MovieContract.DetailMovieEntry.COLUMN_IS_FAVORITE + "=?";
            String[] selectionArgs = {"1"};
            return new CursorLoader(getActivity(),
                    MovieContract.DetailMovieEntry.CONTENT_URI,
                    DETAIL_PROJECTION,
                    selection,
                    selectionArgs,
                    null);
        }

        @Override
        public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
            movieAdapter.swapCursor(data);
            onFinishedLoad();
        }

        @Override
        public void onLoaderReset(Loader<Cursor> loader) {
            movieAdapter.swapCursor(null);
        }
    };

    /**
     * Scroll adapter on saved position and canceled display progress bar
     */
    private void onFinishedLoad() {
        if (currentVisiblePosition != 0 && movieAdapter.getItemCount() >= currentVisiblePosition) {
            recyclerViewPoster.scrollToPosition(currentVisiblePosition);
        }
        swipeRefreshLayout.setRefreshing(false);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt(KEY_RECYCLER_POSITION, gridLayoutManager.findFirstVisibleItemPosition());
    }

    private void startMovieService(boolean isReload) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
        final String sortingOrder = preferences.getString(SORTING_ORDER,
                getActivity().getString(R.string.pref_sort_popular_value));
        Intent intent = new Intent(getActivity(), MovieService.class);
        intent.putExtra(KEY_RELOAD, isReload);

        switch (sortingOrder) {
            case SORT_POPULAR:
                intent.putExtra(SORTING_ORDER, SORT_POPULAR);
                intent.putExtra(KEY_URI, MovieContract.PopularMovieEntry.CONTENT_URI.toString());
                getActivity().startService(intent);
                break;
            case SORT_TOP:
                intent.putExtra(SORTING_ORDER, SORT_TOP);
                intent.putExtra(KEY_URI, MovieContract.TopMovieEntry.CONTENT_URI.toString());
                getActivity().startService(intent);
                break;
            case SORT_FAVORITE:
                swipeRefreshLayout.setRefreshing(false);
                break;
            default:
                break;
        }
    }
}
