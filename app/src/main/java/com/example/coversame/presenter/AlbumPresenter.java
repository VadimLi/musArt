package com.example.coversame.presenter;

import android.content.ContentResolver;
import android.content.SharedPreferences;

import com.deezer.sdk.network.connect.DeezerConnect;
import com.example.coversame.state.StateCover;
import com.example.coversame.state.SynchronizationCover;

import java.util.ArrayList;
import java.util.List;

public class AlbumPresenter {

    private AlbumView albumView;

    private final DeezerConnect deezerConnect;

    private static List<com.example.coversame.model.Album> albumList = new ArrayList<>();

    private StateCover stateCover;

    private boolean synchronization;

    private boolean loadingOfCovers;

    private boolean savingOfCovers;

    private boolean savingWithReplacement;

    public AlbumPresenter(final DeezerConnect deezerConnect,
                          final SharedPreferences sharedPreferences) {
        this.deezerConnect = deezerConnect;
        stateCover = new SynchronizationCover(this);

        synchronization = sharedPreferences.getBoolean("synchronization_of_covers",
                false);
        loadingOfCovers = sharedPreferences.getBoolean("loading_of_covers",
                false);
        savingOfCovers = sharedPreferences.getBoolean("saving_of_covers",
                false);
        savingWithReplacement = sharedPreferences.getBoolean("saving_with_replacement",
                false);
    }

    public void initAlbums(ContentResolver contentResolver) {
        stateCover.syncCovers(contentResolver, deezerConnect);
        stateCover.saveCovers();
        stateCover.loadCovers();
    }

    public AlbumView getAlbumView() {
        return albumView;
    }

    public void changeOfCoversState(StateCover stateCover) {
        this.stateCover = stateCover;
    }

    public StateCover getStateCover() {
        return stateCover;
    }

    public void attachView(AlbumView albumView) {
        this.albumView = albumView;
    }

    public void detachView() {
        albumView = null;
    }

    public boolean isSynchronization() {
        return synchronization;
    }

    public boolean isLoadingOfCovers() {
        return loadingOfCovers;
    }

    public boolean isSavingOfCovers() {
        return savingOfCovers;
    }

    public boolean isSavingWithReplacement() {
        return savingWithReplacement;
    }

    public interface AlbumView {
        void addAlbums(List<com.example.coversame.model.Album> albumList);
    }

}
