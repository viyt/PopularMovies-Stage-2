package cf.javadev.popularmovies.ui;

import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;

abstract class EndlessRecyclerViewScrollListener extends RecyclerView.OnScrollListener {
    private int visibleThreshold = 5;
    private int previousTotalItemCount = 0;
    private boolean loading = true;

    private final RecyclerView.LayoutManager layoutManager;

    EndlessRecyclerViewScrollListener(GridLayoutManager layoutManager) {
        this.layoutManager = layoutManager;
        visibleThreshold = visibleThreshold * layoutManager.getSpanCount();
    }

    @Override
    public void onScrolled(RecyclerView view, int dx, int dy) {
        int firstVisibleItemPosition;
        int lastVisibleItemPosition;
        int totalItemCount = layoutManager.getItemCount();
        firstVisibleItemPosition = ((GridLayoutManager) layoutManager).findFirstVisibleItemPosition();
        lastVisibleItemPosition = ((GridLayoutManager) layoutManager).findLastVisibleItemPosition();

        if (totalItemCount < previousTotalItemCount) {
            this.previousTotalItemCount = totalItemCount;
            if (totalItemCount == 0) {
                this.loading = true;
            }
        }
        if (loading && (totalItemCount > previousTotalItemCount)) {
            loading = false;
            previousTotalItemCount = totalItemCount;
        }

        if (!loading && (lastVisibleItemPosition + visibleThreshold) > totalItemCount) {
            onLoadMore();
            loading = true;
        }

        if (firstVisibleItemPosition > 0) {
            enableFabButton(true);
        } else {
            enableFabButton(false);
        }
    }

    public abstract void onLoadMore();

    public abstract void enableFabButton(boolean enabled);
}
