package com.example.coversame.presenter;

import android.util.Log;

import com.deezer.sdk.model.Album;
import com.deezer.sdk.model.Artist;
import com.deezer.sdk.model.Track;
import com.deezer.sdk.network.connect.DeezerConnect;
import com.deezer.sdk.network.request.DeezerRequest;
import com.deezer.sdk.network.request.DeezerRequestFactory;
import com.deezer.sdk.network.request.event.JsonRequestListener;
import com.deezer.sdk.network.request.event.RequestListener;
import com.example.coversame.model.Audio;

import java.util.ArrayList;
import java.util.List;

public class AlbumPresenter {

    private final AlbumView albumView;

    private final DeezerConnect deezerConnect;

    private DeezerRequest request;

    private List<com.example.coversame.model.Album> albumList = new ArrayList<>();

    public AlbumPresenter(AlbumView albumView, DeezerConnect deezerConnect) {
        this.albumView = albumView;
        this.deezerConnect = deezerConnect;
    }

    public void initAlbums(List<Audio> audioList) {
        requestListenerForTracks();
        requestListenerForArtists();
        for (Audio audio : audioList) {
            Log.d("Album: ", audio.getName());
            request = DeezerRequestFactory.requestSearchTracks(
                    audio.getArtist() + " " + audio.getName());
            request.setId(audio.getArtist());
            deezerConnect.requestAsync(request, requestListenerForTracks());
        }
        albumView.addAlbums(albumList);
    }

    private JsonRequestListener requestListenerForArtists() {
        return new JsonRequestListener() {
            @Override
            public void onResult(Object result, Object requestId) {
                List<Artist> artistList = (List<Artist>) result;
                if (!artistList.isEmpty()) {
                    Artist artist = artistList.get(0);
                    String artistName = artist.getName();
                    Log.d("Artist: ", artistName);
                    com.example.coversame.model.Album album =
                            new com.example.coversame.model.Album();
                    album.setName(artistName);
                    album.setPath(artist.getMediumImageUrl());
                    albumList.add(album);
                }
            }

            @Override
            public void onUnparsedResult(String requestResponse, Object requestId) { }

            @Override
            public void onException(Exception e, Object requestId) { }
        };
    }

    private JsonRequestListener requestListenerForTracks() {
        return new JsonRequestListener() {
            @Override
            public void onResult(Object result, Object requestId) {
                List<Track> tracks = (List<Track>) result;
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
                    onUnparsedResult(String.valueOf(requestId), requestId);
                }
            }

            @Override
            public void onUnparsedResult(String requestResponse, Object requestId) {
                request = DeezerRequestFactory.requestSearchArtists(
                        String.valueOf(requestId));
                request.setId(String.valueOf(requestId));
                deezerConnect.requestAsync(request, requestListenerForArtists());
            }

            @Override
            public void onException(Exception e, Object requestId) { }
        };
    }

    public interface AlbumView {
        void addAlbums(List<com.example.coversame.model.Album> albumList);
    }

}
