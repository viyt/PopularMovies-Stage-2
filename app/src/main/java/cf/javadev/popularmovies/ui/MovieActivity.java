package cf.javadev.popularmovies.ui;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;

import butterknife.BindView;
import butterknife.ButterKnife;
import cf.javadev.popularmovies.R;
import cf.javadev.popularmovies.service.NetworkUtil;

@SuppressWarnings("WeakerAccess")
public class MovieActivity extends AppCompatActivity implements MovieAdapterCallback {
    private static final String KEY_MOVIE_ID = "MOVIE_ID";
    private static final String DETAIL_MOVIE_FRAGMENT_TAG = "detailMovieFragmentTag";
    private boolean twoPane;
    private SharedPreferences preferences;

    @SuppressWarnings("CanBeFinal")
    @BindView(R.id.toolbar)
    Toolbar toolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_movie);
        //Set null in background. Override background in layout file
        getWindow().setBackgroundDrawable(null);
        ButterKnife.bind(this);
        setSupportActionBar(toolbar);
        twoPane = findViewById(R.id.container_detail) != null;
        preferences = PreferenceManager.getDefaultSharedPreferences(this);
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (!NetworkUtil.isConnected(getApplicationContext())) {
            Snackbar snackbar = Snackbar.make(findViewById(R.id.root_coordinator),
                    R.string.no_internet,
                    Snackbar.LENGTH_LONG);
            snackbar.setAction(R.string.action_hide, v -> {}).show();
        }
        if (twoPane) {
            int movieId = preferences.getInt(KEY_MOVIE_ID, 0);
            if (movieId != 0) {
                SharedPreferences.Editor editor = preferences.edit();
                editor.putInt(KEY_MOVIE_ID, 0);
                editor.apply();
                editor.commit();
                onItemClick(movieId);
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_movie, menu);
        return true;
    }

    @Override
    public void onItemClick(int movieId) {
        if (twoPane) {
            Bundle bundle = new Bundle();
            bundle.putInt(KEY_MOVIE_ID, movieId);
            DetailMovieFragment fragment = new DetailMovieFragment();
            fragment.setArguments(bundle);
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.container_detail, fragment, DETAIL_MOVIE_FRAGMENT_TAG)
                    .commit();
        } else {
            Intent intent = new Intent(this, DetailMovieActivity.class);
            intent.putExtra(KEY_MOVIE_ID, movieId);
            startActivity(intent);
        }
    }
}
