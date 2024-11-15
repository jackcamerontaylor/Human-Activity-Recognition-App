//package com.specknet.pdiotapp.history
//
//import android.view.ViewGroup
//import androidx.recyclerview.widget.DiffUtil
//import androidx.recyclerview.widget.ListAdapter
//
//
//internal class ActivityListAdapter(diffCallback: WordDiff) :
//    ListAdapter<Activity, ActivityViewHolder>(diffCallback) {
//    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ActivityViewHolder {
//        return ActivityViewHolder.create(parent)
//    }
//
//    override fun onBindViewHolder(holder: ActivityViewHolder, position: Int) {
//        val current: Activity? = getItem(position)
//        if (current != null) {
//            holder.bind(current.activityName)
//        }
//    }
//
//    internal class WordDiff : DiffUtil.ItemCallback<Activity>() {
//        override fun areItemsTheSame(oldItem: Activity, newItem: Activity): Boolean {
//            return oldItem === newItem
//        }
//
//        override fun areContentsTheSame(oldItem: Activity, newItem: Activity): Boolean {
//            return oldItem.activityName.equals(newItem.activityName)
//        }
//    }
//}