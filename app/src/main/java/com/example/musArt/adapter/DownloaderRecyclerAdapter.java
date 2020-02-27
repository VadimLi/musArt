package com.example.musArt.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.widget.PopupMenu;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.musArt.MainActivity;
import com.example.musArt.R;
import com.example.musArt.fragment.GalleryDownloaderPagerFragment;
import com.example.musArt.fragment.SearcherOfAlbumFragment;
import com.example.musArt.model.AlbumModel;
import com.example.musArt.view.utils.UtilsUI;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;

import butterknife.BindView;
import butterknife.ButterKnife;

public class DownloaderRecyclerAdapter extends
        RecyclerView.Adapter<DownloaderRecyclerAdapter.DownloaderViewHolder>
        implements Filterable {

    private static final String TAG = DownloaderRecyclerAdapter.class.getSimpleName();

    private final Context context;

    private LayoutInflater layoutInflater;

    private List<AlbumModel> albumModelList;

    private List<AlbumModel> filterAlbumModels;

    private int positionWithoutNull = 0;

    private int position = 0;

    @SuppressLint("UseSparseArrays")
    private static HashMap<Integer, Integer> positionMap = new HashMap<>();

    @RequiresApi(api = Build.VERSION_CODES.N)
    public DownloaderRecyclerAdapter(Context context,
                                     List<AlbumModel> albumModels) {
        layoutInflater = LayoutInflater.from(context);
        this.context = context;
        albumModelList = albumModels;
        this.filterAlbumModels = albumModels;
        positionMap.clear();
    }

    @NonNull
    @Override
    public DownloaderViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        final View view = layoutInflater.inflate(R.layout.album_from_downloader, parent, false);
        return new DownloaderRecyclerAdapter.DownloaderViewHolder(view, this);
    }

    @Override
    public void onBindViewHolder(@NonNull DownloaderViewHolder holder, int position) {
        AlbumModel albumModel = albumModelList.get(position);
        Bitmap albumArt = albumModel.getArt();
        setAlbumArtIfExists(holder, albumModel, albumArt);
        holder.albumName.setText(albumModel.getName());
        holder.artistName.setText(albumModel.getGroupName());
        this.position++;
    }

    private void setAlbumArtIfExists(DownloaderViewHolder holder,
                                     AlbumModel albumModel,
                                     Bitmap albumArt) {
        if (albumArt == null) {
            albumArt = BitmapFactory
                    .decodeResource(context.getResources(),
                            R.mipmap.ic_not_found_album_foreground);
            holder.artAlbum.setImageBitmap(albumArt);
        } else {
            holder.artAlbum.setImageBitmap(albumArt);
            positionMap.put(position, positionWithoutNull);
            positionWithoutNull++;
        }
        albumModel.setArt(albumArt);
    }

    @Override
    public int getItemCount() {
        return albumModelList.size();
    }

    @Override
    public Filter getFilter() {
        return new Filter() {
            @Override
            protected FilterResults performFiltering(CharSequence constraint) {
                final FilterResults results = new FilterResults();
                if (constraint != null && constraint.length() > 0) {
                    constraint = constraint.toString().toLowerCase();
                    final List<AlbumModel> filters = new ArrayList<>();
                    for (AlbumModel albumModel : filterAlbumModels) {
                        if (albumModel.getName().toLowerCase()
                                .contains(constraint) ||
                                albumModel.getGroupName()
                                        .toLowerCase()
                                        .contains(constraint)) {
                            filters.add(albumModel);
                        }
                    }
                    results.count = filters.size();
                    results.values = filters;
                } else {
                    results.count = filterAlbumModels.size();
                    results.values = filterAlbumModels;
                }
                return results;
            }

            @Override
            protected void publishResults(CharSequence charSequence,
                                          FilterResults filterResults) {
                albumModelList = (List<AlbumModel>) filterResults.values;
                notifyDataSetChanged();
            }
        };
    }

    public AlbumModel[] getDownloaderAlbumModelArray() {
        return albumModelList.toArray(
                new AlbumModel[albumModelList.size()]);
    }

    public static HashMap<Integer, Integer> getPositionMap() {
        return positionMap;
    }

    public class DownloaderViewHolder extends RecyclerView.ViewHolder
            implements View.OnClickListener {

        @BindView(R.id.artDownloadingAlbum)
        ImageView artAlbum;

        @BindView(R.id.albumDownloadingName)
        TextView albumName;

        @BindView(R.id.artistDownloadingName)
        TextView artistName;

        @BindView(R.id.moreOfDownloadingAlbum)
        ImageView moreOfAlbum;

        DownloaderViewHolder(View itemView, DownloaderRecyclerAdapter downloaderRecyclerAdapter) {
            super(itemView);
            ButterKnife.bind(this, itemView);
            itemView.setOnClickListener(this);
            moreOfAlbum.setOnClickListener(v -> {
                PopupMenu popup = new PopupMenu(v.getContext(), v);
                popup.setOnMenuItemClickListener((PopupMenu.OnMenuItemClickListener) itemMenu -> {
                    try {
                        final int itemPosition = getLayoutPosition();
                        AlbumModel albumModel = albumModelList.get(itemPosition);
                        if (itemMenu.getItemId() == R.id.share_of_art) {
                            Bitmap albumWithoutArt = BitmapFactory
                                    .decodeResource(context.getResources(),
                                            R.mipmap.ic_not_found_album_foreground);
                            Bitmap artBitmap = albumModel.getArt();
                            if (!artBitmap.sameAs(albumWithoutArt)) {
                                UtilsUI utilsUI = new UtilsUI(context);
                                utilsUI.shareOfCover(artBitmap);
                            } else {
                                Toast.makeText(context,
                                        "Отсутствует обложка, перейдите в рекоммендации " +
                                                "для того, чтобы добавить ее",
                                        Toast.LENGTH_SHORT).show();
                            }
                        } else {
                            searchOfAlbums();
                        }
                    } catch (IndexOutOfBoundsException e) {
                        Log.e(TAG, Objects.requireNonNull(e.getMessage()));
                    }
                    return true;
                });
                MenuInflater inflater = popup.getMenuInflater();
                inflater.inflate(R.menu.menu_downloading_art, popup.getMenu());
                popup.show();
            });
        }

        private void searchOfAlbums() {
            Bundle bundleRecommendation = new Bundle();
            final int layoutPosition = getLayoutPosition();
            AlbumModel albumModel = albumModelList.get(layoutPosition);
            bundleRecommendation.putParcelable("albumModel", albumModel);
            final FragmentManager fragmentManager = ((MainActivity) context)
                    .getSupportFragmentManager();
            SearcherOfAlbumFragment searcherOfAlbumFragment = new SearcherOfAlbumFragment();
            searcherOfAlbumFragment.setArguments(bundleRecommendation);
            fragmentManager.beginTransaction()
                    .replace(R.id.searcherFragmentOfAlbum, searcherOfAlbumFragment)
                    .addToBackStack(null)
                    .commit();
        }

        @RequiresApi(api = Build.VERSION_CODES.N)
        @Override
        public void onClick(View view) {
            Drawable drawableOfArtAlbum = artAlbum.getDrawable();
            final Bitmap bitmapOfArtAlbum = ((BitmapDrawable) drawableOfArtAlbum).getBitmap();
            Resources resources = context.getResources();
            Drawable withoutOfArtDrawable =
                    resources.getDrawable(R.mipmap.ic_not_found_album_foreground);
            final Bitmap bitmapOfWithoutArt = ((BitmapDrawable) withoutOfArtDrawable).getBitmap();
            if ( !bitmapOfArtAlbum.sameAs(bitmapOfWithoutArt) ) {
                final DialogFragment galleryFragment = new GalleryDownloaderPagerFragment();
                Bundle bundleGallery = new Bundle();
                final int position = getLayoutPosition();
                bundleGallery.putInt("albumPositionId", position);
                final AlbumModel[] albumModelArray = albumModelList.toArray(
                        new AlbumModel[albumModelList.size()]);
                bundleGallery.putParcelableArray("albumModelList", albumModelArray);
                galleryFragment.setArguments(bundleGallery);
                final FragmentManager fragmentManager = ((MainActivity) view.getContext())
                        .getSupportFragmentManager();
                galleryFragment.show(fragmentManager, "gallery_of_downloader");
            }
        }

    }

}
