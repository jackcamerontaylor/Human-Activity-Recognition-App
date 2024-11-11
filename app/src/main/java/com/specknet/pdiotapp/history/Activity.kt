//package com.specknet.pdiotapp.history
//
//import androidx.room.ColumnInfo
//import androidx.room.Entity
//import androidx.room.PrimaryKey
//import java.util.Date
//
//@Entity
//data class Activity(
//    // TODO: use @ColumnInfo() so that you can change column name without changing class var names
//
//    @PrimaryKey(autoGenerate = true) val uid: Int,
//    // TODO: Delete later
//    @ColumnInfo(name = "first_name") val firstName: String?,
//    @ColumnInfo(name = "last_name") val lastName: String?,
//
//    @ColumnInfo(name = "activity_date") val activityDate: String,  // TODO: change to DATE
//    @ColumnInfo(name = "activity_time") val activityTime: String,  // TODO: change to TIME
//    @ColumnInfo(name = "activity_name") val activityName: String,  // TODO: change to Enum - String
//    // @ColumnInfo(name = "activity_type")
//    @ColumnInfo(name = "activity_duration") val activityDuration: String  // TODO: change to DURATION
//)