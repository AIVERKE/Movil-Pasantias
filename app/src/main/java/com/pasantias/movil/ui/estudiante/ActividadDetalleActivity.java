package com.pasantias.movil.ui.estudiante;

import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.pasantias.movil.R;
import com.pasantias.movil.data.api.ApiClient;
import com.pasantias.movil.data.dto.ActividadDto;
import com.pasantias.movil.data.dto.BitacoraEstudianteRequest;
import com.pasantias.movil.data.dto.UpdateEstadoActividadRequest;

public class ActividadDetalleActivity extends AppCompatActivity {

    public static final String EXTRA_ID = "id_actividad";
    public static final String EXTRA_TITULO = "titulo";
    public static final String EXTRA_ESTADO = "estado";
    public static final String EXTRA_DESCRIPCION = "descripcion";
    public static final String EXTRA_FECHA = "fecha";

    private int actividadId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_actividad_detalle);
        actividadId = getIntent().getIntExtra(EXTRA_ID, -1);
        if (getSupportActionBar() != null) getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        setTitle(getIntent().getStringExtra(EXTRA_TITULO));

        TextView textTitulo = findViewById(R.id.textTitulo);
        TextView textEstado = findViewById(R.id.textEstado);
        TextView textFecha = findViewById(R.id.textFecha);
        TextView textDescripcion = findViewById(R.id.textDescripcion);
        TextInputEditText inputBitacora = findViewById(R.id.inputBitacora);
        TextInputEditText inputPorcentaje = findViewById(R.id.inputPorcentaje);
        MaterialButton btnGuardar = findViewById(R.id.btnGuardarBitacora);
        MaterialButton btnFinalizar = findViewById(R.id.btnFinalizar);

        textTitulo.setText(getIntent().getStringExtra(EXTRA_TITULO));
        textEstado.setText(getIntent().getStringExtra(EXTRA_ESTADO));
        textFecha.setText("Fecha límite: " + getIntent().getStringExtra(EXTRA_FECHA));
        textDescripcion.setText(getIntent().getStringExtra(EXTRA_DESCRIPCION));

        btnGuardar.setOnClickListener(v -> {
            String contenido = inputBitacora.getText() != null ? inputBitacora.getText().toString().trim() : "";
            int pct = 0;
            try {
                pct = Integer.parseInt(inputPorcentaje.getText() != null ? inputPorcentaje.getText().toString() : "0");
            } catch (NumberFormatException ignored) {
            }
            if (contenido.isEmpty()) {
                Toast.makeText(this, "Escribí el avance", Toast.LENGTH_SHORT).show();
                return;
            }
            ApiClient.enqueue(
                    ApiClient.get().api().crearBitacoraEstudiante(new BitacoraEstudianteRequest(actividadId, contenido, pct)),
                    new ApiClient.ApiCallback<Object>() {
                        @Override
                        public void onSuccess(Object data) {
                            Toast.makeText(ActividadDetalleActivity.this, "Avance registrado", Toast.LENGTH_SHORT).show();
                        }

                        @Override
                        public void onError(String message) {
                            Toast.makeText(ActividadDetalleActivity.this, message, Toast.LENGTH_SHORT).show();
                        }
                    }
            );
        });

        btnFinalizar.setOnClickListener(v -> ApiClient.enqueue(
                ApiClient.get().api().actualizarEstadoActividadEstudiante(actividadId, new UpdateEstadoActividadRequest("finalizada")),
                new ApiClient.ApiCallback<ActividadDto>() {
                    @Override
                    public void onSuccess(ActividadDto data) {
                        Toast.makeText(ActividadDetalleActivity.this, "Actividad finalizada", Toast.LENGTH_SHORT).show();
                        finish();
                    }

                    @Override
                    public void onError(String message) {
                        Toast.makeText(ActividadDetalleActivity.this, message, Toast.LENGTH_SHORT).show();
                    }
                }
        ));
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }
}
