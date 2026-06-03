package com.pasantias.movil.ui.gerente;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.pasantias.movil.R;
import com.pasantias.movil.data.api.ApiClient;
import com.pasantias.movil.data.dto.PasantiaDto;
import com.pasantias.movil.ui.common.CardRowAdapter;
import com.pasantias.movil.ui.common.ListRefreshFragment;

import java.util.ArrayList;
import java.util.List;

public class GerentePasantiasFragment extends ListRefreshFragment {

    private List<PasantiaDto> pasantias = new ArrayList<>();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_pasantias_list, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        swipeRefresh = view.findViewById(R.id.swipeRefresh);
        recycler = view.findViewById(R.id.recycler);
        progress = view.findViewById(R.id.progress);
        textError = view.findViewById(R.id.textError);
        textEmpty = view.findViewById(R.id.textEmpty);
        adapter = new CardRowAdapter();
        recycler.setLayoutManager(new androidx.recyclerview.widget.LinearLayoutManager(requireContext()));
        recycler.setAdapter(adapter);
        swipeRefresh.setColorSchemeResources(R.color.primary);
        swipeRefresh.setOnRefreshListener(this::loadData);

        FloatingActionButton fab = view.findViewById(R.id.fabAdd);
        fab.setOnClickListener(v -> startActivity(new Intent(requireContext(), PasantiaFormActivity.class)));

        adapter.setOnRowClick(pos -> {
            PasantiaDto p = pasantias.get(pos);
            Intent i = new Intent(requireContext(), PasantiaFormActivity.class);
            i.putExtra(PasantiaFormActivity.EXTRA_ID, p.id_pasantia);
            startActivity(i);
        });
        loadData();
    }

    @Override
    protected void loadData() {
        showLoading(!swipeRefresh.isRefreshing());
        ApiClient.enqueue(ApiClient.get().api().listPasantiasGerente(), new ApiClient.ApiCallback<List<PasantiaDto>>() {
            @Override
            public void onSuccess(List<PasantiaDto> data) {
                pasantias = data != null ? data : new ArrayList<>();
                List<CardRowAdapter.Row> rows = new ArrayList<>();
                for (PasantiaDto p : pasantias) {
                    String desc = p.descripcion != null ? p.descripcion : "";
                    if (desc.length() > 80) desc = desc.substring(0, 80) + "…";
                    rows.add(new CardRowAdapter.Row(p.titulo, desc, p.estado, R.color.primary));
                }
                adapter.setItems(rows);
                showContent(!rows.isEmpty());
            }

            @Override
            public void onError(String message) {
                showError(message);
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        loadData();
    }
}
