package com.pasantias.movil.ui.jefe;

import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.pasantias.movil.R;
import com.pasantias.movil.data.api.ApiClient;
import com.pasantias.movil.data.dto.EstudianteCatalogoDto;
import com.pasantias.movil.data.dto.InscripcionDto;
import com.pasantias.movil.data.dto.PasantiaDto;
import com.pasantias.movil.ui.common.DialogHelper;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class InvitarEstudianteActivity extends AppCompatActivity {

    private ProgressBar progress;
    private View contentLayout;
    private Spinner spinnerEstudiante;
    private Spinner spinnerPasantia;

    private List<EstudianteCatalogoDto> estudiantesList = new ArrayList<>();
    private List<PasantiaDto> pasantiasList = new ArrayList<>();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_invitar_estudiante);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        progress = findViewById(R.id.progress);
        contentLayout = findViewById(R.id.contentLayout);
        spinnerEstudiante = findViewById(R.id.spinnerEstudiante);
        spinnerPasantia = findViewById(R.id.spinnerPasantia);

        findViewById(R.id.btnEnviar).setOnClickListener(v -> enviar());

        loadData();
    }

    private void loadData() {
        progress.setVisibility(View.VISIBLE);
        contentLayout.setVisibility(View.GONE);

        // Carga secuencial de catálogos
        ApiClient.enqueue(ApiClient.get().api().getEstudiantesCatalogo(), new ApiClient.ApiCallback<List<EstudianteCatalogoDto>>() {
            @Override
            public void onSuccess(List<EstudianteCatalogoDto> estudiantes) {
                estudiantesList = estudiantes != null ? estudiantes : new ArrayList<>();
                loadPasantias();
            }

            @Override
            public void onError(String message) {
                progress.setVisibility(View.GONE);
                Toast.makeText(InvitarEstudianteActivity.this, "Error cargando estudiantes: " + message, Toast.LENGTH_LONG).show();
                finish();
            }
        });
    }

    private void loadPasantias() {
        ApiClient.enqueue(ApiClient.get().api().listPasantiasJefe(), new ApiClient.ApiCallback<List<PasantiaDto>>() {
            @Override
            public void onSuccess(List<PasantiaDto> pasantias) {
                pasantiasList = pasantias != null ? pasantias : new ArrayList<>();
                progress.setVisibility(View.GONE);
                
                if (estudiantesList.isEmpty() || pasantiasList.isEmpty()) {
                    Toast.makeText(InvitarEstudianteActivity.this, "No tenés estudiantes o convocatorias activas para invitar", Toast.LENGTH_LONG).show();
                    finish();
                    return;
                }

                setupSpinners();
                contentLayout.setVisibility(View.VISIBLE);
            }

            @Override
            public void onError(String message) {
                progress.setVisibility(View.GONE);
                Toast.makeText(InvitarEstudianteActivity.this, "Error cargando convocatorias: " + message, Toast.LENGTH_LONG).show();
                finish();
            }
        });
    }

    private void setupSpinners() {
        List<String> estNames = new ArrayList<>();
        for (EstudianteCatalogoDto e : estudiantesList) {
            String carrera = e.carrera != null ? " (" + e.carrera + ")" : "";
            estNames.add(e.getNombreCompleto() + carrera);
        }
        ArrayAdapter<String> estAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, estNames);
        estAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerEstudiante.setAdapter(estAdapter);

        List<String> pasTitles = new ArrayList<>();
        for (PasantiaDto p : pasantiasList) {
            pasTitles.add(p.titulo);
        }
        ArrayAdapter<String> pasAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, pasTitles);
        pasAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerPasantia.setAdapter(pasAdapter);
    }

    private void enviar() {
        int estPos = spinnerEstudiante.getSelectedItemPosition();
        int pasPos = spinnerPasantia.getSelectedItemPosition();

        if (estPos < 0 || estPos >= estudiantesList.size() || pasPos < 0 || pasPos >= pasantiasList.size()) {
            Toast.makeText(this, "Selección inválida", Toast.LENGTH_SHORT).show();
            return;
        }

        EstudianteCatalogoDto est = estudiantesList.get(estPos);
        PasantiaDto pas = pasantiasList.get(pasPos);

        progress.setVisibility(View.VISIBLE);
        contentLayout.setVisibility(View.GONE);

        Map<String, Object> body = new HashMap<>();
        body.put("estudianteId", est.id_estudiante);
        body.put("pasantiaId", pas.id_pasantia);

        ApiClient.enqueue(ApiClient.get().api().invitarEstudiante(body), new ApiClient.ApiCallback<InscripcionDto>() {
            @Override
            public void onSuccess(InscripcionDto data) {
                progress.setVisibility(View.GONE);
                DialogHelper.showSuccess(InvitarEstudianteActivity.this, "Invitación enviada con éxito", () -> finish());
            }

            @Override
            public void onError(String message) {
                progress.setVisibility(View.GONE);
                contentLayout.setVisibility(View.VISIBLE);
                Toast.makeText(InvitarEstudianteActivity.this, message, Toast.LENGTH_LONG).show();
            }
        });
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }
}
