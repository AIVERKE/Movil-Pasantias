package com.pasantias.movil.worker;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.pasantias.movil.PasantiasApplication;
import com.pasantias.movil.R;
import com.pasantias.movil.data.api.ApiClient;
import com.pasantias.movil.data.dto.NotificacionConteoDto;
import com.pasantias.movil.data.local.TokenManager;
import com.pasantias.movil.ui.auth.LoginActivity;

import retrofit2.Response;

public class NotificationSyncWorker extends Worker {

    public NotificationSyncWorker(@NonNull Context context, @NonNull WorkerParameters params) {
        super(context, params);
    }

    @NonNull
    @Override
    public Result doWork() {
        if (!TokenManager.get().isLoggedIn()) {
            return Result.success();
        }
        try {
            Response<NotificacionConteoDto> response =
                    ApiClient.get().api().notificacionesConteo().execute();
            if (!response.isSuccessful() || response.body() == null) {
                return Result.retry();
            }
            int noLeidas = response.body().getNoLeidas();
            int last = TokenManager.get().getLastNotificationCount();
            if (noLeidas > last) {
                showNotification(noLeidas - last);
            }
            TokenManager.get().setLastNotificationCount(noLeidas);
            return Result.success();
        } catch (Exception e) {
            return Result.retry();
        }
    }

    private void showNotification(int nuevas) {
        Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        PendingIntent pi = PendingIntent.getActivity(
                getApplicationContext(),
                0,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );
        NotificationCompat.Builder builder = new NotificationCompat.Builder(
                getApplicationContext(),
                PasantiasApplication.CHANNEL_NOTIFICATIONS
        )
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                .setContentTitle(getApplicationContext().getString(R.string.app_name))
                .setContentText("Tenés " + nuevas + " notificación(es) nueva(s)")
                .setContentIntent(pi)
                .setAutoCancel(true)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT);

        NotificationManager nm = (NotificationManager)
                getApplicationContext().getSystemService(Context.NOTIFICATION_SERVICE);
        if (nm != null) {
            nm.notify((int) System.currentTimeMillis(), builder.build());
        }
    }
}
