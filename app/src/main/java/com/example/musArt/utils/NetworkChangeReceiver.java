package com.example.musArt.utils;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class NetworkChangeReceiver extends BroadcastReceiver {

    private static boolean checkInternetConnection = true;

    @SuppressLint("UnsafeProtectedBroadcastReceiver")
    @Override
    public void onReceive(final Context context, final Intent intent) {
        checkInternetConnection = NetworkUtils.isOnline(context);
    }

    public static boolean isCheckInternetConnection() {
        return checkInternetConnection;
    }

}