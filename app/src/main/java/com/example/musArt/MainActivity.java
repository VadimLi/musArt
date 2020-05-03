package com.example.musArt;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.viewpager.widget.ViewPager;

import com.example.musArt.adapter.CoverFragmentPagerAdapter;
import com.example.musArt.fragment.DownloaderOfCoverFragment;
import com.example.musArt.presenter.DownloaderCoverPresenter;
import com.example.musArt.presenter.InstallationAndVerificationPresenter;
import com.example.musArt.utils.NetworkChangeReceiver;
import com.example.musArt.view.custom.SwipeViewPager;
import com.example.musArt.view.utils.UtilsUI;
import com.google.android.material.tabs.TabLayout;

import java.util.List;
import java.util.Objects;

import butterknife.BindView;
import butterknife.ButterKnife;
import de.hdodenhof.circleimageview.CircleImageView;

public class MainActivity extends AppCompatActivity
        implements InstallationAndVerificationPresenter.VerificationView {

    private static final String TAG = MainActivity.class.getSimpleName();

    @BindView(R.id.toolbar)
    Toolbar toolbar;

    @BindView(R.id.coverTabs)
    public TabLayout tabOfCoverLayout;

    @BindView(R.id.coverTitleLayout)
    public LinearLayout coverTitleLayout;

    @BindView(R.id.titleDownloadingAlbum)
    public CircleImageView titleDownloaderCover;

    @BindView(R.id.titleGroupNameAndAlbumName)
    public TextView titleGroupNameAndAlbumName;

    @BindView(R.id.swipePagerOfTitle)
    public SwipeViewPager swipePagerOfTitle;

    private InstallationAndVerificationPresenter installationAndVerificationPresenter;

    private UtilsUI utilsUI;

    private SearchView searchView;

    private Bundle savedInstanceState;

    @SuppressLint("ResourceType")
    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        IntentFilter intentFilter = new IntentFilter("android.net.conn.CONNECTIVITY_CHANGE");
        getApplicationContext().registerReceiver(new NetworkChangeReceiver(), intentFilter);
        this.savedInstanceState = savedInstanceState;
        installationAndVerificationPresenter = new InstallationAndVerificationPresenter(this);
        installationAndVerificationPresenter.attachVerificationView(this);
        installationAndVerificationPresenter.setVerificationView();
    }

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        searchView = (SearchView) menu.findItem(R.id.searchAlbums).getActionView();
        searchView.setMaxWidth(Integer.MAX_VALUE);
        if (savedInstanceState != null) {
            Boolean focusable = (Boolean) savedInstanceState.get("focusable");
            if (focusable != null && focusable) {
                String searchViewText = String.valueOf(savedInstanceState.get("searchViewText"));
                searchView.setQuery(searchViewText, true);
                toolbar.setContentInsetsAbsolute(0, 0);
                utilsUI.displayHomeEnabled(false);
                searchView.setIconified(false);
            }
        }
        searchView.setOnSearchClickListener(v -> {
            toolbar.setContentInsetsAbsolute(0, 0);
            utilsUI.displayHomeEnabled(false);
        });
        searchView.setOnCloseListener(() -> {
            int pixels = utilsUI.convertDpToPixel(72);
            toolbar.setContentInsetsAbsolute(pixels, 0);
            FragmentManager fragmentManager = getSupportFragmentManager();
            List<Fragment> filterableFragmentOfCovers = fragmentManager.getFragments();
            if (filterableFragmentOfCovers.size() == 3) {
                filterableFragmentOfCovers.remove(1);
            }
            getUtilsFragment().displayHomeEnabled();
            return false;
        });

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                searchView.setFocusable(false);
                getUtilsFragment().filter(query);
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                if (newText.isEmpty() && coverTitleLayout.getVisibility() == View.GONE) {
                    tabOfCoverLayout.setVisibility(View.VISIBLE);
                    swipePagerOfTitle.setSwipeEnabled(true);
                } else {
                    tabOfCoverLayout.setVisibility(View.GONE);
                    swipePagerOfTitle.setSwipeEnabled(false);
                }
                getUtilsFragment().filter(newText);
                return false;
            }
        });
        return true;
    }

    private UtilsUI.UtilsFragment getUtilsFragment() {
        FragmentManager fragmentManager = getSupportFragmentManager();
        List<Fragment> filterableFragmentOfCovers = fragmentManager.getFragments();
        if (filterableFragmentOfCovers.size() == 3) {
            filterableFragmentOfCovers.remove(1);
        }
        UtilsUI.UtilsFragment utilsFragment;
        if (coverTitleLayout.getVisibility() == View.GONE) {
             utilsFragment =
                    (UtilsUI.UtilsFragment) filterableFragmentOfCovers
                            .get(tabOfCoverLayout.getSelectedTabPosition());
        } else {
            utilsFragment =
                    (UtilsUI.UtilsFragment) filterableFragmentOfCovers
                            .get(filterableFragmentOfCovers.size() - 1);
        }
        return utilsFragment;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            Intent settingsIntent = new Intent(this, SettingsActivity.class);
            startActivity(settingsIntent);
            return true;
        } else if (id == android.R.id.home) {
            FragmentManager fragmentManager = getSupportFragmentManager();
            fragmentManager.popBackStackImmediate();
            return true;
        } else if (id == R.id.action_share) {
            shareOfApplication();
        }
        return super.onOptionsItemSelected(item);
    }

    private void shareOfApplication() {
        Intent sendIntent = new Intent();
        sendIntent.setAction(Intent.ACTION_SEND);
        sendIntent.putExtra(Intent.EXTRA_TEXT,
                "https://play.google.com/store/apps/details?id="
                        + BuildConfig.APPLICATION_ID);
        sendIntent.setType("text/plain");
        startActivity(sendIntent);
    }


    @Override
    public void onStart() {
        super.onStart();
        ActionBar actionBar = getSupportActionBar();
        utilsUI = new UtilsUI(this, actionBar);
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        installationAndVerificationPresenter.attachVerificationView(this);
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        if (searchView != null) {
            outState.putBoolean("focusable", searchView.requestFocus());
            outState.putString("searchViewText", String.valueOf(searchView.getQuery()));
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        installationAndVerificationPresenter.detachVerificationView();
    }

    @Override
    public void onBackPressed() {
        if (searchView != null && !searchView.isIconified()) {
            searchView.setIconified(true);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public void transitToSettingsActivity() {
        Intent settingsIntent = new Intent(this, SettingsActivity.class);
        startActivity(settingsIntent);
    }

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
    @Override
    public void displayMainActivity() {
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        assert actionBar != null;
        actionBar.setTitle(R.string.title_app_name);
        CoverFragmentPagerAdapter coverFragmentPagerAdapter
                = new CoverFragmentPagerAdapter(getSupportFragmentManager(),
                FragmentPagerAdapter.BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT);
        swipePagerOfTitle.setAdapter(coverFragmentPagerAdapter);
        tabOfCoverLayout.setupWithViewPager(swipePagerOfTitle);
        setIconsForTabLayout(coverFragmentPagerAdapter);
        addOnTabSelectedListener(swipePagerOfTitle);
    }

    public void setIconifiedOfSearchView() {
        if (searchView != null) {
            if (!searchView.isSubmitButtonEnabled()) {
                searchView.setQuery("", false);
                searchView.setIconified(true);
            }
        }
    }

    private void setIconsForTabLayout(CoverFragmentPagerAdapter coverFragmentPagerAdapter) {
        int uploadDrawableId = coverFragmentPagerAdapter.getDrawablesOfId()[0];
        TabLayout.Tab tabOfDownloads  = tabOfCoverLayout.getTabAt(0);
        if (tabOfDownloads  != null) {
            tabOfDownloads.setIcon(uploadDrawableId)
                    .setText(R.string.my_albums);
        }
        int searchDrawableId = coverFragmentPagerAdapter.getDrawablesOfId()[1];
        TabLayout.Tab tabOfSearch = tabOfCoverLayout.getTabAt(1);
        if (tabOfSearch != null) {
            tabOfSearch.setIcon(searchDrawableId)
                    .setText(R.string.recommendations);
            setUnSelectedTab(tabOfSearch);
        }
    }

    private void addOnTabSelectedListener(ViewPager viewPager) {
        tabOfCoverLayout.addOnTabSelectedListener(
                new TabLayout.ViewPagerOnTabSelectedListener(viewPager) {
                    @RequiresApi(api = Build.VERSION_CODES.Q)
                    @SuppressLint("Range")
                    @Override
                    public void onTabSelected(@NonNull TabLayout.Tab tab) {
                        super.onTabSelected(tab);
                        Objects.requireNonNull(tab.getIcon())
                                .clearColorFilter();
                        if (tab.getPosition() == 0) {
                            refreshDownloaderCovers();
                        } else {
                            utilsUI.displayHomeEnabled(
                                    utilsUI.isDisplayedHomeEnabled());
                        }
                    }

                    @Override
                    public void onTabUnselected(TabLayout.Tab tab) {
                        super.onTabUnselected(tab);
                        setUnSelectedTab(tab);
                        if (searchView != null) {
                            searchView.setIconified(true);
                        }
                    }

                    @SuppressLint("NewApi")
                    @RequiresApi(api = Build.VERSION_CODES.N)
                    @Override
                    public void onTabReselected(TabLayout.Tab tab) {
                        super.onTabReselected(tab);
                    }
                });
    }

    @RequiresApi(api = Build.VERSION_CODES.Q)
    public void refreshDownloaderCovers() {
        FragmentManager fragmentManager = getSupportFragmentManager();
        List<Fragment> fragments = fragmentManager.getFragments();
        try {
            DownloaderOfCoverFragment downloaderOfCoverFragment =
                    (DownloaderOfCoverFragment) fragments.get(0);
            if (!downloaderOfCoverFragment.swipeRefreshLayout.isRefreshing()) {
                DownloaderCoverPresenter downloaderCoverPresenter =
                        downloaderOfCoverFragment.getDownloaderCoverPresenter();
                downloaderCoverPresenter.clearCovers(false);
                downloaderCoverPresenter.downloadCovers();
            }
        } catch (IndexOutOfBoundsException e) {
            Log.e(TAG, Objects.requireNonNull(e.getMessage()));
        }
    }

    private void setUnSelectedTab(TabLayout.Tab tab) {
        int tabIconColor = ContextCompat.getColor(getApplicationContext(),
                R.color.colorTabTextUnSelectedColor);
        final Drawable tabIcon = tab.getIcon();
        Objects.requireNonNull(tabIcon)
                .setColorFilter(tabIconColor, PorterDuff.Mode.SRC_IN);
    }

}

