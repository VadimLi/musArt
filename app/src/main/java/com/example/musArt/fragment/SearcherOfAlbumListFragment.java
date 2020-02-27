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
import com.example.musArt.adapter.SearcherCoverListRecyclerAdapter;
import com.example.musArt.model.AlbumModel;
import com.example.musArt.presenter.SearcherCoverListPresenter;
import com.example.musArt.view.utils.UtilsUI;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class SearcherOfAlbumListFragment extends Fragment
        implements SearcherCoverListPresenter.SearcherCoverListView {

    @BindView(R.id.progressBarSearchingOfAlbumList)
    ProgressBar progressBarSearchOfAlbums;

    @BindView(R.id.swipeSearchingOfAlbumListContainer)
    SwipeRefreshLayout swipeRefreshLayout;

    @BindView(R.id.notConnectionTextOfSearcherList)
    TextView notConnectionTextOfSearcherList;

    @BindView(R.id.emptySearcherCoverListText)
    TextView emptySearcherCoverListText;

    @BindView(R.id.albumListSearchingRecyclerView)
    RecyclerView albumRecyclerView;

    private Context context;

    private SearcherCoverListPresenter searcherCoverListPresenter;

    private SearcherCoverListRecyclerAdapter searcherCoverListRecyclerAdapter;

    private UtilsUI utilsUI;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
    }

    @SuppressLint("NewApi")
    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_page_of_searcher_cover_list,
                container, false);
        ButterKnife.bind(this, view);
        MainActivity mainActivity = (MainActivity) getActivity();
        assert mainActivity != null;
        swipeRefreshLayout.setColorSchemeResources(R.color.colorSwipeRefreshLayout);
        context = getContext();
        ActionBar actionBar = mainActivity.getSupportActionBar();
        utilsUI = new UtilsUI(context, actionBar);
        utilsUI.setStateList(UtilsUI.StateList.DISPLAY_LIST.name());
        searcherCoverListPresenter = new SearcherCoverListPresenter(context);
        searcherCoverListPresenter.attachSearcherCoverView(this);
        displayCovers(new ArrayList<>());
        addSwipeRefresherListener();
        return view;
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        if (savedInstanceState == null) {
            progressBarSearchOfAlbums.setVisibility(View.VISIBLE);
            searcherCoverListPresenter.searchCovers();
        } else {
            AlbumModel[] albumModels =
                    (AlbumModel[]) savedInstanceState.get("albumSearcherListModels");
            assert albumModels != null;
            String stateList = (String) savedInstanceState.get("stateList");
            assert stateList != null;
            displayByStateList(stateList, albumModels, savedInstanceState);
        }
    }

    private void displayByStateList(String stateList, AlbumModel[] albumModels,
                                    Bundle savedInstanceState) {
        if (stateList.equals(UtilsUI.StateList.DISPLAY_LIST.name())) {
            displayCovers(Arrays.asList(albumModels));
            startLoader(savedInstanceState);
        } else if (stateList.equals(UtilsUI.StateList.DISPLAY_EMPTY_LIST.name())) {
            displayEmptyListView();
        } else if (stateList.equals(UtilsUI.StateList.DISPLAY_NOT_CONNECTION.name())) {
            displayNotConnectionView();
        }
    }

    private void startLoader(Bundle savedInstanceState) {
        if (savedInstanceState.get("typeOfLoader") != null) {
            int typeOfLoader = (int) savedInstanceState.get("typeOfLoader");
            if (typeOfLoader == 1) {
                progressBarSearchOfAlbums.setVisibility(View.VISIBLE);
            } else if (typeOfLoader == 2) {
                swipeRefreshLayout.setRefreshing(true);
            }
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        if (progressBarSearchOfAlbums.getVisibility() == View.VISIBLE) {
            outState.putInt("typeOfLoader", 1);
        } else if (swipeRefreshLayout.isRefreshing()) {
            outState.putInt("typeOfLoader", 2);
        }
        outState.putParcelableArray("albumSearcherListModels",
                searcherCoverListRecyclerAdapter.getSearcherListAlbumModelArray());
        outState.putString("stateList", utilsUI.getStateList());
        searcherCoverListPresenter.searchCovers().dispose();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        searcherCoverListPresenter.detachSearcherCoverView();
    }

    @RequiresApi(api = Build.VERSION_CODES.Q)
    private void addSwipeRefresherListener() {
        swipeRefreshLayout.setOnRefreshListener(() -> {
            if (progressBarSearchOfAlbums.getVisibility() == View.VISIBLE) {
                swipeRefreshLayout.setRefreshing(false);
            } else {
                searcherCoverListPresenter.searchCovers();
            }
        });
    }

    @Override
    public void displayCovers(List<AlbumModel> albumModelHashSet) {
        albumRecyclerView.setLayoutManager(new LinearLayoutManager(context));
        final List<AlbumModel> albumModelList = new ArrayList<>(albumModelHashSet);
        searcherCoverListRecyclerAdapter =
                new SearcherCoverListRecyclerAdapter(context, albumModelList);
        albumRecyclerView.setAdapter(searcherCoverListRecyclerAdapter);
        if (!albumModelHashSet.isEmpty()) {
            displayCoverList();
        }
    }

    private void displayCoverList() {
        finishProgressOffLoading();
        albumRecyclerView.setVisibility(View.VISIBLE);
        emptySearcherCoverListText.setVisibility(View.GONE);
        notConnectionTextOfSearcherList.setVisibility(View.GONE);
        utilsUI.setStateList(UtilsUI.StateList.DISPLAY_LIST.name());
    }

    public SearcherCoverListPresenter getSearcherCoverListPresenter() {
        return searcherCoverListPresenter;
    }

    public void setSearcherCoverListPresenter(SearcherCoverListPresenter searcherCoverListPresenter) {
        this.searcherCoverListPresenter = searcherCoverListPresenter;
    }

    @Override
    public void displayHomeEnabled() {
        utilsUI.displayHomeEnabled(false);
    }

    @Override
    public void filter(String text) {
        searcherCoverListRecyclerAdapter.getFilter().filter(text);
    }

    @Override
    public void displayEmptyListView() {
        finishProgressOffLoading();
        emptySearcherCoverListText.setVisibility(View.VISIBLE);
        albumRecyclerView.setVisibility(View.GONE);
        notConnectionTextOfSearcherList.setVisibility(View.GONE);
        utilsUI.setStateList(UtilsUI.StateList.DISPLAY_EMPTY_LIST.name());
    }

    @Override
    public void displayNotConnectionView() {
        finishProgressOffLoading();
        notConnectionTextOfSearcherList.setVisibility(View.VISIBLE);
        emptySearcherCoverListText.setVisibility(View.GONE);
        albumRecyclerView.setVisibility(View.GONE);
        utilsUI.setStateList(UtilsUI.StateList.DISPLAY_NOT_CONNECTION.name());
    }

    private void finishProgressOffLoading() {
        if (swipeRefreshLayout.isRefreshing()) {
            swipeRefreshLayout.setRefreshing(false);
        } else {
            progressBarSearchOfAlbums.setVisibility(View.GONE);
        }
    }

}
