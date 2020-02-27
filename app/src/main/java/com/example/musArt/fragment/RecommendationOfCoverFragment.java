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
import com.example.musArt.adapter.RecommendationCoverRecyclerAdapter;
import com.example.musArt.model.AlbumModel;
import com.example.musArt.presenter.RecommendationCoverPresenter;
import com.example.musArt.view.utils.UtilsUI;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

import butterknife.BindView;
import butterknife.ButterKnife;

public class RecommendationOfCoverFragment extends Fragment
        implements RecommendationCoverPresenter.RecommendationCoverView {

    @BindView(R.id.progressBarRecommendationOfAlbum)
    ProgressBar progressBarRecommendationOfAlbums;

    @BindView(R.id.swipeRecommendationContainerOfAlbum)
    SwipeRefreshLayout swipeRefreshLayout;

    @BindView(R.id.notConnectionTextOfRecommendation)
    TextView notConnectionTextOfRecommendation;

    @BindView(R.id.emptyRecommendationCoverText)
    TextView emptyRecommendationCoverText;

    @BindView(R.id.recommendationRecyclerViewOfAlbum)
    RecyclerView albumRecyclerView;

    private Context context;

    private MainActivity mainActivity;

    private RecommendationCoverRecyclerAdapter recommendationCoverRecyclerAdapter;

    private RecommendationCoverPresenter recommendationCoverPresenter;

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
        View view = inflater.inflate(R.layout.fragment_page_of_recommendation_cover,
                container, false);
        ButterKnife.bind(this, view);
        swipeRefreshLayout.setColorSchemeResources(R.color.colorSwipeRefreshLayout);
        context = getContext();
        final ActionBar actionBar = ((MainActivity) Objects
                .requireNonNull(getActivity()))
                .getSupportActionBar();
        utilsUI = new UtilsUI(context, actionBar);
        utilsUI.setStateList(UtilsUI.StateList.DISPLAY_LIST.name());
        mainActivity = (MainActivity) getActivity();
        displayCovers(new ArrayList<>());
        recommendationCoverPresenter = new RecommendationCoverPresenter(context);
        recommendationCoverPresenter.attachRecommendationCover(this);
        displayCovers(new ArrayList<>());
        addSwipeRefresherListener(getArguments());
        return view;
    }

    @SuppressLint("NewApi")
    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        if (savedInstanceState == null) {
            progressBarRecommendationOfAlbums.setVisibility(View.VISIBLE);
            recommendationCoverPresenter.recommendCovers(getArguments());
        } else {
            AlbumModel[] albumModels = (AlbumModel[])
                    savedInstanceState.get("albumRecommendationModels");
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
                progressBarRecommendationOfAlbums.setVisibility(View.VISIBLE);
            } else if (typeOfLoader == 2) {
                swipeRefreshLayout.setRefreshing(true);
            }
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        mainActivity.setIconifiedOfSearchView();
    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        if (progressBarRecommendationOfAlbums.getVisibility() == View.VISIBLE) {
            outState.putInt("typeOfLoader", 1);
        } else if (swipeRefreshLayout.isRefreshing()) {
            outState.putInt("typeOfLoader", 2);
        }
        outState.putParcelableArray("albumRecommendationModels",
                recommendationCoverRecyclerAdapter.getRecommendationAlbumModelArray());
        outState.putString("stateList", utilsUI.getStateList());
        recommendationCoverPresenter.recommendCovers(getArguments()).dispose();
    }

    @RequiresApi(api = Build.VERSION_CODES.Q)
    @Override
    public void onDestroy() {
        super.onDestroy();
        recommendationCoverPresenter.detachRecommendationCover();
    }

    @RequiresApi(api = Build.VERSION_CODES.Q)
    private void addSwipeRefresherListener(Bundle arguments) {
        swipeRefreshLayout.setOnRefreshListener(() -> {
            if (progressBarRecommendationOfAlbums.getVisibility() == View.VISIBLE) {
                swipeRefreshLayout.setRefreshing(false);
            } else {
                recommendationCoverPresenter.recommendCovers(arguments);
            }
        });
    }

    @Override
    public void displayCovers(List<AlbumModel> albumModelHashSet) {
        albumRecyclerView.setLayoutManager(new LinearLayoutManager(context));
        recommendationCoverRecyclerAdapter =
                new RecommendationCoverRecyclerAdapter(context,
                        recommendationCoverPresenter, albumModelHashSet);
        albumRecyclerView.setAdapter(recommendationCoverRecyclerAdapter);
        if (!albumModelHashSet.isEmpty()) {
            displayCoverList();
        }
    }

    private void displayCoverList() {
        finishProgressOffLoading();
        albumRecyclerView.setVisibility(View.VISIBLE);
        emptyRecommendationCoverText.setVisibility(View.GONE);
        notConnectionTextOfRecommendation.setVisibility(View.GONE);
        utilsUI.setStateList(UtilsUI.StateList.DISPLAY_LIST.name());
    }

    @Override
    public void displayHomeEnabled() {
        utilsUI.displayHomeEnabled(true);
    }

    @Override
    public void filter(String text) {
        recommendationCoverRecyclerAdapter.getFilter().filter(text);
    }

    @Override
    public void displayEmptyListView() {
        finishProgressOffLoading();
        emptyRecommendationCoverText.setVisibility(View.VISIBLE);
        albumRecyclerView.setVisibility(View.GONE);
        notConnectionTextOfRecommendation.setVisibility(View.GONE);
        utilsUI.setStateList(UtilsUI.StateList.DISPLAY_EMPTY_LIST.name());
    }

    @Override
    public void displayNotConnectionView() {
        finishProgressOffLoading();
        notConnectionTextOfRecommendation.setVisibility(View.VISIBLE);
        emptyRecommendationCoverText.setVisibility(View.GONE);
        albumRecyclerView.setVisibility(View.GONE);
        utilsUI.setStateList(UtilsUI.StateList.DISPLAY_NOT_CONNECTION.name());
    }

    private void finishProgressOffLoading() {
        if (swipeRefreshLayout.isRefreshing()) {
            swipeRefreshLayout.setRefreshing(false);
        } else {
            progressBarRecommendationOfAlbums.setVisibility(View.GONE);
        }
    }

}
