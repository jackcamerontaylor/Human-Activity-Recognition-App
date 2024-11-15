package com.specknet.pdiotapp.history;


import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.specknet.pdiotapp.R;

class ActivityViewHolder extends RecyclerView.ViewHolder {
    private final TextView activityItemView;

    private ActivityViewHolder(View itemView) {
        super(itemView);
        activityItemView = itemView.findViewById(R.id.textView);
    }

    public void bind(String text) {
        activityItemView.setText(text);
    }

    static ActivityViewHolder create(ViewGroup parent) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.recyclerview_item, parent, false);
        return new ActivityViewHolder(view);
    }
}