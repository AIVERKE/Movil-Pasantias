package com.pasantias.movil.ui.auth;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;

import com.pasantias.movil.R;
import com.pasantias.movil.data.api.ApiClient;
import com.pasantias.movil.data.local.TokenManager;
import com.pasantias.movil.util.RoleRouter;

/**
 * Punto de entrada real de la app.
 *
 * El problema: EncryptedSharedPreferences inicializa el Keystore de Android
 * en el PRIMER arranque, lo cual bloquea el hilo principal varios segundos
 * haciendo que la pantalla quede congelada.
 *
 * Solución: hacemos la inicialización de TokenManager + ApiClient en un
 * hilo de fondo y mostramos un splash mientras tanto.
 */
public class SplashActivity extends AppCompatActivity {

    private static final String TAG = "SplashActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        // Correr en hilo de fondo para no bloquear el UI thread
        new Thread(() -> {
            try {
                // Esperar a que TokenManager termine de inicializarse en su hilo
                // (puede tardar hasta ~500ms en primer arranque con Keystore)
                int intentos = 0;
                while (TokenManager.get() == null && intentos < 40) {
                    Thread.sleep(50);
                    intentos++;
                }

                boolean loggedIn = TokenManager.get() != null && TokenManager.get().isLoggedIn();

                runOnUiThread(() -> {
                    Intent intent;
                    if (loggedIn) {
                        intent = RoleRouter.dashboardIntent(this, TokenManager.get().getUser());
                    } else {
                        intent = new Intent(this, LoginActivity.class);
                    }
                    startActivity(intent);
                    finish();
                });

            } catch (Exception e) {
                Log.e(TAG, "Error inicializando app", e);
                runOnUiThread(() -> {
                    startActivity(new Intent(this, LoginActivity.class));
                    finish();
                });
            }
        }, "splash-init").start();
    }
}
