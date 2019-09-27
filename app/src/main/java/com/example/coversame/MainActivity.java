package com.example.coversame;

import android.Manifest;
import android.content.ContentResolver;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
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
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.deezer.sdk.network.connect.DeezerConnect;
import com.example.coversame.adapter.AlbumRecyclerAdapter;
import com.example.coversame.model.Album;
import com.example.coversame.model.Audio;
import com.example.coversame.presenter.AlbumPresenter;

import java.util.ArrayList;
import java.util.List;

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
        setSupportActionBar(toolbar);

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
            albumPresenter = new AlbumPresenter(deezerConnect);
            albumPresenter.attachView(this);
            List<Audio> audioList = findAllAudioFromDevice();
            albumPresenter.initAlbums(audioList);
            addRefresherListener(audioList);
        }
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
        albumPresenter.detachView();
    }

    public List<Audio> findAllAudioFromDevice() {
        ContentResolver contentResolver = getContentResolver();
        Uri songUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
        Cursor songCursor = contentResolver.query(songUri,
                null, null, null, null);

        List<Audio> audioList = new ArrayList<>();

        if (songCursor != null && songCursor.moveToFirst()) {
            int songTitle = songCursor.getColumnIndex(MediaStore.Audio.Media.TITLE);
            int songArtist = songCursor.getColumnIndex(MediaStore.Audio.Media.ARTIST);
            int songAlbum = songCursor.getColumnIndex(MediaStore.Audio.Media.ALBUM);

            do {
                String currentTitle = songCursor.getString(songTitle);
                String currentArtist = songCursor.getString(songArtist);
                String currentAlbum = songCursor.getColumnName(songAlbum);
                String currentPath = songUri.getPath();

                final Audio audio = new Audio();
                audio.setArtist(currentArtist);
                audio.setName(currentTitle);
                final Album album = new Album();
                album.setName(currentAlbum);
                audio.setAlbum(album);
                audioList.add(audio);

                Log.d("title ", currentTitle);
                Log.d("path", currentPath);
                Log.d("artist ", currentArtist);
                Log.d("album ", currentAlbum);
            } while (songCursor.moveToNext());
        }
        return audioList;
    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    private void addRefresherListener(List<Audio> audioList) {
        swipeContainer.setOnRefreshListener(() -> {
            if (progressBar.getVisibility() == View.GONE) {
                albumPresenter.initAlbums(audioList);
            } else {
                final int delay = 300;
                swipeContainer.postDelayed(() ->
                        swipeContainer.setRefreshing(false), delay);
            }
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
    }

}
