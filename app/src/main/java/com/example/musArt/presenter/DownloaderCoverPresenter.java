package com.example.musArt.presenter;

import android.annotation.SuppressLint;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.ParcelFileDescriptor;
import android.provider.MediaStore;

import androidx.annotation.RequiresApi;

import com.example.musArt.model.AlbumModel;
import com.example.musArt.model.Song;
import com.example.musArt.view.utils.UtilsUI;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;

import java.io.FileDescriptor;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import io.reactivex.Emitter;
import io.reactivex.Observable;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

public class DownloaderCoverPresenter {

    private DownloaderCoverView downloaderCoverView;

    private Context context;

    private static Set<AlbumModel> albumModels = new HashSet<>();

    private static Cursor songCursor;

    private List<Song> songArrayList = new ArrayList<>();

    public DownloaderCoverPresenter(Context context) {
        this.context = context;
    }

    public void clearCovers(boolean started) {
        if (started) {
            downloaderCoverView.displayCovers(new ArrayList<>());
        }
        albumModels.clear();
        songArrayList.clear();
    }

    @RequiresApi(api = Build.VERSION_CODES.Q)
    public Disposable downloadCovers() {
        return getObservableForDownloadOfCover()
                .doOnComplete(() -> {
                    if (downloaderCoverView != null) {
                        displayDownloaderList();
                    }
                }).subscribe();
    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    private Observable<? extends AlbumModel> getObservableForDownloadOfCover() {
        return Observable.create((ObservableOnSubscribe<AlbumModel>) emitter -> {
            extractOfAlbums(emitter);
            emitter.onComplete();
        }).subscribeOn(Schedulers.single())
                .observeOn(AndroidSchedulers.mainThread());
    }

    private void displayDownloaderList() {
        if (albumModels.isEmpty()) {
            downloaderCoverView.displayEmptyListView();
        } else {
            downloaderCoverView.displayCovers(new LinkedList<>(albumModels));
        }
    }

    @SuppressLint("NewApi")
    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    private void extractOfAlbums(Emitter<AlbumModel> emitter) {
        Uri songUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
        ContentResolver contentResolver = context.getContentResolver();
        songCursor = contentResolver.query(songUri,
                null, null, null, null);
        if (songCursor != null && songCursor.moveToFirst()) {
            do {
                AlbumModel albumModel = createAlbumAndSong();
                emitter.onNext(albumModel);
            } while (songCursor.moveToNext());
        }
        addToAlbumModelLinkedList();
        songCursor = null;
    }

    private AlbumModel createAlbumAndSong() {
        final AlbumModel albumModel = new AlbumModel();
        long currentAlbumId = SongCurrentData.AlbumId.getLong();
        albumModel.setId(currentAlbumId);

        String currentAlbumName = SongCurrentData.AlbumName.getString();
        albumModel.setName(currentAlbumName);

        Bitmap bitmap = extractAlbumArt(currentAlbumId);
        if (bitmap != null) {
            albumModel.setUploaded(true);
            albumModel.setArt(bitmap);
        } else {
            albumModel.setUploaded(false);
        }

        String currentArtistName = SongCurrentData.ArtistName.getString();
        albumModel.setGroupName(currentArtistName);

        long currentDateToMilliseconds = SongCurrentData.DateInt.getLong();
        DateTime currentDateTime = new DateTime(currentDateToMilliseconds,
                DateTimeZone.UTC);
        albumModel.setDateTime(currentDateTime);

        final Song song = new Song();
        String currentSongName = SongCurrentData.SongName.getString();
        song.setName(currentSongName);

        long currentSongId = SongCurrentData.SongId.getLong();
        song.setId(currentSongId);

        String currentSongFullName = SongCurrentData.SongFullName.getString();
        String songFullName = parseGroupNameAndTrackOfSongName(currentSongFullName);
        song.setFullName(songFullName);

        song.setAlbumModel(albumModel);
        songArrayList.add(song);

        albumModels.add(albumModel);
        return albumModel;
    }

    private Bitmap extractAlbumArt(Long albumId) {
        Bitmap bitmap = null;
        try {
            final Uri artworkUri = Uri
                    .parse("content://media/external/audio/albumart");
            final Uri uri = ContentUris.withAppendedId(artworkUri, albumId);
            ParcelFileDescriptor pfd = context.getContentResolver()
                    .openFileDescriptor(uri, "r");
            if (pfd != null) {
                FileDescriptor fd = pfd.getFileDescriptor();
                bitmap = BitmapFactory.decodeFileDescriptor(fd);
            }
        } catch (Exception ignored) { }
        return bitmap;
    }

    private String parseGroupNameAndTrackOfSongName(String songName) {
        if (songName != null && songName.indexOf(".") > 0) {
            songName = songName.substring(0, songName.lastIndexOf("."));
        }
        return songName;
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    private void addToAlbumModelLinkedList() {
        albumModels.forEach(albumModel ->
                songArrayList.stream()
                        .filter(song -> albumModel.equals(song.getAlbumModel()))
                        .forEach(song -> {
                            albumModel.getSongs().add(song);
                            albumModels.add(albumModel);
                        }));
    }

    public void attachDownloaderCover(DownloaderCoverView downloaderCoverView) {
        this.downloaderCoverView = downloaderCoverView;
    }

    public void detachDownloaderCover() {
        downloaderCoverView = null;
    }

    public List<AlbumModel> getAlbumModelList() {
        return new ArrayList<>(albumModels);
    }

    public List<Song> getSongArrayList() {
        return songArrayList;
    }

    public void setSongArrayList(List<Song> songArrayList) {
        this.songArrayList = songArrayList;
    }

    public interface DownloaderCoverView extends UtilsUI.UtilsFragment {
        void displayCovers(List<AlbumModel> albumModelList);
    }

    private enum SongCurrentData {

        AlbumId(SongColumnIndex.AlbumId.getColumnIndex()),
        AlbumName(SongColumnIndex.AlbumName.getColumnIndex()),
        ArtistName(SongColumnIndex.ArtistName.getColumnIndex()),
        DateInt(SongColumnIndex.DateInt.getColumnIndex()),
        SongId(SongColumnIndex.SongId.getColumnIndex()),
        SongName(SongColumnIndex.SongName.getColumnIndex()),
        SongFullName(SongColumnIndex.SongFullName.getColumnIndex());

        private int columnIndex;

        SongCurrentData(int columnIndex) {
            this.columnIndex = columnIndex;
        }

        public long getLong() {
            return songCursor.getLong(columnIndex);
        }

        public String getString() {
            return songCursor.getString(columnIndex);
        }

        private enum SongColumnIndex {

            AlbumId(MediaStore.Audio.Playlists.Members.ALBUM_ID),
            AlbumName(MediaStore.Audio.Media.ALBUM),
            ArtistName(MediaStore.Audio.Media.ARTIST),
            DateInt(MediaStore.Audio.Media.DATE_MODIFIED),
            SongId(MediaStore.Audio.Media._ID),
            SongName(MediaStore.Audio.Media.TITLE),
            SongFullName(MediaStore.Audio.Media.DISPLAY_NAME);

            private String mediaString;

            SongColumnIndex(String mediaString) {
                this.mediaString = mediaString;
            }

            public int getColumnIndex() {
                return songCursor.getColumnIndex(mediaString);
            }

        }

    }

}
