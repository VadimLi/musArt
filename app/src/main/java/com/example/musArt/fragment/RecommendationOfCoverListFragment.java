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
import com.example.musArt.adapter.RecommendationCoverListRecyclerAdapter;
import com.example.musArt.model.AlbumModel;
import com.example.musArt.presenter.RecommendationCoverPresenter;
import com.example.musArt.view.utils.UtilsUI;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class RecommendationOfCoverListFragment extends Fragment
        implements RecommendationCoverPresenter.RecommendationCoverView {

    @BindView(R.id.progressBarRecommendationOfAlbumList)
    ProgressBar progressBarRecommendationOfAlbums;

    @BindView(R.id.swipeRecommendationContainerOfAlbumList)
    SwipeRefreshLayout swipeRefreshLayout;

    @BindView(R.id.notConnectionTextOfRecommendationList)
    TextView notConnectionTextOfRecommendationList;

    @BindView(R.id.emptyRecommendationCoverListText)
    TextView emptyRecommendationCoverListText;

    @BindView(R.id.recommendationRecyclerViewOfAlbumList)
    RecyclerView albumRecyclerView;

    private Context context;

    private MainActivity mainActivity;

    private RecommendationCoverListRecyclerAdapter recommendationCoverListRecyclerAdapter;

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
        View view = inflater.inflate(R.layout.fragment_page_of_recommendation_cover_list,
                container, false);
        ButterKnife.bind(this, view);
        swipeRefreshLayout.setColorSchemeResources(R.color.colorSwipeRefreshLayout);
        mainActivity = (MainActivity) getActivity();
        assert mainActivity != null;
        final ActionBar actionBar = mainActivity.getSupportActionBar();
        context = getContext();
        utilsUI = new UtilsUI(context, actionBar);
        utilsUI.setStateList(UtilsUI.StateList.DISPLAY_LIST.name());
        displayCovers(new ArrayList<>());
        recommendationCoverPresenter = new RecommendationCoverPresenter(context);
        recommendationCoverPresenter.attachRecommendationCover(this);
        displayCovers(new ArrayList<>());
        Bundle arguments = getArguments();
        addSwipeRefresherListener(arguments);
        return view;
    }

    @SuppressLint("NewApi")
    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        if (savedInstanceState == null) {
            progressBarRecommendationOfAlbums.setVisibility(View.VISIBLE);
            Bundle arguments = getArguments();
            recommendationCoverPresenter.recommendCovers(arguments);
        } else {
            AlbumModel[] albumModels = (AlbumModel[])
                    savedInstanceState.get("albumRecommendationListModels");
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
        recommendationCoverPresenter.attachRecommendationCover(this);
        utilsUI.setAndDisplayHomeEnabled(true);
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

    @SuppressLint("NewApi")
    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        if (progressBarRecommendationOfAlbums.getVisibility() == View.VISIBLE) {
            outState.putInt("typeOfLoader", 1);
        } else if (swipeRefreshLayout.isRefreshing()) {
            outState.putInt("typeOfLoader", 2);
        }
        outState.putParcelableArray("albumRecommendationListModels",
                recommendationCoverListRecyclerAdapter.getRecommendationAlbumModelArray());
        outState.putString("stateList", utilsUI.getStateList());
        recommendationCoverPresenter.recommendCovers(getArguments()).dispose();
    }

    @RequiresApi(api = Build.VERSION_CODES.Q)
    @Override
    public void onDestroy() {
        super.onDestroy();
        utilsUI.setAndDisplayHomeEnabled(false);
        recommendationCoverPresenter.detachRecommendationCover();
    }

    @Override
    public void displayCovers(List<AlbumModel> albumModelHashSet) {
        albumRecyclerView.setLayoutManager(new LinearLayoutManager(context));
        recommendationCoverListRecyclerAdapter =
                new RecommendationCoverListRecyclerAdapter(context,
                        recommendationCoverPresenter, albumModelHashSet);
        albumRecyclerView.setAdapter(recommendationCoverListRecyclerAdapter);
        if (!albumModelHashSet.isEmpty()) {
            displayCoverList();
        }
    }

    private void displayCoverList() {
        finishProgressOffLoading();
        albumRecyclerView.setVisibility(View.VISIBLE);
        emptyRecommendationCoverListText.setVisibility(View.GONE);
        notConnectionTextOfRecommendationList.setVisibility(View.GONE);
        utilsUI.setStateList(UtilsUI.StateList.DISPLAY_LIST.name());
    }

    public RecyclerView.Adapter<RecommendationCoverListRecyclerAdapter
            .RecommendationViewHolder> getRecyclerAdapter() {
        return recommendationCoverListRecyclerAdapter;
    }

    @Override
    public void displayHomeEnabled() {
        utilsUI.displayHomeEnabled(utilsUI.isDisplayedHomeEnabled());
    }

    @Override
    public void filter(String text) {
        recommendationCoverListRecyclerAdapter.getFilter().filter(text);
    }

    @Override
    public void displayEmptyListView() {
        finishProgressOffLoading();
        emptyRecommendationCoverListText.setVisibility(View.VISIBLE);
        albumRecyclerView.setVisibility(View.GONE);
        notConnectionTextOfRecommendationList.setVisibility(View.GONE);
        utilsUI.setStateList(UtilsUI.StateList.DISPLAY_EMPTY_LIST.name());
    }

    @Override
    public void displayNotConnectionView() {
        finishProgressOffLoading();
        notConnectionTextOfRecommendationList.setVisibility(View.VISIBLE);
        emptyRecommendationCoverListText.setVisibility(View.GONE);
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
