package com.pasantias.movil.ui.auth;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.pasantias.movil.R;
import com.pasantias.movil.data.api.ApiClient;
import com.pasantias.movil.data.dto.LoginRequest;
import com.pasantias.movil.data.dto.LoginResponse;
import com.pasantias.movil.data.local.TokenManager;
import com.pasantias.movil.util.RoleRouter;
import com.pasantias.movil.util.UiUtils;

public class LoginActivity extends AppCompatActivity {

    private TextInputEditText inputEmail;
    private TextInputEditText inputPassword;
    private ProgressBar progressLogin;
    private TextView textError;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (TokenManager.get().isLoggedIn()) {
            openDashboard();
            finish();
            return;
        }

        setContentView(R.layout.activity_login);
        inputEmail = findViewById(R.id.inputEmail);
        inputPassword = findViewById(R.id.inputPassword);
        progressLogin = findViewById(R.id.progressLogin);
        textError = findViewById(R.id.textError);
        MaterialButton btnLogin = findViewById(R.id.btnLogin);
        btnLogin.setOnClickListener(v -> attemptLogin());
    }

    private void attemptLogin() {
        String email = inputEmail.getText() != null ? inputEmail.getText().toString().trim() : "";
        String password = inputPassword.getText() != null ? inputPassword.getText().toString() : "";
        if (email.isEmpty() || password.isEmpty()) {
            showError("Completá correo y contraseña.");
            return;
        }
        progressLogin.setVisibility(View.VISIBLE);
        textError.setVisibility(View.GONE);

        ApiClient.enqueue(
                ApiClient.get().api().login(new LoginRequest(email, password)),
                new ApiClient.ApiCallback<LoginResponse>() {
                    @Override
                    public void onSuccess(LoginResponse data) {
                        progressLogin.setVisibility(View.GONE);
                        TokenManager.get().saveSession(data.access_token, data.user);
                        openDashboard();
                        finish();
                    }

                    @Override
                    public void onError(String message) {
                        progressLogin.setVisibility(View.GONE);
                        if (message != null && message.contains("401")) {
                            showError(getString(R.string.error_credentials));
                        } else if (message != null && message.contains("Unable to resolve host")) {
                            showError(getString(R.string.error_network));
                        } else {
                            showError(getString(R.string.error_generic));
                        }
                    }
                }
        );
    }

    private void openDashboard() {
        startActivity(RoleRouter.dashboardIntent(this, TokenManager.get().getUser()));
    }

    private void showError(String msg) {
        textError.setText(msg);
        textError.setVisibility(View.VISIBLE);
    }
}
