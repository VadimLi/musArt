package com.example.musArt.view.utils;

import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.provider.MediaStore;
import android.util.DisplayMetrics;

import androidx.appcompat.app.ActionBar;

import com.example.musArt.R;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.URL;

public class UtilsUI {

    private final Context context;

    private ActionBar actionBar;

    private final static int QUALITY = 100;

    private static boolean displayedHomeEnabled;

    private String stateList;

    public UtilsUI(Context context) {
        this.context = context;
    }

    public UtilsUI(Context context, ActionBar actionBar) {
        this.context = context;
        this.actionBar = actionBar;
    }

    public void shareOfCover(Bitmap photoBitmap) {
        Intent shareIntent = new Intent();
        shareIntent.setAction(Intent.ACTION_SEND);
        Uri photoUri = getImageUri(photoBitmap);
        shareIntent.putExtra(Intent.EXTRA_STREAM, photoUri);
        shareIntent.setType("image/jpeg");
        Resources resources = context.getResources();
        context.startActivity(Intent.createChooser(shareIntent,
                resources.getString(R.string.share)));
    }

    public void displayHomeEnabled(boolean showed) {
        actionBar.setDisplayHomeAsUpEnabled(showed);
        actionBar.setDisplayShowHomeEnabled(showed);
    }

    public int convertDpToPixel(float dp){
        DisplayMetrics metrics = Resources.getSystem().getDisplayMetrics();
        float px = dp * (metrics.densityDpi / 160f);
        return Math.round(px);
    }

    public void setAndDisplayHomeEnabled(boolean displayedHomeEnabled) {
        UtilsUI.displayedHomeEnabled = displayedHomeEnabled;
        displayHomeEnabled(displayedHomeEnabled);
    }

    public boolean isDisplayedHomeEnabled() {
        return displayedHomeEnabled;
    }

    public static Bitmap getBitmapByUrl(String stringUrl) {
        Bitmap art = null;
        try {
            URL url = new URL(stringUrl);
            art = BitmapFactory.decodeStream(url.openConnection().getInputStream());
        } catch (IOException e) {
            e.printStackTrace();
        }
        return art;
    }

    private Uri getImageUri(Bitmap inImage) {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        inImage.compress(Bitmap.CompressFormat.JPEG, QUALITY, bytes);
        String path = MediaStore.Images.Media.insertImage(context.getContentResolver(),
                inImage, "Title", null);
        return Uri.parse(path);
    }

    public String getStateList() {
        return stateList;
    }

    public void setStateList(String stateList) {
        this.stateList = stateList;
    }

    public interface UtilsFragment {
        void displayHomeEnabled();
        void filter(String text);
        void displayEmptyListView();
    }

    public enum StateList {
        DISPLAY_LIST,
        DISPLAY_EMPTY_LIST,
        DISPLAY_NOT_CONNECTION
    }


}
