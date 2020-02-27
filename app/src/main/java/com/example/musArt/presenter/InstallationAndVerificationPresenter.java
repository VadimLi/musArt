package com.example.musArt.presenter;

import android.app.Activity;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.util.Log;

import java.io.File;
import java.io.IOException;
import java.util.Objects;

public class InstallationAndVerificationPresenter {

    private static final String TAG = InstallationAndVerificationPresenter.class.getSimpleName();

    private static final int READ_EXTERNAL_STORAGE_PERMISSION = 0x4;

    private final static String NAME_OF_INSTALLATION_FILE = "setup.txt";

    private File installingFile;

    private VerificationView verificationView;

    private InstallationView installationView;

    public InstallationAndVerificationPresenter(Activity activity) {
        final PackageManager packageManager = activity.getPackageManager();
        String packageName = activity.getPackageName();
        PackageInfo packageInfo = null;
        try {
            packageInfo = packageManager.getPackageInfo(packageName, 0);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        String packagePathName = Objects.requireNonNull(packageInfo).applicationInfo.dataDir;
        installingFile = new File(packagePathName + "/" + NAME_OF_INSTALLATION_FILE);
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

    public void setVerificationView() {
        if (!installingFile.exists()) {
            verificationView.transitToSettingsActivity();
        } else {
            verificationView.displayMainActivity();
        }
    }

    public void setInstallationView() {
        if (!installingFile.exists()) {
            installationView.confirmSettingsActivity();
        } else {
            installationView.displaySettingsActivity();
        }
    }

    public void createInstallingFile() {
        try {
            if (installingFile.createNewFile()) {
                Log.d(TAG, "file is created");
            }
        } catch (IOException e) {
            Log.e(TAG, Objects.requireNonNull(e.getMessage()));
        }
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
