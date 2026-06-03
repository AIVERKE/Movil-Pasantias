package com.pasantias.movil.ui.common;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.ColorRes;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.pasantias.movil.R;

import java.util.ArrayList;
import java.util.List;

public class CardRowAdapter extends RecyclerView.Adapter<CardRowAdapter.VH> {

    public static class Row {
        public final String title;
        public final String subtitle;
        public final String meta;
        @ColorRes public final int accentColor;

        public Row(String title, String subtitle, String meta, @ColorRes int accentColor) {
            this.title = title;
            this.subtitle = subtitle;
            this.meta = meta;
            this.accentColor = accentColor;
        }
    }

    public interface OnRowClick {
        void onClick(int position);
    }

    private final List<Row> items = new ArrayList<>();
    private OnRowClick listener;

    public void setItems(List<Row> rows) {
        items.clear();
        if (rows != null) items.addAll(rows);
        notifyDataSetChanged();
    }

    public void setOnRowClick(OnRowClick listener) {
        this.listener = listener;
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_card_row, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH holder, int position) {
        Row row = items.get(position);
        holder.title.setText(row.title);
        holder.subtitle.setText(row.subtitle != null ? row.subtitle : "");
        if (row.meta != null && !row.meta.isEmpty()) {
            holder.meta.setVisibility(View.VISIBLE);
            holder.meta.setText(row.meta);
        } else {
            holder.meta.setVisibility(View.GONE);
        }
        holder.accent.setBackgroundColor(ContextCompat.getColor(holder.itemView.getContext(), row.accentColor));
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) listener.onClick(holder.getBindingAdapterPosition());
        });
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class VH extends RecyclerView.ViewHolder {
        TextView title, subtitle, meta;
        View accent;

        VH(@NonNull View itemView) {
            super(itemView);
            title = itemView.findViewById(R.id.textTitle);
            subtitle = itemView.findViewById(R.id.textSubtitle);
            meta = itemView.findViewById(R.id.textMeta);
            accent = itemView.findViewById(R.id.accentBar);
        }
    }
}
