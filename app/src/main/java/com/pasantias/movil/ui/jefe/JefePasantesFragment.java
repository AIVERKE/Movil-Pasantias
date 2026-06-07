package com.pasantias.movil.ui.jefe;

import android.content.Intent;
import com.pasantias.movil.R;
import com.pasantias.movil.data.api.ApiClient;
import com.pasantias.movil.data.dto.InscripcionDto;
import com.pasantias.movil.ui.common.CardRowAdapter;
import com.pasantias.movil.ui.common.ListRefreshFragment;

import java.util.ArrayList;
import java.util.List;

public class JefePasantesFragment extends ListRefreshFragment {

    private List<InscripcionDto> pasantes = new ArrayList<>();

    @Override
    public void onResume() {
        super.onResume();
        loadData();
    }

    @Override
    protected void loadData() {
        showLoading(!swipeRefresh.isRefreshing());
        ApiClient.enqueue(ApiClient.get().api().listPasantesJefe(), new ApiClient.ApiCallback<List<InscripcionDto>>() {
            @Override
            public void onSuccess(List<InscripcionDto> data) {
                pasantes = data != null ? data : new ArrayList<>();
                List<CardRowAdapter.Row> rows = new ArrayList<>();
                for (InscripcionDto i : pasantes) {
                    rows.add(new CardRowAdapter.Row(
                            i.getEstudianteNombre(),
                            i.getPasantiaTitulo(),
                            String.valueOf(i.estado).toUpperCase(),
                            R.color.success
                    ));
                }
                adapter.setItems(rows);
                adapter.setOnRowClick(pos -> {
                    if (pos >= 0 && pos < pasantes.size()) {
                        InscripcionDto selected = pasantes.get(pos);
                        Intent intent = new Intent(requireContext(), PasanteDetalleJefeActivity.class);
                        intent.putExtra(PasanteDetalleJefeActivity.EXTRA_INSCRIPCION_ID, selected.getId());
                        startActivity(intent);
                    }
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
