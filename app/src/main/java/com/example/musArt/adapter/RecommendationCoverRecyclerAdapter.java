package com.example.musArt.adapter;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.widget.PopupMenu;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.musArt.MainActivity;
import com.example.musArt.R;
import com.example.musArt.fragment.GalleryRecommendationPagerFragment;
import com.example.musArt.model.AlbumModel;
import com.example.musArt.presenter.RecommendationCoverPresenter;
import com.example.musArt.presenter.UploaderCoverPresenter;
import com.example.musArt.view.utils.UtilsUI;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class RecommendationCoverRecyclerAdapter extends
        RecyclerView.Adapter<RecommendationCoverRecyclerAdapter.RecommendationViewHolder>
        implements Filterable {

    private LayoutInflater layoutInflater;

    private RecommendationCoverPresenter recommendationCoverPresenter;

    private List<AlbumModel> albumModelList;

    private List<AlbumModel> filterAlbumModels;

    public RecommendationCoverRecyclerAdapter(Context context,
                                              RecommendationCoverPresenter recommendationCoverPresenter,
                                              List<AlbumModel> albumModels) {
        layoutInflater = LayoutInflater.from(context);
        this.recommendationCoverPresenter = recommendationCoverPresenter;
        albumModelList = albumModels;
        this.filterAlbumModels = albumModels;
    }

    @RequiresApi(api = Build.VERSION_CODES.Q)
    @NonNull
    @Override
    public RecommendationCoverRecyclerAdapter.RecommendationViewHolder onCreateViewHolder(
            @NonNull ViewGroup parent, int viewType) {
        final View view = layoutInflater.inflate(
                R.layout.album_from_recommendation, parent, false);
        return new RecommendationCoverRecyclerAdapter.
                RecommendationViewHolder(view);
    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    @Override
    public void onBindViewHolder(@NonNull RecommendationCoverRecyclerAdapter
            .RecommendationViewHolder holder, int position) {
        AlbumModel albumModel = albumModelList.get(position);
        Bitmap albumArt = albumModel.getArt();
        holder.artAlbum.setImageBitmap(albumArt);
        holder.albumName.setText(albumModel.getName());
        holder.artistName.setText(albumModel.getGroupName());
        if (albumModel.isUploaded()) {
            holder.stopLoader();
        }
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

    public AlbumModel[] getRecommendationAlbumModelArray() {
        return albumModelList.toArray(
                new AlbumModel[albumModelList.size()]);
    }

    public class RecommendationViewHolder extends RecyclerView.ViewHolder
            implements View.OnClickListener, UploaderCoverPresenter.UploaderCoverView {

        @BindView(R.id.recommendationLinearLayout)
        LinearLayout recommendationLinearLayout;

        @BindView(R.id.artRecommendationOfAlbum)
        ImageView artAlbum;

        @BindView(R.id.albumRecommendationNameOfAlbum)
        TextView albumName;

        @BindView(R.id.artistRecommendationNameOfAlbum)
        TextView artistName;

        @BindView(R.id.moreOfRecommendationOfAlbum)
        ImageView moreOfAlbum;

        @BindView(R.id.downloadOfRecommendationOfAlbum)
        ImageView uploaderOfRecommendationAlbum;

        @BindView(R.id.loaderOfAlbum)
        ProgressBar loaderOfCover;

        private Context context;

        private UploaderCoverPresenter uploaderCoverPresenter;

        @RequiresApi(api = Build.VERSION_CODES.Q)
        RecommendationViewHolder(View itemView) {
            super(itemView);
            this.context = itemView.getContext();
            ButterKnife.bind(this, itemView);
            itemView.setOnClickListener(this);
            moreOfAlbum.setOnClickListener(v -> {
                final int itemPosition = getLayoutPosition();
                PopupMenu popup = new PopupMenu(v.getContext(), v);
                popup.setOnMenuItemClickListener((PopupMenu.OnMenuItemClickListener) itemMenu -> {
                    if (itemMenu.getItemId() == R.id.remove_of_cover) {
                        albumModelList.remove(itemPosition);
                        RecommendationCoverRecyclerAdapter.this.notifyDataSetChanged();
                        return true;
                    } else {
                        AlbumModel albumModel = albumModelList.get(itemPosition);
                        Bitmap artBitmap = albumModel.getArt();
                        UtilsUI utilsUI = new UtilsUI(context);
                        utilsUI.shareOfCover(artBitmap);
                        return true;
                    }
                });
                MenuInflater inflater = popup.getMenuInflater();
                inflater.inflate(R.menu.menu_recommendation_art, popup.getMenu());
                popup.show();
            });
            uploaderOfRecommendationAlbum.setOnClickListener(listenerOfCoverUpload());
        }

        @RequiresApi(api = Build.VERSION_CODES.Q)
        private View.OnClickListener listenerOfCoverUpload() {
            return v -> {
                uploaderCoverPresenter = new UploaderCoverPresenter(context);
                uploaderCoverPresenter.attachCoverView(this);
                final int itemPosition = getLayoutPosition();
                AlbumModel albumModel = albumModelList.get(itemPosition);
                uploaderCoverPresenter.uploadCover(albumModel);
            };
        }

        @RequiresApi(api = Build.VERSION_CODES.N)
        @Override
        public void onClick(View view) {
            Drawable drawableOfArtAlbum = artAlbum.getDrawable();
            final Bitmap bitmapOfArtAlbum = ((BitmapDrawable) drawableOfArtAlbum).getBitmap();
            Context context = view.getContext();
            Resources resources = context.getResources();
            final DialogFragment galleryFragment =
                    new GalleryRecommendationPagerFragment();
            Bundle bundleGallery = new Bundle();
            final int position = getLayoutPosition();
            bundleGallery.putInt("albumPositionId", position);
            final AlbumModel[] albumModelArray = albumModelList.toArray(
                    new AlbumModel[albumModelList.size()]);
            bundleGallery.putParcelableArray("albumModelList", albumModelArray);
            galleryFragment.setArguments(bundleGallery);
            final FragmentManager fragmentManager = ((MainActivity) view.getContext())
                    .getSupportFragmentManager();
            galleryFragment.show(fragmentManager, "gallery_of_recommendation");
        }

        @Override
        public void startLoader() {
            loaderOfCover.setVisibility(View.VISIBLE);
            uploaderOfRecommendationAlbum.setVisibility(View.GONE);
        }

        @RequiresApi(api = Build.VERSION_CODES.KITKAT)
        @Override
        public void stopLoader() {
            loaderOfCover.setVisibility(View.GONE);
            uploaderOfRecommendationAlbum.setVisibility(View.GONE);
            recommendationLinearLayout.setBackgroundResource(R.color.uploadedLinearLayoutColor);
            if (uploaderCoverPresenter != null) {
                uploaderCoverPresenter.detachCoverView();
            }
        }
    }

}