package com.example.coversame.presenter;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.ParcelFileDescriptor;
import android.provider.MediaStore;

import com.example.coversame.model.Album;

import java.io.FileDescriptor;
import java.util.HashSet;
import java.util.Set;

public class DownloaderCoverPresenter {

    private DownloaderCoverView downloaderCoverView;

    private Set<Album> albumHashSet = new HashSet<>();

    public void downloadCovers(ContentResolver contentResolver,
                               Context context) {
        albumHashSet.clear();
        Uri songUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
        Cursor songCursor = contentResolver.query(songUri,
                null, null, null, null);
        if (songCursor != null && songCursor.moveToFirst()) {

            int albumName = songCursor.getColumnIndex(MediaStore.Audio.Media.ALBUM);
            int artistName = songCursor.getColumnIndex(MediaStore.Audio.Media.ARTIST);
            int albumId = songCursor.getColumnIndex(MediaStore.Audio.Media.ALBUM_ID);

            do {
                String currentAlbumName = songCursor.getString(albumName);
                String currentArtistName = songCursor.getString(artistName);
                long currentAlbumId = songCursor.getLong(albumId);

                final Album album = new Album();
                album.setName(currentAlbumName);
                Bitmap bitmap = extractAlbumArt(currentAlbumId, context);
                album.setArt(bitmap);
                album.setGroupName(currentArtistName);
                albumHashSet.add(album);
            } while (songCursor.moveToNext());

        }
        downloaderCoverView.displayCovers(albumHashSet);
    }

    private Bitmap extractAlbumArt(Long albumId, Context context) {
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

    public void attachDownloaderCover(DownloaderCoverView downloaderCoverView) {
        this.downloaderCoverView = downloaderCoverView;
    }

    public void detachDownloaderCover() {
        downloaderCoverView = null;
    }

    public interface DownloaderCoverView {
        void displayCovers(Set<Album> albumList);
    }

}
