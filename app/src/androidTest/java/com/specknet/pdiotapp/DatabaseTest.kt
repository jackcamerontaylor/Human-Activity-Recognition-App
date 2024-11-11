package com.specknet.pdiotapp

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.specknet.pdiotapp.history.Activity
import com.specknet.pdiotapp.history.ActivityDao
import com.specknet.pdiotapp.history.ActivityDatabase
import org.junit.After

import org.junit.Test
import org.junit.runner.RunWith

import org.junit.Assert.*
import org.junit.Before
import java.io.IOException

import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals

/**
 * Instrumented test, which will execute on an Android device.
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
@RunWith(AndroidJUnit4::class)
class DatabaseTest {
    private lateinit var itemDao: ActivityDao
    private lateinit var inventoryDatabase: ActivityDatabase

    @Before
    fun createDb() {
        val context: Context = ApplicationProvider.getApplicationContext()
        // Using an in-memory database because the information stored here disappears when the
        // process is killed.
        inventoryDatabase = ActivityDatabase.getDatabase(context)
        itemDao = inventoryDatabase.activityDao()
    }

    private var item1 = Activity(1, "Apples", "10.0", "20", "2", "3", "3")
    private var item2 = Activity(2, "Bananas", "15.0", "97", "3", "3", "3")

    private suspend fun addOneItemToDb() {
        itemDao.insert(item1)
    }

    private suspend fun addTwoItemsToDb() {
        itemDao.insert(item1)
        itemDao.insert(item2)
    }

    @Test
    @Throws(Exception::class)
    fun daoInsert_insertsItemIntoDB() = runBlocking {
        addOneItemToDb()
        val allItems = itemDao.getAll().first()
        assertEquals(allItems, item1)
    }

//    @Test
//    fun useAppContext() {
//        // Context of the app under test.
//        val appContext = InstrumentationRegistry.getInstrumentation().targetContext
//        assertEquals("com.specknet.pdiotapp", appContext.packageName)
//    }

//    @After
//    @Throws(IOException::class)
//    fun closeDb() {
//        inventoryDatabase.close()
//    }
}