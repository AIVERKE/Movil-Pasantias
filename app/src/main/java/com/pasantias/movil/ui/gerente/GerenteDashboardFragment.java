package com.pasantias.movil.ui.gerente;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.pasantias.movil.R;
import com.pasantias.movil.data.api.ApiClient;
import com.pasantias.movil.data.dto.GerenteDashboardDto;
import com.pasantias.movil.ui.common.CardRowAdapter;

import java.util.ArrayList;
import java.util.List;

public class GerenteDashboardFragment extends Fragment {

    private SwipeRefreshLayout swipeRefresh;
    private ProgressBar progress;
    private TextView textError;
    private RecyclerView recyclerRecientes;
    private CardRowAdapter adapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_gerente_dashboard, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        swipeRefresh = view.findViewById(R.id.swipeRefresh);
        progress = view.findViewById(R.id.progress);
        textError = view.findViewById(R.id.textError);
        recyclerRecientes = view.findViewById(R.id.recyclerRecientes);
        adapter = new CardRowAdapter();
        recyclerRecientes.setLayoutManager(new LinearLayoutManager(requireContext()));
        recyclerRecientes.setAdapter(adapter);

        bindStat(view.findViewById(R.id.stat1), "Pasantes activos", "0", R.color.success);
        bindStat(view.findViewById(R.id.stat2), "Pasantías publicadas", "0", R.color.primary);
        bindStat(view.findViewById(R.id.stat3), "Equipo (jefes)", "0", R.color.secondary);

        swipeRefresh.setOnRefreshListener(this::load);
        load();
    }

    private void bindStat(View root, String title, String value, int color) {
        ((TextView) root.findViewById(R.id.statTitle)).setText(title);
        ((TextView) root.findViewById(R.id.statValue)).setText(value);
        root.findViewById(R.id.statAccent).setBackgroundColor(requireContext().getColor(color));
    }

    private void load() {
        progress.setVisibility(View.VISIBLE);
        textError.setVisibility(View.GONE);
        ApiClient.enqueue(ApiClient.get().api().gerenteDashboard(), new ApiClient.ApiCallback<GerenteDashboardDto>() {
            @Override
            public void onSuccess(GerenteDashboardDto data) {
                swipeRefresh.setRefreshing(false);
                progress.setVisibility(View.GONE);
                View root = getView();
                if (root == null) return;
                bindStat(root.findViewById(R.id.stat1), "Pasantes activos", String.valueOf(data.pasantesActivos), R.color.success);
                bindStat(root.findViewById(R.id.stat2), "Pasantías publicadas", String.valueOf(data.pasantiasPublicadas), R.color.primary);
                bindStat(root.findViewById(R.id.stat3), "Equipo (jefes)", String.valueOf(data.equipo), R.color.secondary);
                List<CardRowAdapter.Row> rows = new ArrayList<>();
                if (data.pasantiasRecientes != null) {
                    for (GerenteDashboardDto.PasantiaResumenDto p : data.pasantiasRecientes) {
                        rows.add(new CardRowAdapter.Row(
                                p.titulo,
                                "Jefe: " + (p.jefe != null ? p.jefe : "—"),
                                p.estado + " · " + p.pasantes + " pasantes",
                                R.color.primary
                        ));
                    }
                }
                adapter.setItems(rows);
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
}
