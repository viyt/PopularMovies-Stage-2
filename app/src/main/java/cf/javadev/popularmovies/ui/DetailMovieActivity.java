package cf.javadev.popularmovies.ui;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import cf.javadev.popularmovies.R;

public class DetailMovieActivity extends AppCompatActivity {
    private static final String DETAIL_MOVIE_FRAGMENT_TAG = "detailMovieFragmentTag";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.detail_layout);

        getWindow().setBackgroundDrawable(null);

        if (getResources().getBoolean(R.bool.has_two_panes)) {
            finish();
            return;
        }

        DetailMovieFragment fragment = new DetailMovieFragment();
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.container_detail, fragment, DETAIL_MOVIE_FRAGMENT_TAG)
                .commit();
    }
}
