//package com.specknet.pdiotapp.history
//
//import android.view.LayoutInflater
//import android.view.View
//import android.view.ViewGroup
//import android.widget.TextView
//import androidx.recyclerview.widget.RecyclerView
//import com.specknet.pdiotapp.R
//
//internal class ActivityViewHolder private constructor(itemView: View) :
//    RecyclerView.ViewHolder(itemView) {
//    private val wordItemView: TextView = itemView.findViewById<TextView>(R.id.textView)
//
//    fun bind(text: String?) {
//        wordItemView.text = text
//    }
//
//    companion object {
//        fun create(parent: ViewGroup): ActivityViewHolder {
//            val view: View = LayoutInflater.from(parent.context)
//                .inflate(R.layout.recyclerview_item, parent, false)
//            return ActivityViewHolder(view)
//        }
//    }
//}
