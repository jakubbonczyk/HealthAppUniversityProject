package com.example.pamietajozdrowiu;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class HealthResultsAdapter extends RecyclerView.Adapter<HealthResultsAdapter.ViewHolder> {

    private List<HealthResult> healthResultsList;
    private Context context;

    public HealthResultsAdapter(List<HealthResult> healthResultsList, Context context) {
        this.healthResultsList = healthResultsList;
        this.context = context;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.health_result_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        HealthResult result = healthResultsList.get(position);
        holder.typeTextView.setText("Typ: " + result.getType());
        holder.valueTextView.setText("Wartość: " + result.getValue());
        holder.dateTextView.setText("Data: " + result.getDate());
    }

    @Override
    public int getItemCount() {
        return healthResultsList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView typeTextView, valueTextView, dateTextView;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            typeTextView = itemView.findViewById(R.id.typeTextView);
            valueTextView = itemView.findViewById(R.id.valueTextView);
            dateTextView = itemView.findViewById(R.id.dateTextView);
        }
    }
}
