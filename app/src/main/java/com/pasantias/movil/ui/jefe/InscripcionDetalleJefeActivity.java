package com.pasantias.movil.ui.jefe;

import android.content.res.ColorStateList;
import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.widget.NestedScrollView;

import com.pasantias.movil.R;
import com.pasantias.movil.data.api.ApiClient;
import com.pasantias.movil.data.dto.InscripcionDetalleJefeDto;
import com.pasantias.movil.data.dto.InscripcionDto;
import com.pasantias.movil.ui.common.DialogHelper;
import com.pasantias.movil.util.UiUtils;

public class InscripcionDetalleJefeActivity extends AppCompatActivity {

    public static final String EXTRA_INSCRIPCION_ID = "inscripcion_id";

    private int inscripcionId;
    private ProgressBar progress;
    private TextView textError;
    private NestedScrollView contentScroll;
    private View layoutAcciones;

    private TextView textIniciales;
    private TextView textNombre;
    private TextView textEstadoLabel;

    private View fieldCi;
    private View fieldSemestre;
    private View fieldCorreo;
    private View fieldTelefono;
    private View fieldPasantia;
    private View fieldFecha;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_inscripcion_detalle_jefe);

        inscripcionId = getIntent().getIntExtra(EXTRA_INSCRIPCION_ID, -1);
        if (inscripcionId == -1) {
            Toast.makeText(this, "ID de inscripción inválido", Toast.LENGTH_SHORT).show();
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
        layoutAcciones = findViewById(R.id.layoutAcciones);

        textIniciales = findViewById(R.id.textIniciales);
        textNombre = findViewById(R.id.textNombre);
        textEstadoLabel = findViewById(R.id.textEstadoLabel);

        fieldCi = findViewById(R.id.fieldCi);
        fieldSemestre = findViewById(R.id.fieldSemestre);
        fieldCorreo = findViewById(R.id.fieldCorreo);
        fieldTelefono = findViewById(R.id.fieldTelefono);
        fieldPasantia = findViewById(R.id.fieldPasantia);
        fieldFecha = findViewById(R.id.fieldFecha);

        findViewById(R.id.btnAprobar).setOnClickListener(v -> evaluar(true));
        findViewById(R.id.btnRechazar).setOnClickListener(v -> evaluar(false));

        loadData();
    }

    private void loadData() {
        progress.setVisibility(View.VISIBLE);
        contentScroll.setVisibility(View.GONE);
        layoutAcciones.setVisibility(View.GONE);
        textError.setVisibility(View.GONE);

        ApiClient.enqueue(ApiClient.get().api().getInscripcionDetalleJefe(inscripcionId), new ApiClient.ApiCallback<InscripcionDetalleJefeDto>() {
            @Override
            public void onSuccess(InscripcionDetalleJefeDto data) {
                progress.setVisibility(View.GONE);
                if (data == null) {
                    textError.setText("No se encontraron datos");
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
        String nombre = d.estudiante != null ? d.estudiante : "Estudiante";
        textNombre.setText(nombre);
        
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

        String estado = d.estado != null ? d.estado : "PENDIENTE";
        textEstadoLabel.setText(estado.toUpperCase());

        int colorRes = R.color.text_muted;
        if (estado.toLowerCase().contains("pendiente")) {
            colorRes = R.color.tertiary;
            layoutAcciones.setVisibility(View.VISIBLE);
        } else if (estado.toLowerCase().contains("aprobada") || estado.toLowerCase().contains("completada")) {
            colorRes = R.color.success;
            layoutAcciones.setVisibility(View.GONE);
        } else if (estado.toLowerCase().contains("rechazada")) {
            colorRes = R.color.danger;
            layoutAcciones.setVisibility(View.GONE);
        } else if (estado.toLowerCase().contains("invitado")) {
            colorRes = R.color.primary;
            layoutAcciones.setVisibility(View.GONE);
        }
        textEstadoLabel.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(colorRes, getTheme())));

        // Detalles estudiante
        bindField(fieldCi, "Registro Universitario / CI", d.ci);
        bindField(fieldSemestre, "Semestre Actual", "8vo o superior (Requisito)");
        bindField(fieldCorreo, "Correo Electrónico", d.email);
        bindField(fieldTelefono, "Jefe / Supervisor asignado", d.jefe);

        // Detalles pasantía
        bindField(fieldPasantia, "Convocatoria postulada", d.pasantia);
        bindField(fieldFecha, "Fecha de postulación", d.fecha);
    }

    private void bindField(View root, String title, String value) {
        root.findViewById(R.id.accentBar).setVisibility(View.GONE);
        ((TextView) root.findViewById(R.id.textTitle)).setText(title);
        ((TextView) root.findViewById(R.id.textSubtitle)).setText(value != null ? value : "No especificado");
        root.findViewById(R.id.textMeta).setVisibility(View.GONE);
    }

    private void evaluar(boolean aprobar) {
        progress.setVisibility(View.VISIBLE);
        layoutAcciones.setVisibility(View.GONE);

        ApiClient.ApiCallback<InscripcionDto> callback = new ApiClient.ApiCallback<InscripcionDto>() {
            @Override
            public void onSuccess(InscripcionDto data) {
                progress.setVisibility(View.GONE);
                String msg = aprobar ? "Inscripción aprobada exitosamente" : "Inscripción rechazada exitosamente";
                DialogHelper.showSuccess(InscripcionDetalleJefeActivity.this, msg, () -> finish());
            }

            @Override
            public void onError(String message) {
                progress.setVisibility(View.GONE);
                layoutAcciones.setVisibility(View.VISIBLE);
                Toast.makeText(InscripcionDetalleJefeActivity.this, message, Toast.LENGTH_LONG).show();
            }
        };

        if (aprobar) {
            ApiClient.enqueue(ApiClient.get().api().aprobarInscripcionJefe(inscripcionId), callback);
        } else {
            ApiClient.enqueue(ApiClient.get().api().rechazarInscripcionJefe(inscripcionId), callback);
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }
}
