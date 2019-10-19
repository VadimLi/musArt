package com.example.coversame.adapter;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.coversame.MainActivity;
import com.example.coversame.R;
import com.example.coversame.fragment.GalleryAlbumArtFragment;
import com.example.coversame.model.Album;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class DownloaderRecyclerAdapter extends
        RecyclerView.Adapter<DownloaderRecyclerAdapter.DownloaderViewHolder>
        implements Filterable {

    private LayoutInflater layoutInflater;

    private List<Album> albumList;

    private List<Album> filterAlbums;

    private final Context context;

    public DownloaderRecyclerAdapter(Context context,
                                     List<Album> albumList) {
        layoutInflater = LayoutInflater.from(context);
        this.context = context;
        this.albumList = albumList;
        this.filterAlbums = albumList;
    }

    @NonNull
    @Override
    public DownloaderViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        final View view = layoutInflater.inflate(R.layout.album_from_downloader, parent, false);
        return new DownloaderRecyclerAdapter.DownloaderViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull DownloaderViewHolder holder, int position) {
        Album album = albumList.get(position);
        Bitmap albumArt = album.getArt();
        setAlbumArtIfExists(holder, albumArt);
        holder.albumName.setText(album.getName());
        holder.artistName.setText(album.getGroupName());
    }

    private void setAlbumArtIfExists(DownloaderViewHolder holder, Bitmap albumArt) {
        if (albumArt == null) {
            holder.artAlbum.setImageBitmap(BitmapFactory
                    .decodeResource(context.getResources(),
                            R.mipmap.ic_not_found_album_foreground));
        } else {
            holder.artAlbum.setImageBitmap(albumArt);
        }
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
                                .contains(constraint) ||
                                album.getGroupName()
                                        .toLowerCase()
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

    static class DownloaderViewHolder extends RecyclerView.ViewHolder
                                    implements View.OnClickListener {

        @BindView(R.id.artAlbum)
        ImageView artAlbum;

        @BindView(R.id.albumName)
        TextView albumName;

        @BindView(R.id.artistName)
        TextView artistName;

        DownloaderViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
            artAlbum.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            Drawable drawableOfArtAlbum = artAlbum.getDrawable();
            final Bitmap bitmapOfArtAlbum = ((BitmapDrawable) drawableOfArtAlbum).getBitmap();
            Context context = view.getContext();
            Resources resources = context.getResources();
            //@TODO deprecated
            Drawable withoutOfArtDrawable =
                    resources.getDrawable(R.mipmap.ic_not_found_album_foreground);
            final Bitmap bitmapOfWithoutArt = ((BitmapDrawable) withoutOfArtDrawable).getBitmap();
            if ( !bitmapOfArtAlbum.sameAs(bitmapOfWithoutArt) ) {
                GalleryAlbumArtFragment galleryAlbumArtFragment = new GalleryAlbumArtFragment();
                FragmentManager fragmentManager = ((MainActivity) view.getContext())
                        .getSupportFragmentManager();
                galleryAlbumArtFragment.show(fragmentManager, "gallery album art fragment");
            }
        }
    }

}
