//package com.specknet.pdiotapp.history;
//
//import android.app.Application;
//import androidx.lifecycle.AndroidViewModel;
//import androidx.lifecycle.LiveData;
//
//import java.util.List;
//
///**
// * View Model to keep a reference to the word repository and
// * an up-to-date list of all words.
// */
//
//public class ActivityViewModel extends AndroidViewModel {
//
//    private ActivityRepository mRepository;
//    // Using LiveData and caching what getAlphabetizedWords returns has several benefits:
//    // - We can put an observer on the data (instead of polling for changes) and only update the
//    //   the UI when the data actually changes.
//    // - Repository is completely separated from the UI through the ViewModel.
//    private final LiveData<List<Activity>> mAllWords;
//
//    public ActivityViewModel(Application application) {
//        super(application);
//        mRepository = new ActivityRepository(application);
//        mAllWords = mRepository.getAllWords();
//    }
//
//    LiveData<List<Activity>> getAllWords() {
//        return mAllWords;
//    }
//
//    void insert(Activity word) {
//        mRepository.insert(word);
//    }
//}