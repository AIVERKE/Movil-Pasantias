package com.pasantias.movil.ui.jefe;

import android.os.Bundle;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;
import com.pasantias.movil.R;
import com.pasantias.movil.data.api.ApiClient;
import com.pasantias.movil.data.dto.ComentarioDto;
import com.pasantias.movil.data.dto.ComentarioRequest;
import com.pasantias.movil.data.local.TokenManager;
import com.pasantias.movil.ui.common.CardRowAdapter;

import java.util.ArrayList;
import java.util.List;

public class ActividadComentariosActivity extends AppCompatActivity {

    public static final String EXTRA_ACTIVIDAD_ID = "id_actividad";
    public static final String EXTRA_TITULO = "titulo";
    public static final String EXTRA_TIPO = "tipo_actividad"; // "actividad" o "tarea"

    private int actividadId;
    private String tipoActividad;
    private CardRowAdapter adapter;
    private EditText inputMensaje;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_comentarios);
        actividadId = getIntent().getIntExtra(EXTRA_ACTIVIDAD_ID, -1);
        tipoActividad = getIntent().getStringExtra(EXTRA_TIPO);
        if (tipoActividad == null) tipoActividad = "tarea";

        setTitle(getIntent().getStringExtra(EXTRA_TITULO));
        if (getSupportActionBar() != null) getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        RecyclerView recycler = findViewById(R.id.recycler);
        inputMensaje = findViewById(R.id.inputMensaje);
        MaterialButton btnEnviar = findViewById(R.id.btnEnviar);
        adapter = new CardRowAdapter();
        recycler.setLayoutManager(new LinearLayoutManager(this));
        recycler.setAdapter(adapter);
        btnEnviar.setOnClickListener(v -> enviar());
        cargar();
    }

    private void cargar() {
        retrofit2.Call<List<ComentarioDto>> call;
        if ("actividad".equalsIgnoreCase(tipoActividad)) {
            call = ApiClient.get().api().listComentariosActividadPasantia(actividadId);
        } else {
            call = ApiClient.get().api().listComentariosActividad(actividadId);
        }

        ApiClient.enqueue(call, new ApiClient.ApiCallback<List<ComentarioDto>>() {
            @Override
            public void onSuccess(List<ComentarioDto> data) {
                List<CardRowAdapter.Row> rows = new ArrayList<>();
                if (data != null) {
                    for (ComentarioDto c : data) {
                        rows.add(new CardRowAdapter.Row(
                                c.autor + " (" + c.rol + ")",
                                c.texto,
                                c.fecha,
                                R.color.primary
                        ));
                    }
                }
                adapter.setItems(rows);
            }

            @Override
            public void onError(String message) {
                Toast.makeText(ActividadComentariosActivity.this, message, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void enviar() {
        String texto = inputMensaje.getText().toString().trim();
        if (texto.isEmpty()) return;

        String autor = TokenManager.get().getUser().getNombreCompleto();
        String tipoUsuario = TokenManager.get().getUser().tipo;
        String rol = "Estudiante";
        if (tipoUsuario != null && (tipoUsuario.toLowerCase().contains("jefe") || tipoUsuario.toLowerCase().contains("gerente"))) {
            rol = "Jefe";
        }

        ComentarioRequest req = new ComentarioRequest(texto, rol, autor);
        retrofit2.Call<ComentarioDto> call;
        if ("actividad".equalsIgnoreCase(tipoActividad)) {
            call = ApiClient.get().api().crearComentarioActividadPasantia(actividadId, req);
        } else {
            call = ApiClient.get().api().crearComentarioActividad(actividadId, req);
        }

        ApiClient.enqueue(call, new ApiClient.ApiCallback<ComentarioDto>() {
            @Override
            public void onSuccess(ComentarioDto data) {
                inputMensaje.setText("");
                cargar();
            }

            @Override
            public void onError(String message) {
                Toast.makeText(ActividadComentariosActivity.this, message, Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }
}
