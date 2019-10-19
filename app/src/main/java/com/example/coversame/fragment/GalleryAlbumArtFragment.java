package com.example.coversame.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.fragment.app.DialogFragment;

import com.example.coversame.R;

import butterknife.ButterKnife;

public class GalleryAlbumArtFragment extends DialogFragment {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_page_of_covers,
                container, false);
        setStyle(STYLE_NORMAL, android.R.style.Theme_Black_NoTitleBar_Fullscreen);
        ButterKnife.bind(this, view);
        return view;
    }
}
