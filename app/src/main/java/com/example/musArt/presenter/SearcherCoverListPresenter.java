package com.example.musArt.presenter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Build;
import android.util.Log;

import androidx.annotation.RequiresApi;

import com.deezer.sdk.network.connect.DeezerConnect;
import com.example.musArt.model.AlbumModel;
import com.example.musArt.model.Song;
import com.example.musArt.utils.NetworkChangeReceiver;
import com.example.musArt.utils.NetworkUtils;
import com.example.musArt.view.utils.UtilsUI;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentLinkedQueue;

import io.reactivex.Observable;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

public class SearcherCoverListPresenter {

    private static final String TAG = SearcherCoverListPresenter.class.getSimpleName();

    private static final String APPLICATION_ID = "370244";

    private SearcherCoverListView searcherCoverListView;

    private DownloaderCoverPresenter downloaderCoverPresenter;

    private SearcherCoverPresenter searcherCoverPresenter;

    private Set<AlbumModel> searcherAlbumModelSet = new HashSet<>();

    private DeezerConnect deezerConnect;

    public SearcherCoverListPresenter(Context context) {
        this.downloaderCoverPresenter = new DownloaderCoverPresenter(context);
        searcherCoverPresenter = new SearcherCoverPresenter(context);
        deezerConnect  = new DeezerConnect(context, APPLICATION_ID);
    }

    public void attachSearcherCoverView(SearcherCoverListView searcherCoverListView) {
        this.searcherCoverListView = searcherCoverListView;
    }

    public void detachSearcherCoverView() {
        searcherCoverListView = null;
    }

    @SuppressLint({"CheckResult", "NewApi"})
    @RequiresApi(api = Build.VERSION_CODES.N)
    public Disposable searchCovers() {
        return Observable.create((ObservableOnSubscribe<Observable<Song>>) emitter -> {
            searcherAlbumModelSet.clear();
            List<AlbumModel> albumModels = downloaderCoverPresenter.getAlbumModelList();
            ConcurrentLinkedQueue<AlbumModel> albumModelConcurrentLinkedQueue =
                    new ConcurrentLinkedQueue<>(albumModels);
            for (AlbumModel albumModel : albumModelConcurrentLinkedQueue) {
                searcherCoverPresenter.setAlbumModel(albumModel);
                Set<AlbumModel> albumModelSet = searcherCoverPresenter.getAlbumModelSet();
                searcherCoverPresenter
                        .getObservableForSearchingOfAlbums()
                        .subscribeOn(Schedulers.trampoline())
                        .doOnComplete(() -> {
                            Log.i(TAG, "completed " + albumModel.getName());
                            searcherAlbumModelSet.addAll(albumModelSet);
                            albumModelSet.clear();
                        }).subscribe();
            }
            emitter.onComplete();
        }).subscribeOn(Schedulers.single())
                .observeOn(AndroidSchedulers.mainThread())
                .doOnComplete(() -> {
                    if (searcherCoverListView != null) {
                        displaySearcherCoverList();
                    }
                }).subscribe();
    }

    private void displaySearcherCoverList() {
        if (!NetworkChangeReceiver.isCheckInternetConnection()) {
            searcherCoverListView.displayNotConnectionView();
        } else if (searcherAlbumModelSet.isEmpty()) {
            searcherCoverListView.displayEmptyListView();
        } else {
            searcherCoverListView.displayCovers(new ArrayList<>(searcherAlbumModelSet));
        }
    }

    public DeezerConnect getDeezerConnect() {
        return deezerConnect;
    }

    public interface SearcherCoverListView extends UtilsUI.UtilsFragment,
            NetworkUtils.NetworkView {
        void displayCovers(List<AlbumModel> albumModelList);
    }

}