//package com.specknet.pdiotapp.history
//
//import android.app.Application
//import androidx.lifecycle.AndroidViewModel
//import androidx.lifecycle.LiveData
//
///**
// * View Model to keep a reference to the word repository and
// * an up-to-date list of all words.
// */
//class KtActivityViewModel(application: Application) : AndroidViewModel(application!!) {
//    private val mRepository: KtActivityRepository
//
//    // Using LiveData and caching what getAlphabetizedWords returns has several benefits:
//    // - We can put an observer on the data (instead of polling for changes) and only update the
//    //   the UI when the data actually changes.
//    // - Repository is completely separated from the UI through the ViewModel.
//    private val mAllWords: LiveData<List<Activity>>
//
//    init {
//        mRepository = KtActivityRepository(application)
//        mAllWords = mRepository.getAllWords()
//    }
//
//    fun getAllWords(): LiveData<List<Activity>> {
//        return mAllWords
//    }
//
////    fun insert(word: Activity?) {
////        mRepository.insert(word)
////    }
//}
