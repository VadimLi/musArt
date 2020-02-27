package com.example.musArt.presenter;

import android.os.Environment;

import java.io.File;

public class SettingsPresenter {

    private static final String TAG = SettingsPresenter.class.getSimpleName();

    private final String ART_WORKS_FOLDER = Environment.getExternalStorageDirectory()
            + "/album_artworks/";

    private SettingsView settingsView;

    public SettingsPresenter(SettingsView settingsView) {
        this.settingsView = settingsView;
    }

    public void detachView() {
        settingsView = null;
    }

    public void deleteOfCovers() {
        File artFolder = new File(ART_WORKS_FOLDER);
        String[] files = artFolder.list();
        assert files != null;
        if (files.length == 0) {
            settingsView.notifyEmptyFolder();
        } else {
            for (String fileName : files) {
                File currentFile = new File(artFolder.getPath() + "/" + fileName);
                currentFile.delete();
            }
            settingsView.notifyAboutDeleting();
        }
    }

    public interface SettingsView {
        void notifyAboutDeleting();
        void notifyEmptyFolder();
    }

}
