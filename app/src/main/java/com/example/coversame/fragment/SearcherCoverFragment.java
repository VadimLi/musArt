package com.example.coversame.fragment;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.example.coversame.R;
import com.example.coversame.model.Album;
import com.example.coversame.presenter.SearcherCoverPresenter;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class SearcherCoverFragment extends Fragment
        implements SearcherCoverPresenter.SearcherCoverView {

    @BindView(R.id.progressBarSearchOfAlbums)
    ProgressBar progressBarSearchOfAlbums;

    @BindView(R.id.swipeContainer)
    SwipeRefreshLayout swipeRefreshLayout;

    @BindView(R.id.albumRecyclerView)
    RecyclerView albumRecyclerView;

    private SearcherCoverPresenter searcherCoverPresenter;

    private Context context;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_page_of_covers,
                container, false);
        ButterKnife.bind(this, view);

        context = getContext();
        searcherCoverPresenter = new SearcherCoverPresenter();
        searcherCoverPresenter.attachSearcherCoverView(this);
        searcherCoverPresenter.searchCovers();

        return view;
    }

    @Override
    public void onStart() {
        super.onStart();
        searcherCoverPresenter.attachSearcherCoverView(this);
    }

    @Override
    public void onStop() {
        super.onStop();
        searcherCoverPresenter.detachSearcherVoverView();
    }

    @Override
    public void displayCovers(List<Album> albumList) {
        albumRecyclerView.setLayoutManager(new LinearLayoutManager(context));
        progressBarSearchOfAlbums.setVisibility(View.GONE);
    }

}
