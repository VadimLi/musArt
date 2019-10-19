package com.example.coversame.fragment;

import android.content.ContentResolver;
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
import com.example.coversame.adapter.DownloaderRecyclerAdapter;
import com.example.coversame.model.Album;
import com.example.coversame.presenter.DownloaderCoverPresenter;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import butterknife.BindView;
import butterknife.ButterKnife;

public class DownloaderCoverFragment extends Fragment
        implements DownloaderCoverPresenter.DownloaderCoverView {

    @BindView(R.id.progressBarSearchOfAlbums)
    ProgressBar progressBarSearchOfAlbums;

    @BindView(R.id.swipeContainer)
    SwipeRefreshLayout swipeRefreshLayout;

    @BindView(R.id.albumRecyclerView)
    RecyclerView albumRecyclerView;

    private DownloaderCoverPresenter downloaderCoverPresenter;

    private Context context;

    private DownloaderRecyclerAdapter downloaderRecyclerAdapter;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_page_of_covers,
                container, false);
        ButterKnife.bind(this, view);
        downloaderCoverPresenter = new DownloaderCoverPresenter();
        downloaderCoverPresenter.attachDownloaderCover(this);
        context = getContext();
        ContentResolver contentResolver = Objects.requireNonNull(context).getContentResolver();
        downloaderCoverPresenter.downloadCovers(contentResolver, context);
        addSwipeRefresherListener(contentResolver);

        return view;
    }

    @Override
    public void onStart() {
        super.onStart();
        downloaderCoverPresenter.attachDownloaderCover(this);
    }

    @Override
    public void onStop() {
        super.onStop();
        downloaderCoverPresenter.detachDownloaderCover();
    }

    private void addSwipeRefresherListener(ContentResolver contentResolver) {
        swipeRefreshLayout.setOnRefreshListener(() ->
                downloaderCoverPresenter.downloadCovers(contentResolver, context));
    }

    @Override
    public void displayCovers(Set<Album> albumHashSet) {
        albumRecyclerView.setLayoutManager(new LinearLayoutManager(context));
        final List<Album> albumList = new ArrayList<>(albumHashSet);
        downloaderRecyclerAdapter = new DownloaderRecyclerAdapter(context, albumList);
        albumRecyclerView.setAdapter(downloaderRecyclerAdapter);
        downloaderRecyclerAdapter.notifyDataSetChanged();
        finishProgressOffLoading();
    }

    private void finishProgressOffLoading() {
        if (swipeRefreshLayout.isRefreshing()) {
            final int delay = 300;
            swipeRefreshLayout.postDelayed(() ->
                    swipeRefreshLayout.setRefreshing(false), delay);
        } else {
            progressBarSearchOfAlbums.setVisibility(View.GONE);
        }
    }

    public DownloaderRecyclerAdapter getDownloaderRecyclerAdapter() {
        return downloaderRecyclerAdapter;
    }

}
