package com.example.musArt.fragment;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
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
import com.example.musArt.adapter.SearcherCoverRecyclerAdapter;
import com.example.musArt.model.AlbumModel;
import com.example.musArt.presenter.SearcherCoverPresenter;
import com.example.musArt.view.utils.UtilsUI;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class SearcherOfAlbumFragment extends Fragment implements
        SearcherCoverPresenter.SearcherCoverView {

    @BindView(R.id.progressBarSearchingOfAlbum)
    ProgressBar progressBarSearchOfAlbums;

    @BindView(R.id.swipeSearchingOfAlbumContainer)
    SwipeRefreshLayout swipeRefreshLayout;

    @BindView(R.id.notConnectionTextOfSearcher)
    TextView notConnectionText;

    @BindView(R.id.emptySearcherCoverText)
    TextView emptySearcherCoverText;

    @BindView(R.id.albumSearchingRecyclerView)
    RecyclerView albumRecyclerView;

    private Context context;

    private MainActivity mainActivity;

    private SearcherCoverRecyclerAdapter searcherCoverRecyclerAdapter;

    private UtilsUI utilsUI;

    private SearcherCoverPresenter searcherCoverPresenter;

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
        View view = inflater.inflate(R.layout.fragment_page_of_searcher_cover,
                container, false);
        ButterKnife.bind(this, view);
        mainActivity = (MainActivity) getActivity();
        swipeRefreshLayout.setColorSchemeResources(R.color.colorSwipeRefreshLayout);
        context = getContext();
        ActionBar actionBar = mainActivity.getSupportActionBar();
        utilsUI = new UtilsUI(context, actionBar);
        utilsUI.setStateList(UtilsUI.StateList.DISPLAY_LIST.name());
        displayCovers(new ArrayList<>());
        searcherCoverPresenter = new SearcherCoverPresenter(context);
        searcherCoverPresenter.attachSearcherCoverView(this);
        Bundle arguments = getArguments();
        assert arguments != null;
        searcherCoverPresenter.addValuesToTitle(arguments);
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
            progressBarSearchOfAlbums.setVisibility(View.VISIBLE);
            searcherCoverPresenter.searchCovers();
        } else {
            AlbumModel[] albumModels = (AlbumModel[]) savedInstanceState.get("albumSearcherModels");
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
    public void onStart() {
        super.onStart();
        setVisibilityMainView(false);
        utilsUI.displayHomeEnabled(true);
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
        outState.putParcelableArray("albumSearcherModels",
                searcherCoverRecyclerAdapter.getSearcherAlbumModelArray());
        outState.putString("stateList", utilsUI.getStateList());
        searcherCoverPresenter.searchCovers().dispose();
    }

    @RequiresApi(api = Build.VERSION_CODES.Q)
    @Override
    public void onDestroy() {
        super.onDestroy();
        setVisibilityMainView(true);
        utilsUI.displayHomeEnabled(false);
        mainActivity.refreshDownloaderCovers();
        searcherCoverPresenter.detachSearcherCoverView();
    }

    @RequiresApi(api = Build.VERSION_CODES.Q)
    private void addSwipeRefresherListener() {
        swipeRefreshLayout.setOnRefreshListener(() -> {
            if (progressBarSearchOfAlbums.getVisibility() == View.VISIBLE) {
                swipeRefreshLayout.setRefreshing(false);
            } else {
                searcherCoverPresenter.searchCovers();
            }
        });
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void addValuesToTitleLayout(Bitmap art, String groupNameAndAlbumName) {
        mainActivity.titleDownloaderCover.setImageBitmap(art);
        mainActivity.titleGroupNameAndAlbumName.setText(groupNameAndAlbumName);
    }

    private void setVisibilityMainView(boolean visibility) {
        if (!visibility) {
            mainActivity.tabOfCoverLayout.setVisibility(View.GONE);
            mainActivity.swipePagerOfTitle.setVisibility(View.GONE);
            mainActivity.coverTitleLayout.setVisibility(View.VISIBLE);
            mainActivity.setIconifiedOfSearchView();
        } else {
            mainActivity.tabOfCoverLayout.setVisibility(View.VISIBLE);
            mainActivity.swipePagerOfTitle.setVisibility(View.VISIBLE);
            mainActivity.swipePagerOfTitle.setSwipeEnabled(true);
            mainActivity.coverTitleLayout.setVisibility(View.GONE);
        }
    }

    @Override
    public void displayCovers(List<AlbumModel> albumModelHashSet) {
        albumRecyclerView.setLayoutManager(new LinearLayoutManager(context));
        List<AlbumModel> albumModels = new ArrayList<>(albumModelHashSet);
        searcherCoverRecyclerAdapter = new SearcherCoverRecyclerAdapter(context, albumModels);
        searcherCoverRecyclerAdapter.notifyDataSetChanged();
        albumRecyclerView.setAdapter(searcherCoverRecyclerAdapter);
        if (!albumModelHashSet.isEmpty()) {
            displayCoverList();
        }
    }

    private void displayCoverList() {
        finishProgressOffLoading();
        albumRecyclerView.setVisibility(View.VISIBLE);
        emptySearcherCoverText.setVisibility(View.GONE);
        notConnectionText.setVisibility(View.GONE);
        utilsUI.setStateList(UtilsUI.StateList.DISPLAY_LIST.name());
    }

    @Override
    public void filter(String text) {
        searcherCoverRecyclerAdapter.getFilter().filter(text);
    }

    @Override
    public void displayHomeEnabled() {
        utilsUI.displayHomeEnabled(true);
    }

    SearcherCoverPresenter getSearcherCoverPresenter() {
        return searcherCoverPresenter;
    }

    public void setSearcherCoverPresenter(SearcherCoverPresenter searcherCoverPresenter) {
        this.searcherCoverPresenter = searcherCoverPresenter;
    }

    @Override
    public void displayNotConnectionView() {
        finishProgressOffLoading();
        notConnectionText.setVisibility(View.VISIBLE);
        albumRecyclerView.setVisibility(View.GONE);
        emptySearcherCoverText.setVisibility(View.GONE);
        utilsUI.setStateList(UtilsUI.StateList.DISPLAY_NOT_CONNECTION.name());
    }

    @Override
    public void displayEmptyListView() {
        finishProgressOffLoading();
        emptySearcherCoverText.setVisibility(View.VISIBLE);
        albumRecyclerView.setVisibility(View.GONE);
        notConnectionText.setVisibility(View.GONE);
        utilsUI.setStateList(UtilsUI.StateList.DISPLAY_EMPTY_LIST.name());
    }

    private void finishProgressOffLoading() {
        if (swipeRefreshLayout.isRefreshing()) {
            swipeRefreshLayout.setRefreshing(false);
        } else {
            progressBarSearchOfAlbums.setVisibility(View.GONE);
        }
    }

}
