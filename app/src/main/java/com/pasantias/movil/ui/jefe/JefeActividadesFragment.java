package com.pasantias.movil.ui.jefe;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.tabs.TabLayout;
import com.pasantias.movil.R;
import com.pasantias.movil.data.api.ApiClient;
import com.pasantias.movil.data.dto.JefeActividadDto;
import com.pasantias.movil.data.dto.JefeTareaDto;
import com.pasantias.movil.ui.common.CardRowAdapter;
import com.pasantias.movil.ui.common.ListRefreshFragment;

import java.util.ArrayList;
import java.util.List;

public class JefeActividadesFragment extends ListRefreshFragment {

    private TabLayout tabLayout;
    private FloatingActionButton fabCrearTarea;

    private List<JefeActividadDto> actividadesGerente = new ArrayList<>();
    private List<JefeTareaDto> tareasJefe = new ArrayList<>();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_jefe_actividades, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        tabLayout = view.findViewById(R.id.tabLayout);
        fabCrearTarea = view.findViewById(R.id.fabCrearTarea);

        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                int position = tab.getPosition();
                fabCrearTarea.setVisibility(position == 1 ? View.VISIBLE : View.GONE);
                loadData();
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {}

            @Override
            public void onTabReselected(TabLayout.Tab tab) {}
        });

        fabCrearTarea.setOnClickListener(v -> {
            startActivity(new Intent(requireContext(), CrearTareaActivity.class));
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        loadData();
    }

    @Override
    protected void loadData() {
        if (tabLayout == null) return;
        int position = tabLayout.getSelectedTabPosition();
        showLoading(!swipeRefresh.isRefreshing());

        if (position == 0) {
            // Cargar Actividades del Gerente
            ApiClient.enqueue(ApiClient.get().api().listActividadesJefe(), new ApiClient.ApiCallback<List<JefeActividadDto>>() {
                @Override
                public void onSuccess(List<JefeActividadDto> data) {
                    actividadesGerente = data != null ? data : new ArrayList<>();
                    setupActividadesList();
                }

                @Override
                public void onError(String message) {
                    showError(message);
                }
            });
        } else {
            // Cargar Tareas del Jefe
            ApiClient.enqueue(ApiClient.get().api().listTareasJefe(), new ApiClient.ApiCallback<List<JefeTareaDto>>() {
                @Override
                public void onSuccess(List<JefeTareaDto> data) {
                    tareasJefe = data != null ? data : new ArrayList<>();
                    setupTareasList();
                }

                @Override
                public void onError(String message) {
                    showError(message);
                }
            });
        }
    }

    private void setupActividadesList() {
        List<CardRowAdapter.Row> rows = new ArrayList<>();
        for (JefeActividadDto a : actividadesGerente) {
            String pasTitle = a.pasantia != null ? a.pasantia.titulo : "Pasantía";
            String meta = a.estado + (a.fecha_fin != null ? " · Fin: " + a.fecha_fin : "");
            
            rows.add(new CardRowAdapter.Row(
                    a.getTitulo(),
                    pasTitle,
                    meta.toUpperCase(),
                    R.color.primary
            ));
        }

        adapter.setItems(rows);
        adapter.setOnRowClick(pos -> {
            if (pos >= 0 && pos < actividadesGerente.size()) {
                JefeActividadDto a = actividadesGerente.get(pos);
                Intent i = new Intent(requireContext(), JefeActividadDetalleActivity.class);
                i.putExtra(JefeActividadDetalleActivity.EXTRA_ACTIVIDAD_ID, a.id_actividad);
                startActivity(i);
            }
        });

        showContent(!rows.isEmpty());
    }

    private void setupTareasList() {
        List<CardRowAdapter.Row> rows = new ArrayList<>();
        for (JefeTareaDto t : tareasJefe) {
            String sub = t.inscripcion != null && t.inscripcion.getEstudianteNombre() != null 
                    ? "Asignado a: " + t.inscripcion.getEstudianteNombre() 
                    : "Asignado a todos";
            String meta = "Estado: " + t.estado_semaforo;
            
            rows.add(new CardRowAdapter.Row(
                    t.getTitulo(),
                    sub,
                    meta.toUpperCase(),
                    R.color.tertiary
            ));
        }

        adapter.setItems(rows);
        adapter.setOnRowClick(pos -> {
            if (pos >= 0 && pos < tareasJefe.size()) {
                JefeTareaDto t = tareasJefe.get(pos);
                // Las tareas abren directamente los comentarios (mismo comportamiento de comentarios de actividad)
                Intent i = new Intent(requireContext(), ActividadComentariosActivity.class);
                // Nota: Usamos EXTRA_ACTIVIDAD_ID ya que comentarios hereda ese extra
                i.putExtra(ActividadComentariosActivity.EXTRA_ACTIVIDAD_ID, t.id_tarea);
                i.putExtra(ActividadComentariosActivity.EXTRA_TITULO, t.getTitulo());
                startActivity(i);
            }
        });

        showContent(!rows.isEmpty());
    }
}
