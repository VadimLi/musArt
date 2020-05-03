package com.example.musArt.model;

import android.graphics.Bitmap;
import android.os.Build;
import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.RequiresApi;

import org.joda.time.DateTime;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class AlbumModel implements Parcelable, Comparable<AlbumModel> {

    private long id;

    private String name;

    private Bitmap art;

    private String groupName;

    private DateTime dateTime;

    private List<Song> songs = new ArrayList<>();

    private Boolean uploaded;

    public AlbumModel() {}

    @RequiresApi(api = Build.VERSION_CODES.Q)
    public AlbumModel(Parcel in){
        this.id = in.readLong();
        this.name = in.readString();
        this.groupName = in.readString();
        this.uploaded = in.readBoolean();
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

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

    public DateTime getDateTime() {
        return dateTime;
    }

    public void setDateTime(DateTime dateTime) {
        this.dateTime = dateTime;
    }

    public List<Song> getSongs() {
        return songs;
    }

    public void setSongs(List<Song> songs) {
        this.songs = songs;
    }

    public Boolean isUploaded() {
        return uploaded;
    }

    public void setUploaded(Boolean uploaded) {
        this.uploaded = uploaded;
    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AlbumModel albumModel = (AlbumModel) o;
        return Objects.equals(name, albumModel.name) &&
                Objects.equals(groupName, albumModel.groupName);
    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    @Override
    public int hashCode() {
        return Objects.hash(name, groupName);
    }

    @Override
    public int compareTo(AlbumModel albumModel) {
        return getDateTime().compareTo(albumModel.getDateTime());
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @RequiresApi(api = Build.VERSION_CODES.Q)
    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeLong(id);
        dest.writeString(name);
        dest.writeString(groupName);
    }

    public static final Parcelable.Creator CREATOR = new Parcelable.Creator() {

        @RequiresApi(api = Build.VERSION_CODES.Q)
        public AlbumModel createFromParcel(Parcel in) {
            return new AlbumModel(in);
        }

        public AlbumModel[] newArray(int size) {
            return new AlbumModel[size];
        }

    };

}
