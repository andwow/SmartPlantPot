package com.timusandrei.smartpot.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.timusandrei.smartpot.R;
import com.timusandrei.smartpot.interfaces.OnSmartPotItemClick;
import com.timusandrei.smartpot.models.SmartPot;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;

public class SmartPotAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private ArrayList<SmartPot> smartPots;
    private OnSmartPotItemClick onSmartPotItemClick;
    public SmartPotAdapter(ArrayList<SmartPot> smartPots, OnSmartPotItemClick onSmartPotItemClick) {
        this.smartPots = smartPots;
        this.onSmartPotItemClick = onSmartPotItemClick;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull @NotNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View view = inflater.inflate(R.layout.smart_pot_cell, parent, false);
        SmartPotViewHolder smartPotViewHolder = new SmartPotViewHolder(view);
        return smartPotViewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull @NotNull RecyclerView.ViewHolder holder, int position) {
        SmartPot smartPot = (SmartPot) smartPots.get(position);
        ((SmartPotViewHolder) holder).bind(smartPot);
    }

    @Override
    public int getItemCount() {
        return smartPots.size();
    }

    class SmartPotViewHolder extends RecyclerView.ViewHolder {
        private TextView name;
        private TextView description;
        private View view;

        SmartPotViewHolder(View view) {
            super(view);
            name = view.findViewById(R.id.smartpot_name);
            description = view.findViewById(R.id.smartpot_description);
            this.view = view;
        }

        void bind(SmartPot smartPot) {
            name.setText(smartPot.getName());
            description.setText(smartPot.getDescription());
            view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    onSmartPotItemClick.onClick(smartPot);
                }
            });
        }
    }
}
