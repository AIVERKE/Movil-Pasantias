package com.pasantias.movil.ui.jefe;

import android.content.Intent;

import com.pasantias.movil.R;
import com.pasantias.movil.data.api.ApiClient;
import com.pasantias.movil.data.dto.JefeActividadDto;
import com.pasantias.movil.ui.common.CardRowAdapter;
import com.pasantias.movil.ui.common.ListRefreshFragment;

import java.util.ArrayList;
import java.util.List;

public class JefeActividadesFragment extends ListRefreshFragment {

    private List<JefeActividadDto> actividades = new ArrayList<>();

    @Override
    protected void loadData() {
        showLoading(!swipeRefresh.isRefreshing());
        ApiClient.enqueue(ApiClient.get().api().listActividadesJefe(), new ApiClient.ApiCallback<List<JefeActividadDto>>() {
            @Override
            public void onSuccess(List<JefeActividadDto> data) {
                actividades = data != null ? data : new ArrayList<>();
                List<CardRowAdapter.Row> rows = new ArrayList<>();
                for (JefeActividadDto a : actividades) {
                    String pasantia = a.pasantia != null ? a.pasantia.titulo : "";
                    rows.add(new CardRowAdapter.Row(
                            a.getTitulo(),
                            pasantia,
                            a.estado + (a.fecha_fin != null ? " · " + a.fecha_fin : ""),
                            R.color.primary
                    ));
                }
                adapter.setItems(rows);
                adapter.setOnRowClick(pos -> {
                    JefeActividadDto a = actividades.get(pos);
                    Intent i = new Intent(requireContext(), ActividadComentariosActivity.class);
                    i.putExtra(ActividadComentariosActivity.EXTRA_ACTIVIDAD_ID, a.id_actividad);
                    i.putExtra(ActividadComentariosActivity.EXTRA_TITULO, a.getTitulo());
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
}
