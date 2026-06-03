package com.pasantias.movil.ui.estudiante;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.pasantias.movil.R;
import com.pasantias.movil.data.api.ApiClient;
import com.pasantias.movil.data.dto.EstudianteDashboardDto;
import com.pasantias.movil.data.local.TokenManager;

public class EstudianteDashboardFragment extends Fragment {

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_estudiante_dashboard, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        SwipeRefreshLayout swipe = view.findViewById(R.id.swipeRefresh);
        ProgressBar progress = view.findViewById(R.id.progress);
        TextView textError = view.findViewById(R.id.textError);
        TextView textProxima = view.findViewById(R.id.textProxima);
        bindStat(view.findViewById(R.id.stat1), "Tareas completadas", "0/0", R.color.tertiary);
        bindStat(view.findViewById(R.id.stat2), "Evaluación promedio", "—", R.color.primary);
        bindStat(view.findViewById(R.id.stat3), "Estado pasantía", "—", R.color.success);
        int userId = TokenManager.get().getUser().id;
        swipe.setOnRefreshListener(() -> load(view, swipe, progress, textError, textProxima, userId));
        load(view, swipe, progress, textError, textProxima, userId);
    }

    private void load(View root, SwipeRefreshLayout swipe, ProgressBar progress, TextView textError, TextView textProxima, int userId) {
        progress.setVisibility(View.VISIBLE);
        ApiClient.enqueue(ApiClient.get().api().dashboardEstudiante(userId), new ApiClient.ApiCallback<EstudianteDashboardDto>() {
            @Override
            public void onSuccess(EstudianteDashboardDto d) {
                swipe.setRefreshing(false);
                progress.setVisibility(View.GONE);
                String tareas = "0/0";
                if (d.tareas_completadas != null) {
                    tareas = d.tareas_completadas.completadas + "/" + d.tareas_completadas.totales;
                }
                bindStat(root.findViewById(R.id.stat1), "Tareas completadas", tareas, R.color.tertiary);
                bindStat(root.findViewById(R.id.stat2), "Evaluación promedio",
                        d.evaluacion_promedio != null ? String.valueOf(d.evaluacion_promedio) : "—",
                        R.color.primary);
                bindStat(root.findViewById(R.id.stat3), "Estado pasantía",
                        d.estado_pasantia != null ? d.estado_pasantia : "Sin pasantía",
                        R.color.success);
                String prox = "";
                if (d.pasantia_activa && d.titulo_pasantia != null) {
                    prox = d.titulo_pasantia + (d.empresa_nombre != null ? " · " + d.empresa_nombre : "");
                } else if (d.tiene_postulaciones_pendientes) {
                    prox = "Tenés postulaciones pendientes de revisión.";
                } else {
                    prox = "Explorá el catálogo para postularte a una pasantía.";
                }
                textProxima.setText(prox);
            }

            @Override
            public void onError(String message) {
                swipe.setRefreshing(false);
                progress.setVisibility(View.GONE);
                textError.setText(message);
                textError.setVisibility(View.VISIBLE);
            }
        });
    }

    private void bindStat(View root, String title, String value, int color) {
        ((TextView) root.findViewById(R.id.statTitle)).setText(title);
        ((TextView) root.findViewById(R.id.statValue)).setText(value);
        root.findViewById(R.id.statAccent).setBackgroundColor(requireContext().getColor(color));
    }
}
