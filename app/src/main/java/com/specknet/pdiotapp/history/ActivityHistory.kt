package com.specknet.pdiotapp.history

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.specknet.pdiotapp.R


class ActivityHistory : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        println("here1")
        super.onCreate(savedInstanceState)
        println("here2")
        setContentView(R.layout.activity_history)
        println("here3")
    }
}