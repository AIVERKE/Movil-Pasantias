package com.pasantias.movil.ui.estudiante;

import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
import com.pasantias.movil.R;
import com.pasantias.movil.data.api.ApiClient;
import com.pasantias.movil.data.dto.CreateInscripcionRequest;
import com.pasantias.movil.data.dto.InscripcionDto;
import com.pasantias.movil.data.dto.PasantiaDto;
import com.pasantias.movil.data.local.TokenManager;

public class PasantiaDetalleActivity extends AppCompatActivity {

    public static final String EXTRA_ID = "id_pasantia";

    private int pasantiaId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pasantia_detalle);
        pasantiaId = getIntent().getIntExtra(EXTRA_ID, -1);
        if (getSupportActionBar() != null) getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        TextView textTitulo = findViewById(R.id.textTitulo);
        TextView textEmpresa = findViewById(R.id.textEmpresa);
        TextView textDescripcion = findViewById(R.id.textDescripcion);
        TextView textMeta = findViewById(R.id.textMeta);
        ProgressBar progress = findViewById(R.id.progress);
        MaterialButton btnPostular = findViewById(R.id.btnPostular);

        progress.setVisibility(View.VISIBLE);
        ApiClient.enqueue(ApiClient.get().api().getPasantia(pasantiaId), new ApiClient.ApiCallback<PasantiaDto>() {
            @Override
            public void onSuccess(PasantiaDto p) {
                progress.setVisibility(View.GONE);
                textTitulo.setText(p.titulo);
                textEmpresa.setText(p.empresa_nombre != null ? p.empresa_nombre : "");
                textDescripcion.setText(p.descripcion);
                textMeta.setText("Inicio: " + p.fecha_inicio + " · Modalidad: " + (p.modalidad != null ? p.modalidad : "—"));
            }

            @Override
            public void onError(String message) {
                progress.setVisibility(View.GONE);
                Toast.makeText(PasantiaDetalleActivity.this, message, Toast.LENGTH_SHORT).show();
            }
        });

        btnPostular.setOnClickListener(v -> {
            int estudianteId = TokenManager.get().getUser().id;
            ApiClient.enqueue(
                    ApiClient.get().api().crearInscripcion(new CreateInscripcionRequest(estudianteId, pasantiaId)),
                    new ApiClient.ApiCallback<InscripcionDto>() {
                        @Override
                        public void onSuccess(InscripcionDto data) {
                            Toast.makeText(PasantiaDetalleActivity.this, "Postulación enviada", Toast.LENGTH_SHORT).show();
                            finish();
                        }

                        @Override
                        public void onError(String message) {
                            Toast.makeText(PasantiaDetalleActivity.this, message, Toast.LENGTH_SHORT).show();
                        }
                    }
            );
        });
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }
}
