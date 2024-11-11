//package com.specknet.pdiotapp.history
//
//import android.content.Context
//import androidx.room.Database
//import androidx.room.Room
//import androidx.room.RoomDatabase
//import androidx.sqlite.db.SupportSQLiteDatabase
//import java.util.concurrent.ExecutorService
//import java.util.concurrent.Executors
//
///**
// * Database class with a singleton Instance object.
// */
//@Database(entities = [Activity::class], views =[ActivityHistory::class], version = 1, exportSchema = false)
//abstract class ActivityDatabase : RoomDatabase() {
//    // TODO: Should define a migration strategy (if schema becomes more complex)
//    //  https://medium.com/androiddevelopers/understanding-migrations-with-room-f01e04b07929
//
//    abstract fun activityDao(): ActivityDao
//
//
////    val databaseWriteExecutor: ExecutorService =
////        Executors.newFixedThreadPool(com.specknet.pdiotapp.history.ActivityDatabase.NUMBER_OF_THREADS)
//
//    companion object {
////        @JvmStatic private val NUMBER_OF_THREADS: Int = 4
//
//        @Volatile
//        private var Instance: ActivityDatabase? = null
//
//        fun getDatabase(context: Context): ActivityDatabase {
//            // if the Instance is not null, return it, otherwise create a new database instance.
//            return Instance ?: synchronized(this) {
//                Room.databaseBuilder(context, ActivityDatabase::class.java, "item_database")
//                    /**
//                     * Setting this option in your app's database builder means that Room
//                     * permanently deletes all data from the tables in your database when it
//                     * attempts to perform a migration with no defined migration path.
//                     */
//                    .fallbackToDestructiveMigration()
//                    .build()
//                    .also { Instance = it }
//            }
//        }
//    }
//
////    /**
////     * Override the onCreate method to populate the database.
////     * For this sample, we clear the database every time it is created.
////     */
////    private var sRoomDatabaseCallback: Callback = object : Callback() {
////        override fun onCreate(db: SupportSQLiteDatabase) {
////            super.onCreate(db)
////
////            databaseWriteExecutor.execute(
////                Runnable {
////                    // Populate the database in the background.
////                    // If you want to start with more words, just add them.
////                    val dao: ActivityDao =
////                        com.example.android.roomwordssample.WordRoomDatabase.INSTANCE.activityDao()
////                    dao.deleteAll()
////
////                    var word: Word? = Word("Hello")
////                    dao.insert(word)
////                    word = Word("World")
////                    dao.insert(word)
////                })
////        }
////    }
//}