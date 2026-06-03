package com.pasantias.movil.ui.estudiante;

import android.content.Intent;

import com.pasantias.movil.R;
import com.pasantias.movil.data.api.ApiClient;
import com.pasantias.movil.data.dto.PasantiaDto;
import com.pasantias.movil.ui.common.CardRowAdapter;
import com.pasantias.movil.ui.common.ListRefreshFragment;

import java.util.ArrayList;
import java.util.List;

public class EstudianteCatalogoFragment extends ListRefreshFragment {

    private List<PasantiaDto> pasantias = new ArrayList<>();

    @Override
    protected void loadData() {
        showLoading(!swipeRefresh.isRefreshing());
        ApiClient.enqueue(ApiClient.get().api().listPasantias(null), new ApiClient.ApiCallback<List<PasantiaDto>>() {
            @Override
            public void onSuccess(List<PasantiaDto> data) {
                pasantias = data != null ? data : new ArrayList<>();
                List<CardRowAdapter.Row> rows = new ArrayList<>();
                for (PasantiaDto p : pasantias) {
                    rows.add(new CardRowAdapter.Row(
                            p.titulo,
                            p.empresa_nombre != null ? p.empresa_nombre : "",
                            p.modalidad != null ? p.modalidad : p.estado,
                            R.color.primary
                    ));
                }
                adapter.setItems(rows);
                adapter.setOnRowClick(pos -> {
                    Intent i = new Intent(requireContext(), PasantiaDetalleActivity.class);
                    i.putExtra(PasantiaDetalleActivity.EXTRA_ID, pasantias.get(pos).id_pasantia);
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
