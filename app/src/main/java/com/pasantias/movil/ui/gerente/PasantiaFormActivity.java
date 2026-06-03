package com.pasantias.movil.ui.gerente;

import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.pasantias.movil.R;
import com.pasantias.movil.data.api.ApiClient;
import com.pasantias.movil.data.dto.CreatePasantiaRequest;
import com.pasantias.movil.data.dto.MessageResponse;
import com.pasantias.movil.data.dto.PasantiaDto;

import java.util.HashMap;
import java.util.Map;

public class PasantiaFormActivity extends AppCompatActivity {

    public static final String EXTRA_ID = "id_pasantia";

    private int pasantiaId = -1;
    private TextInputEditText inputTitulo, inputDescripcion, inputFechaInicio, inputFechaFin, inputArea;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pasantia_form);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        pasantiaId = getIntent().getIntExtra(EXTRA_ID, -1);
        setTitle(pasantiaId > 0 ? "Editar pasantía" : "Nueva pasantía");

        inputTitulo = findViewById(R.id.inputTitulo);
        inputDescripcion = findViewById(R.id.inputDescripcion);
        inputFechaInicio = findViewById(R.id.inputFechaInicio);
        inputFechaFin = findViewById(R.id.inputFechaFin);
        inputArea = findViewById(R.id.inputArea);
        MaterialButton btnGuardar = findViewById(R.id.btnGuardar);
        btnGuardar.setOnClickListener(v -> guardar());

        if (pasantiaId > 0) {
            ApiClient.enqueue(ApiClient.get().api().getPasantia(pasantiaId), new ApiClient.ApiCallback<PasantiaDto>() {
                @Override
                public void onSuccess(PasantiaDto p) {
                    inputTitulo.setText(p.titulo);
                    inputDescripcion.setText(p.descripcion);
                    inputFechaInicio.setText(p.fecha_inicio);
                    inputFechaFin.setText(p.fecha_fin);
                    inputArea.setText(p.area);
                }

                @Override
                public void onError(String message) {
                    Toast.makeText(PasantiaFormActivity.this, message, Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    private void guardar() {
        CreatePasantiaRequest req = new CreatePasantiaRequest();
        req.titulo = text(inputTitulo);
        req.descripcion = text(inputDescripcion);
        req.fecha_inicio = text(inputFechaInicio);
        req.fecha_fin = text(inputFechaFin);
        req.area = text(inputArea);
        if (req.titulo.isEmpty() || req.descripcion.isEmpty()) {
            Toast.makeText(this, "Título y descripción son obligatorios", Toast.LENGTH_SHORT).show();
            return;
        }
        if (pasantiaId > 0) {
            ApiClient.enqueue(ApiClient.get().api().updatePasantia(pasantiaId, req), new ApiClient.ApiCallback<PasantiaDto>() {
                @Override
                public void onSuccess(PasantiaDto data) {
                    finish();
                }

                @Override
                public void onError(String message) {
                    Toast.makeText(PasantiaFormActivity.this, message, Toast.LENGTH_SHORT).show();
                }
            });
        } else {
            ApiClient.enqueue(ApiClient.get().api().createPasantiaGerente(req), new ApiClient.ApiCallback<PasantiaDto>() {
                @Override
                public void onSuccess(PasantiaDto data) {
                    finish();
                }

                @Override
                public void onError(String message) {
                    Toast.makeText(PasantiaFormActivity.this, message, Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if (pasantiaId > 0) {
            menu.add(0, 1, 0, R.string.delete);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        if (item.getItemId() == 1) {
            ApiClient.enqueue(ApiClient.get().api().deletePasantia(pasantiaId), new ApiClient.ApiCallback<MessageResponse>() {
                @Override
                public void onSuccess(MessageResponse data) {
                    finish();
                }

                @Override
                public void onError(String message) {
                    Toast.makeText(PasantiaFormActivity.this, message, Toast.LENGTH_SHORT).show();
                }
            });
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private String text(TextInputEditText et) {
        return et.getText() != null ? et.getText().toString().trim() : "";
    }
}
