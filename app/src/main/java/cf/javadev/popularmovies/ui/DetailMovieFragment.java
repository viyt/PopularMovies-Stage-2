package cf.javadev.popularmovies.ui;

import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import cf.javadev.popularmovies.R;
import cf.javadev.popularmovies.data.MovieContract;
import cf.javadev.popularmovies.model.Review;
import cf.javadev.popularmovies.model.Trailer;
import cf.javadev.popularmovies.service.DetailMovieService;
import cf.javadev.popularmovies.service.FavoriteService;

@SuppressWarnings("WeakerAccess")
public class DetailMovieFragment extends Fragment
        implements LoaderManager.LoaderCallbacks<Cursor> {
    private static final String KEY_MOVIE_ID = "MOVIE_ID";
    private final static String BASE_POSTER_URL = "https://image.tmdb.org/t/p/";
    private final static String IMAGE_SIZE = "w185";
    private final static String IMAGE_BACK_SIZE = "w500";
    private static final String YOUTUBE_APP_PACKAGE = "com.google.android.youtube";
    private static final String YOUTUBE_URL_APP = "vnd.youtube://";
    private static final String YOUTUBE_URL_BROWSER = "https://www.youtube.com/watch";
    private static final String VIDEO_PARAMETER = "v";
    private static final int LOADER_ID = 1;
    private static final String[] DETAIL_PROJECTION = {
            MovieContract.DetailMovieEntry._ID,
            MovieContract.DetailMovieEntry.COLUMN_MOVIES_ID,
            MovieContract.DetailMovieEntry.COLUMN_POSTER_PATH,
            MovieContract.DetailMovieEntry.COLUMN_BACKDROP_PATH,
            MovieContract.DetailMovieEntry.COLUMN_ORIGINAL_TITLE,
            MovieContract.DetailMovieEntry.COLUMN_OVERVIEW,
            MovieContract.DetailMovieEntry.COLUMN_POPULARITY,
            MovieContract.DetailMovieEntry.COLUMN_VOTE_AVERAGE,
            MovieContract.DetailMovieEntry.COLUMN_RUNTIME,
            MovieContract.DetailMovieEntry.COLUMN_RELEASE_DATE,
            MovieContract.DetailMovieEntry.COLUMN_IS_FAVORITE,
            MovieContract.DetailMovieEntry.COLUMN_VIDEOS,
            MovieContract.DetailMovieEntry.COLUMN_REVIEWS
    };
    private static final int COLUMN_POSTER_PATH = 2;
    private static final int COLUMN_BACKDROP_PATH = 3;
    private static final int COLUMN_ORIGINAL_TITLE = 4;
    private static final int COLUMN_OVERVIEW = 5;
    private static final int COLUMN_VOTE_AVERAGE = 7;
    private static final int COLUMN_RUNTIME = 8;
    private static final int COLUMN_RELEASE_DATE = 9;
    private static final int COLUMN_IS_FAVORITE = 10;
    private static final int COLUMN_VIDEOS = 11;
    private static final int COLUMN_REVIEWS = 12;

    @BindView(R.id.toolbar_detail)
    Toolbar toolbar;
    @BindView(R.id.toolbar_layout)
    CollapsingToolbarLayout appBarLayout;
    @BindView(R.id.backdrop_image)
    ImageView imageViewBackdrop;
    @BindView(R.id.image_logo)
    ImageView imageViewLogo;
    @BindView(R.id.tv_year)
    TextView textViewYear;
    @BindView(R.id.tv_movie_runtime)
    TextView textViewRuntime;
    @BindView(R.id.tv_rating)
    TextView textViewRating;
    @BindView(R.id.tv_description)
    TextView textViewDescription;
    @BindView(R.id.detail_fab)
    FloatingActionButton actionButton;
    @BindView(R.id.tv_trailers_title)
    TextView trailersTitle;
    @BindView(R.id.recycler_trailers)
    RecyclerView recyclerViewTrailers;
    @BindView(R.id.tv_review_title)
    TextView reviewsTitle;
    @BindView(R.id.recycler_reviews)
    RecyclerView recyclerViewReviews;

    private int movieId;
    private ArrayList<Trailer> trailersList;
    private ArrayList<Review> reviewsList;
    private TrailerAdapter trailerAdapter;
    private ReviewAdapter reviewAdapter;

    public DetailMovieFragment() {
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle arguments = getArguments();
        if (arguments != null) {
            movieId = arguments.getInt(KEY_MOVIE_ID);
        } else {
            movieId = getActivity().getIntent().getIntExtra(KEY_MOVIE_ID, 0);
        }
        trailersList = new ArrayList<>();
        reviewsList = new ArrayList<>();
        trailerAdapter = new TrailerAdapter(getActivity(), trailersList, trailerKey -> {
            if (trailerKey != null) {
                playVideo(trailerKey);
            }
        });

        reviewAdapter = new ReviewAdapter(reviewsList);

        if (getActivity().getClass().equals(MovieActivity.class)) {
            getActivity().getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
        }
    }

    private void playVideo(String trailerKey) {
        Intent intent;
        if (isYouTubeAppInstalled()) {
            intent = new Intent(Intent.ACTION_VIEW, Uri.parse(YOUTUBE_URL_APP + trailerKey));
        } else {
            Uri uri = Uri.parse(YOUTUBE_URL_BROWSER)
                    .buildUpon()
                    .appendQueryParameter(VIDEO_PARAMETER, trailerKey)
                    .build();
            intent = new Intent(Intent.ACTION_VIEW, uri);
        }
        startActivity(intent);
    }

    private boolean isYouTubeAppInstalled() {
        return getActivity().getPackageManager()
                .getLaunchIntentForPackage(YOUTUBE_APP_PACKAGE) != null;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_detail_movie, container, false);
        ButterKnife.bind(this, view);
        recyclerViewTrailers.setLayoutManager(new LinearLayoutManager(getActivity(),
                LinearLayoutManager.HORIZONTAL, false));
        recyclerViewTrailers.setHasFixedSize(true);
        recyclerViewTrailers.setAdapter(trailerAdapter);
        recyclerViewTrailers.setNestedScrollingEnabled(false);

        recyclerViewReviews.setLayoutManager(new CustomLinearLayoutManager(getActivity()));
        recyclerViewReviews.setHasFixedSize(true);
        recyclerViewReviews.setAdapter(reviewAdapter);
        recyclerViewReviews.setNestedScrollingEnabled(false);
        return view;
    }

    @OnClick(R.id.detail_fab)
    public void onClickFabButton() {
        startService(movieId, FavoriteService.class);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        getLoaderManager().initLoader(LOADER_ID, null, this);
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public void onResume() {
        super.onResume();
        AppCompatActivity activity = (AppCompatActivity) getActivity();
        if (!activity.getClass().equals(MovieActivity.class)) {
            activity.setSupportActionBar(toolbar);
            ActionBar actionBar = activity.getSupportActionBar();
            if (actionBar != null) {
                actionBar.setDisplayHomeAsUpEnabled(true);
            }
        }
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        Uri uri = MovieContract.DetailMovieEntry.buildDetailUriWithId(movieId);
        return new CursorLoader(getActivity(),
                uri,
                DETAIL_PROJECTION,
                null,
                null,
                null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        if (data.getCount() == 0) {
            startService(movieId, DetailMovieService.class);
        } else if (data.moveToFirst()) {
            if (appBarLayout != null) {
                appBarLayout.setTitle(data.getString(COLUMN_ORIGINAL_TITLE));
            }
            String backdropPath = data.getString(COLUMN_BACKDROP_PATH);
            loadBackImage(backdropPath);
            String posterPath = data.getString(COLUMN_POSTER_PATH);
            loadLogoImage(posterPath);
            textViewYear.setText(String.format("%.4s", data.getString(COLUMN_RELEASE_DATE)));
            textViewRating.setText(String.format("%s/10", data.getString(COLUMN_VOTE_AVERAGE)));
            textViewRuntime.setText(String.format("%s min", data.getString(COLUMN_RUNTIME)));
            int statusFavorite = data.getInt(COLUMN_IS_FAVORITE);
            setImageResourceOnFabButton(statusFavorite);
            textViewDescription.setText(data.getString(COLUMN_OVERVIEW));
            String videosJsonString = data.getString(COLUMN_VIDEOS);
            updateTrailers(videosJsonString);
            String reviewsJsonString = data.getString(COLUMN_REVIEWS);
            updateReviews(reviewsJsonString);
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        if (getActivity().getClass().equals(DetailMovieActivity.class)) {
            SharedPreferences preferences =
                    PreferenceManager.getDefaultSharedPreferences(getActivity());
            SharedPreferences.Editor editor = preferences.edit();
            editor.putInt(KEY_MOVIE_ID, movieId);
            editor.apply();
            editor.commit();
        }
    }

    @SuppressWarnings("StringBufferReplaceableByString")
    private void loadBackImage(String imagePath) {
        String url = new StringBuilder().append(BASE_POSTER_URL)
                .append(IMAGE_BACK_SIZE).append(imagePath).toString();
        Picasso.with(getActivity())
                .load(url)
                .fit()
                .into(imageViewBackdrop);
    }

    @SuppressWarnings("StringBufferReplaceableByString")
    private void loadLogoImage(String posterPath) {
        String url = new StringBuilder().append(BASE_POSTER_URL)
                .append(IMAGE_SIZE).append(posterPath).toString();
        Picasso.with(getActivity())
                .load(url)
                .into(imageViewLogo);
    }

    private void setImageResourceOnFabButton(int status) {
        if (status == 0) {
            actionButton.setImageResource(R.drawable.ic_favorite_border_white_48dp);
        } else {
            actionButton.setImageResource(R.drawable.ic_favorite_white_48dp);
        }
    }

    private void updateTrailers(String jsonString) {
        final String TRAILER_ID = "id";
        final String TRAILER_NAME = "name";
        final String TRAILER_KEY = "key";
        final String RESULT_ARRAY = "results";
        JSONObject jsonObject;
        JSONArray jsonArray;

        try {
            jsonObject = new JSONObject(jsonString);
            jsonArray = jsonObject.getJSONArray(RESULT_ARRAY);

            trailersList.clear();
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject object = jsonArray.getJSONObject(i);
                trailersList.add(new Trailer(
                        object.getString(TRAILER_ID),
                        object.getString(TRAILER_KEY),
                        object.getString(TRAILER_NAME)));
            }
            trailerAdapter.notifyDataSetChanged();
            visibleTrailersTitle();

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void visibleTrailersTitle() {
        if (trailerAdapter.getItemCount() != 0) {
            trailersTitle.setVisibility(View.VISIBLE);
        } else {
            trailersTitle.setVisibility(View.INVISIBLE);
        }
    }

    private void updateReviews(String jsonString) {
        final String REVIEW_ID = "id";
        final String REVIEW_AUTHOR = "author";
        final String REVIEW_CONTENT = "content";
        final String RESULT_ARRAY = "results";
        JSONObject jsonObject;
        JSONArray jsonArray;

        try {
            jsonObject = new JSONObject(jsonString);
            jsonArray = jsonObject.getJSONArray(RESULT_ARRAY);

            reviewsList.clear();
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject object = jsonArray.getJSONObject(i);
                reviewsList.add(new Review(
                        object.getString(REVIEW_ID),
                        object.getString(REVIEW_AUTHOR),
                        object.getString(REVIEW_CONTENT)));
            }
            reviewAdapter.notifyDataSetChanged();
            visibleReviewsTitle();
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void visibleReviewsTitle() {
        if (reviewAdapter.getItemCount() != 0) {
            reviewsTitle.setVisibility(View.VISIBLE);
        } else {
            reviewsTitle.setVisibility(View.INVISIBLE);
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
    }

    private void startService(int movieId, Class aClass) {
        Intent intent = new Intent(getActivity(), aClass);
        intent.putExtra(KEY_MOVIE_ID, movieId);
        getActivity().startService(intent);
    }
}
