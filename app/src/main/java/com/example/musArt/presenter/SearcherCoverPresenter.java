package com.example.musArt.presenter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;

import androidx.annotation.RequiresApi;

import com.deezer.sdk.model.Album;
import com.deezer.sdk.model.Artist;
import com.deezer.sdk.model.Playlist;
import com.deezer.sdk.model.Track;
import com.deezer.sdk.network.connect.DeezerConnect;
import com.deezer.sdk.network.request.DeezerRequest;
import com.deezer.sdk.network.request.DeezerRequestFactory;
import com.deezer.sdk.network.request.JsonUtils;
import com.deezer.sdk.network.request.event.DeezerError;
import com.example.musArt.model.AlbumModel;
import com.example.musArt.model.Song;
import com.example.musArt.utils.NetworkChangeReceiver;
import com.example.musArt.utils.NetworkUtils;
import com.example.musArt.view.utils.UtilsUI;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

public class SearcherCoverPresenter {

    private static final String TAG = SearcherCoverPresenter.class.getSimpleName();

    private static final String DELIMITER = " ";

    private static final String DASH = "-";

    private static final String APPLICATION_ID = "370244";

    private SearcherCoverView searcherCoverView;

    private DownloaderCoverPresenter downloaderCoverPresenter;

    private final DeezerConnect deezerConnect;

    private Set<AlbumModel> albumModelSet = new HashSet<>();

    private List<Song> songList = new ArrayList<>();

    private AlbumModel albumModel;

    private boolean checkIsNotEmptyAlbums = false;

    public SearcherCoverPresenter(Context context) {
        deezerConnect  = new DeezerConnect(context, APPLICATION_ID);
        downloaderCoverPresenter = new DownloaderCoverPresenter(context);
    }

    public void attachSearcherCoverView(SearcherCoverView searchView) {
        this.searcherCoverView = searchView;
    }

    public void detachSearcherCoverView() {
        searcherCoverView = null;
    }

    public void addValuesToTitle(Bundle arguments) {
        albumModel = (AlbumModel) arguments.get("albumModel");
        assert albumModel != null;
        String albumName = albumModel.getName();
        String groupName = albumModel.getGroupName();
        StringBuilder groupNameAndAlbumNameBuilder = new StringBuilder();
        groupNameAndAlbumNameBuilder.append(groupName);
        if (!albumName.equals(groupName)) {
            groupNameAndAlbumNameBuilder
                    .append(DELIMITER)
                    .append(DASH)
                    .append(DELIMITER)
                    .append(albumName);
        }
        searcherCoverView.addValuesToTitleLayout(albumModel.getArt(),
                String.valueOf(groupNameAndAlbumNameBuilder));
    }

    @SuppressLint("CheckResult")
    @RequiresApi(api = Build.VERSION_CODES.N)
    public Disposable searchCovers() {
        albumModelSet.clear();
        List<Song> songs = albumModel.getSongs();
        return getObservableForSearchingOfAlbums()
                .subscribeOn(Schedulers.single())
                .observeOn(AndroidSchedulers.mainThread())
                .doOnComplete(() -> {
                    if (searcherCoverView != null) {
                        displaySearcherCover();
                    }
                }).subscribe();
    }

    private void displaySearcherCover() {
        if (!NetworkChangeReceiver.isCheckInternetConnection()) {
            searcherCoverView.displayNotConnectionView();
        } else if (albumModelSet.isEmpty()) {
            searcherCoverView.displayEmptyListView();
        } else {
            searcherCoverView.displayCovers(new ArrayList<>(albumModelSet));
        }
    }

    public AlbumModel getAlbumModel() {
        return albumModel;
    }

