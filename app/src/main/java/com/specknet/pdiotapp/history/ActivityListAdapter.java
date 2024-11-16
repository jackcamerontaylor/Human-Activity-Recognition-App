package com.specknet.pdiotapp.history;

import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;


public class ActivityListAdapter extends ListAdapter<Activity, ActivityViewHolder> {

    public ActivityListAdapter(@NonNull DiffUtil.ItemCallback<Activity> diffCallback) {
        super(diffCallback);
    }

    @Override
    public ActivityViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return ActivityViewHolder.create(parent);
    }

    @Override
    public void onBindViewHolder(ActivityViewHolder holder, int position) {
        Activity current = getItem(position);
        holder.bind(current.getActivity());
    }

    static class WordDiff extends DiffUtil.ItemCallback<Activity> {

        @Override
        public boolean areItemsTheSame(@NonNull Activity oldItem, @NonNull Activity newItem) {
            return oldItem == newItem;
        }

        @Override
        public boolean areContentsTheSame(@NonNull Activity oldItem, @NonNull Activity newItem) {
            return oldItem.getActivity().equals(newItem.getActivity());
        }
    }
}