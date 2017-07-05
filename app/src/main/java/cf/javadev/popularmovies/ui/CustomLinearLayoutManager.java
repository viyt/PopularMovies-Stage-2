package cf.javadev.popularmovies.ui;

import android.content.Context;
import android.support.v7.widget.LinearLayoutManager;

class CustomLinearLayoutManager extends LinearLayoutManager {

    CustomLinearLayoutManager(Context context) {
        super(context);
    }

    @Override
    public boolean canScrollVertically() {
        return false;
    }
}
