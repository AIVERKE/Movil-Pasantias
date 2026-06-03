package com.pasantias.movil.ui.estudiante;

import android.content.Intent;

import com.pasantias.movil.R;
import com.pasantias.movil.data.api.ApiClient;
import com.pasantias.movil.data.dto.EstudianteActividadItemDto;
import com.pasantias.movil.data.dto.EstudianteActividadesResponse;
import com.pasantias.movil.data.local.TokenManager;
import com.pasantias.movil.ui.common.CardRowAdapter;
import com.pasantias.movil.ui.common.ListRefreshFragment;

import java.util.ArrayList;
import java.util.List;

public class EstudianteActividadesFragment extends ListRefreshFragment {

    private List<EstudianteActividadItemDto> actividades = new ArrayList<>();

    @Override
    protected void loadData() {
        int id = TokenManager.get().getUser().id;
        showLoading(!swipeRefresh.isRefreshing());
        ApiClient.enqueue(ApiClient.get().api().actividadesEstudiante(id), new ApiClient.ApiCallback<EstudianteActividadesResponse>() {
            @Override
            public void onSuccess(EstudianteActividadesResponse data) {
                actividades = data.actividades != null ? data.actividades : new ArrayList<>();
                List<CardRowAdapter.Row> rows = new ArrayList<>();
                for (EstudianteActividadItemDto a : actividades) {
                    if (a.id_actividad == null) continue;
                    String pct = a.porcentaje_actual != null ? a.porcentaje_actual + "%" : "";
                    rows.add(new CardRowAdapter.Row(
                            a.getTitulo(),
                            a.descripcion_actividad != null ? a.descripcion_actividad : "",
                            a.getEstado() + (pct.isEmpty() ? "" : " · " + pct),
                            R.color.primary
                    ));
                }
                adapter.setItems(rows);
                adapter.setOnRowClick(pos -> {
                    EstudianteActividadItemDto a = actividades.get(pos);
                    if (a.id_actividad == null) return;
                    Intent i = new Intent(requireContext(), ActividadDetalleActivity.class);
                    i.putExtra(ActividadDetalleActivity.EXTRA_ID, a.id_actividad);
                    i.putExtra(ActividadDetalleActivity.EXTRA_TITULO, a.getTitulo());
                    i.putExtra(ActividadDetalleActivity.EXTRA_ESTADO, a.getEstado());
                    i.putExtra(ActividadDetalleActivity.EXTRA_DESCRIPCION, a.descripcion_actividad);
                    i.putExtra(ActividadDetalleActivity.EXTRA_FECHA, a.getFechaLimite());
                    startActivity(i);
                });
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
