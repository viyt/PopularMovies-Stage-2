package cf.javadev.popularmovies.ui;


import android.content.Context;
import android.database.Cursor;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.squareup.picasso.Picasso;

import butterknife.BindView;
import butterknife.ButterKnife;
import cf.javadev.popularmovies.R;

class MovieAdapter extends RecyclerView.Adapter<MovieAdapter.ViewHolder> {
    private final static String BASE_POSTER_URL = "https://image.tmdb.org/t/p/";
    private final static String IMAGE_SIZE = "w185";
    private Cursor cursor;
    private final Context context;
    private final MovieAdapterCallback callback;

    MovieAdapter(Context context, MovieAdapterCallback callback) {
        this.context = context;
        this.callback = callback;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_movies,
                parent, false);
        return new ViewHolder(view);
    }

    @SuppressWarnings("StringBufferReplaceableByString")
    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        cursor.moveToPosition(position);
        holder.movieId = cursor.getInt(MovieActivityFragment.COLUMN_MOVIES_ID);
        String posterPath = cursor.getString(MovieActivityFragment.COLUMN_POSTER_PATH);
        String url = new StringBuilder()
                .append(BASE_POSTER_URL)
                .append(IMAGE_SIZE)
                .append(posterPath).toString();
        Picasso.with(context)
                .load(url)
                .into(holder.imageViewPoster);
    }

    @Override
    public int getItemCount() {
        if (null == cursor) {
            return 0;
        } else {
            return cursor.getCount();
        }
    }

    void swapCursor(Cursor newCursor) {
        cursor = newCursor;
        notifyDataSetChanged();
    }

    class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        @BindView(R.id.poster_image)
        ResizableImageView imageViewPoster;
        int movieId;

        ViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            callback.onItemClick(movieId);
        }
    }
}
