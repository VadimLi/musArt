package com.example.musArt.presenter;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.RequiresApi;

import com.deezer.sdk.model.Album;
import com.example.musArt.model.AlbumModel;
import com.example.musArt.utils.NetworkChangeReceiver;
import com.example.musArt.utils.NetworkUtils;
import com.example.musArt.view.utils.UtilsUI;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import io.reactivex.Observable;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

public class RecommendationCoverPresenter {

    private static final String TAG = RecommendationCoverPresenter.class.getSimpleName();

    private static final String DELIMITER = " ";

    private static final String DASH = "-";

    private RecommendationCoverView recommendationCoverView;

    private SearcherCoverPresenter searcherCoverPresenter;

    private StringBuilder groupAndAlbumName;

    private List<AlbumModel> newAlbumList = new ArrayList<>();

    private AlbumModel attachAlbumModel;

    private AlbumModel uploadedAlbum;

    private Bitmap titleCoverArt;

    public RecommendationCoverPresenter(Context context) {
        this.searcherCoverPresenter = new SearcherCoverPresenter(context);
    }

    public void attachRecommendationCover(RecommendationCoverView recommendationCoverView) {
        this.recommendationCoverView = recommendationCoverView;
    }

    public void detachRecommendationCover() {
        recommendationCoverView = null;
    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    public Disposable recommendCovers(Bundle arguments) {
        newAlbumList.clear();
        groupAndAlbumName = new StringBuilder();
        appendAlbumModel(arguments);
        int threadCt = Runtime.getRuntime().availableProcessors() + 1;
        ExecutorService executor = Executors.newFixedThreadPool(threadCt);
        return Observable.create((ObservableOnSubscribe<Album>) emitter -> {
            List<Album> albums = searcherCoverPresenter.findAlbumsByAlbumName(
                    String.valueOf(groupAndAlbumName));
            for (Album album : albums) {
                emitter.onNext(album);
                AlbumModel albumModel = new AlbumModel();
                albumModel.setId(attachAlbumModel.getId());
                albumModel.setArt(UtilsUI.getBitmapByUrl(album.getBigImageUrl()));
                albumModel.setName(album.getTitle());
                albumModel.setGroupName(album.getArtist().getName());
                albumModel.getSongs().addAll(attachAlbumModel.getSongs());
                newAlbumList.add(albumModel);
            }
            emitter.onComplete();
        }).subscribeOn(Schedulers.from(executor))
                .observeOn(AndroidSchedulers.mainThread())
                .doFinally(executor::shutdown)
                .doOnComplete(() -> {
                    if (recommendationCoverView != null) {
                        displayDownloaderList();
                    }
                }).subscribe();
    }

    private void appendAlbumModel(Bundle arguments) {
        attachAlbumModel = (AlbumModel) arguments.get("albumModel");
        assert attachAlbumModel != null;
        addGroupNameAndAlbumName();
    }

    private void addGroupNameAndAlbumName() {
        String groupName = attachAlbumModel.getGroupName();
        groupAndAlbumName.append(groupName);
        String albumName = attachAlbumModel.getName();
        if (!groupName.equals(albumName)) {
            groupAndAlbumName.append(DELIMITER)
                    .append(DASH)
                    .append(DELIMITER)
                    .append(albumName);
        }
    }

    SearcherCoverPresenter getSearcherCoverPresenter() {
        return searcherCoverPresenter;
    }

    public void setSearcherCoverPresenter(SearcherCoverPresenter searcherCoverPresenter) {
        this.searcherCoverPresenter = searcherCoverPresenter;
    }

    private void displayDownloaderList() {
        if (!NetworkChangeReceiver.isCheckInternetConnection()) {
            recommendationCoverView.displayNotConnectionView();
        } else if (newAlbumList.isEmpty()) {
            recommendationCoverView.displayEmptyListView();
        } else {
            recommendationCoverView.displayCovers(
                    new LinkedList<>(newAlbumList));
        }
    }

    public interface RecommendationCoverView extends UtilsUI.UtilsFragment,
            NetworkUtils.NetworkView {
        void displayCovers(List<AlbumModel> albumModels);
    }

}
