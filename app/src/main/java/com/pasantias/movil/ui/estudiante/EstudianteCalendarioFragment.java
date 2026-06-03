package com.pasantias.movil.ui.estudiante;

import com.pasantias.movil.R;
import com.pasantias.movil.data.api.ApiClient;
import com.pasantias.movil.data.dto.EstudianteActividadItemDto;
import com.pasantias.movil.data.dto.EstudianteActividadesResponse;
import com.pasantias.movil.data.local.TokenManager;
import com.pasantias.movil.ui.common.CardRowAdapter;
import com.pasantias.movil.ui.common.ListRefreshFragment;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class EstudianteCalendarioFragment extends ListRefreshFragment {

    @Override
    protected void loadData() {
        int id = TokenManager.get().getUser().id;
        showLoading(!swipeRefresh.isRefreshing());
        ApiClient.enqueue(ApiClient.get().api().actividadesEstudiante(id), new ApiClient.ApiCallback<EstudianteActividadesResponse>() {
            @Override
            public void onSuccess(EstudianteActividadesResponse data) {
                List<EstudianteActividadItemDto> sorted = data.actividades != null ? new ArrayList<>(data.actividades) : new ArrayList<>();
                Collections.sort(sorted, Comparator.comparing(a -> a.getFechaLimite() != null ? a.getFechaLimite() : ""));
                List<CardRowAdapter.Row> rows = new ArrayList<>();
                for (EstudianteActividadItemDto a : sorted) {
                    rows.add(new CardRowAdapter.Row(
                            a.getFechaLimite() != null ? a.getFechaLimite() : "Sin fecha",
                            a.getTitulo(),
                            a.getEstado(),
                            R.color.tertiary
                    ));
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
}
