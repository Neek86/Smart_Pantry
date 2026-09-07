package com.example.smartpantry;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class PantryAdapter extends RecyclerView.Adapter<java.PantryViewHolder> {

    private List<PantryItem> pantryList;
    private OnItemClickListener listener;

    public interface OnItemClickListener {
        void onItemClick(PantryItem item);
        void onDeleteClick(PantryItem item);
    }

    public PantryAdapter(List<PantryItem> pantryList, OnItemClickListener listener) {
        this.pantryList = pantryList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public PantryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_pantry, parent, false);
        return new PantryViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PantryViewHolder holder, int position) {
        PantryItem item = pantryList.get(position);
        holder.tvName.setText(item.getName());
        String qtyText = item.getQuantity() + " " + item.getUnit();
        holder.tvQuantity.setText(qtyText);

        holder.itemView.setOnClickListener(v -> listener.onItemClick(item));
        holder.btnDelete.setOnClickListener(v -> listener.onDeleteClick(item));
    }

    @Override
    public int getItemCount() {
        return pantryList.size();
    }

    public void updateData(List<PantryItem> newList) {
        this.pantryList = newList;
        notifyDataSetChanged();
    }

    static class PantryViewHolder extends RecyclerView.ViewHolder {
        TextView tvName, tvQuantity;
        ImageButton btnDelete;

        public PantryViewHolder(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tvItemName);
            tvQuantity = itemView.findViewById(R.id.tvItemQuantity);
            btnDelete = itemView.findViewById(R.id.btnDelete);
        }
    }
}