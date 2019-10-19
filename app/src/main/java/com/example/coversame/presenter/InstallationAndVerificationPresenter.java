package com.example.coversame.presenter;

import android.app.Activity;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.util.Log;

import java.io.File;
import java.io.IOException;
import java.util.Objects;

public class InstallationAndVerificationPresenter {

    private final static String NAME_OF_INSTALLATION_FILE = "install.txt";

    private String packagePathName;

    private File installingFile;

    private VerificationView verificationView;

    private InstallationView installationView;

    public void setVerificationView() {
        if (!installingFile.exists()) {
            verificationView.transitToSettingsActivity();
        } else {
            verificationView.displayMainActivity();
        }
    }

    public void setInstallationView() {
        if (!installingFile.exists()) {
            createInstallingFile();
            installationView.confirmSettingsActivity();
        } else {
            installationView.displaySettingsActivity();
        }
    }

    private void createInstallingFile() {
        try {
            if (installingFile.createNewFile()) {
                Log.d("SettingsActivity", "File is created");
            } else {
                Log.d("SettingsActivity", "File is not created");
            }
        } catch (IOException e) {
            Log.e("SettingsActivity", String.valueOf(e));
        }
    }

    public void attachVerificationView(VerificationView verificationView) {
        this.verificationView = verificationView;
    }

    public void detachVerificationView() {
        verificationView = null;
    }

    public void attachInstallationView(InstallationView installationView) {
        this.installationView = installationView;
    }

    public void detachInstallationView() {
        installationView = null;
    }

    public void createPackagePathName(Activity activity) {
        final PackageManager packageManager = activity.getPackageManager();
        String packageName = activity.getPackageName();
        PackageInfo packageInfo = null;
        try {
            packageInfo = packageManager.getPackageInfo(packageName, 0);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        packagePathName = Objects.requireNonNull(packageInfo).applicationInfo.dataDir;
    }

    public void initInstallingFile() {
        installingFile = new File(packagePathName +
                "/" + NAME_OF_INSTALLATION_FILE);
    }

    public interface InstallationView {
        void confirmSettingsActivity();
        void displaySettingsActivity();
    }

    public interface VerificationView {
        void transitToSettingsActivity();
        void displayMainActivity();
    }

}
