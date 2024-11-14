package com.specknet.pdiotapp.history;


import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;
import androidx.annotation.NonNull;

import java.time.LocalDateTime;
import java.time.Period;

/**
 * A basic class representing an entity that is a row in a three-column database table.
 * Every row is one activity that the user has carried out.
 * <p>
 * @ Entity - You must annotate the class as an entity and supply a table name if not class name.
 * @ PrimaryKey - You must identify the primary key.
 * @ ColumnInfo - You must supply the column name if it is different from the variable name.
 * <p>
 * See the documentation for the full rich set of annotations.
 * <a href="https://developer.android.com/topic/libraries/architecture/room.html">...</a>
 */


@Entity(tableName = "activity_table")
public class Activity {
    @NonNull
    @ColumnInfo(name = "Start")
    private LocalDateTime start;

    @NonNull
    @ColumnInfo(name = "End")
    private LocalDateTime end;

    @NonNull
    @ColumnInfo(name = "Type")
    private String activity_type;  // TODO: enum?

    // TODO: activity type vs name.

    @NonNull
    @ColumnInfo(name = "Duration")
    private Period duration;

    public Activity(
            @NonNull LocalDateTime start,
            @NonNull LocalDateTime end,
            @NonNull String activity_type,
            @NonNull Period duration
    ) {
        this.start = start;
        this.end = end;
        this.activity_type = activity_type;
        this.duration = duration;
    }

    // TODO: check whether this actually  auto-generates
    @PrimaryKey(autoGenerate = true)
    int id = 0;


//    @NonNull
//    public String getWord() {
//        return this.mWord;
//    }
}