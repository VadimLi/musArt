package com.example.musArt;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.PreferenceScreen;
import androidx.preference.TwoStatePreference;

import com.example.musArt.presenter.InstallationAndVerificationPresenter;
import com.example.musArt.presenter.SettingsPresenter;

import java.util.Objects;

import butterknife.BindView;
import butterknife.ButterKnife;

public class SettingsActivity extends AppCompatActivity
        implements InstallationAndVerificationPresenter.InstallationView{

    private static final int STORAGE_PERMISSION_ALL = 1;

    @BindView(R.id.toolbar)
    Toolbar toolbar;

    @BindView(R.id.confirm)
    Button confirm;

    private InstallationAndVerificationPresenter installationAndVerificationPresenter;

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.settings_activity);
        ButterKnife.bind(this);
        if (savedInstanceState == null) {
            if (ContextCompat.checkSelfPermission(
                    this, Manifest.permission.WRITE_EXTERNAL_STORAGE )
                    != PackageManager.PERMISSION_GRANTED ) {
                ActivityCompat.requestPermissions(SettingsActivity.this,
                        new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE,
                                Manifest.permission.READ_EXTERNAL_STORAGE},
                        STORAGE_PERMISSION_ALL);
                getSupportFragmentManager()
                        .beginTransaction()
                        .replace(R.id.permissionsOrSettingsFrameLayout, new PermissionFragment())
                        .commit();
            } else {
                getSupportFragmentManager()
                        .beginTransaction()
                        .replace(R.id.permissionsOrSettingsFrameLayout, new SettingsFragment())
                        .commit();
            }
        }
        installView();
    }

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        if (requestCode == STORAGE_PERMISSION_ALL &&
                grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            installView();
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.permissionsOrSettingsFrameLayout, new SettingsFragment())
                    .commit();
        }
    }

    @Override
    public void confirmSettingsActivity() {
        confirm.setOnClickListener(v -> {
            installationAndVerificationPresenter.createInstallingFile();
            Intent mainIntent = new Intent(this, MainActivity.class);
            startActivity(mainIntent);
        });
    }

    @Override
    public void displaySettingsActivity() {
        confirm.setVisibility(View.GONE);
        toolbar.setTitle(R.string.title_activity_settings);
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar())
                .setDefaultDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
    private void installView() {
        installationAndVerificationPresenter =
                new InstallationAndVerificationPresenter(this);
        installationAndVerificationPresenter.attachInstallationView(this);
        installationAndVerificationPresenter.setInstallationView();
    }

    public static class PermissionFragment extends Fragment {

        private SettingsActivity settingsActivity;

        @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            View view = inflater.inflate(R.layout.fragment_page_of_permission,
                    container, false);
            settingsActivity = (SettingsActivity) getActivity();
            assert settingsActivity != null;
            settingsActivity.confirm.setOnClickListener(v ->
                    ActivityCompat.requestPermissions(settingsActivity,
                    new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE,
                            Manifest.permission.READ_EXTERNAL_STORAGE},
                    STORAGE_PERMISSION_ALL));
            return view;
        }

    }

    public static class SettingsFragment extends PreferenceFragmentCompat
            implements SettingsPresenter.SettingsView {

        private TwoStatePreference deleteOfAllCovers;

        private SettingsPresenter settingsPresenter;

        private Context context;

        private SettingsActivity settingsActivity;

        private AlertDialog alertDialog;

        @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
        @Override
        public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
            setPreferencesFromResource(R.xml.root_preferences, rootKey);
            SettingsActivity settingsActivity = (SettingsActivity) getActivity();
            assert settingsActivity != null;
            context = settingsActivity.getApplicationContext();
            deleteOfAllCovers = findPreference("deleteOfAllCovers");
            assert deleteOfAllCovers != null;
            deleteOfAllCovers.setChecked(false);
            deleteOfAllCovers.setOnPreferenceClickListener(preference -> {
                AlertDialog.Builder builder =
                        new AlertDialog.Builder(Objects.requireNonNull(
                                SettingsFragment.this.getContext()),
                                R.style.Theme_AppCompat_Light_Dialog_Alert);
                builder.setTitle(R.string.title_saving_covers)
                        .setMessage(R.string.message_saving_covers)
                        .setOnCancelListener(dialog -> deleteOfAllCovers.setChecked(false))
                        .setPositiveButton(R.string.confirm_settings, (dialog, which) -> {
                            settingsPresenter = new SettingsPresenter(this);
                            settingsPresenter.deleteOfCovers();
                            settingsPresenter.detachView();
                        })
                        .setNegativeButton(R.string.cancel_saving_covers, (dialog, which) ->
                                deleteOfAllCovers.setChecked(false));
                alertDialog  = builder.create();
                alertDialog.show();
                return false;
            });

            PreferenceScreen preferenceScreen = findPreference("support");
            Objects.requireNonNull(preferenceScreen)
                    .setOnPreferenceClickListener(preference -> {
                        sendEmail();
                        return false;
                    });

        }

        private void sendEmail() {
            final Intent emailIntent = new Intent(android.content.Intent.ACTION_SEND);
            emailIntent.setType("plain/text");
            emailIntent.putExtra(android.content.Intent.EXTRA_EMAIL,
                    new String[]{"vadimlipakov@gmail.com"});
            emailIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, "Приложение МузАрт");
            context.startActivity(Intent.createChooser(emailIntent, "Отправить письмо"));
        }

        @Override
        public void onStop() {
            super.onStop();
            if (alertDialog != null) {
                deleteOfAllCovers.setChecked(false);
                alertDialog.dismiss();
            }
        }

        @Override
        public void notifyAboutDeleting() {
            Toast.makeText(context,
                    "Файлы в папке очищены",
                    Toast.LENGTH_SHORT).show();
            deleteOfAllCovers.setChecked(false);
        }

        @Override
        public void notifyEmptyFolder() {
            Toast.makeText(context,
                    "В папке нет файлов",
                    Toast.LENGTH_SHORT).show();
            deleteOfAllCovers.setChecked(false);
        }

    }

}