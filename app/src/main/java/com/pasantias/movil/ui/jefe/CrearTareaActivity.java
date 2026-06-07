package com.pasantias.movil.ui.jefe;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.widget.NestedScrollView;

import com.pasantias.movil.R;
import com.pasantias.movil.data.api.ApiClient;
import com.pasantias.movil.data.dto.InscripcionDto;
import com.pasantias.movil.data.dto.JefeActividadDto;
import com.pasantias.movil.ui.common.DialogHelper;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class CrearTareaActivity extends AppCompatActivity {

    private ProgressBar progress;
    private NestedScrollView contentScroll;
    private EditText inputTitulo;
    private EditText inputDescripcion;
    private EditText inputFecha;
    private Spinner spinnerPasante;

    private List<InscripcionDto> pasantesList = new ArrayList<>();
    private Calendar calendar = Calendar.getInstance();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_crear_tarea);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        progress = findViewById(R.id.progress);
        contentScroll = findViewById(R.id.contentScroll);
        inputTitulo = findViewById(R.id.inputTitulo);
        inputDescripcion = findViewById(R.id.inputDescripcion);
        inputFecha = findViewById(R.id.inputFecha);
        spinnerPasante = findViewById(R.id.spinnerPasante);

        inputFecha.setOnClickListener(v -> mostrarDatePicker());
        findViewById(R.id.btnGuardar).setOnClickListener(v -> guardar());

        loadPasantes();
    }

    private void loadPasantes() {
        progress.setVisibility(View.VISIBLE);
        contentScroll.setVisibility(View.GONE);

        ApiClient.enqueue(ApiClient.get().api().listPasantesJefe(), new ApiClient.ApiCallback<List<InscripcionDto>>() {
            @Override
            public void onSuccess(List<InscripcionDto> data) {
                progress.setVisibility(View.GONE);
                pasantesList = data != null ? data : new ArrayList<>();
                setupSpinner();
                contentScroll.setVisibility(View.VISIBLE);
            }

            @Override
            public void onError(String message) {
                progress.setVisibility(View.GONE);
                Toast.makeText(CrearTareaActivity.this, "Error cargando pasantes: " + message, Toast.LENGTH_LONG).show();
                finish();
            }
        });
    }

    private void setupSpinner() {
        List<String> names = new ArrayList<>();
        names.add("A todos los pasantes"); // Opción por defecto (id_inscripcion = null)
        
        for (InscripcionDto i : pasantesList) {
            names.add(i.getEstudianteNombre());
        }

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, names);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerPasante.setAdapter(adapter);
    }

    private void mostrarDatePicker() {
        DatePickerDialog picker = new DatePickerDialog(this, (view, year, month, dayOfMonth) -> {
            calendar.set(Calendar.YEAR, year);
            calendar.set(Calendar.MONTH, month);
            calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
            
            String fechaStr = String.format(Locale.US, "%d-%02d-%02d", year, month + 1, dayOfMonth);
            inputFecha.setText(fechaStr);
        }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH));
        
        picker.getDatePicker().setMinDate(System.currentTimeMillis() - 1000); // No permitir fechas pasadas
        picker.show();
    }

    private void guardar() {
        String titulo = inputTitulo.getText().toString().trim();
        String descripcion = inputDescripcion.getText().toString().trim();
        String fecha = inputFecha.getText().toString().trim();
        int selectedPos = spinnerPasante.getSelectedItemPosition();

        if (titulo.isEmpty()) {
            inputTitulo.setError("Ingresá el título de la tarea");
            return;
        }
        if (descripcion.isEmpty()) {
            inputDescripcion.setError("Ingresá la descripción");
            return;
        }
        if (fecha.isEmpty()) {
            inputFecha.setError("Seleccioná la fecha límite");
            return;
        }

        progress.setVisibility(View.VISIBLE);
        contentScroll.setVisibility(View.GONE);

        Map<String, Object> body = new HashMap<>();
        body.put("titulo", titulo);
        body.put("descripcion", descripcion);
        body.put("fecha_limite", fecha);

        if (selectedPos > 0) {
            // Pasante específico
            InscripcionDto selected = pasantesList.get(selectedPos - 1);
            body.put("id_inscripcion", selected.getId());
        }

        ApiClient.enqueue(ApiClient.get().api().crearTareaJefe(body), new ApiClient.ApiCallback<JefeActividadDto>() {
            @Override
            public void onSuccess(JefeActividadDto data) {
                progress.setVisibility(View.GONE);
                DialogHelper.showSuccess(CrearTareaActivity.this, "Tarea creada y asignada con éxito", () -> finish());
            }

            @Override
            public void onError(String message) {
                progress.setVisibility(View.GONE);
                contentScroll.setVisibility(View.VISIBLE);
                Toast.makeText(CrearTareaActivity.this, message, Toast.LENGTH_LONG).show();
            }
        });
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }
}
