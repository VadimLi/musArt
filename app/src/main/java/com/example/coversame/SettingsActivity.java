package com.example.coversame;

import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.TwoStatePreference;

import java.io.File;
import java.io.IOException;
import java.util.Objects;

import butterknife.BindView;
import butterknife.ButterKnife;

public class SettingsActivity extends AppCompatActivity {

    @BindView(R.id.toolbar)
    Toolbar toolbar;

    @BindView(R.id.confirm)
    Button confirm;

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.settings_activity);
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.settings, new SettingsFragment())
                .commit();
        ButterKnife.bind(this);

        final String packagePathName = getPackagePathName();
        final File installingFile = new File(packagePathName + "/install.txt");
        if (!installingFile.exists()) {
            createInstallingFile(installingFile);
            confirm.setOnClickListener(v -> {
                Intent mainIntent = new Intent(this, MainActivity.class);
                startActivity(mainIntent);
            });
        } else {
            confirm.setVisibility(View.GONE);
            toolbar.setTitle(R.string.title_activity_settings);
            setSupportActionBar(toolbar);
            Objects.requireNonNull(getSupportActionBar())
                    .setDefaultDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
    }

    private void createInstallingFile(File file) {
        try {
            if (file.createNewFile()) {
                Log.d("SettingsActivity", "File is created");
            } else {
                Log.d("SettingsActivity", "File is not created");
            }
        } catch (IOException e) {
            Log.e("SettingsActivity", String.valueOf(e));
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
        return Objects.requireNonNull(packageInfo).applicationInfo.dataDir;
    }

    public static class SettingsFragment extends PreferenceFragmentCompat {

        private TwoStatePreference synchronizationOfCovers;

        private Preference categoryOfSaving;

        private TwoStatePreference savingOfCovers;

        private TwoStatePreference savingWithReplacement;

        public SettingsFragment() { }

        @Override
        public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
            setPreferencesFromResource(R.xml.root_preferences, rootKey);
            synchronizationOfCovers = findPreference("synchronization_of_covers");

            categoryOfSaving = findPreference("category_of_saving");
            savingOfCovers = findPreference("saving_of_covers");
            savingWithReplacement = findPreference("saving_with_replacement");

            setEnabledForSavingOfCovers();
            setEnabledForSavingWithReplacement();

            synchronizationOfCovers.setOnPreferenceClickListener(preference -> {
                setEnabledForSavingOfCovers();
                return false;
            });

            savingOfCovers.setOnPreferenceClickListener(preference -> {
                setEnabledForSavingWithReplacement();
                return false;
            });

            savingWithReplacement.setOnPreferenceClickListener(preference -> {
                if (savingWithReplacement.isChecked()) {
                    AlertDialog.Builder builder =
                            new AlertDialog.Builder(Objects.requireNonNull(
                                    SettingsFragment.this.getContext()),
                                    R.style.Theme_AppCompat_Light_Dialog_Alert);
                    builder.setTitle(R.string.title_saving_covers)
                            .setMessage(R.string.message_saving_covers)
                            .setPositiveButton(R.string.confirm_settings, null)
                            .setOnCancelListener(dialog -> {
                                savingWithReplacement.setChecked(false);
                            })
                            .setNegativeButton(R.string.cancel_saving_covers, (dialog, which) ->
                                    savingWithReplacement.setChecked(false));
                    AlertDialog dialog  = builder.create();
                    dialog.show();
                }
                return false;
            });
        }

        private void setEnabledForSavingOfCovers() {
            if (!synchronizationOfCovers.isChecked()) {
                categoryOfSaving.setEnabled(false);
            } else {
                categoryOfSaving.setEnabled(true);
            }
        }

        private void setEnabledForSavingWithReplacement() {
            if (!savingOfCovers.isChecked()) {
                savingWithReplacement.setEnabled(false);
                savingWithReplacement.setChecked(false);
            } else {
                savingWithReplacement.setEnabled(true);
            }
        }

    }

}