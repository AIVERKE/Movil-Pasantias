package com.pasantias.movil.ui.jefe;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.google.android.material.tabs.TabLayout;
import com.pasantias.movil.R;
import com.pasantias.movil.data.api.ApiClient;
import com.pasantias.movil.data.dto.BitacoraJefeDto;
import com.pasantias.movil.ui.common.CardRowAdapter;

import java.util.ArrayList;
import java.util.List;

public class JefeBitacorasActivity extends AppCompatActivity {

    private SwipeRefreshLayout swipeRefresh;
    private RecyclerView recycler;
    private ProgressBar progress;
    private TextView textError;
    private TextView textEmpty;
    private TabLayout tabLayout;
    private CardRowAdapter adapter;

    private Integer filterInscripcionId;
    private Integer filterActividadId;

    private List<BitacoraJefeDto> allBitacoras = new ArrayList<>();
    private List<BitacoraJefeDto> filteredBitacoras = new ArrayList<>();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_jefe_bitacoras);

        filterInscripcionId = getIntent().getIntExtra("inscripcion_id", 0);
        if (filterInscripcionId == 0) filterInscripcionId = null;

        filterActividadId = getIntent().getIntExtra("actividadId", 0);
        if (filterActividadId == 0) filterActividadId = null;

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        swipeRefresh = findViewById(R.id.swipeRefresh);
        recycler = findViewById(R.id.recycler);
        progress = findViewById(R.id.progress);
        textError = findViewById(R.id.textError);
        textEmpty = findViewById(R.id.textEmpty);
        tabLayout = findViewById(R.id.tabLayout);

        adapter = new CardRowAdapter();
        recycler.setLayoutManager(new LinearLayoutManager(this));
        recycler.setAdapter(adapter);

        swipeRefresh.setColorSchemeResources(R.color.primary);
        swipeRefresh.setOnRefreshListener(this::loadData);

        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                updateList();
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {}

            @Override
            public void onTabReselected(TabLayout.Tab tab) {}
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadData();
    }

    private void loadData() {
        progress.setVisibility(View.VISIBLE);
        recycler.setVisibility(View.GONE);
        textError.setVisibility(View.GONE);
        textEmpty.setVisibility(View.GONE);

        ApiClient.enqueue(ApiClient.get().api().getJefeBitacoras(filterInscripcionId, filterActividadId), new ApiClient.ApiCallback<List<BitacoraJefeDto>>() {
            @Override
            public void onSuccess(List<BitacoraJefeDto> data) {
                swipeRefresh.setRefreshing(false);
                progress.setVisibility(View.GONE);
                allBitacoras = data != null ? data : new ArrayList<>();
                updateList();
            }

            @Override
            public void onError(String message) {
                swipeRefresh.setRefreshing(false);
                progress.setVisibility(View.GONE);
                textError.setText(message);
                textError.setVisibility(View.VISIBLE);
            }
        });
    }

    private void updateList() {
        int position = tabLayout.getSelectedTabPosition();
        filteredBitacoras.clear();

        for (BitacoraJefeDto b : allBitacoras) {
            String estado = b.estado != null ? b.estado.toLowerCase() : "";
            if (position == 0) {
                // Pendientes
                if (estado.contains("pendiente")) {
                    filteredBitacoras.add(b);
                }
            } else {
                // Calificadas
                if (estado.contains("calificada")) {
                    filteredBitacoras.add(b);
                }
            }
        }

        if (filteredBitacoras.isEmpty()) {
            recycler.setVisibility(View.GONE);
            textEmpty.setVisibility(View.VISIBLE);
            return;
        }

        textEmpty.setVisibility(View.GONE);
        List<CardRowAdapter.Row> rows = new ArrayList<>();
        for (BitacoraJefeDto b : filteredBitacoras) {
            String title = b.estudiante != null ? b.estudiante : "Estudiante";
            String sub = "Semana " + b.semana + " · " + b.actividad;
            String meta = "Avance: " + b.avanceRegistrado + "%" 
                    + (b.nota != null ? " · Nota: " + b.nota + "/100" : "");
            
            rows.add(new CardRowAdapter.Row(
                    title,
                    sub,
                    meta.toUpperCase(),
                    b.nota != null ? R.color.success : R.color.tertiary
            ));
        }

        adapter.setItems(rows);
        adapter.setOnRowClick(pos -> {
            if (pos >= 0 && pos < filteredBitacoras.size()) {
                BitacoraJefeDto selected = filteredBitacoras.get(pos);
                Intent intent = new Intent(this, JefeCalificarBitacoraActivity.class);
                intent.putExtra(JefeCalificarBitacoraActivity.EXTRA_BITACORA_ID, selected.id);
                startActivity(intent);
            }
        });

        recycler.setVisibility(View.VISIBLE);
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }
}
