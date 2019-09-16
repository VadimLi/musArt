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
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import io.reactivex.Observable;
import io.reactivex.Observer;
import io.reactivex.Scheduler;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

public class AlbumPresenter {

    private final AlbumView albumView;

    private final DeezerConnect deezerConnect;

    private DeezerRequest request;

    private List<com.example.coversame.model.Album> albumList = new ArrayList<>();

    private Thread thread;

    public AlbumPresenter(AlbumView albumView, DeezerConnect deezerConnect) {
        this.albumView = albumView;
        this.deezerConnect = deezerConnect;
    }

    public void initAlbums(List<Audio> audioList)   {
//        executor = Executors.newFixedThreadPool(audioList.size() * 2);
//        Scheduler scheduler = Schedulers.from(executor);

        Observable.fromIterable(audioList)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<Audio>() {
                    @Override
                    public void onSubscribe(Disposable d) { }

                    @Override
                    public void onNext(Audio audio) {
                        Log.d("Album: ", audioList.get(0).getAlbum().getName());
                        request = DeezerRequestFactory.requestSearchTracks(
                                audioList.get(0).getArtist() + " " + audioList.get(0).getName());
                        request.setId(audioList.get(0).getArtist());
                        RequestListener listenerForTracks = requestListenerForTracks();
                        deezerConnect.requestAsync(request, listenerForTracks);
                    }

                    @Override
                    public void onError(Throwable e) { }

                    @Override
                    public void onComplete() {
                        albumView.addAlbums(albumList);
                    }
                });
    }

    private JsonRequestListener requestListenerForArtists() {
        return new JsonRequestListener() {
            @Override
            public void onResult(Object result, Object requestId) {
                thread = new Thread(() -> {
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
                });
                thread.start();
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
                thread = new Thread(() -> {
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
                });
                thread.start();
            }

            @Override
            public void onUnparsedResult(String requestResponse, Object requestId) {
                request = DeezerRequestFactory.requestSearchArtists(
                        String.valueOf(requestId));
                request.setId(String.valueOf(requestId));
                RequestListener listenerForArtists = requestListenerForArtists();
                deezerConnect.requestAsync(request, listenerForArtists);
            }

            @Override
            public void onException(Exception e, Object requestId) { }
        };

    }

    public interface AlbumView {
        void addAlbums(List<com.example.coversame.model.Album> albumList);
    }

}
