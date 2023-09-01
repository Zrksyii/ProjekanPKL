package com.akmalzarkasyi.project_pkl.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import com.akmalzarkasyi.project_pkl.database.DatabaseClient.Companion.getInstance
import com.akmalzarkasyi.project_pkl.database.DatabaseDao
import com.akmalzarkasyi.project_pkl.model.ModelDatabase
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.schedulers.Schedulers

class HistoryViewModel(application: Application) : AndroidViewModel(application) {
    var dataLaporan: LiveData<List<ModelDatabase>>
    var databaseDao: DatabaseDao? = getInstance(application)?.appDatabase?.databaseDao()

    fun deleteDataById(uid: Int) {
        Completable.fromAction {
            databaseDao?.deleteHistoryById(uid)
        }
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe()
    }

    init {
        dataLaporan = databaseDao!!.getAllHistory()
    }
}