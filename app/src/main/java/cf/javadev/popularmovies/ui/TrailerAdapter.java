package cf.javadev.popularmovies.ui;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.squareup.picasso.Picasso;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;
import cf.javadev.popularmovies.R;
import cf.javadev.popularmovies.model.Trailer;

public class TrailerAdapter extends RecyclerView.Adapter<TrailerAdapter.ViewHolder> {
    @SuppressWarnings("SpellCheckingInspection")
    private static final String IMAGE_THUMBNAIL = "https://img.youtube.com/vi/%s/mqdefault.jpg";
    private final ArrayList<Trailer> trailers;
    private final TrailerAdapterCallback adapterCallback;
    private final Context context;

    public TrailerAdapter(Context context, ArrayList<Trailer> trailers,
                          TrailerAdapterCallback callback) {
        this.context = context;
        this.trailers = trailers;
        this.adapterCallback = callback;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_trailer_list, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        Trailer trailer = trailers.get(position);
        holder.trailer = trailer;
        String url = String.format(IMAGE_THUMBNAIL, trailer.getKey());
        Picasso.with(context)
                .load(url)
                .into(holder.imageThumbnail);
    }

    @Override
    public int getItemCount() {
        return trailers.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        Trailer trailer;
        @BindView(R.id.image_thumbnail)
        ImageView imageThumbnail;

        ViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
            if (null != trailer) {
                imageThumbnail.setContentDescription(trailer.getName());
            }
            itemView.setOnClickListener(view ->
                    adapterCallback.onItemClickListener(trailer.getKey()));
        }
    }
}
