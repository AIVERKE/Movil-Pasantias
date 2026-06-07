package com.pasantias.movil.ui.gerente;

import android.content.res.ColorStateList;
import android.graphics.drawable.GradientDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.pasantias.movil.R;
import com.pasantias.movil.data.dto.PasantiaDto;

import java.util.ArrayList;
import java.util.List;

public class PasantiaCardAdapter extends RecyclerView.Adapter<PasantiaCardAdapter.ViewHolder> {

    public interface Callbacks {
        void onEdit(int id);
        void onDelete(int id);
        void onChangeState(int id, String nuevoEstado);
        void onAssignMentor(PasantiaDto p);
        void onRemoveMentor(int pasantiaId, int jefeId);
        void onManageActivities(PasantiaDto p);
    }

    private List<PasantiaDto> items = new ArrayList<>();
    private Callbacks callbacks;

    public void setCallbacks(Callbacks callbacks) {
        this.callbacks = callbacks;
    }

    public void setItems(List<PasantiaDto> list) {
        this.items = list != null ? list : new ArrayList<>();
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_pasantia_card, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder h, int position) {
        PasantiaDto p = items.get(position);
        h.bind(p, callbacks);
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {

        final View viewEstadoBar;
        final TextView tvTitulo, tvArea, tvDescripcion, tvEstadoBadge;
        final ImageButton btnEditar, btnPublicar, btnPausar, btnFinalizar;
        final ImageButton btnArchivar, btnReabrir, btnRestaurar, btnEliminar;

        final View layoutMentoresContainer;
        final LinearLayout layoutMentoresList;
        final TextView btnAsignarMentor;

        final View btnActividades;
        final TextView tvActividadesCount;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            viewEstadoBar  = itemView.findViewById(R.id.viewEstadoBar);
            tvTitulo       = itemView.findViewById(R.id.tvTitulo);
            tvArea         = itemView.findViewById(R.id.tvArea);
            tvDescripcion  = itemView.findViewById(R.id.tvDescripcion);
            tvEstadoBadge  = itemView.findViewById(R.id.tvEstadoBadge);
            btnEditar      = itemView.findViewById(R.id.btnEditar);
            btnPublicar    = itemView.findViewById(R.id.btnPublicar);
            btnPausar      = itemView.findViewById(R.id.btnPausar);
            btnFinalizar   = itemView.findViewById(R.id.btnFinalizar);
            btnArchivar    = itemView.findViewById(R.id.btnArchivar);
            btnReabrir     = itemView.findViewById(R.id.btnReabrir);
            btnRestaurar   = itemView.findViewById(R.id.btnRestaurar);
            btnEliminar    = itemView.findViewById(R.id.btnEliminar);

            layoutMentoresContainer = itemView.findViewById(R.id.layoutMentoresContainer);
            layoutMentoresList      = itemView.findViewById(R.id.layoutMentoresList);
            btnAsignarMentor        = itemView.findViewById(R.id.btnAsignarMentor);

            btnActividades          = itemView.findViewById(R.id.btnActividades);
            tvActividadesCount      = itemView.findViewById(R.id.tvActividadesCount);
        }

        void bind(PasantiaDto p, Callbacks cb) {
            tvTitulo.setText(p.titulo != null ? p.titulo : "");
            tvArea.setText(p.area != null ? p.area.toUpperCase() : "");
            tvDescripcion.setText(p.descripcion != null ? p.descripcion : "");

            // Actividades
            int actCount = (p.actividades != null) ? p.actividades.size() : 0;
            tvActividadesCount.setText(String.valueOf(actCount));
            if (cb != null) {
                btnActividades.setOnClickListener(v -> cb.onManageActivities(p));
            }

            // Mentores / Jefes
            layoutMentoresList.removeAllViews();
            if (p.jefe_pasantes != null && !p.jefe_pasantes.isEmpty()) {
                for (com.pasantias.movil.data.dto.JefeDto jefe : p.jefe_pasantes) {
                    View itemJefe = LayoutInflater.from(itemView.getContext())
                            .inflate(R.layout.item_mentor_badge, layoutMentoresList, false);
                    TextView tvNombre = itemJefe.findViewById(R.id.tvMentorNombre);
                    View btnQuitar = itemJefe.findViewById(R.id.btnQuitarMentor);

                    String nombre = (jefe.usuario != null) ? jefe.usuario.getNombreCompleto() : "Mentor";
                    tvNombre.setText(nombre);

                    if (cb != null) {
                        btnQuitar.setOnClickListener(v -> cb.onRemoveMentor(p.id_pasantia, jefe.id_jefe));
                    }
                    layoutMentoresList.addView(itemJefe);
                }
            } else {
                View emptyView = LayoutInflater.from(itemView.getContext())
                        .inflate(R.layout.item_mentor_empty, layoutMentoresList, false);
                layoutMentoresList.addView(emptyView);
            }

            if (cb != null) {
                btnAsignarMentor.setOnClickListener(v -> cb.onAssignMentor(p));
            }

            String estado = p.estado != null ? p.estado : "pendiente";

            // Color del estado
            int color = resolveEstadoColor(estado);
            colorBar(viewEstadoBar, color);
            colorBadge(tvEstadoBadge, color);
            tvEstadoBadge.setText(resolveEstadoLabel(estado));

            // Ocultar todos los botones primero
            hideAll();

            // Mostrar según estado
            switch (estado) {
                case "pendiente":
                    show(btnEditar);
                    show(btnPublicar);
                    show(btnEliminar);
                    if (cb != null) {
                        btnEditar.setOnClickListener(v -> cb.onEdit(p.id_pasantia));
                        btnPublicar.setOnClickListener(v -> cb.onChangeState(p.id_pasantia, "en_curso"));
                        btnEliminar.setOnClickListener(v -> cb.onDelete(p.id_pasantia));
                    }
                    break;

                case "en_curso":
                    show(btnEditar);
                    show(btnPausar);
                    show(btnFinalizar);
                    show(btnArchivar);
                    if (cb != null) {
                        btnEditar.setOnClickListener(v -> cb.onEdit(p.id_pasantia));
                        btnPausar.setOnClickListener(v -> cb.onChangeState(p.id_pasantia, "pendiente"));
                        btnFinalizar.setOnClickListener(v -> cb.onChangeState(p.id_pasantia, "finalizada"));
                        btnArchivar.setOnClickListener(v -> cb.onChangeState(p.id_pasantia, "cancelada"));
                    }
                    break;

                case "finalizada":
                    show(btnReabrir);
                    show(btnArchivar);
                    if (cb != null) {
                        btnReabrir.setOnClickListener(v -> cb.onChangeState(p.id_pasantia, "en_curso"));
                        btnArchivar.setOnClickListener(v -> cb.onChangeState(p.id_pasantia, "cancelada"));
                    }
                    break;

                case "cancelada":
                    show(btnRestaurar);
                    if (cb != null) {
                        btnRestaurar.setOnClickListener(v -> cb.onChangeState(p.id_pasantia, "en_curso"));
                    }
                    break;
            }
        }

        private void hideAll() {
            btnEditar.setVisibility(View.GONE);
            btnPublicar.setVisibility(View.GONE);
            btnPausar.setVisibility(View.GONE);
            btnFinalizar.setVisibility(View.GONE);
            btnArchivar.setVisibility(View.GONE);
            btnReabrir.setVisibility(View.GONE);
            btnRestaurar.setVisibility(View.GONE);
            btnEliminar.setVisibility(View.GONE);
        }

        private void show(View v) {
            v.setVisibility(View.VISIBLE);
        }

        private void colorBar(View bar, int color) {
            bar.setBackgroundColor(color);
        }

        private void colorBadge(TextView badge, int color) {
            if (badge.getBackground() instanceof GradientDrawable) {
                ((GradientDrawable) badge.getBackground()).setColor(color);
            } else {
                badge.setBackgroundTintList(ColorStateList.valueOf(color));
            }
        }

        private int resolveEstadoColor(String estado) {
            android.content.Context ctx = itemView.getContext();
            switch (estado) {
                case "en_curso":   return ctx.getResources().getColor(R.color.estado_en_curso, ctx.getTheme());
                case "finalizada": return ctx.getResources().getColor(R.color.estado_finalizada, ctx.getTheme());
                case "cancelada":  return ctx.getResources().getColor(R.color.estado_cancelada, ctx.getTheme());
                default:           return ctx.getResources().getColor(R.color.estado_pendiente, ctx.getTheme());
            }
        }

        private String resolveEstadoLabel(String estado) {
            android.content.Context ctx = itemView.getContext();
            switch (estado) {
                case "en_curso":   return ctx.getString(R.string.estado_en_curso);
                case "finalizada": return ctx.getString(R.string.estado_finalizada);
                case "cancelada":  return ctx.getString(R.string.estado_archivada);
                default:           return ctx.getString(R.string.estado_pendiente);
            }
        }
    }
}
