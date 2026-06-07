package com.pasantias.movil.ui.gerente;

import android.content.res.ColorStateList;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.pasantias.movil.R;
import com.pasantias.movil.data.dto.ActividadDto;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class ActividadesAdapter extends RecyclerView.Adapter<ActividadesAdapter.ViewHolder> {

    public interface Callbacks {
        void onEdit(ActividadDto act);
        void onDelete(int id);
    }

    private List<ActividadDto> items = new ArrayList<>();
    private Callbacks callbacks;

    public void setCallbacks(Callbacks callbacks) {
        this.callbacks = callbacks;
    }

    public void setItems(List<ActividadDto> list) {
        this.items = list != null ? new ArrayList<>(list) : new ArrayList<>();
        notifyDataSetChanged();
    }

    public void addItem(ActividadDto item) {
        if (item == null) return;
        items.add(0, item);
        notifyItemInserted(0);
    }

    public void updateItem(ActividadDto item) {
        if (item == null) return;
        for (int i = 0; i < items.size(); i++) {
            if (items.get(i).id_actividad == item.id_actividad) {
                items.set(i, item);
                notifyItemChanged(i);
                return;
            }
        }
        // Si no lo encontró (caso raro), agregarlo
        addItem(item);
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_actividad_row, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.bind(items.get(position), callbacks);
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {

        final View layoutEstadoCircle;
        final ImageView imgEstadoIcon;
        final TextView tvActDescripcion, tvActRangoFechas, tvActEstadoLabel;
        final ImageButton btnEditarActividad, btnEliminarActividad;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            layoutEstadoCircle  = itemView.findViewById(R.id.layoutEstadoCircle);
            imgEstadoIcon       = itemView.findViewById(R.id.imgEstadoIcon);
            tvActDescripcion    = itemView.findViewById(R.id.tvActDescripcion);
            tvActRangoFechas    = itemView.findViewById(R.id.tvActRangoFechas);
            tvActEstadoLabel    = itemView.findViewById(R.id.tvActEstadoLabel);
            btnEditarActividad  = itemView.findViewById(R.id.btnEditarActividad);
            btnEliminarActividad = itemView.findViewById(R.id.btnEliminarActividad);
        }

        void bind(ActividadDto act, Callbacks cb) {
            tvActDescripcion.setText(act.descripcion != null ? act.descripcion : "");

            String fInicio = formatFecha(act.fecha_inicio);
            String fFin = formatFecha(act.fecha_limite != null ? act.fecha_limite : act.fecha_inicio);
            tvActRangoFechas.setText(fInicio + " – " + fFin);

            String estado = act.estado != null ? act.estado : "pendiente";
            tvActEstadoLabel.setText(estado.replace("_", " ").toUpperCase());

            // Colors styling
            int circleBg, iconColor, labelBg, labelText, iconRes;
            switch (estado) {
                case "en_desarrollo":
                    circleBg = Color.parseColor("#FEF3C7");
                    iconColor = Color.parseColor("#D97706");
                    labelBg = Color.parseColor("#FEF3C7");
                    labelText = Color.parseColor("#92400E");
                    iconRes = R.drawable.ic_clock_outline;
                    break;
                case "finalizada":
                    circleBg = Color.parseColor("#F3E8FF");
                    iconColor = Color.parseColor("#7C3AED");
                    labelBg = Color.parseColor("#F3E8FF");
                    labelText = Color.parseColor("#5B21B6");
                    iconRes = R.drawable.ic_check_circle;
                    break;
                case "cerrada":
                    circleBg = Color.parseColor("#D1FAE5");
                    iconColor = Color.parseColor("#059669");
                    labelBg = Color.parseColor("#D1FAE5");
                    labelText = Color.parseColor("#065F46");
                    iconRes = R.drawable.ic_check_circle;
                    break;
                default: // pendiente
                    circleBg = Color.parseColor("#EBF5FF");
                    iconColor = Color.parseColor("#3B82F6");
                    labelBg = Color.parseColor("#DBEAFE");
                    labelText = Color.parseColor("#1E40AF");
                    iconRes = R.drawable.ic_calendar;
                    break;
            }

            layoutEstadoCircle.setBackgroundTintList(ColorStateList.valueOf(circleBg));
            imgEstadoIcon.setImageResource(iconRes);
            imgEstadoIcon.setImageTintList(ColorStateList.valueOf(iconColor));
            tvActEstadoLabel.setBackgroundTintList(ColorStateList.valueOf(labelBg));
            tvActEstadoLabel.setTextColor(labelText);

            if (cb != null) {
                btnEditarActividad.setOnClickListener(v -> cb.onEdit(act));
                btnEliminarActividad.setOnClickListener(v -> cb.onDelete(act.id_actividad));
            }
        }

        private String formatFecha(String rawDate) {
            if (rawDate == null || rawDate.isEmpty()) return "";
            try {
                if (rawDate.length() >= 10) {
                    String clean = rawDate.substring(0, 10);
                    SimpleDateFormat from = new SimpleDateFormat("yyyy-MM-dd", Locale.ROOT);
                    SimpleDateFormat to = new SimpleDateFormat("dd MMM", new Locale("es", "ES"));
                    Date d = from.parse(clean);
                    if (d != null) return to.format(d);
                }
            } catch (Exception e) {
                // fall back
            }
            return rawDate;
        }
    }
}
