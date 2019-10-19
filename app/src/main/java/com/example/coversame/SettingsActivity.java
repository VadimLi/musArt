package com.example.coversame;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.TwoStatePreference;

import com.example.coversame.presenter.InstallationAndVerificationPresenter;

import java.util.Objects;

import butterknife.BindView;
import butterknife.ButterKnife;

public class SettingsActivity extends AppCompatActivity
        implements InstallationAndVerificationPresenter.InstallationView {

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
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.settings, new SettingsFragment())
                .commit();
        ButterKnife.bind(this);

        installationAndVerificationPresenter = new InstallationAndVerificationPresenter();
        installationAndVerificationPresenter.createPackagePathName(SettingsActivity.this);
        installationAndVerificationPresenter.initInstallingFile();
        installationAndVerificationPresenter.attachInstallationView(this);
        installationAndVerificationPresenter.setInstallationView();
    }

    @Override
    public void confirmSettingsActivity() {
        confirm.setOnClickListener(v -> {
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

    @Override
    protected void onRestart() {
        super.onRestart();
        installationAndVerificationPresenter.attachInstallationView(this);
    }

    @Override
    protected void onStop() {
        super.onStop();
        installationAndVerificationPresenter.detachInstallationView();
    }

    public static class SettingsFragment extends PreferenceFragmentCompat {

        private TwoStatePreference savingOfCovers;

        private TwoStatePreference savingWithReplacement;

        public SettingsFragment() { }

        @Override
        public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
            setPreferencesFromResource(R.xml.root_preferences, rootKey);

            savingOfCovers = findPreference("saving_of_covers");
            savingWithReplacement = findPreference("saving_with_replacement");

            setEnabledForSavingWithReplacement();

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