package com.example.coversame.presenter;

import android.net.Uri;
import android.util.Log;

import com.deezer.sdk.model.Album;
import com.deezer.sdk.model.Artist;
import com.deezer.sdk.model.Track;
import com.deezer.sdk.network.connect.DeezerConnect;
import com.deezer.sdk.network.request.DeezerRequest;
import com.deezer.sdk.network.request.DeezerRequestFactory;
import com.deezer.sdk.network.request.JsonUtils;
import com.example.coversame.R;
import com.example.coversame.model.Audio;

import java.util.ArrayList;
import java.util.List;

import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

public class AlbumPresenter {

    private AlbumView albumView;

    private final DeezerConnect deezerConnect;

    private static List<com.example.coversame.model.Album> albumList = new ArrayList<>();

    public AlbumPresenter(final DeezerConnect deezerConnect) {
        this.deezerConnect = deezerConnect;
    }

    public void initAlbums(List<Audio> audioList) {
        albumList.clear();
        Observable.from(audioList)
                .subscribeOn(Schedulers.io())
                .doOnNext(audio -> {
                    Log.d("Album: ", audio.getName());
                    DeezerRequest request = DeezerRequestFactory.requestSearchTracks(
                            audio.getArtist() + " " + audio.getName());
                    request.setId(audio.getArtist());
                    try {
                        String jsonOfTracksResponse = deezerConnect.requestSync(request);
                        List<Track> tracks = (List<Track>) JsonUtils.deserializeJson(jsonOfTracksResponse);
                        if (!tracks.isEmpty()) {
                            Track track = tracks.get(0);
                            Album album = track.getAlbum();
                            com.example.coversame.model.Album albumModel =
                                    new com.example.coversame.model.Album();
                            final String albumName = album.getTitle();
                            albumModel.setName(albumName);
                            Log.d("Album: ", albumName);
                            albumModel.setPath(album.getMediumImageUrl());
                            albumList.add(albumModel);
                        } else {
                            request = DeezerRequestFactory.requestSearchArtists(
                                    String.valueOf(audio.getArtist()));
                            request.setId(String.valueOf(audio.getArtist()));
                            String jsonOfArtistsResponse = deezerConnect.requestSync(request);
                            List<Artist> artists = (List<Artist>) JsonUtils.deserializeJson(jsonOfArtistsResponse);
                            if (!artists.isEmpty()) {
                                Artist artist = artists.get(0);
                                String artistName = artist.getName();
                                Log.d("Artist: ", artistName);
                                com.example.coversame.model.Album album =
                                        new com.example.coversame.model.Album();
                                album.setName(artistName);
                                album.setPath(artist.getMediumImageUrl());
                                albumList.add(album);
                            } else {
                                com.example.coversame.model.Album album =
                                        new com.example.coversame.model.Album();
                                album.setName(audio.getName());
                                album.setPath(getURLForResource(
                                        R.mipmap.ic_not_found_album_foreground));
                                albumList.add(album);
                            }
                        }
                    } catch (Exception e) {
                        Log.e("", "");
                    }
                })
                .observeOn(AndroidSchedulers.mainThread())
                .doOnCompleted(() -> {
                    Log.d("albumList = ", albumList.toString());
                    albumView.addAlbums(albumList);
                })
                .subscribe();
    }

    public void attachView(AlbumView albumView) {
        this.albumView = albumView;
    }

    public void detachView() {
        albumView = null;
    }

    private String getURLForResource(int resourceId) {
        return Uri.parse("android.resource://" + R.class.getPackage().getName() + "/" +
                resourceId).toString();
    }

    public interface AlbumView {
        void addAlbums(List<com.example.coversame.model.Album> albumList);
    }

}
