package com.example.coversame.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.coversame.R;

public class AudioRecyclerAdapter extends RecyclerView.Adapter<AudioRecyclerAdapter.AudioViewHolder> {

    private LayoutInflater layoutInflater;

    public AudioRecyclerAdapter(Context context) {
        layoutInflater = LayoutInflater.from(context);
    }

    @NonNull
    @Override
    public AudioViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return null;
    }

    @Override
    public void onBindViewHolder(@NonNull AudioViewHolder holder, int position) {

    }

    @Override
    public int getItemCount() {
        return 0;
    }

    static class AudioViewHolder extends RecyclerView.ViewHolder {


        public AudioViewHolder(@NonNull View itemView) {
            super(itemView);
        }

    }

}
