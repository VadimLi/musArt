package com.example.coversame.state;

import android.content.ContentResolver;

import com.deezer.sdk.network.connect.DeezerConnect;
import com.example.coversame.model.Album;
import com.example.coversame.presenter.AlbumPresenter;

import java.util.ArrayList;
import java.util.List;

public abstract class StateCover {

    AlbumPresenter albumPresenter;

    protected List<Album> albumList = new ArrayList<>();

    public StateCover(AlbumPresenter albumPresenter) {
        this.albumPresenter = albumPresenter;
    }

    public abstract void syncCovers(ContentResolver contentResolver, DeezerConnect deezerConnect);
    public abstract void saveCovers();
    public abstract void loadCovers();

    public List<Album> getAlbumList() {
        return albumList;
    }

    public void setAlbumList(List<Album> albumList) {
        this.albumList = albumList;
    }

}
