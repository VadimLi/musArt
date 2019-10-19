package com.example.coversame.adapter;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;

import com.example.coversame.R;
import com.example.coversame.fragment.DownloaderCoverFragment;
import com.example.coversame.fragment.SearcherCoverFragment;

public class CoverFragmentPagerAdapter extends FragmentPagerAdapter {

    private static final int PAGE_COUNT_OF_COVER = 2;

    private int[] drawablesOfId = new int[] {R.mipmap.ic_search_cover_foreground,
                                            R.mipmap.ic_upload_cover_foreground };

    public CoverFragmentPagerAdapter(@NonNull FragmentManager fm,
                                     int behavior) {
        super(fm, behavior);
    }

    @NonNull
    @Override
    public Fragment getItem(int position) {
        if (position == 0) {
            return new SearcherCoverFragment();
        } else {
            return new DownloaderCoverFragment();
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
