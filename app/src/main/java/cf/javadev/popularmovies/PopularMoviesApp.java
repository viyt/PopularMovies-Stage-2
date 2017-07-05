package cf.javadev.popularmovies;


import android.app.Application;

import com.facebook.stetho.Stetho;

public class PopularMoviesApp extends Application {

    public void onCreate() {
        super.onCreate();
        Stetho.initializeWithDefaults(this);
    }
}
