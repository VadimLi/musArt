package com.example.coversame;

import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.preference.PreferenceManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.deezer.sdk.network.connect.DeezerConnect;
import com.example.coversame.adapter.AlbumRecyclerAdapter;
import com.example.coversame.model.Album;
import com.example.coversame.presenter.AlbumPresenter;

import java.io.File;
import java.util.List;
import java.util.Objects;

import butterknife.BindView;
import butterknife.ButterKnife;

public class MainActivity extends AppCompatActivity implements AlbumPresenter.AlbumView {

    private static final int PERMISSION_STORAGE_REQUEST = 1;

    private static final String APPLICATION_ID = "370244";

    @BindView(R.id.toolbar)
    Toolbar toolbar;

    @BindView(R.id.progressBarSearchOfAlbums)
    ProgressBar progressBar;

    @BindView(R.id.swipeContainer)
    SwipeRefreshLayout swipeContainer;

    @BindView(R.id.recyclerView)
    RecyclerView recyclerView;

    private AlbumPresenter albumPresenter;

    private AlbumRecyclerAdapter albumAdapter;

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

        final String packagePathName = getPackagePathName();
        final File installingFile = new File(packagePathName + "/install.txt");
        if (!installingFile.exists()) {
            Intent settingsIntent = new Intent(this, SettingsActivity.class);
            startActivity(settingsIntent);
        } else {
            if (ContextCompat.checkSelfPermission(MainActivity.this,
                    Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
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
            } else {
                final DeezerConnect deezerConnect = new DeezerConnect(this, APPLICATION_ID);
                final SharedPreferences sharedPreferences = PreferenceManager
                        .getDefaultSharedPreferences(this);
                albumPresenter = new AlbumPresenter(deezerConnect, sharedPreferences);
                albumPresenter.attachView(this);
                albumPresenter.initAlbums(getContentResolver());
                addRefresherListener();
            }
        }

    }

    private String getPackagePathName() {
        final PackageManager m = getPackageManager();
        String s = getPackageName();
        PackageInfo packageInfo = null;
        try {
            packageInfo = m.getPackageInfo(s, 0);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return Objects.requireNonNull(packageInfo)
                .applicationInfo.dataDir;
    }

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
                albumAdapter.getFilter().filter(newText);
                return false;
            }
        });
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            if (!swipeContainer.isRefreshing()) {
                Intent settingsIntent = new Intent(this, SettingsActivity.class);
                startActivity(settingsIntent);
            }
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        Log.d("state ", "stop activity");
        albumPresenter.attachView(this);
    }

    @Override
    protected void onStop() {
        super.onStop();
        Log.d("state ", "stop activity");
        if (albumPresenter != null) {
            albumPresenter.detachView();
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    private void addRefresherListener() {
        swipeContainer.setOnRefreshListener(() -> {
            albumPresenter.initAlbums(getContentResolver());
        });
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    public void addAlbums(List<Album> albumList) {
        albumPresenter.attachView(this);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        albumAdapter = new AlbumRecyclerAdapter(this, albumList);
        recyclerView.setAdapter(albumAdapter);
        albumAdapter.notifyDataSetChanged();
        if (progressBar.getVisibility() == View.VISIBLE) {
            progressBar.setVisibility(View.GONE);
        } else if (swipeContainer.getVisibility() == View.VISIBLE) {
            swipeContainer.setRefreshing(false);
        }
        setSupportActionBar(toolbar);
    }

}
