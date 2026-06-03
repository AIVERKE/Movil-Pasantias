package com.pasantias.movil;

import android.app.Application;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.os.Build;

import androidx.work.ExistingPeriodicWorkPolicy;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkManager;

import com.pasantias.movil.data.api.ApiClient;
import com.pasantias.movil.data.local.TokenManager;
import com.pasantias.movil.worker.NotificationSyncWorker;

import java.util.concurrent.TimeUnit;

public class PasantiasApplication extends Application {

    public static final String CHANNEL_NOTIFICATIONS = "pasantias_notifications";

    @Override
    public void onCreate() {
        super.onCreate();
        TokenManager.init(this);
        ApiClient.init(this);
        createNotificationChannel();
        scheduleNotificationSync();
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_NOTIFICATIONS,
                    getString(R.string.notifications_channel),
                    NotificationManager.IMPORTANCE_DEFAULT
            );
            channel.setDescription(getString(R.string.notifications_channel_desc));
            NotificationManager nm = getSystemService(NotificationManager.class);
            if (nm != null) {
                nm.createNotificationChannel(channel);
            }
        }
    }

    private void scheduleNotificationSync() {
        PeriodicWorkRequest work = new PeriodicWorkRequest.Builder(
                NotificationSyncWorker.class,
                15,
                TimeUnit.MINUTES
        ).build();
        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
                "notification_sync",
                ExistingPeriodicWorkPolicy.KEEP,
                work
        );
    }
}
