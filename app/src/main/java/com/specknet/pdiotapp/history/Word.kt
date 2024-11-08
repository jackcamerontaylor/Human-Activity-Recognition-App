package com.specknet.pdiotapp.history

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

/*
    TODO: This class is for demonstration purposes. Delete when no longer needed.
 */

@Entity(tableName = "word_table")
class Word(
//    @field:PrimaryKey(autoGenerate = true) val id: Int,
    @field:ColumnInfo(name = "word") @field:PrimaryKey val word: String
)

//@Entity
//data class User(
//    @PrimaryKey val id: Int,
//
//    val firstName: String?,
//    val lastName: String?
//)
