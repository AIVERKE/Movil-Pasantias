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
import com.pasantias.movil.data.dto.InformeJefeDto;
import com.pasantias.movil.ui.common.CardRowAdapter;

import java.util.ArrayList;
import java.util.List;

public class JefeInformesActivity extends AppCompatActivity {

    private SwipeRefreshLayout swipeRefresh;
    private RecyclerView recycler;
    private ProgressBar progress;
    private TextView textError;
    private TextView textEmpty;
    private TabLayout tabLayout;
    private CardRowAdapter adapter;

    private List<InformeJefeDto> allInformes = new ArrayList<>();
    private List<InformeJefeDto> filteredInformes = new ArrayList<>();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_jefe_informes);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Informes Finales");
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

        ApiClient.enqueue(ApiClient.get().api().getJefeInformes(), new ApiClient.ApiCallback<List<InformeJefeDto>>() {
            @Override
            public void onSuccess(List<InformeJefeDto> data) {
                swipeRefresh.setRefreshing(false);
                progress.setVisibility(View.GONE);
                allInformes = data != null ? data : new ArrayList<>();
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
        filteredInformes.clear();

        for (InformeJefeDto inf : allInformes) {
            String estado = inf.estado != null ? inf.estado.toLowerCase() : "";
            if (position == 0) {
                // Pendientes
                if (estado.contains("pendiente")) {
                    filteredInformes.add(inf);
                }
            } else {
                // Emitidos
                if (estado.contains("emitido")) {
                    filteredInformes.add(inf);
                }
            }
        }

        if (filteredInformes.isEmpty()) {
            recycler.setVisibility(View.GONE);
            textEmpty.setVisibility(View.VISIBLE);
            return;
        }

        textEmpty.setVisibility(View.GONE);
        List<CardRowAdapter.Row> rows = new ArrayList<>();
        for (InformeJefeDto inf : filteredInformes) {
            String title = inf.estudiante != null ? inf.estudiante : "Estudiante";
            String sub = inf.pasantia != null ? inf.pasantia : "Pasantía";
            String meta = "Bitácoras: " + inf.bitacorasEvaluadas + "/" + inf.totalBitacoras
                    + (inf.notaFinal != null ? " · Nota Final: " + inf.notaFinal + "/100" : "");

            rows.add(new CardRowAdapter.Row(
                    title,
                    sub,
                    meta.toUpperCase(),
                    inf.notaFinal != null ? R.color.success : R.color.tertiary
            ));
        }

        adapter.setItems(rows);
        adapter.setOnRowClick(pos -> {
            if (pos >= 0 && pos < filteredInformes.size()) {
                InformeJefeDto selected = filteredInformes.get(pos);
                Intent intent = new Intent(this, JefeEmitirInformeActivity.class);
                intent.putExtra(JefeEmitirInformeActivity.EXTRA_INSC_ID, selected.id);
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

