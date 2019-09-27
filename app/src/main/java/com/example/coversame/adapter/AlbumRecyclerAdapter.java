package com.example.coversame.adapter;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.recyclerview.widget.RecyclerView;

import com.example.coversame.R;
import com.example.coversame.model.Album;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class AlbumRecyclerAdapter extends
        RecyclerView.Adapter<AlbumRecyclerAdapter.AlbumViewHolder> implements Filterable {

    private LayoutInflater layoutInflater;

    private List<Album> albumList;

    private List<Album> filterAlbums;

    public AlbumRecyclerAdapter(Context context,
                                List<Album> albumList) {
        layoutInflater = LayoutInflater.from(context);
        this.albumList = albumList;
        this.filterAlbums = albumList;
    }

    @NonNull
    @Override
    public AlbumViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        final View view = layoutInflater.inflate(R.layout.album, parent, false);
        return new AlbumRecyclerAdapter.AlbumViewHolder(view);
    }

    @RequiresApi(api = Build.VERSION_CODES.P)
    @Override
    public void onBindViewHolder(@NonNull AlbumViewHolder holder, int position) {
        Album album = albumList.get(position);
        Picasso.get().load(album.getPath()).into(new Target() {
            @Override
            public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
                holder.coverAlbum.setImageBitmap(bitmap);
            }

            @Override
            public void onBitmapFailed(Exception e, Drawable errorDrawable) { }

            @Override
            public void onPrepareLoad(Drawable placeHolderDrawable) { }
        });
        holder.fullName.setText(album.getName());
    }

    @Override
    public int getItemCount() {
        return albumList.size();
    }

    @Override
    public Filter getFilter() {
        return new Filter() {
            @Override
            protected FilterResults performFiltering(CharSequence constraint) {
                final FilterResults results = new FilterResults();
                if (constraint != null && constraint.length() > 0) {
                    constraint = constraint.toString().toLowerCase();
                    final List<Album> filters = new ArrayList<>();
                    for (Album album : filterAlbums) {
                        if (album.getName().toLowerCase()
                                .contains(constraint)) {
                            filters.add(album);
                        }
                    }
                    results.count = filters.size();
                    results.values = filters;
                } else {
                    results.count = filterAlbums.size();
                    results.values = filterAlbums;
                }
                return results;
            }

            @Override
            protected void publishResults(CharSequence charSequence,
                                          FilterResults filterResults) {
                albumList = (List<Album>) filterResults.values;
                notifyDataSetChanged();
            }
        };
    }

    static class AlbumViewHolder extends RecyclerView.ViewHolder {

        @BindView(R.id.coverAlbum)
        ImageView coverAlbum;

        @BindView(R.id.fullName)
        TextView fullName;

        AlbumViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }

    }

}
