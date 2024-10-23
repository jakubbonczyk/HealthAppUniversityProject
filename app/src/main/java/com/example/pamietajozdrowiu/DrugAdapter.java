package com.example.pamietajozdrowiu;

import android.app.AlertDialog;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class DrugAdapter extends RecyclerView.Adapter<DrugAdapter.DrugViewHolder> {

    private List<Drug> drugList;
    private DatabaseHelper dbHelper;
    private Context context;

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
        byte[] imageBlob = drug.getImageBlob();

        if (imageBlob != null && imageBlob.length > 0) {
            Bitmap bitmap = BitmapFactory.decodeByteArray(imageBlob, 0, imageBlob.length);
            holder.drugImageView.setImageBitmap(bitmap);
        }   else {
            holder.drugImageView.setImageResource(R.drawable.placeholder_image);
        }

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
    public static class DrugViewHolder extends RecyclerView.ViewHolder {
        TextView nameTextView, pillsQuantityTextView, expirationDateTextView;
        ImageView drugImageView;

        public DrugViewHolder(@NonNull View itemView) {
            super(itemView);
            nameTextView = itemView.findViewById(R.id.drugNameTextView);
            pillsQuantityTextView = itemView.findViewById(R.id.pillsQuantityTextView);
            expirationDateTextView = itemView.findViewById(R.id.expirationDateTextView);
            drugImageView = itemView.findViewById(R.id.drugImageView);
        }
    }
}
