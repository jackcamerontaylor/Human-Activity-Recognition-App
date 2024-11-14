package com.specknet.pdiotapp.history;

import android.app.Application;
import androidx.lifecycle.LiveData;

import java.util.List;

/**
 * Abstracted Repository as promoted by the Architecture Guide.
 * https://developer.android.com/topic/libraries/architecture/guide.html
 */

class ActivityRepository {

    private ActivityDao mActivityDao;
    private LiveData<List<Activity>> mAllActivities;

    // Note that in order to unit test the WordRepository, you have to remove the Application
    // dependency. This adds complexity and much more code, and this sample is not about testing.
    // See the BasicSample in the android-architecture-components repository at
    // https://github.com/googlesamples
    ActivityRepository(Application application) {
        ActivityDatabase db = ActivityDatabase.getDatabase(application);
        mActivityDao = db.activityDao();
        mAllActivities = mActivityDao.getAllActivities();
    }

    // Room executes all queries on a separate thread.
    // Observed LiveData will notify the observer when the data has changed.
    LiveData<List<Activity>> getAllActivities() {
        return mAllActivities;
    }

    // You must call this on a non-UI thread or your app will throw an exception. Room ensures
    // that you're not doing any long running operations on the main thread, blocking the UI.
    void insert(Activity activity) {
        WordDatabase.databaseWriteExecutor.execute(() -> {
            mActivityDao.insert(activity);
        });
    }
}