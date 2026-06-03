package com.pasantias.movil.ui.common;

import com.pasantias.movil.R;
import com.pasantias.movil.data.api.ApiClient;
import com.pasantias.movil.data.dto.NotificacionDto;

import java.util.ArrayList;
import java.util.List;

public class NotificacionesFragment extends ListRefreshFragment {

    private List<NotificacionDto> items = new ArrayList<>();

    @Override
    protected void loadData() {
        showLoading(!swipeRefresh.isRefreshing());
        ApiClient.enqueue(ApiClient.get().api().notificacionesMias(), new ApiClient.ApiCallback<List<NotificacionDto>>() {
            @Override
            public void onSuccess(List<NotificacionDto> data) {
                items = data != null ? data : new ArrayList<>();
                List<CardRowAdapter.Row> rows = new ArrayList<>();
                for (NotificacionDto n : items) {
                    rows.add(new CardRowAdapter.Row(
                            n.titulo != null ? n.titulo : "Notificación",
                            n.mensaje,
                            n.isLeida() ? "Leída" : "Nueva",
                            n.isLeida() ? R.color.success : R.color.tertiary
                    ));
                }
                adapter.setItems(rows);
                adapter.setOnRowClick(pos -> marcarLeida(items.get(pos)));
                showContent(!rows.isEmpty());
            }

            @Override
            public void onError(String message) {
                showError(message);
            }
        });
    }

    private void marcarLeida(NotificacionDto n) {
        if (n.isLeida()) return;
        ApiClient.enqueue(ApiClient.get().api().marcarNotificacionLeida(n.id_notificacion),
                new ApiClient.ApiCallback<NotificacionDto>() {
                    @Override
                    public void onSuccess(NotificacionDto data) {
                        loadData();
                    }

                    @Override
                    public void onError(String message) {
                        // ignore
                    }
                });
    }
}
