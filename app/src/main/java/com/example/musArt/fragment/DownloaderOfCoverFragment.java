package com.example.musArt.fragment;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.ActionBar;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.example.musArt.MainActivity;
import com.example.musArt.R;
import com.example.musArt.adapter.DownloaderRecyclerAdapter;
import com.example.musArt.model.AlbumModel;
import com.example.musArt.presenter.DownloaderCoverPresenter;
import com.example.musArt.view.utils.UtilsUI;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class DownloaderOfCoverFragment extends Fragment
        implements DownloaderCoverPresenter.DownloaderCoverView {

    public static final String TAG = DownloaderOfCoverFragment.class.getSimpleName();

    @BindView(R.id.progressBarDownloadingOfAlbums)
    ProgressBar progressBarDownloaderOfAlbums;

    @BindView(R.id.swipeDownloadingContainer)
    public SwipeRefreshLayout swipeRefreshLayout;

    @BindView(R.id.albumDownloadingRecyclerView)
    RecyclerView albumRecyclerView;

    @BindView(R.id.emptyDownloaderCoverText)
    TextView emptyDownloaderCoverText;

    private DownloaderCoverPresenter downloaderCoverPresenter;

    private Context context;

    private ActionBar actionBar;

    private DownloaderRecyclerAdapter downloaderRecyclerAdapter;

    private UtilsUI utilsUI;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
    }

    @RequiresApi(api = Build.VERSION_CODES.Q)
    @SuppressLint("ResourceAsColor")
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_page_of_downloader_covers,
                container, false);
        ButterKnife.bind(this, view);
        swipeRefreshLayout.setColorSchemeResources(R.color.colorSwipeRefreshLayout);
        context = getContext();
        MainActivity mainActivity = (MainActivity) getActivity();
        assert mainActivity != null;
        utilsUI = new UtilsUI(context);
        utilsUI.setStateList(UtilsUI.StateList.DISPLAY_LIST.name());
        actionBar = mainActivity.getSupportActionBar();
        downloaderCoverPresenter = new DownloaderCoverPresenter(context);
        downloaderCoverPresenter.attachDownloaderCover(this);
        displayCovers(new ArrayList<>());
        addSwipeRefresherListener();
        return view;
    }

    @SuppressLint("NewApi")
    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        if (savedInstanceState == null) {
            progressBarDownloaderOfAlbums.setVisibility(View.VISIBLE);
            downloaderCoverPresenter.clearCovers(true);
            downloaderCoverPresenter.downloadCovers();
        } else {
            displayByStateList(savedInstanceState);
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    private void displayByStateList(Bundle savedInstanceState) {
        AlbumModel[] albumModels = (AlbumModel[]) savedInstanceState.get("albumDownloaderModels");
        String stateList = String.valueOf(savedInstanceState.get("stateList"));
        if (stateList.equals(UtilsUI.StateList.DISPLAY_EMPTY_LIST.name())) {
            displayEmptyListView();
        } else if (albumModels != null) {
            displayCovers(Arrays.asList(albumModels));
            startLoader(savedInstanceState);
        }
    }

    private void startLoader(Bundle savedInstanceState) {
        if (savedInstanceState.get("typeOfLoader") != null) {
            int typeOfLoader = (int) savedInstanceState.get("typeOfLoader");
            if (typeOfLoader == 1) {
                progressBarDownloaderOfAlbums.setVisibility(View.VISIBLE);
            } else if (typeOfLoader == 2) {
                swipeRefreshLayout.setRefreshing(true);
            }
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.Q)
    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        if (progressBarDownloaderOfAlbums.getVisibility() == View.VISIBLE) {
            outState.putInt("typeOfLoader", 1);
        } else if (swipeRefreshLayout.isRefreshing()) {
            outState.putInt("typeOfLoader", 2);
        }
        outState.putParcelableArray("albumDownloaderModels",
                downloaderRecyclerAdapter.getDownloaderAlbumModelArray());
        outState.putString("stateList", utilsUI.getStateList());
        downloaderCoverPresenter.downloadCovers().dispose();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        downloaderCoverPresenter.detachDownloaderCover();
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    public void displayCovers(List<AlbumModel> albumModelList) {
        albumRecyclerView.setLayoutManager(new LinearLayoutManager(context));
        downloaderRecyclerAdapter = new DownloaderRecyclerAdapter(context, albumModelList);
        albumRecyclerView.setAdapter(downloaderRecyclerAdapter);
        downloaderRecyclerAdapter.notifyDataSetChanged();
        if (!albumModelList.isEmpty()) {
            displayCoverList();
        }
    }

    private void displayCoverList() {
        finishProgressOffLoading();
        albumRecyclerView.setVisibility(View.VISIBLE);
        emptyDownloaderCoverText.setVisibility(View.GONE);
        utilsUI.setStateList(UtilsUI.StateList.DISPLAY_LIST.name());
    }

    @RequiresApi(api = Build.VERSION_CODES.Q)
    private void addSwipeRefresherListener() {
        swipeRefreshLayout.setOnRefreshListener(() -> {
            if (progressBarDownloaderOfAlbums.getVisibility() == View.VISIBLE) {
                swipeRefreshLayout.setRefreshing(false);
            } else {
                downloaderCoverPresenter.clearCovers(false);
                downloaderCoverPresenter.downloadCovers();
            }
        });
    }

    @Override
    public void filter(String text) {
        downloaderRecyclerAdapter.getFilter().filter(text);
    }

    @Override
    public void displayEmptyListView() {
        finishProgressOffLoading();
        albumRecyclerView.setVisibility(View.GONE);
        emptyDownloaderCoverText.setVisibility(View.VISIBLE);
        utilsUI.setStateList(UtilsUI.StateList.DISPLAY_EMPTY_LIST.name());
    }

    public DownloaderCoverPresenter getDownloaderCoverPresenter() {
        return downloaderCoverPresenter;
    }

    public void setDownloaderCoverPresenter(DownloaderCoverPresenter downloaderCoverPresenter) {
        this.downloaderCoverPresenter = downloaderCoverPresenter;
    }

    @Override
    public void displayHomeEnabled() {
        UtilsUI utilsUI = new UtilsUI(context, actionBar);
        utilsUI.displayHomeEnabled(false);
    }

    private void finishProgressOffLoading() {
        if (swipeRefreshLayout.isRefreshing()) {
            swipeRefreshLayout.setRefreshing(false);
        } else {
            progressBarDownloaderOfAlbums.setVisibility(View.GONE);
        }
    }

}
