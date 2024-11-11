//package com.specknet.pdiotapp.history;
//
//import android.content.Context;
//
//import androidx.annotation.NonNull;
//import androidx.room.Database;
//import androidx.room.Room;
//import androidx.room.RoomDatabase;
//import androidx.sqlite.db.SupportSQLiteDatabase;
//
//import java.util.concurrent.ExecutorService;
//import java.util.concurrent.Executors;
//
///**
// * This is the backend. The database. This used to be done by the OpenHelper.
// * The fact that this has very few comments emphasizes its coolness.  In a real
// * app, consider exporting the schema to help you with migrations.
// */
//
//@Database(entities = {Activity.class}, version = 1, exportSchema = false)
//abstract class ActivityDatabase extends RoomDatabase {
//
//    abstract ActivityDao activityDao();
//
//    // marking the instance as volatile to ensure atomic access to the variable
//    private static volatile ActivityDatabase INSTANCE;
//    private static final int NUMBER_OF_THREADS = 4;
//    static final ExecutorService databaseWriteExecutor =
//            Executors.newFixedThreadPool(NUMBER_OF_THREADS);
//
//    static ActivityDatabase getDatabase(final Context context) {
//        if (INSTANCE == null) {
//            synchronized (ActivityDatabase.class) {
//                if (INSTANCE == null) {
//                    INSTANCE = Room.databaseBuilder(context.getApplicationContext(),
//                                    ActivityDatabase.class, "word_database")
//                            .addCallback(sRoomDatabaseCallback)
//                            .build();
//                }
//            }
//        }
//        return INSTANCE;
//    }
//
//    /**
//     * Override the onCreate method to populate the database.
//     * For this sample, we clear the database every time it is created.
//     */
//    private static RoomDatabase.Callback sRoomDatabaseCallback = new RoomDatabase.Callback() {
//        @Override
//        public void onCreate(@NonNull SupportSQLiteDatabase db) {
//            super.onCreate(db);
//
//            databaseWriteExecutor.execute(() -> {
//                // Populate the database in the background.
//                // If you want to start with more words, just add them.
//                ActivityDao dao = INSTANCE.activityDao();
//                dao.deleteAll();
//
//                Activity word = new Activity(1, "Hello", "Hello", "Hello", "Hello", "Hello", "Hello");
//                dao.insert(word);
//                word = new Activity(2, "World", "World", "World", "World", "World", "World");
//                dao.insert(word);
//            });
//        }
//    };
//}
