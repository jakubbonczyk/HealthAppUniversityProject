package com.example.pamietajozdrowiu;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class FeelingsHistoryAdapter extends RecyclerView.Adapter<FeelingsHistoryAdapter.ViewHolder> {

    private List<Feeling> feelingsList;

    public FeelingsHistoryAdapter(List<Feeling> feelingsList) {
        this.feelingsList = feelingsList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.feeling_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Feeling feeling = feelingsList.get(position);
        holder.nameTextView.setText("Nastrój: " + feeling.getName());
        holder.dateTextView.setText("Data: " + feeling.getDate());
        holder.notesTextView.setText("Notatka: " + feeling.getNotes());
    }

    @Override
    public int getItemCount() {
        return feelingsList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView nameTextView, dateTextView, notesTextView;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            nameTextView = itemView.findViewById(R.id.feelingNameTextView);
            dateTextView = itemView.findViewById(R.id.feelingDateTextView);
            notesTextView = itemView.findViewById(R.id.feelingNotesTextView);
        }
    }
}
