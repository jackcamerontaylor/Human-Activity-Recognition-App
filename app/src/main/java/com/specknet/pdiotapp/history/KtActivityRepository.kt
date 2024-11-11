//package com.specknet.pdiotapp.history
//
//import android.app.Application
//import androidx.lifecycle.LiveData
//
///**
// * Repository that provides insert, update, delete, and retrieve of [Activity] from a given data source.
// */
//internal class KtActivityRepository(application: Application?) {
//    private val mWordDao: ActivityDao
//    private val mAllWords: LiveData<List<Activity>>
//
//    // Note that in order to unit test the WordRepository, you have to remove the Application
//    // dependency. This adds complexity and much more code, and this sample is not about testing.
//    // See the BasicSample in the android-architecture-components repository at
//    // https://github.com/googlesamples
//    init {
//        val db: ActivityDatabase = ActivityDatabase.getDatabase(application!!.applicationContext)
//        mWordDao = db.activityDao()
//        mAllWords = mWordDao.getLiveActivities()
//    }
//
//    // Room executes all queries on a separate thread.
//    // Observed LiveData will notify the observer when the data has changed.
//    fun getAllWords(): LiveData<List<Activity>> {
//        return mAllWords
//    }
//
//    // You must call this on a non-UI thread or your app will throw an exception. Room ensures
//    // that you're not doing any long running operations on the main thread, blocking the UI.
////    fun insert(word: Activity?) {
////        ActivityDatabase.databaseWriteExecutor.execute {
////            mWordDao.insert(word)
////        }
////    }
//}