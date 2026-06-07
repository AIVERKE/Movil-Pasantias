package com.pasantias.movil.ui.gerente;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.pasantias.movil.R;
import com.pasantias.movil.data.api.ApiClient;
import com.pasantias.movil.data.dto.MessageResponse;
import com.pasantias.movil.data.dto.PasantiaDto;
import com.pasantias.movil.ui.common.ListRefreshFragment;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GerentePasantiasFragment extends ListRefreshFragment {

    private List<PasantiaDto> pasantias = new ArrayList<>();
    private PasantiaCardAdapter cardAdapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_pasantias_list, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        swipeRefresh = view.findViewById(R.id.swipeRefresh);
        recycler     = view.findViewById(R.id.recycler);
        progress     = view.findViewById(R.id.progress);
        textError    = view.findViewById(R.id.textError);
        textEmpty    = view.findViewById(R.id.textEmpty);

        // Usar el nuevo PasantiaCardAdapter
        cardAdapter = new PasantiaCardAdapter();
        cardAdapter.setCallbacks(new PasantiaCardAdapter.Callbacks() {
            @Override
            public void onEdit(int id) {
                Intent i = new Intent(requireContext(), PasantiaFormActivity.class);
                i.putExtra(PasantiaFormActivity.EXTRA_ID, id);
                startActivity(i);
            }

            @Override
            public void onDelete(int id) {
                ApiClient.enqueue(
                        ApiClient.get().api().deletePasantia(id),
                        new ApiClient.ApiCallback<MessageResponse>() {
                            @Override
                            public void onSuccess(MessageResponse data) {
                                loadData();
                            }

                            @Override
                            public void onError(String message) {
                                showError(message);
                            }
                        });
            }

            @Override
            public void onChangeState(int id, String nuevoEstado) {
                Map<String, String> body = new HashMap<>();
                body.put("estado", nuevoEstado);
                ApiClient.enqueue(
                        ApiClient.get().api().updateEstadoPasantia(id, body),
                        new ApiClient.ApiCallback<PasantiaDto>() {
                            @Override
                            public void onSuccess(PasantiaDto data) {
                                loadData();
                            }

                            @Override
                            public void onError(String message) {
                                showError(message);
                            }
                        });
            }

            @Override
            public void onAssignMentor(PasantiaDto p) {
                showLoading(true);
                ApiClient.enqueue(ApiClient.get().api().getEmpresaGerente(), new ApiClient.ApiCallback<com.pasantias.movil.data.dto.EmpresaDto>() {
                    @Override
                    public void onSuccess(com.pasantias.movil.data.dto.EmpresaDto empresa) {
                        if (empresa != null) {
                            ApiClient.enqueue(ApiClient.get().api().getJefesByEmpresa(empresa.id_empresa), new ApiClient.ApiCallback<List<com.pasantias.movil.data.dto.JefeDto>>() {
                                @Override
                                public void onSuccess(List<com.pasantias.movil.data.dto.JefeDto> jefes) {
                                    showContent(!pasantias.isEmpty());
                                    if (jefes == null || jefes.isEmpty()) {
                                        Toast.makeText(requireContext(), "No hay jefes registrados en tu empresa.", Toast.LENGTH_SHORT).show();
                                        return;
                                    }

                                    String[] nombres = new String[jefes.size()];
                                    for (int i = 0; i < jefes.size(); i++) {
                                        com.pasantias.movil.data.dto.JefeDto jefe = jefes.get(i);
                                        nombres[i] = (jefe.usuario != null) ? jefe.usuario.getNombreCompleto() : "Mentor sin nombre";
                                    }

                                    new AlertDialog.Builder(requireContext())
                                            .setTitle("Designar Mentor / Jefe")
                                            .setItems(nombres, (dialog, which) -> {
                                                com.pasantias.movil.data.dto.JefeDto selectedJefe = jefes.get(which);
                                                if (selectedJefe.usuario != null) {
                                                    Map<String, Object> body = new HashMap<>();
                                                    body.put("jefe_user_id", selectedJefe.id_jefe);
                                                    showLoading(true);
                                                    ApiClient.enqueue(ApiClient.get().api().assignJefe(p.id_pasantia, body), new ApiClient.ApiCallback<PasantiaDto>() {
                                                        @Override
                                                        public void onSuccess(PasantiaDto result) {
                                                            loadData();
                                                        }
                                                        @Override
                                                        public void onError(String msg) {
                                                            showError(msg);
                                                        }
                                                    });
                                                }
                                            })
                                            .setNegativeButton("Cancelar", null)
                                            .show();
                                }

                                @Override
                                public void onError(String message) {
                                    showContent(!pasantias.isEmpty());
                                    Toast.makeText(requireContext(), "Error al cargar jefes: " + message, Toast.LENGTH_SHORT).show();
                                }
                            });
                        } else {
                            showContent(!pasantias.isEmpty());
                            Toast.makeText(requireContext(), "No tenés una empresa asociada.", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onError(String message) {
                        showContent(!pasantias.isEmpty());
                        Toast.makeText(requireContext(), "Error al obtener empresa: " + message, Toast.LENGTH_SHORT).show();
                    }
                });
            }

            @Override
            public void onRemoveMentor(int pasantiaId, int jefeId) {
                new AlertDialog.Builder(requireContext())
                        .setTitle("¿Quitar jefe?")
                        .setMessage("Se removerá este jefe de la convocatoria")
                        .setPositiveButton("Sí, remover", (dialog, which) -> {
                            showLoading(true);
                            ApiClient.enqueue(ApiClient.get().api().removeJefe(pasantiaId, jefeId), new ApiClient.ApiCallback<MessageResponse>() {
                                @Override
                                public void onSuccess(MessageResponse data) {
                                    loadData();
                                }
                                @Override
                                public void onError(String message) {
                                    showContent(!pasantias.isEmpty());
                                    Toast.makeText(requireContext(), "Error al remover jefe: " + message, Toast.LENGTH_SHORT).show();
                                }
                            });
                        })
                        .setNegativeButton("Cancelar", null)
                        .show();
            }

            @Override
            public void onManageActivities(PasantiaDto p) {
                Intent intent = new Intent(requireContext(), PasantiaActividadesActivity.class);
                intent.putExtra(PasantiaActividadesActivity.EXTRA_PASANTIA_ID, p.id_pasantia);
                intent.putExtra(PasantiaActividadesActivity.EXTRA_PASANTIA_TITULO, p.titulo);
                intent.putExtra(PasantiaActividadesActivity.EXTRA_PASANTIA_FECHA_INICIO, p.fecha_inicio);
                intent.putExtra(PasantiaActividadesActivity.EXTRA_PASANTIA_FECHA_FIN, p.fecha_fin);
                startActivity(intent);
            }
        });

        recycler.setLayoutManager(new androidx.recyclerview.widget.LinearLayoutManager(requireContext()));
        recycler.setAdapter(cardAdapter);
        // No asignamos adapter (base) porque es CardRowAdapter; usamos showContent/showLoading directamente

        swipeRefresh.setColorSchemeResources(R.color.primary);
        swipeRefresh.setOnRefreshListener(this::loadData);

        FloatingActionButton fab = view.findViewById(R.id.fabAdd);
        fab.setOnClickListener(v ->
                startActivity(new Intent(requireContext(), PasantiaFormActivity.class)));

        loadData();
    }

    @Override
    protected void loadData() {
        showLoading(!swipeRefresh.isRefreshing());
        ApiClient.enqueue(
                ApiClient.get().api().listPasantiasGerente(),
                new ApiClient.ApiCallback<List<PasantiaDto>>() {
                    @Override
                    public void onSuccess(List<PasantiaDto> data) {
                        pasantias = data != null ? data : new ArrayList<>();
                        cardAdapter.setItems(pasantias);
                        showContent(!pasantias.isEmpty());
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
