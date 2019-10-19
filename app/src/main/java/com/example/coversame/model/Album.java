package com.example.coversame.model;

import android.graphics.Bitmap;
import android.os.Build;

import androidx.annotation.RequiresApi;

import java.util.Objects;

public class Album {

    private String name;

    private Bitmap art;

    private String groupName;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Bitmap getArt() {
        return art;
    }

    public void setArt(Bitmap art) {
        this.art = art;
    }

    public String getGroupName() {
        return groupName;
    }

    public void setGroupName(String groupName) {
        this.groupName = groupName;
    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Album album = (Album) o;
        return Objects.equals(name, album.name) &&
                Objects.equals(groupName, album.groupName);
    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    @Override
    public int hashCode() {
        return Objects.hash(name, groupName);
    }

}
