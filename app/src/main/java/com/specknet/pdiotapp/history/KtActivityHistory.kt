//package com.specknet.pdiotapp.history
//
//import android.os.Bundle
//import androidx.appcompat.app.AppCompatActivity
//import androidx.lifecycle.ViewModelProvider
//import androidx.recyclerview.widget.LinearLayoutManager
//import androidx.recyclerview.widget.RecyclerView
//import com.specknet.pdiotapp.R
//
///*
//    Corresponds to MainActivity.
// */
//class KtActivityHistory : AppCompatActivity() {
//    private var mWordViewModel: ActivityViewModel? = null
//
//    override fun onCreate(savedInstanceState: Bundle?) {
//        super.onCreate(savedInstanceState)
//        setContentView(R.layout.activity_history)  // TODO: layout should have title + table
//
//        // RecyclerView to see DB as list
//        val recyclerView = findViewById<RecyclerView>(R.id.recyclerview)
//        val adapter: ActivityListAdapter = ActivityListAdapter(ActivityListAdapter.WordDiff())
//        recyclerView.adapter = adapter
//        recyclerView.layoutManager = LinearLayoutManager(this)
//
//
//        // ViewModel, get the actual DB objects
//        mWordViewModel = ViewModelProvider(this).get(ActivityViewModel::class.java)
//
//        // Ignore the observer for now, unless it never refreshes
//        // Ignore (+) button. In the future, (DELETE) button could be instantiated here
//    }
//
////    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent) {
////
////    ...
////
////    }
//}