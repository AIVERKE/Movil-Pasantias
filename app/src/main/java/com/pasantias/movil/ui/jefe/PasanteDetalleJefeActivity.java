package com.pasantias.movil.ui.jefe;

import android.content.Intent;
import android.content.res.ColorStateList;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.widget.NestedScrollView;

import com.pasantias.movil.R;
import com.pasantias.movil.ui.common.DialogHelper;
import com.pasantias.movil.data.api.ApiClient;
import com.pasantias.movil.data.dto.InscripcionDetalleJefeDto;
import com.pasantias.movil.data.dto.MessageResponse;

import java.util.HashMap;
import java.util.Map;

public class PasanteDetalleJefeActivity extends AppCompatActivity {

    public static final String EXTRA_INSCRIPCION_ID = "inscripcion_id";

    private int inscripcionId;
    private ProgressBar progress;
    private TextView textError;
    private NestedScrollView contentScroll;

    private TextView textIniciales;
    private TextView textNombre;
    private TextView textPasantia;
    private TextView textEstado;

    private View fieldCi;
    private View fieldCorreo;
    private View fieldTelefono;
    private View fieldFecha;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pasante_detalle_jefe);

        inscripcionId = getIntent().getIntExtra(EXTRA_INSCRIPCION_ID, -1);
        if (inscripcionId == -1) {
            Toast.makeText(this, "ID de pasante inválido", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        progress = findViewById(R.id.progress);
        textError = findViewById(R.id.textError);
        contentScroll = findViewById(R.id.contentScroll);

        textIniciales = findViewById(R.id.textIniciales);
        textNombre = findViewById(R.id.textNombre);
        textPasantia = findViewById(R.id.textPasantia);
        textEstado = findViewById(R.id.textEstado);

        fieldCi = findViewById(R.id.fieldCi);
        fieldCorreo = findViewById(R.id.fieldCorreo);
        fieldTelefono = findViewById(R.id.fieldTelefono);
        fieldFecha = findViewById(R.id.fieldFecha);

        findViewById(R.id.btnVerBitacoras).setOnClickListener(v -> {
            Intent intent = new Intent(this, JefeBitacorasActivity.class);
            intent.putExtra(EXTRA_INSCRIPCION_ID, inscripcionId);
            startActivity(intent);
        });

        findViewById(R.id.btnEmitirInforme).setOnClickListener(v -> {
            Intent intent = new Intent(this, JefeEmitirInformeActivity.class);
            intent.putExtra(JefeEmitirInformeActivity.EXTRA_INSCRIPCION_ID, inscripcionId);
            startActivity(intent);
        });

        findViewById(R.id.btnDarDeBaja).setOnClickListener(v -> mostrarDialogoBaja());

        loadData();
    }

    private void loadData() {
        progress.setVisibility(View.VISIBLE);
        contentScroll.setVisibility(View.GONE);
        textError.setVisibility(View.GONE);

        ApiClient.enqueue(ApiClient.get().api().getInscripcionDetalleJefe(inscripcionId), new ApiClient.ApiCallback<InscripcionDetalleJefeDto>() {
            @Override
            public void onSuccess(InscripcionDetalleJefeDto data) {
                progress.setVisibility(View.GONE);
                if (data == null) {
                    textError.setText("No se encontraron datos del pasante");
                    textError.setVisibility(View.VISIBLE);
                    return;
                }
                bindData(data);
                contentScroll.setVisibility(View.VISIBLE);
            }

            @Override
            public void onError(String message) {
                progress.setVisibility(View.GONE);
                textError.setText(message);
                textError.setVisibility(View.VISIBLE);
            }
        });
    }

    private void bindData(InscripcionDetalleJefeDto d) {
        String nombre = d.estudiante != null ? d.estudiante : "Pasante";
        textNombre.setText(nombre);
        textPasantia.setText(d.pasantia != null ? d.pasantia : "Pasantía");

        String iniciales = "";
        if (nombre.trim().contains(" ")) {
            String[] parts = nombre.split("\\s+");
            if (parts.length >= 2) {
                iniciales = ("" + parts[0].charAt(0) + parts[1].charAt(0)).toUpperCase();
            } else if (parts.length == 1) {
                iniciales = ("" + parts[0].charAt(0)).toUpperCase();
            }
        } else if (!nombre.isEmpty()) {
            iniciales = ("" + nombre.charAt(0)).toUpperCase();
        }
        textIniciales.setText(iniciales.isEmpty() ? "??" : iniciales);

        String estado = d.estado != null ? d.estado : "APROBADA";
        textEstado.setText(estado.toUpperCase());

        int colorRes = R.color.text_muted;
        if (estado.toLowerCase().contains("aprobada") || estado.toLowerCase().contains("completada")) {
            colorRes = R.color.success;
        } else if (estado.toLowerCase().contains("rechazada") || estado.toLowerCase().contains("baja")) {
            colorRes = R.color.danger;
        } else {
            colorRes = R.color.tertiary;
        }
        textEstado.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(colorRes, getTheme())));

        bindField(fieldCi, "Registro Universitario / CI", d.ci);
        bindField(fieldCorreo, "Correo Electrónico", d.email);
        bindField(fieldTelefono, "Supervisor asignado", d.jefe);
        bindField(fieldFecha, "Fecha de inicio", d.fecha);
    }

    private void bindField(View root, String title, String value) {
        root.findViewById(R.id.accentBar).setVisibility(View.GONE);
        ((TextView) root.findViewById(R.id.textTitle)).setText(title);
        ((TextView) root.findViewById(R.id.textSubtitle)).setText(value != null ? value : "No especificado");
        root.findViewById(R.id.textMeta).setVisibility(View.GONE);
    }

    private void mostrarDialogoBaja() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Dar de Baja al Pasante");

        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(50, 40, 50, 20);

        TextView labelMotivo = new TextView(this);
        labelMotivo.setText("Seleccioná el motivo de la baja:");
        labelMotivo.setPadding(0, 0, 0, 10);
        layout.addView(labelMotivo);

        Spinner spinner = new Spinner(this);
        String[] motivos = {
                "Ausencia injustificada prolongada",
                "Incumplimiento de tareas",
                "Decisión del estudiante",
                "Otro"
        };
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, motivos);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);
        spinner.setPadding(0, 0, 0, 20);
        layout.addView(spinner);

        TextView labelObs = new TextView(this);
        labelObs.setText("Detalles adicionales:");
        labelObs.setPadding(0, 15, 0, 10);
        layout.addView(labelObs);

        EditText input = new EditText(this);
        input.setHint("Escribí observaciones aquí...");
        layout.addView(input);

        builder.setView(layout);

        builder.setPositiveButton("Confirmar Baja", (dialog, which) -> {
            String motivo = spinner.getSelectedItem().toString();
            String observacion = input.getText().toString().trim();
            darDeBaja(motivo, observacion);
        });
        builder.setNegativeButton("Cancelar", (dialog, which) -> dialog.cancel());

        builder.show();
    }

    private void darDeBaja(String motivo, String observacion) {
        progress.setVisibility(View.VISIBLE);
        contentScroll.setVisibility(View.GONE);

        Map<String, Object> body = new HashMap<>();
        body.put("motivo", motivo);
        body.put("observacion", observacion);

        ApiClient.enqueue(ApiClient.get().api().darDeBajaPasante(inscripcionId, body), new ApiClient.ApiCallback<MessageResponse>() {
            @Override
            public void onSuccess(MessageResponse data) {
                progress.setVisibility(View.GONE);
                DialogHelper.showSuccess(PasanteDetalleJefeActivity.this, "El pasante fue dado de baja", () -> loadData());
            }

            @Override
            public void onError(String message) {
                progress.setVisibility(View.GONE);
                contentScroll.setVisibility(View.VISIBLE);
                Toast.makeText(PasanteDetalleJefeActivity.this, message, Toast.LENGTH_LONG).show();
            }
        });
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }
}
