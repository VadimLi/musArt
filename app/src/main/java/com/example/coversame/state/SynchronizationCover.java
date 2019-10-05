package com.example.coversame.state;

import android.content.ContentResolver;
import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore;
import android.util.Log;

import com.deezer.sdk.model.Artist;
import com.deezer.sdk.model.Track;
import com.deezer.sdk.network.connect.DeezerConnect;
import com.deezer.sdk.network.request.DeezerRequest;
import com.deezer.sdk.network.request.DeezerRequestFactory;
import com.deezer.sdk.network.request.JsonUtils;
import com.example.coversame.R;
import com.example.coversame.model.Audio;
import com.example.coversame.presenter.AlbumPresenter;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

public class SynchronizationCover extends StateCover {

    public SynchronizationCover(AlbumPresenter albumPresenter) {
        super(albumPresenter);
    }

    @Override
    public void syncCovers(ContentResolver contentResolver,
                           DeezerConnect deezerConnect) {
        albumList.clear();
        List<Audio> audioList = findAllAudioFromDevice(contentResolver);
        Observable.from(audioList)
                .subscribeOn(Schedulers.io())
                .doOnNext(audio -> {
                    Log.d("Album: ", audio.getName());
                    DeezerRequest request = DeezerRequestFactory.requestSearchTracks(
                            audio.getArtist() + " " + audio.getName());
                    request.setId(audio.getArtist());
                    try {
                        String jsonOfTracksResponse = deezerConnect.requestSync(request);
                        List<Track> tracks =
                                (List<Track>) JsonUtils.deserializeJson(jsonOfTracksResponse);
                        if (!tracks.isEmpty()) {
                            Track track = tracks.get(0);
                            com.deezer.sdk.model.Album album = track.getAlbum();
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
                            List<Artist> artists =
                                    (List<Artist>) JsonUtils.deserializeJson(jsonOfArtistsResponse);
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
                    albumPresenter.getAlbumView().addAlbums(albumList);
                })
                .subscribe();
    }

    private List<Audio> findAllAudioFromDevice(ContentResolver contentResolver) {
        Uri songUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
        Cursor songCursor = contentResolver.query(songUri,
                null, null, null, null);

        List<Audio> audioList = new ArrayList<>();

        if (songCursor != null && songCursor.moveToFirst()) {
            int songTitle = songCursor.getColumnIndex(MediaStore.Audio.Media.TITLE);
            int songArtist = songCursor.getColumnIndex(MediaStore.Audio.Media.ARTIST);
            int songAlbum = songCursor.getColumnIndex(MediaStore.Audio.Media.ALBUM);

            do {
                String currentTitle = songCursor.getString(songTitle);
                String currentArtist = songCursor.getString(songArtist);
                String currentAlbum = songCursor.getColumnName(songAlbum);
                String currentPath = songUri.getPath();

                final Audio audio = new Audio();
                audio.setArtist(currentArtist);
                audio.setName(currentTitle);
                final com.example.coversame.model.Album album = new com.example.coversame.model.Album();
                album.setName(currentAlbum);
                audio.setAlbum(album);
                audioList.add(audio);

                Log.d("title ", currentTitle);
                Log.d("path", currentPath);
                Log.d("artist ", currentArtist);
                Log.d("album ", currentAlbum);
            } while (songCursor.moveToNext());
        }
        return audioList;
    }

    private String getURLForResource(int resourceId) {
        return Uri.parse("android.resource://" + Objects.requireNonNull(
                R.class.getPackage()).getName() + "/" +
                resourceId).toString();
    }

    @Override
    public void saveCovers() {

    }

    @Override
    public void loadCovers() {

    }

}
