package com.specknet.pdiotapp.history;


import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

/**
 * The Room abstraction is in this file, where you map a Java method call to an SQL query.
 *
 * When you are using complex data types, such as Date, you have to also supply type converters.
 * To keep this example basic, no types that require type converters are used.
 * See the documentation at
 * https://developer.android.com/topic/libraries/architecture/room.html#type-converters
 */

@Dao
public interface ActivityDao {
    @Insert()
    void insert(Activity activity);

    @Insert
    public void insertListOfActivities(List<Activity> activitiesList);

    @Delete
    public void delete(Activity activity);

    @Query("DELETE FROM activity_table WHERE id = :userId")
    abstract void deleteByActivityId(long userId);

    // TODO
    @Query("DELETE from activity_table WHERE(start == :startTimestamp)")
    abstract void deleteActivitiesInGivenTimeframe(Long startTimestamp);

    @Query("SELECT * from activity_table ORDER BY start ASC")
    abstract LiveData<List<Activity>> getAllActivities();

    // TODO
    @Query("SELECT * from activity_table WHERE (start == :startTimestamp)")
    abstract Activity[] getAllActivitiesInTimeframe(Long startTimestamp);

    // TODO
    @Query("SELECT * from activity_table WHERE type = :activityType")
    abstract Activity[] getAllActivitiesByType(String activityType);

    @Query("SELECT * FROM activity_table WHERE start BETWEEN :startOfDay AND :endOfDay AND model = :model AND type = :activityType ORDER BY start ASC")
    LiveData<List<Activity>> getActivitiesForDay(long startOfDay, long endOfDay, String model, String activityType);

    @Update
    public void updateActivity(Activity activity);
}
