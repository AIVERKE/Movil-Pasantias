package com.pasantias.movil.ui.jefe;

import com.pasantias.movil.R;
import com.pasantias.movil.data.api.ApiClient;
import com.pasantias.movil.data.dto.InscripcionDto;
import com.pasantias.movil.ui.common.CardRowAdapter;
import com.pasantias.movil.ui.common.ListRefreshFragment;

import java.util.ArrayList;
import java.util.List;

public class JefePasantesFragment extends ListRefreshFragment {

    @Override
    protected void loadData() {
        showLoading(!swipeRefresh.isRefreshing());
        ApiClient.enqueue(ApiClient.get().api().listPasantesJefe(), new ApiClient.ApiCallback<List<InscripcionDto>>() {
            @Override
            public void onSuccess(List<InscripcionDto> data) {
                List<CardRowAdapter.Row> rows = new ArrayList<>();
                if (data != null) {
                    for (InscripcionDto i : data) {
                        rows.add(new CardRowAdapter.Row(
                                i.getEstudianteNombre(),
                                i.getPasantiaTitulo(),
                                String.valueOf(i.estado),
                                R.color.success
                        ));
                    }
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
