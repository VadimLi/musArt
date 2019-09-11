package com.example.coversame.adapter;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.ImageDecoder;
import android.net.Uri;
import android.os.Build;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.recyclerview.widget.RecyclerView;

import com.example.coversame.R;
import com.example.coversame.model.Album;

import java.io.File;
import java.io.IOException;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class AlbumRecyclerAdapter extends RecyclerView.Adapter<AlbumRecyclerAdapter.AlbumViewHolder> {

    private LayoutInflater layoutInflater;

    private List<Album> albumList;

    public AlbumRecyclerAdapter(Context context, List<Album> albumList) {
        layoutInflater = LayoutInflater.from(context);
        this.albumList = albumList;
    }

    @NonNull
    @Override
    public AlbumViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        final View view = layoutInflater.inflate(R.layout.album, parent, false);
        return new AlbumRecyclerAdapter.AlbumViewHolder(view);
    }

    @RequiresApi(api = Build.VERSION_CODES.P)
    @Override
    public void onBindViewHolder(@NonNull AlbumViewHolder holder, int position) {
        Album album = albumList.get(position);
        Bitmap covertBitmap;
        try {
            Log.d("path: ", album.getPath());
            covertBitmap = ImageDecoder.decodeBitmap(
                    ImageDecoder.createSource(new File(album.getPath())));
            holder.coverAlbum.setImageBitmap(covertBitmap);
        } catch (IOException e) {
            e.printStackTrace();
        }
        holder.fullName.setText(album.getName());
    }

    @Override
    public int getItemCount() {
        return albumList.size();
    }

    static class AlbumViewHolder extends RecyclerView.ViewHolder {

        @BindView(R.id.coverAlbum)
        ImageView coverAlbum;

        @BindView(R.id.fullName)
        TextView fullName;

        AlbumViewHolder(@NonNull View itemView) {
            super(itemView);
            ButterKnife.bind(itemView);
        }

    }

}
