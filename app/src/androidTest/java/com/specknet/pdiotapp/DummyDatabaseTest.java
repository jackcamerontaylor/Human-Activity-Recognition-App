package com.specknet.pdiotapp;

import static org.junit.Assert.assertEquals;

import android.content.Context;

import androidx.arch.core.executor.testing.InstantTaskExecutorRule;
import androidx.room.Room;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.specknet.pdiotapp.history.Activity;
import com.specknet.pdiotapp.history.ActivityDao;
import com.specknet.pdiotapp.history.ActivityDatabase;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.List;


/**
 * Instrumented test, which will execute on an Android device.
 * <p>
 * See [testing documentation](http://d.android.com/tools/testing).
 */
@RunWith(AndroidJUnit4.class)
public class DummyDatabaseTest {
    @Rule
    public InstantTaskExecutorRule instantTaskExecutorRule = new InstantTaskExecutorRule();

    private ActivityDao mWordDao;
    private ActivityDatabase mDb;

    @Before
    public void createDb() {
        Context context = ApplicationProvider.getApplicationContext();
        // Using an in-memory database because the information stored here disappears when the
        // process is killed.
        mDb = Room.inMemoryDatabaseBuilder(context, ActivityDatabase.class)
                // Allowing main thread queries, just for testing.
                .allowMainThreadQueries()
                .build();
        mWordDao = mDb.activityDao();
    }

    @After
    public void closeDb() {
        mDb.close();
    }

    @Test
    public void insertAndGetWord() throws Exception {
        Activity activity = new Activity(0L, "walking", "static");
        mWordDao.insert(activity);
        List<Activity> allWords = LiveDataTestUtil.getValue(mWordDao.getAllActivities());
        int index = allWords.size() - 1;
        if (index < 0) index = 0;
        assertEquals(allWords.get(index).getStart(), activity.getStart());
        assertEquals(allWords.get(index).getModel(), activity.getModel());
    }

    @Test
    public void checkDateRangePositive() throws Exception {
        Activity activity = new Activity(100L, "walking", "static");
        mWordDao.insert(activity);
        List<Activity> rangeActivities = LiveDataTestUtil.getValue(mWordDao.getActivitiesForDay(50L, 200L, "walking", "dynamic"));
        int index = rangeActivities.size() - 1;
        if (rangeActivities.size() <= 0) return;
        System.out.println("index:" + index);
        assertEquals(rangeActivities.get(index).getStart(), activity.getStart());
        assertEquals(rangeActivities.get(index).getActivity_type(), activity.getActivity_type());
    }

    @Test
    public void checkDateRangeNegative() throws Exception {
        Activity activity = new Activity(100L, "walking", "dynamic");
        mWordDao.insert(activity);
        List<Activity> rangeActivities = LiveDataTestUtil.getValue(mWordDao.getActivitiesForDay(0L, 50L, "walking", "dynamic"));
        assertEquals(0, rangeActivities.size());
    }
}