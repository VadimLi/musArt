package com.example.coversame;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.viewpager.widget.ViewPager;

import com.example.coversame.adapter.CoverFragmentPagerAdapter;
import com.example.coversame.adapter.DownloaderRecyclerAdapter;
import com.example.coversame.fragment.DownloaderCoverFragment;
import com.example.coversame.presenter.InstallationAndVerificationPresenter;
import com.google.android.material.tabs.TabLayout;

import java.util.Objects;

import butterknife.BindView;
import butterknife.ButterKnife;

public class MainActivity extends AppCompatActivity
        implements InstallationAndVerificationPresenter.VerificationView {

    private static final int PERMISSION_STORAGE_REQUEST = 1;

   // private static final String APPLICATION_ID = "370244";

    @BindView(R.id.toolbar)
    Toolbar toolbar;

    @BindView(R.id.coverTabs)
    TabLayout tabOfCoverLayout;

    @BindView(R.id.pagerOfCover)
    ViewPager pagerOfCover;

    private InstallationAndVerificationPresenter installationAndVerificationPresenter;

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        installationAndVerificationPresenter = new InstallationAndVerificationPresenter();
        installationAndVerificationPresenter.createPackagePathName(MainActivity.this);
        installationAndVerificationPresenter.initInstallingFile();
        installationAndVerificationPresenter.attachVerificationView(this);
        installationAndVerificationPresenter.setVerificationView();
    }

    /*@TODO is deprecated ?*/
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        SearchView searchView = (SearchView) menu.findItem(R.id.searchAlbums).getActionView();
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                int indexOfDownloaderFragment = 1;
                final DownloaderCoverFragment downloaderCoverFragment =
                        (DownloaderCoverFragment) getSupportFragmentManager()
                                .getFragments().get(indexOfDownloaderFragment);
                final DownloaderRecyclerAdapter downloaderRecyclerAdapter =
                        downloaderCoverFragment.getDownloaderRecyclerAdapter();
                downloaderRecyclerAdapter.getFilter().filter(newText);
                return false;
            }
        });
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            Intent settingsIntent = new Intent(this, SettingsActivity.class);
            startActivity(settingsIntent);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        installationAndVerificationPresenter.attachVerificationView(this);
    }

    @Override
    protected void onStop() {
        super.onStop();
        installationAndVerificationPresenter.detachVerificationView();
    }

    @Override
    public void transitToSettingsActivity() {
        Intent settingsIntent = new Intent(this, SettingsActivity.class);
        startActivity(settingsIntent);
    }

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
    @Override
    public void displayMainActivity() {
        if (ContextCompat.checkSelfPermission(MainActivity.this,
                Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            showRequestPermission();
        }
        setSupportActionBar(toolbar);
        CoverFragmentPagerAdapter coverFragmentPagerAdapter
                = new CoverFragmentPagerAdapter(getSupportFragmentManager(),
                FragmentPagerAdapter.BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT);
        pagerOfCover.setAdapter(coverFragmentPagerAdapter);
        tabOfCoverLayout.setupWithViewPager(pagerOfCover);
        setIconsForTabLayout(coverFragmentPagerAdapter);
        addOnTabSelectedListener(pagerOfCover);
    }

    private void showRequestPermission() {
        if (ActivityCompat.shouldShowRequestPermissionRationale(MainActivity.this,
                Manifest.permission.READ_EXTERNAL_STORAGE)) {
            ActivityCompat.requestPermissions(MainActivity.this,
                    new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                    PERMISSION_STORAGE_REQUEST);
        } else {
            ActivityCompat.requestPermissions(MainActivity.this,
                    new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                    PERMISSION_STORAGE_REQUEST);
        }
    }

    private void setIconsForTabLayout(CoverFragmentPagerAdapter coverFragmentPagerAdapter) {
        int searchDrawableId = coverFragmentPagerAdapter.getDrawablesOfId()[0];
        TabLayout.Tab tabOfSearch = tabOfCoverLayout.getTabAt(0);
        if (tabOfSearch != null) {
            tabOfSearch.setIcon(searchDrawableId)
                    .setText(R.string.search_online);
        }
        int uploadDrawableId = coverFragmentPagerAdapter.getDrawablesOfId()[1];
        TabLayout.Tab tabOfDownloads = tabOfCoverLayout.getTabAt(1);
        if (tabOfDownloads != null) {
            tabOfDownloads.setIcon(uploadDrawableId)
                    .setText(R.string.downloads);
        }
    }

    private void addOnTabSelectedListener(ViewPager viewPager) {
        tabOfCoverLayout.addOnTabSelectedListener(
                new TabLayout.ViewPagerOnTabSelectedListener(viewPager) {
            @SuppressLint("Range")
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                super.onTabSelected(tab);
                int tabIconColor = ContextCompat.getColor(getApplicationContext(),
                        R.color.colorTabIndicator);
                final Drawable tabIcon = tab.getIcon();
                Objects.requireNonNull(tabIcon)
                        .setColorFilter(tabIconColor, PorterDuff.Mode.SRC_IN);
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {
                super.onTabUnselected(tab);
                Objects.requireNonNull(tab.getIcon())
                        .clearColorFilter();
            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {
                super.onTabReselected(tab);
            }
        });
    }

}

