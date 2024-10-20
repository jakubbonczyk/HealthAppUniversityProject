package com.example.pamietajozdrowiu;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.database.sqlite.SQLiteDatabase;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class DrugAdapter extends RecyclerView.Adapter<DrugAdapter.DrugViewHolder> {

    private List<Drug> drugList;
    private DatabaseHelper dbHelper;

    public DrugAdapter(List<Drug> drugList, Context context) {
        this.drugList = drugList;
        this.dbHelper = new DatabaseHelper(context);
    }

    @NonNull
    @Override
    public DrugViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.drug_item, parent, false);
        return new DrugViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull DrugViewHolder holder, int position) {
        Drug drug = drugList.get(position);
        holder.nameTextView.setText(drug.getName());
        holder.pillsQuantityTextView.setText("Ilość: " + drug.getPillsQuantity());
        holder.expirationDateTextView.setText("Termin ważności: " + drug.getExpirationDate());

        // Dodanie obsługi długiego kliknięcia do usunięcia leku
        holder.itemView.setOnLongClickListener(v -> {
            new AlertDialog.Builder(v.getContext())
                    .setTitle("Usuń lek")
                    .setMessage("Czy na pewno chcesz usunąć ten lek?")
                    .setPositiveButton("Tak", (dialog, which) -> {
                        removeDrug(drug);
                    })
                    .setNegativeButton("Anuluj", null)
                    .show();
            return true;
        });
    }

    @Override
    public int getItemCount() {
        return drugList.size();
    }

    // Pomocnicza metoda do usunięcia leku
    private void removeDrug(Drug drug) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        db.delete("DRUGS", "ID_DRUG=?", new String[]{String.valueOf(drug.getId())});

        int position = drugList.indexOf(drug);
        if (position != -1) {
            drugList.remove(position);
            notifyItemRemoved(position);
        }
    }

    // ViewHolder dla elementów RecyclerView
    static class DrugViewHolder extends RecyclerView.ViewHolder {
        TextView nameTextView, pillsQuantityTextView, expirationDateTextView;

        public DrugViewHolder(@NonNull View itemView) {
            super(itemView);
            nameTextView = itemView.findViewById(R.id.drugNameTextView);
            pillsQuantityTextView = itemView.findViewById(R.id.pillsQuantityTextView);
            expirationDateTextView = itemView.findViewById(R.id.expirationDateTextView);
        }
    }
}