    public void setAlbumModel(AlbumModel albumModel) {
        this.albumModel = albumModel;
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    Observable<Song> getObservableForSearchingOfAlbums() {
        List<Song> songs = albumModel.getSongs();
        return Observable.fromIterable(songs)
                .doOnNext(song -> {
                    checkIsNotEmptyAlbums = false;
                    getObservableForAlbumsByGroupNameAndAlbumName().subscribe();
                    getObservableForTracksBySongFullName(song).subscribe();
                    getObservableForTracksBySongName(song).subscribe();
                    getObservableForArtistByGroupName().subscribe();
                });
    }

    private Observable<Album> getObservableForAlbumsByGroupNameAndAlbumName() throws Exception {
        String groupNameAndAlbumNameBuilder = albumModel.getGroupName() +
                DELIMITER +
                albumModel.getName();
        List<Album> albums = findAlbumsByAlbumName(groupNameAndAlbumNameBuilder);
        return Observable.fromIterable(albums)
                .take(1)
                .doOnNext(this::createNewAlbumByAlbum)
                .doOnComplete(() -> Log.i(TAG, "observable for albums by album name"));
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    @SuppressLint("CheckResult")
    private Observable<Track> getObservableForTracksBySongFullName(Song song) throws Exception {
        if (!checkIsNotEmptyAlbums) {
            List<Track> tracks = findTracksBySongName(song.getFullName());
            return Observable.fromIterable(tracks)
                    .take(1)
                    .doOnNext(track -> createNewAlbumByAlbum(track.getAlbum()))
                    .doOnComplete(() -> Log.i(TAG, "observable for tracks by song full name"));
        }
        return Observable.empty();
    }

    @SuppressLint("CheckResult")
    private Observable<Track> getObservableForTracksBySongName(Song song) throws Exception {
        if (!checkIsNotEmptyAlbums) {
            List<Track> tracks = findTracksBySongName(song.getName());
            return Observable.fromIterable(tracks)
                    .filter(track -> {
                        Artist artist = track.getArtist();
                        return artist.getName().equals(albumModel.getGroupName());
                    })
                    .doOnNext(track -> createNewAlbumByAlbum(track.getAlbum()))
                    .doOnComplete(() -> Log.i(TAG, "observable for tracks by song name"));
        }
        return Observable.empty();
    }

    private Observable<Artist> getObservableForArtistByGroupName() throws Exception {
        if (!checkIsNotEmptyAlbums) {
            List<Artist> artistList = findArtistsByGroupName(albumModel.getGroupName());
            return Observable.fromIterable(artistList)
                    .take(1)
                    .doOnNext(this::createNewAlbumByArtist)
                    .doOnComplete(() -> Log.i(TAG, "observable for artist by group name"));
        }
        return Observable.empty();
    }

    private void createNewAlbumByAlbum(Album album) {
        AlbumModel albumModel = new AlbumModel();
        albumModel.setId(this.albumModel.getId());
        albumModel.setGroupName(this.albumModel.getGroupName());
        albumModel.setName(album.getTitle());
        albumModel.setArt(UtilsUI.getBitmapByUrl(album.getBigImageUrl()));
        albumModelSet.add(albumModel);
        checkIsNotEmptyAlbums = true;
    }

    private void createNewAlbumByArtist(Artist artist) {
        AlbumModel albumModel = new AlbumModel();
        albumModel.setId(this.albumModel.getId());
        albumModel.setName(artist.getName());
        albumModel.setGroupName(artist.getName());
        albumModel.setArt(UtilsUI.getBitmapByUrl(artist.getBigImageUrl()));
        albumModelSet.add(albumModel);
    }

    private List<Track> findTracksBySongName(String songName) throws Exception {
        DeezerRequest deezerRequest = DeezerRequestFactory.requestSearchTracks(songName);
        deezerRequest.setId(songName);
        try {
            String jsonOfTracksResponse = deezerConnect.requestSync(deezerRequest);
            return (List<Track>) JsonUtils.deserializeJson(jsonOfTracksResponse);
        } catch (IOException e) {
            Log.e(TAG, Objects.requireNonNull(e.getMessage()));
        } catch (DeezerError e) {
            Log.e(TAG, Objects.requireNonNull(e.getMessage()));
        }
        return Collections.emptyList();
    }

    List<Album> findAlbumsByAlbumName(String albumName) throws Exception {
        DeezerRequest deezerRequest = DeezerRequestFactory.requestSearchAlbums(albumName);
        deezerRequest.setId(albumName);
        try {
            String jsonOfAlbumsResponse = deezerConnect.requestSync(deezerRequest);
            return (List<Album>) JsonUtils.deserializeJson(jsonOfAlbumsResponse);
        } catch (IOException e) {
            Log.e(TAG, Objects.requireNonNull(e.getMessage()));
        } catch (DeezerError e) {
            Log.e(TAG, Objects.requireNonNull(e.getMessage()));
        }
        return Collections.emptyList();
    }

    private List<Artist> findArtistsByGroupName(String groupName) throws Exception {
        DeezerRequest deezerRequest = DeezerRequestFactory.requestSearchArtists(groupName);
        deezerRequest.setId(groupName);
        try {
            String jsonOfArtistsResponse = deezerConnect.requestSync(deezerRequest);
            return (List<Artist>) JsonUtils.deserializeJson(jsonOfArtistsResponse);
        } catch (IOException e) {
            Log.e(TAG, Objects.requireNonNull(e.getMessage()));
        } catch (DeezerError e) {
            Log.e(TAG, Objects.requireNonNull(e.getMessage()));
        }
        return Collections.emptyList();
    }

    private List<Playlist> findPlayListsByGroupName(String groupName) throws Exception {
        DeezerRequest deezerRequest = DeezerRequestFactory.requestSearchPlaylists(groupName);
        deezerRequest.setId(groupName);
        try {
            String jsonOfPlayListsResponse = deezerConnect.requestSync(deezerRequest);
            return (List<Playlist>) JsonUtils.deserializeJson(jsonOfPlayListsResponse);
        } catch (IOException e) {
            Log.e(TAG, Objects.requireNonNull(e.getMessage()));
        } catch (DeezerError e) {
            Log.e(TAG, Objects.requireNonNull(e.getMessage()));
        }
        return Collections.emptyList();
    }

    Set<AlbumModel> getAlbumModelSet() {
        return albumModelSet;
    }

    DownloaderCoverPresenter getDownloaderCoverPresenter() {
        return downloaderCoverPresenter;
    }

    public void setDownloaderCoverPresenter(DownloaderCoverPresenter downloaderCoverPresenter) {
        this.downloaderCoverPresenter = downloaderCoverPresenter;
    }

    public interface SearcherCoverView extends UtilsUI.UtilsFragment, NetworkUtils.NetworkView {
        void addValuesToTitleLayout(Bitmap art, String groupNameAndAlbumName);
        void displayCovers(List<AlbumModel> albumModelList);
    }

}