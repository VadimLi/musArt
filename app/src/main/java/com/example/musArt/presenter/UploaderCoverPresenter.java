package com.example.musArt.presenter;

import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.util.Log;

import androidx.annotation.RequiresApi;

import com.example.musArt.model.AlbumModel;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;

public class UploaderCoverPresenter {

    private static final String TAG = UploaderCoverPresenter.class.getSimpleName();

    private static final Uri ALBUM_ART_URI = Uri.parse("content://media/external/audio/albumart");

    private final String ART_WORK_FILE_NAME = Environment.getExternalStorageDirectory()
            + "/album_artworks/" + System.currentTimeMillis();

    private final Context context;

    private UploaderCoverView uploaderCoverView;

    private AlbumModel attachAlbumModel;

    public UploaderCoverPresenter(Context context) {
        this.context = context;
    }

    public void attachCoverView(UploaderCoverView uploaderCoverView) {
        this.uploaderCoverView = uploaderCoverView;
    }

    public void detachCoverView() {
        uploaderCoverView = null;
    }

    @RequiresApi(api = Build.VERSION_CODES.Q)
    public void uploadCover(AlbumModel albumModel) {
        attachAlbumModel = albumModel;
        uploaderCoverView.startLoader();
        final int milliseconds = 250;
        Observable.create(emitter -> {
            if (ensureFileExists()) {
                addPictureToFolder();
                deleteArtWorkIfExists();
                insertArtWork();
                Thread.sleep(milliseconds);
            }
            emitter.onComplete();
        }).subscribeOn(Schedulers.single())
                .observeOn(AndroidSchedulers.mainThread())
                .doOnComplete(() -> {
                    attachAlbumModel.setUploaded(true);
                    uploaderCoverView.stopLoader();
                }).subscribe();
    }

    private boolean ensureFileExists() {
        File file = new File(ART_WORK_FILE_NAME);
        if (file.exists()) {
            return true;
        } else {
            int secondSlash = ART_WORK_FILE_NAME.indexOf('/', 1);
            if (secondSlash < 1) return false;
            String directoryPath = ART_WORK_FILE_NAME.substring(0, secondSlash);
            File directory = new File(directoryPath);
            if (!directory.exists()) return false;
            file.getParentFile().mkdirs();
            try {
                return file.createNewFile();
            } catch (IOException ioe) {
                Log.e(TAG, "File creation failed", ioe);
            }
            return false;
        }
    }

    private void addPictureToFolder() {
        try {
            OutputStream outStream = new FileOutputStream(ART_WORK_FILE_NAME);
            attachAlbumModel.getArt().compress(Bitmap.CompressFormat.PNG, 100, outStream);
            outStream.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void deleteArtWorkIfExists() {
        Uri uri = ContentUris.withAppendedId(ALBUM_ART_URI, attachAlbumModel.getId());
        context.getContentResolver().delete(uri, null, null);
    }

    private void insertArtWork() {
        ContentValues values = new ContentValues();
        values.put("album_id", attachAlbumModel.getId());
        values.put("_data", ART_WORK_FILE_NAME);
        Uri newUri = context.getContentResolver()
                .insert(ALBUM_ART_URI, values);
        Log.d(TAG, String.valueOf(newUri));
    }

    public interface UploaderCoverView {
        void startLoader();
        void stopLoader();
    }

}