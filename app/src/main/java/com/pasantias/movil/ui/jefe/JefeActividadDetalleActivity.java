package com.pasantias.movil.ui.jefe;

import android.content.Intent;
import android.content.res.ColorStateList;
import android.os.Bundle;
import android.view.View;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.widget.NestedScrollView;

import com.pasantias.movil.R;
import com.pasantias.movil.data.api.ApiClient;
import com.pasantias.movil.data.dto.ActividadDto;
import com.pasantias.movil.data.dto.JefeActividadDto;
import com.pasantias.movil.ui.common.DialogHelper;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class JefeActividadDetalleActivity extends AppCompatActivity {

    public static final String EXTRA_ACTIVIDAD_ID = "id_actividad";

    private int actividadId;
    private ProgressBar progress;
    private TextView textError;
    private NestedScrollView contentScroll;

    private TextView textPasantiaTitulo;
    private TextView textActividadTitulo;
    private TextView textEstado;
    private TextView textDescripcion;
    private TextView textFechas;

    private LinearLayout layoutAsignados;
    private TextView textNoPasantes;
    private View btnCerrarActividad;

    private JefeActividadDto actividadActual;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_jefe_actividad_detalle);

        actividadId = getIntent().getIntExtra(EXTRA_ACTIVIDAD_ID, -1);
        if (actividadId == -1) {
            Toast.makeText(this, "ID de actividad inválido", Toast.LENGTH_SHORT).show();
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

        textPasantiaTitulo = findViewById(R.id.textPasantiaTitulo);
        textActividadTitulo = findViewById(R.id.textActividadTitulo);
        textEstado = findViewById(R.id.textEstado);
        textDescripcion = findViewById(R.id.textDescripcion);
        textFechas = findViewById(R.id.textFechas);

        layoutAsignados = findViewById(R.id.layoutAsignados);
        textNoPasantes = findViewById(R.id.textNoPasantes);

        btnCerrarActividad = findViewById(R.id.btnCerrarActividad);
        btnCerrarActividad.setOnClickListener(v -> cerrarActividad());

        findViewById(R.id.btnComentarios).setOnClickListener(v -> {
            if (actividadActual != null) {
                Intent i = new Intent(this, ActividadComentariosActivity.class);
                i.putExtra(ActividadComentariosActivity.EXTRA_ACTIVIDAD_ID, actividadActual.id_actividad);
                i.putExtra(ActividadComentariosActivity.EXTRA_TITULO, actividadActual.getTitulo());
                i.putExtra(ActividadComentariosActivity.EXTRA_TIPO, "actividad");
                startActivity(i);
            }
        });

        findViewById(R.id.btnVerBitacoras).setOnClickListener(v -> {
            if (actividadActual != null) {
                Intent intent = new Intent(this, JefeBitacorasActivity.class);
                intent.putExtra("actividadId", actividadActual.id_actividad);
                startActivity(intent);
            }
        });

        loadData();
    }

    private void loadData() {
        progress.setVisibility(View.VISIBLE);
        contentScroll.setVisibility(View.GONE);
        textError.setVisibility(View.GONE);

        ApiClient.enqueue(ApiClient.get().api().listActividadesJefe(), new ApiClient.ApiCallback<List<JefeActividadDto>>() {
            @Override
            public void onSuccess(List<JefeActividadDto> data) {
                progress.setVisibility(View.GONE);
                if (data == null) {
                    textError.setText("Error cargando detalles");
                    textError.setVisibility(View.VISIBLE);
                    return;
                }

                for (JefeActividadDto a : data) {
                    if (a.id_actividad == actividadId) {
                        actividadActual = a;
                        break;
                    }
                }

                if (actividadActual == null) {
                    textError.setText("No se encontró la actividad seleccionada");
                    textError.setVisibility(View.VISIBLE);
                    return;
                }

                bindData(actividadActual);
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

    private void bindData(JefeActividadDto a) {
        textPasantiaTitulo.setText(a.pasantia != null ? a.pasantia.titulo.toUpperCase() : "PASANTÍA");
        textActividadTitulo.setText(a.descripcion);
        textDescripcion.setText(a.descripcion);

        String ini = a.fecha_inicio != null ? a.fecha_inicio : "S/F";
        String fin = a.fecha_fin != null ? a.fecha_fin : "S/F";
        textFechas.setText("Fechas: " + ini + " al " + fin);

        String estado = a.estado != null ? a.estado : "PENDIENTE";
        textEstado.setText(estado.replace("_", " ").toUpperCase());

        int colorRes = R.color.text_muted;
        if (estado.toLowerCase().contains("pendiente")) {
            colorRes = R.color.tertiary;
            btnCerrarActividad.setVisibility(View.VISIBLE);
        } else if (estado.toLowerCase().contains("desarrollo")) {
            colorRes = R.color.primary;
            btnCerrarActividad.setVisibility(View.VISIBLE);
        } else if (estado.toLowerCase().contains("finalizada")) {
            colorRes = R.color.success;
            btnCerrarActividad.setVisibility(View.VISIBLE);
        } else if (estado.toLowerCase().contains("cerrada")) {
            colorRes = R.color.danger;
            btnCerrarActividad.setVisibility(View.GONE);
        }
        textEstado.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(colorRes, getTheme())));

        setupPasantesCheckboxes(a);
    }

    private void setupPasantesCheckboxes(JefeActividadDto a) {
        layoutAsignados.removeAllViews();

        List<JefeActividadDto.PasanteRef> disponibles = a.pasantesDisponibles != null ? a.pasantesDisponibles : new ArrayList<>();
        List<JefeActividadDto.PasanteRef> asignados = a.asignados != null ? a.asignados : new ArrayList<>();

        if (disponibles.isEmpty()) {
            textNoPasantes.setVisibility(View.VISIBLE);
            return;
        }
        textNoPasantes.setVisibility(View.GONE);

        for (JefeActividadDto.PasanteRef pas : disponibles) {
            CheckBox cb = new CheckBox(this);
            cb.setText(pas.estudiante);
            cb.setPadding(0, 16, 0, 16);

            // Verificar si ya está asignado
            boolean estaAsignado = false;
            for (JefeActividadDto.PasanteRef asig : asignados) {
                if (asig.id_inscripcion == pas.id_inscripcion) {
                    estaAsignado = true;
                    break;
                }
            }
            cb.setChecked(estaAsignado);

            // Bloquear edición si la actividad está cerrada
            if ("cerrada".equalsIgnoreCase(a.estado)) {
                cb.setEnabled(false);
            }

            cb.setOnClickListener(v -> {
                boolean checked = cb.isChecked();
                setAssignment(pas.id_inscripcion, checked, cb);
            });

            layoutAsignados.addView(cb);
        }
    }

    private void setAssignment(int idInscripcion, boolean asignar, CheckBox cb) {
        progress.setVisibility(View.VISIBLE);
        contentScroll.setVisibility(View.GONE);

        Map<String, Object> body = new HashMap<>();
        body.put("id_inscripcion", idInscripcion);

        ApiClient.ApiCallback<ActividadDto> callback = new ApiClient.ApiCallback<ActividadDto>() {
            @Override
            public void onSuccess(ActividadDto data) {
                progress.setVisibility(View.GONE);
                String msg = asignar ? "Pasante asignado con éxito" : "Pasante desasignado con éxito";
                DialogHelper.showSuccess(JefeActividadDetalleActivity.this, msg, () -> loadData());
            }

            @Override
            public void onError(String message) {
                progress.setVisibility(View.GONE);
                contentScroll.setVisibility(View.VISIBLE);
                // Revertir estado del checkbox
                cb.setChecked(!asignar);
                Toast.makeText(JefeActividadDetalleActivity.this, message, Toast.LENGTH_LONG).show();
            }
        };

        if (asignar) {
            ApiClient.enqueue(ApiClient.get().api().asignarPasanteActividad(actividadId, body), callback);
        } else {
            ApiClient.enqueue(ApiClient.get().api().desasignarPasanteActividad(actividadId, body), callback);
        }
    }

    private void cerrarActividad() {
        progress.setVisibility(View.VISIBLE);
        contentScroll.setVisibility(View.GONE);

        Map<String, Object> body = new HashMap<>();
        body.put("estado", "cerrada");

        ApiClient.enqueue(ApiClient.get().api().cambiarEstadoActividadJefe(actividadId, body), new ApiClient.ApiCallback<ActividadDto>() {
            @Override
            public void onSuccess(ActividadDto data) {
                progress.setVisibility(View.GONE);
                DialogHelper.showSuccess(JefeActividadDetalleActivity.this, "Actividad cerrada con éxito", () -> loadData());
            }

            @Override
            public void onError(String message) {
                progress.setVisibility(View.GONE);
                contentScroll.setVisibility(View.VISIBLE);
                Toast.makeText(JefeActividadDetalleActivity.this, message, Toast.LENGTH_LONG).show();
            }
        });
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }
}
