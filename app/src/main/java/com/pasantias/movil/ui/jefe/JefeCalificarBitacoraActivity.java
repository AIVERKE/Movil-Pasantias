package com.pasantias.movil.ui.jefe;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.widget.NestedScrollView;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.pasantias.movil.R;
import com.pasantias.movil.ui.common.DialogHelper;
import com.pasantias.movil.data.api.ApiClient;
import com.pasantias.movil.data.dto.BitacoraJefeDto;
import com.pasantias.movil.data.dto.MessageResponse;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class JefeCalificarBitacoraActivity extends AppCompatActivity {
    public static final String EXTRA_BITACORA_ID = "bitacora_id";

    private int bitacoraId;

    private ProgressBar progress;
    private TextView textError;
    private NestedScrollView contentScroll;

    private TextView textEstudiante;
    private TextView textMeta;
    private TextView textActividad;
    private TextView textAvance;
    private TextView textContenido;

    private TextInputLayout layoutNota;
    private TextInputEditText inputNota;
    private TextInputLayout layoutObs;
    private TextInputEditText inputObs;
    private MaterialButton btnGuardar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_jefe_calificar_bitacora);

        bitacoraId = getIntent().getIntExtra(EXTRA_BITACORA_ID, 0);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Calificar Avance");
        }

        progress = findViewById(R.id.progress);
        textError = findViewById(R.id.textError);
        contentScroll = findViewById(R.id.contentScroll);

        textEstudiante = findViewById(R.id.textEstudiante);
        textMeta = findViewById(R.id.textMeta);
        textActividad = findViewById(R.id.textActividad);
        textAvance = findViewById(R.id.textAvance);
        textContenido = findViewById(R.id.textContenido);

        layoutNota = findViewById(R.id.layoutNota);
        inputNota = findViewById(R.id.inputNota);
        layoutObs = findViewById(R.id.layoutObs);
        inputObs = findViewById(R.id.inputObs);
        btnGuardar = findViewById(R.id.btnGuardar);

        btnGuardar.setOnClickListener(v -> guardarCalificacion());

        loadBitacoraDetalle();
    }

    private void loadBitacoraDetalle() {
        progress.setVisibility(View.VISIBLE);
        contentScroll.setVisibility(View.GONE);
        textError.setVisibility(View.GONE);

        ApiClient.enqueue(ApiClient.get().api().getJefeBitacoras(null, null), new ApiClient.ApiCallback<List<BitacoraJefeDto>>() {
            @Override
            public void onSuccess(List<BitacoraJefeDto> data) {
                progress.setVisibility(View.GONE);
                if (data == null) {
                    showError("No se encontraron bitácoras.");
                    return;
                }

                BitacoraJefeDto bitacora = null;
                for (BitacoraJefeDto b : data) {
                    if (b.id == bitacoraId) {
                        bitacora = b;
                        break;
                    }
                }

                if (bitacora == null) {
                    showError("No se encontró la bitácora solicitada.");
                    return;
                }

                displayBitacora(bitacora);
            }

            @Override
            public void onError(String message) {
                progress.setVisibility(View.GONE);
                showError(message);
            }
        });
    }

    private void displayBitacora(BitacoraJefeDto b) {
        textEstudiante.setText(b.estudiante != null ? b.estudiante : "Estudiante");
        textMeta.setText("Semana " + b.semana + " · Estado: " + (b.estado != null ? b.estado : "Pendiente"));
        textActividad.setText("Actividad: " + (b.actividad != null ? b.actividad : "N/A"));
        textAvance.setText("AVANCE REPORTADO: " + b.avanceRegistrado + "%");
        textContenido.setText(b.contenido != null ? b.contenido : "");

        if (b.nota != null) {
            inputNota.setText(String.valueOf(b.nota));
        }
        if (b.obs != null) {
            inputObs.setText(b.obs);
        }

        contentScroll.setVisibility(View.VISIBLE);
    }

    private void showError(String message) {
        textError.setText(message);
        textError.setVisibility(View.VISIBLE);
    }

    private void guardarCalificacion() {
        layoutNota.setError(null);
        String notaStr = inputNota.getText() != null ? inputNota.getText().toString().trim() : "";
        String obs = inputObs.getText() != null ? inputObs.getText().toString().trim() : "";

        if (TextUtils.isEmpty(notaStr)) {
            layoutNota.setError("La nota es obligatoria");
            return;
        }

        int nota;
        try {
            nota = Integer.parseInt(notaStr);
        } catch (NumberFormatException e) {
            layoutNota.setError("Debe ingresar un número válido");
            return;
        }

        if (nota < 1 || nota > 100) {
            layoutNota.setError("La nota debe estar entre 1 y 100");
            return;
        }

        btnGuardar.setEnabled(false);
        progress.setVisibility(View.VISIBLE);

        Map<String, Object> body = new HashMap<>();
        body.put("nota", nota);
        body.put("observacion", obs);

        ApiClient.enqueue(ApiClient.get().api().calificarBitacora(bitacoraId, body), new ApiClient.ApiCallback<MessageResponse>() {
            @Override
            public void onSuccess(MessageResponse data) {
                progress.setVisibility(View.GONE);
                btnGuardar.setEnabled(true);
                DialogHelper.showSuccess(JefeCalificarBitacoraActivity.this, "Calificación guardada correctamente", () -> finish());
            }

            @Override
            public void onError(String message) {
                progress.setVisibility(View.GONE);
                btnGuardar.setEnabled(true);
                Toast.makeText(JefeCalificarBitacoraActivity.this, "Error: " + message, Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }
}

