package com.example.musArt.adapter;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;

import com.example.musArt.R;
import com.example.musArt.fragment.DownloaderOfCoverFragment;
import com.example.musArt.fragment.SearcherOfAlbumListFragment;

public class CoverFragmentPagerAdapter extends FragmentPagerAdapter {

    private static final int PAGE_COUNT_OF_COVER = 2;

    private int[] drawablesOfId = new int[] { R.mipmap.ic_download_tab_of_cover_foreground,
                                              R.mipmap.ic_recomendation_cover_foreground};

    public CoverFragmentPagerAdapter(@NonNull FragmentManager fm,
                                     int behavior) {
        super(fm, behavior);
    }

    @NonNull
    @Override
    public Fragment getItem(int position) {
        if (position == 0) {
            return new DownloaderOfCoverFragment();
        } else {
            return new SearcherOfAlbumListFragment();
        }
    }

    @Override
    public int getCount() {
        return PAGE_COUNT_OF_COVER;
    }

    public int[] getDrawablesOfId() {
        return drawablesOfId;
    }

}
