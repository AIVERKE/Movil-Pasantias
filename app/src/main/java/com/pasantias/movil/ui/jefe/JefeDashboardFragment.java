package com.pasantias.movil.ui.jefe;

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
import com.pasantias.movil.data.dto.JefeDashboardDto;

public class JefeDashboardFragment extends Fragment {

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_jefe_dashboard, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        SwipeRefreshLayout swipe = view.findViewById(R.id.swipeRefresh);
        ProgressBar progress = view.findViewById(R.id.progress);
        TextView textError = view.findViewById(R.id.textError);
        bindStat(view.findViewById(R.id.stat1), "Pasantes activos", "0", R.color.success);
        bindStat(view.findViewById(R.id.stat2), "Inscripciones pendientes", "0", R.color.tertiary);
        bindStat(view.findViewById(R.id.stat3), "Informes por emitir", "0", R.color.primary);
        bindStat(view.findViewById(R.id.stat4), "Alertas", "0", R.color.secondary);
        swipe.setOnRefreshListener(() -> load(view, swipe, progress, textError));
        load(view, swipe, progress, textError);
    }

    private void load(View root, SwipeRefreshLayout swipe, ProgressBar progress, TextView textError) {
        progress.setVisibility(View.VISIBLE);
        ApiClient.enqueue(ApiClient.get().api().jefeDashboard(), new ApiClient.ApiCallback<JefeDashboardDto>() {
            @Override
            public void onSuccess(JefeDashboardDto d) {
                swipe.setRefreshing(false);
                progress.setVisibility(View.GONE);
                bindStat(root.findViewById(R.id.stat1), "Pasantes activos", String.valueOf(d.pasantesActivos), R.color.success);
                bindStat(root.findViewById(R.id.stat2), "Inscripciones pendientes", String.valueOf(d.inscripcionesPendientes), R.color.tertiary);
                bindStat(root.findViewById(R.id.stat3), "Informes por emitir", String.valueOf(d.informesPorEmitir), R.color.primary);
                int alertas = d.inscripcionesPendientes + d.informesPorEmitir;
                bindStat(root.findViewById(R.id.stat4), "Alertas", String.valueOf(alertas), R.color.secondary);
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
