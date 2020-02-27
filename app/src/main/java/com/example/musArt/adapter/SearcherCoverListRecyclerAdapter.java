package com.example.musArt.adapter;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.musArt.MainActivity;
import com.example.musArt.R;
import com.example.musArt.fragment.RecommendationOfCoverListFragment;
import com.example.musArt.model.AlbumModel;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class SearcherCoverListRecyclerAdapter extends
        RecyclerView.Adapter<SearcherCoverListRecyclerAdapter.SearcherViewHolder>
        implements Filterable {

    private LayoutInflater layoutInflater;

    private static List<AlbumModel> albumModelList;

    private List<AlbumModel> filterAlbumModels;

    public SearcherCoverListRecyclerAdapter(Context context,
                                        List<AlbumModel> albumModels) {
        layoutInflater = LayoutInflater.from(context);
        albumModelList = albumModels;
        this.filterAlbumModels = albumModels;
    }

    @NonNull
    @Override
    public SearcherCoverListRecyclerAdapter.SearcherViewHolder onCreateViewHolder(
            @NonNull ViewGroup parent, int viewType) {
        final View view = layoutInflater.inflate(
                R.layout.album_from_cover_list_searcher, parent, false);
        return new SearcherCoverListRecyclerAdapter.SearcherViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull SearcherCoverListRecyclerAdapter
            .SearcherViewHolder holder, int position) {
        AlbumModel albumModel = albumModelList.get(position);
        Bitmap albumArt = albumModel.getArt();
        holder.albumName.setText(albumModel.getName());
        holder.artistName.setText(albumModel.getGroupName());
        holder.artAlbum.setImageBitmap(albumArt);
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

    public AlbumModel[] getSearcherListAlbumModelArray() {
        return albumModelList.toArray(
                new AlbumModel[albumModelList.size()]);
    }

    public static class SearcherViewHolder extends RecyclerView.ViewHolder
            implements View.OnClickListener {

        @BindView(R.id.artSearchingCoverList)
        ImageView artAlbum;

        @BindView(R.id.albumSearchingCoverListName)
        TextView albumName;

        @BindView(R.id.artistSearchingCoverListName)
        TextView artistName;

        SearcherViewHolder(@NonNull View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
            itemView.setOnClickListener(this);
        }

        @RequiresApi(api = Build.VERSION_CODES.N)
        @Override
        public void onClick(View view) {
            Bundle bundleRecommendation = new Bundle();
            final int layoutPosition = getLayoutPosition();
            AlbumModel albumModel = albumModelList.get(layoutPosition);
            bundleRecommendation.putParcelable("albumModel", albumModel);
            final FragmentManager fragmentManager = ((MainActivity) view.getContext())
                    .getSupportFragmentManager();
            RecommendationOfCoverListFragment recommendationOfCoverListFragment =
                    new RecommendationOfCoverListFragment();
            recommendationOfCoverListFragment.setArguments(bundleRecommendation);
            fragmentManager.beginTransaction()
                    .replace(R.id.recommendationFragmentOfAlbumList, recommendationOfCoverListFragment)
                    .addToBackStack(null)
                    .commit();
        }

    }

}
