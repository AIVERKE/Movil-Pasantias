package com.pasantias.movil.ui.gerente;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.pasantias.movil.R;
import com.pasantias.movil.data.api.ApiClient;
import com.pasantias.movil.data.dto.CreatePasantiaRequest;
import com.pasantias.movil.data.dto.MessageResponse;
import com.pasantias.movil.data.dto.PasantiaDto;
import com.pasantias.movil.ui.common.DialogHelper;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class PasantiaFormActivity extends AppCompatActivity {

    public static final String EXTRA_ID = "id_pasantia";

    private static final String[] MODALIDADES = {"Presencial", "Virtual", "Híbrido"};

    private int pasantiaId = -1;

    private TextInputEditText inputTitulo, inputCupos, inputHorario,
            inputDescripcion, inputRequisitos, inputFechaInicio, inputFechaFin, inputArea;
    private AutoCompleteTextView spinnerModalidad;
    private TextInputLayout layoutFechaInicio, layoutFechaFin;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pasantia_form);

        // Toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }
        toolbar.setNavigationOnClickListener(v -> finish());

        pasantiaId = getIntent().getIntExtra(EXTRA_ID, -1);
        boolean isEditing = pasantiaId > 0;
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(isEditing ? "Editar Convocatoria" : "Nueva Convocatoria");
        }

        // Referencias
        inputTitulo      = findViewById(R.id.inputTitulo);
        spinnerModalidad = findViewById(R.id.spinnerModalidad);
        inputCupos       = findViewById(R.id.inputCupos);
        inputHorario     = findViewById(R.id.inputHorario);
        inputDescripcion = findViewById(R.id.inputDescripcion);
        inputRequisitos  = findViewById(R.id.inputRequisitos);
        inputFechaInicio = findViewById(R.id.inputFechaInicio);
        inputFechaFin    = findViewById(R.id.inputFechaFin);
        inputArea        = findViewById(R.id.inputArea);
        layoutFechaInicio = findViewById(R.id.layoutFechaInicio);
        layoutFechaFin    = findViewById(R.id.layoutFechaFin);
        Button btnGuardar = findViewById(R.id.btnGuardar);

        // Dropdown de modalidad
        ArrayAdapter<String> modalidadAdapter = new ArrayAdapter<>(
                this, android.R.layout.simple_dropdown_item_1line, MODALIDADES);
        spinnerModalidad.setAdapter(modalidadAdapter);
        if (!isEditing) {
            spinnerModalidad.setText(MODALIDADES[0], false);
        }

        // DatePicker — Fecha Inicio
        View.OnClickListener pickerInicio = v -> showDatePicker(inputFechaInicio);
        inputFechaInicio.setOnClickListener(pickerInicio);
        layoutFechaInicio.setEndIconOnClickListener(pickerInicio);

        // DatePicker — Fecha Fin
        View.OnClickListener pickerFin = v -> showDatePicker(inputFechaFin);
        inputFechaFin.setOnClickListener(pickerFin);
        layoutFechaFin.setEndIconOnClickListener(pickerFin);

        // Guardar
        btnGuardar.setOnClickListener(v -> guardar());

        // Eliminar (solo edición) — botón en menú ya no existe,
        // lo agregamos como FAB-less: se muestra el botón delete inline si es edición
        // (el AlertDialog de confirmación está en el método confirmarEliminar)

        // Cargar datos en modo edición
        if (isEditing) {
            cargarDatos();
        }
    }

    private void showDatePicker(TextInputEditText target) {
        Calendar cal = Calendar.getInstance();
        // Si ya tiene fecha, parsear para abrir en esa fecha
        String actual = text(target);
        if (actual.length() == 10) {
            try {
                String[] parts = actual.split("-");
                cal.set(Integer.parseInt(parts[0]),
                        Integer.parseInt(parts[1]) - 1,
                        Integer.parseInt(parts[2]));
            } catch (Exception ignored) { }
        }
        new DatePickerDialog(this, (picker, year, month, day) -> {
            String fecha = String.format(Locale.ROOT, "%04d-%02d-%02d", year, month + 1, day);
            target.setText(fecha);
        }, cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH))
                .show();
    }

    private void cargarDatos() {
        ApiClient.enqueue(ApiClient.get().api().getPasantia(pasantiaId),
                new ApiClient.ApiCallback<PasantiaDto>() {
                    @Override
                    public void onSuccess(PasantiaDto p) {
                        inputTitulo.setText(p.titulo);
                        inputDescripcion.setText(p.descripcion);
                        inputFechaInicio.setText(p.fecha_inicio);
                        inputFechaFin.setText(p.fecha_fin);
                        inputArea.setText(p.area);
                        if (p.horario_laboral != null) inputHorario.setText(p.horario_laboral);
                        if (p.cupos_totales != null)   inputCupos.setText(String.valueOf(p.cupos_totales));
                        if (p.requisitos != null && !p.requisitos.isEmpty()) {
                            inputRequisitos.setText(android.text.TextUtils.join("\n", p.requisitos));
                        }
                        // Seleccionar modalidad
                        if (p.modalidad != null) {
                            spinnerModalidad.setText(p.modalidad, false);
                        }
                    }

                    @Override
                    public void onError(String message) {
                        Toast.makeText(PasantiaFormActivity.this, message, Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void guardar() {
        // Validación
        String titulo      = text(inputTitulo);
        String modalidad   = spinnerModalidad.getText().toString().trim();
        String cuposStr    = text(inputCupos);
        String horario     = text(inputHorario);
        String descripcion = text(inputDescripcion);
        String fechaInicio = text(inputFechaInicio);
        String fechaFin    = text(inputFechaFin);
        String area        = text(inputArea);

        if (titulo.isEmpty()) { inputTitulo.setError("Requerido"); inputTitulo.requestFocus(); return; }
        if (cuposStr.isEmpty()) { inputCupos.setError("Requerido"); inputCupos.requestFocus(); return; }
        int cupos;
        try {
            cupos = Integer.parseInt(cuposStr);
            if (cupos < 1) throw new NumberFormatException();
        } catch (NumberFormatException e) {
            inputCupos.setError("Debe ser un número ≥ 1"); inputCupos.requestFocus(); return;
        }
        if (horario.isEmpty()) { inputHorario.setError("Requerido"); inputHorario.requestFocus(); return; }
        if (descripcion.isEmpty()) { inputDescripcion.setError("Requerido"); inputDescripcion.requestFocus(); return; }
        if (fechaInicio.isEmpty()) { Toast.makeText(this, "Seleccioná la fecha de inicio", Toast.LENGTH_SHORT).show(); return; }
        if (fechaFin.isEmpty()) { Toast.makeText(this, "Seleccioná la fecha de fin", Toast.LENGTH_SHORT).show(); return; }
        if (fechaFin.compareTo(fechaInicio) < 0) {
            Toast.makeText(this, "La fecha de fin debe ser posterior a la de inicio", Toast.LENGTH_SHORT).show();
            return;
        }
        if (area.isEmpty()) { inputArea.setError("Requerido"); inputArea.requestFocus(); return; }

        // Requisitos: split por línea, filtrar vacíos
        List<String> requisitos = new ArrayList<>();
        String reqRaw = text(inputRequisitos);
        if (!reqRaw.isEmpty()) {
            for (String line : reqRaw.split("\n")) {
                String trimmed = line.trim();
                if (!trimmed.isEmpty()) requisitos.add(trimmed);
            }
        }

        CreatePasantiaRequest req = new CreatePasantiaRequest();
        req.titulo          = titulo;
        req.modalidad       = modalidad;
        req.cupos_totales   = cupos;
        req.horario_laboral = horario;
        req.descripcion     = descripcion;
        req.requisitos      = requisitos;
        req.fecha_inicio    = fechaInicio;
        req.fecha_fin       = fechaFin;
        req.area            = area;

        if (pasantiaId > 0) {
            ApiClient.enqueue(ApiClient.get().api().updatePasantia(pasantiaId, req),
                    new ApiClient.ApiCallback<PasantiaDto>() {
                        @Override public void onSuccess(PasantiaDto data) {
                            DialogHelper.showSuccess(PasantiaFormActivity.this, "Convocatoria de pasantía actualizada correctamente", () -> finish());
                        }
                        @Override public void onError(String message) {
                            Toast.makeText(PasantiaFormActivity.this, message, Toast.LENGTH_SHORT).show();
                        }
                    });
        } else {
            ApiClient.enqueue(ApiClient.get().api().createPasantiaGerente(req),
                    new ApiClient.ApiCallback<PasantiaDto>() {
                        @Override public void onSuccess(PasantiaDto data) {
                            DialogHelper.showSuccess(PasantiaFormActivity.this, "Convocatoria de pasantía creada correctamente", () -> finish());
                        }
                        @Override public void onError(String message) {
                            Toast.makeText(PasantiaFormActivity.this, message, Toast.LENGTH_SHORT).show();
                        }
                    });
        }
    }

    /** Llama al AlertDialog de confirmación y luego al endpoint DELETE */
    private void confirmarEliminar() {
        new AlertDialog.Builder(this)
                .setTitle(getString(R.string.dialog_confirm_title))
                .setMessage(getString(R.string.dialog_delete_msg))
                .setPositiveButton(getString(R.string.delete), (dialog, which) -> {
                    ApiClient.enqueue(ApiClient.get().api().deletePasantia(pasantiaId),
                            new ApiClient.ApiCallback<MessageResponse>() {
                                @Override public void onSuccess(MessageResponse data) {
                                    DialogHelper.showSuccess(PasantiaFormActivity.this, "Convocatoria de pasantía eliminada correctamente", () -> finish());
                                }
                                @Override public void onError(String message) {
                                    Toast.makeText(PasantiaFormActivity.this, message, Toast.LENGTH_SHORT).show();
                                }
                            });
                })
                .setNegativeButton(getString(R.string.cancel), null)
                .show();
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
        if (item.getItemId() == 1) {
            confirmarEliminar();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private String text(TextInputEditText et) {
        return et.getText() != null ? et.getText().toString().trim() : "";
    }
}
