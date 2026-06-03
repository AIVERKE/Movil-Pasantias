package com.pasantias.movil.ui.jefe;

import com.pasantias.movil.R;
import com.pasantias.movil.data.api.ApiClient;
import com.pasantias.movil.data.dto.InscripcionDto;
import com.pasantias.movil.ui.common.CardRowAdapter;
import com.pasantias.movil.ui.common.ListRefreshFragment;
import com.pasantias.movil.util.UiUtils;

import java.util.ArrayList;
import java.util.List;

public class JefeInscripcionesFragment extends ListRefreshFragment {

    private List<InscripcionDto> inscripciones = new ArrayList<>();

    @Override
    protected void loadData() {
        showLoading(!swipeRefresh.isRefreshing());
        ApiClient.enqueue(ApiClient.get().api().listInscripcionesJefe(), new ApiClient.ApiCallback<List<InscripcionDto>>() {
            @Override
            public void onSuccess(List<InscripcionDto> data) {
                inscripciones = data != null ? data : new ArrayList<>();
                List<CardRowAdapter.Row> rows = new ArrayList<>();
                for (InscripcionDto i : inscripciones) {
                    rows.add(new CardRowAdapter.Row(
                            i.getEstudianteNombre() != null ? i.getEstudianteNombre() : "Estudiante",
                            i.getPasantiaTitulo(),
                            i.estado + " (tocar para aprobar)",
                            R.color.tertiary
                    ));
                }
                adapter.setItems(rows);
                adapter.setOnRowClick(pos -> aprobar(inscripciones.get(pos)));
                showContent(!rows.isEmpty());
            }

            @Override
            public void onError(String message) {
                showError(message);
            }
        });
    }

    private void aprobar(InscripcionDto i) {
        ApiClient.enqueue(ApiClient.get().api().aprobarInscripcionJefe(i.getId()),
                new ApiClient.ApiCallback<InscripcionDto>() {
                    @Override
                    public void onSuccess(InscripcionDto data) {
                        UiUtils.toast(requireActivity(), "Inscripción actualizada");
                        loadData();
                    }

                    @Override
                    public void onError(String message) {
                        UiUtils.toast(requireActivity(), message);
                    }
                });
    }
}
