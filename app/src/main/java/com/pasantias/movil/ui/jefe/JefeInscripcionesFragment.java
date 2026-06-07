package com.pasantias.movil.ui.jefe;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.material.tabs.TabLayout;
import com.pasantias.movil.R;
import com.pasantias.movil.data.api.ApiClient;
import com.pasantias.movil.data.dto.InscripcionDto;
import com.pasantias.movil.ui.common.CardRowAdapter;
import com.pasantias.movil.ui.common.ListRefreshFragment;

import java.util.ArrayList;
import java.util.List;

public class JefeInscripcionesFragment extends ListRefreshFragment {

    private List<InscripcionDto> allInscripciones = new ArrayList<>();
    private List<InscripcionDto> filteredInscripciones = new ArrayList<>();
    private TabLayout tabLayout;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_jefe_inscripciones, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        tabLayout = view.findViewById(R.id.tabLayout);
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

        view.findViewById(R.id.fabInvitar).setOnClickListener(v -> {
            startActivity(new Intent(requireContext(), InvitarEstudianteActivity.class));
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        loadData();
    }

    @Override
    protected void loadData() {
        showLoading(!swipeRefresh.isRefreshing());
        ApiClient.enqueue(ApiClient.get().api().listInscripcionesJefe(), new ApiClient.ApiCallback<List<InscripcionDto>>() {
            @Override
            public void onSuccess(List<InscripcionDto> data) {
                allInscripciones = data != null ? data : new ArrayList<>();
                updateList();
            }

            @Override
            public void onError(String message) {
                showError(message);
            }
        });
    }

    private void updateList() {
        if (tabLayout == null) return;
        int position = tabLayout.getSelectedTabPosition();
        filteredInscripciones.clear();

        for (InscripcionDto i : allInscripciones) {
            String estado = i.estado != null ? i.estado.toLowerCase() : "";
            switch (position) {
                case 0: // Pendientes/Invitados
                    if (estado.contains("pendiente") || estado.contains("invitado")) {
                        filteredInscripciones.add(i);
                    }
                    break;
                case 1: // Aprobadas/Completadas
                    if (estado.contains("aprobada") || estado.contains("completada")) {
                        filteredInscripciones.add(i);
                    }
                    break;
                case 2: // Rechazadas
                    if (estado.contains("rechazada")) {
                        filteredInscripciones.add(i);
                    }
                    break;
                case 3: // Todas
                    filteredInscripciones.add(i);
                    break;
            }
        }

        List<CardRowAdapter.Row> rows = new ArrayList<>();
        for (InscripcionDto i : filteredInscripciones) {
            String estNombre = i.getEstudianteNombre() != null ? i.getEstudianteNombre() : "Estudiante";
            String sub = i.getPasantiaTitulo();
            int colorRes = R.color.text_muted;
            String estado = i.estado != null ? i.estado.toLowerCase() : "";
            
            if (estado.contains("pendiente")) {
                colorRes = R.color.tertiary;
            } else if (estado.contains("aprobada") || estado.contains("completada")) {
                colorRes = R.color.success;
            } else if (estado.contains("rechazada")) {
                colorRes = R.color.danger;
            } else if (estado.contains("invitado")) {
                colorRes = R.color.primary;
            }

            rows.add(new CardRowAdapter.Row(
                    estNombre,
                    sub,
                    i.estado != null ? i.estado.toUpperCase() : "S/E",
                    colorRes
            ));
        }

        adapter.setItems(rows);
        adapter.setOnRowClick(pos -> {
            if (pos >= 0 && pos < filteredInscripciones.size()) {
                InscripcionDto selected = filteredInscripciones.get(pos);
                Intent intent = new Intent(requireContext(), InscripcionDetalleJefeActivity.class);
                intent.putExtra(InscripcionDetalleJefeActivity.EXTRA_INSCRIPCION_ID, selected.getId());
                startActivity(intent);
            }
        });

        showContent(!rows.isEmpty());
    }
}
