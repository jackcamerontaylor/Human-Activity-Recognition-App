//package com.specknet.pdiotapp;
//
//import static org.junit.Assert.assertThat;
//
//import android.content.Context;
//
//import androidx.arch.core.executor.testing.InstantTaskExecutorRule;
//import androidx.room.Room;
//import androidx.test.core.app.ApplicationProvider;
//import androidx.test.ext.junit.runners.AndroidJUnit4;
//
//import com.specknet.pdiotapp.history.Word;
//import com.specknet.pdiotapp.history.WordDao;
//import com.specknet.pdiotapp.history.WordDatabase;
//
//import org.junit.After;
//import org.junit.Before;
//import org.junit.Rule;
//import org.junit.Test;
//import org.junit.runner.RunWith;
//
//import java.io.IOException;
//
//
///**
// * Instrumented test, which will execute on an Android device.
// *
// * See [testing documentation](http://d.android.com/tools/testing).
// */
//@RunWith(AndroidJUnit4.class)
//public class DummyDatabaseTest {
//    @Rule
//    public InstantTaskExecutorRule instantTaskExecutorRule = new InstantTaskExecutorRule();
//
//    private WordDao mWordDao;
//    private WordDatabase mDb;
//
//    @Before
//    public void createDb() {
//        Context context = ApplicationProvider.getApplicationContext();
//        // Using an in-memory database because the information stored here disappears when the
//        // process is killed.
//        mDb = Room.inMemoryDatabaseBuilder(context, WordDatabase.class)
//                // Allowing main thread queries, just for testing.
//                .allowMainThreadQueries()
//                .build();
//        mWordDao = WordDatabase.getDatabase getWordDao();
//    }
//
//    @After
//    public void closeDb() {
//        mDb.close();
//    }
//
//    @Test
//    public void insertAndGetWord() throws Exception {
//        Word word = new Word("word");
////        mWordDao.insert(word);
////        List<Word> allWords = LiveDataTestUtil.getValue(mWordDao.getAlphabetizedWords());
////        assertEquals(allWords.get(0).getWord(), word.getWord());
//    }
//
//}