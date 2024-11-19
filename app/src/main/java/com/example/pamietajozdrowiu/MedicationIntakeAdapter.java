package com.example.pamietajozdrowiu;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class MedicationIntakeAdapter extends RecyclerView.Adapter<MedicationIntakeAdapter.ViewHolder> {

    private List<Drug> drugList;
    private Context context;

    public MedicationIntakeAdapter(List<Drug> drugList, Context context) {
        this.drugList = drugList;
        this.context = context;
    }

    @Override
    public MedicationIntakeAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.medication_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(MedicationIntakeAdapter.ViewHolder holder, int position) {
        Drug drug = drugList.get(position);
        holder.nameTextView.setText(drug.getName());

        byte[] imageBlob = drug.getImageBlob();
        if (imageBlob != null && imageBlob.length > 0) {
            Bitmap bitmap = BitmapFactory.decodeByteArray(imageBlob, 0, imageBlob.length);
            holder.drugImageView.setImageBitmap(bitmap);
        } else {
            holder.drugImageView.setImageResource(R.drawable.placeholder_image);
        }

        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context, MedicationIntakeDetailActivity.class);
            intent.putExtra("DRUG_ID", drug.getId());
            intent.putExtra("DRUG_NAME", drug.getName());
            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return drugList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        TextView nameTextView;
        ImageView drugImageView;

        public ViewHolder(View itemView) {
            super(itemView);
            nameTextView = itemView.findViewById(R.id.medicationNameTextView);
            drugImageView = itemView.findViewById(R.id.medicationImageView);
        }
    }
}
