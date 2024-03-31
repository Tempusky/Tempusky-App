package com.example.tempusky

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class MainViewModel : ViewModel() {

    private var _bottomBarVisibility = MutableLiveData(false)
    val bottomBarVisibility : LiveData<Boolean> = _bottomBarVisibility
}