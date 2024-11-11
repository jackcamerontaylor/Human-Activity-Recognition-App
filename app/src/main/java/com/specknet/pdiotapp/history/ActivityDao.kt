//package com.specknet.pdiotapp.history
//
//import androidx.lifecycle.LiveData
//import androidx.room.Dao
//import androidx.room.Delete
//import androidx.room.Insert
//import androidx.room.OnConflictStrategy
//import androidx.room.Query
//import androidx.room.Update
//import kotlinx.coroutines.flow.Flow
//
//@Dao
//interface ActivityDao {
//    @Query("SELECT * FROM activity")
//    fun getAll(): List<Activity>
//
//    @Query("SELECT * FROM activity WHERE uid IN (:userIds)")
//    fun loadAllByIds(userIds: IntArray): List<Activity>
//
//    @Query("SELECT * FROM activity WHERE activity_name LIKE :first " +
//            "LIMIT 1")
//    fun findByName(first: String, last: String): Activity
//
//    @Insert
//    fun insertAll(vararg users: Activity)
//
//    @Delete
//    fun delete(activity: Activity)
//
//    @Update
//    fun updateActivity(vararg users: Activity)
//
//    @Insert(onConflict = OnConflictStrategy.IGNORE)
//    suspend fun insert(item: Activity)
//
//    @Update
//    suspend fun update(item: Activity)
//
//    @Query("SELECT * from activity WHERE uid = :id")
//    fun getItem(id: Int): Flow<Activity>
//
//    @Query("SELECT * from activity ORDER BY activity_date ASC")
//    fun getAllActivities(): Flow<List<Activity>>
//
//    @Query("SELECT * FROM Activity ORDER BY activity_date ASC")
//    fun getLiveActivities(): LiveData<List<Activity>>
//
//    @Insert(onConflict = OnConflictStrategy.IGNORE)
//    fun insert(word: Activity?)
//
//    @Query("DELETE FROM activity")
//    fun deleteAll()
//}